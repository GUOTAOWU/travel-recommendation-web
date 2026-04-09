package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.client.FileClient;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.entity.User;
import com.gjq.mapper.CommentMapper;
import com.gjq.service.CommentService;
import com.gjq.service.UserService;
import com.gjq.vo.comment.CommentVO;
import com.gjq.vo.user.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.gjq.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * コメントサービス実装クラス
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional
    public Long addComment(Long userId, CommentAddDTO commentAddDTO) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setItemId(commentAddDTO.getItemId());
        comment.setContent(commentAddDTO.getContent());
        comment.setParentId(commentAddDTO.getParentId());
        comment.setReplyToCommentId(commentAddDTO.getReplyToCommentId());
        comment.setReplyToUserId(commentAddDTO.getReplyToUserId());
        
        // コメントを保存
        save(comment);
        
        return comment.getId();
    }

    @Override
    @Transactional
    public boolean deleteCommentWithPermissionCheck(Long userId, Long commentId, boolean isAdmin, Long itemCreatorId) {
        // コメントが存在するかチェック
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException("コメントが存在しません");
        }
        
        // 権限チェック
        boolean hasPermission = false;
        
        // 1. 管理者はすべてのコメントを削除可能
        if (isAdmin) {
            hasPermission = true;
        } 
        // 2. アイテムの投稿者は自身のアイテムに対するすべてのコメントを削除可能
        else if (itemCreatorId != null && itemCreatorId.equals(userId) && 
                 comment.getItemId() != null) {
            hasPermission = true;
        } 
        // 3. 一般ユーザーは自身が投稿したコメントのみ削除可能
        else if (comment.getUserId().equals(userId)) {
            hasPermission = true;
        }
        
        if (!hasPermission) {
            throw new BusinessException("このコメントを削除する権限がありません");
        }
        
        // トップレベルコメントの場合、付随するすべての返信も削除
        if (comment.getParentId() == null) {
            remove(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        }
        
        // コメントを削除
        return removeById(commentId);
    }

    @Override
    public CommentVO getCommentById(Long commentId) {
        Comment comment = getById(commentId);
        if (comment == null) {
            return null;
        }
        
        return convertToVO(comment);
    }

    @Override
    public Page<CommentVO> pageComments(CommentQueryDTO queryDTO) {
        // 検索条件を構築
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        // アイテムIDが指定されている場合、そのアイテムのコメントを検索
        if (queryDTO.getItemId() != null) {
            queryWrapper.eq(Comment::getItemId, queryDTO.getItemId());
        }
        
        // ユーザーIDが指定されている場合、そのユーザーのコメントを検索
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(Comment::getUserId, queryDTO.getUserId());
        }
        
        // トップレベルコメントのみを検索する場合
        if (queryDTO.getOnlyParent() != null && queryDTO.getOnlyParent()) {
            queryWrapper.isNull(Comment::getParentId);
        }
        
        // 日時の降順でソート
        queryWrapper.orderByDesc(Comment::getCreateTime);
        
        // ページング検索
        Page<Comment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        page = page(page, queryWrapper);
        
        // VOへ変換
        Page<CommentVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        // すべてのコメント投稿者のユーザーIDを取得
        List<Long> userIds = page.getRecords().stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 返信先ユーザーのIDを追加
        page.getRecords().stream()
                .filter(comment -> comment.getReplyToUserId() != null)
                .map(Comment::getReplyToUserId)
                .distinct()
                .forEach(userIds::add);
        
        // ユーザー情報を一括取得
        Map<Long, UserInfo> userMap = getUserMap(userIds);
        
        // レコードを変換
        List<CommentVO> commentVOList = page.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment);
                    // ユーザー情報を設定
                    vo.setUserInfo(userMap.get(comment.getUserId()));
                    // 返信先ユーザー情報を設定
                    if (comment.getReplyToUserId() != null) {
                        vo.setReplyToUserInfo(userMap.get(comment.getReplyToUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        voPage.setRecords(commentVOList);
        
        return voPage;
    }

    @Override
    public List<CommentVO> getCommentTreeByItemId(Long itemId) {
        // 該当アイテムのすべてのコメントを検索
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getItemId, itemId);
        queryWrapper.orderByDesc(Comment::getCreateTime);
        List<Comment> comments = list(queryWrapper);
        
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // すべてのコメント投稿者のユーザーIDを取得
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 返信先ユーザーのIDを追加
        comments.stream()
                .filter(comment -> comment.getReplyToUserId() != null)
                .map(Comment::getReplyToUserId)
                .distinct()
                .forEach(userIds::add);
        
        // ユーザー情報を一括取得
        Map<Long, UserInfo> userMap = getUserMap(userIds);
        
        // VOへ変換
        List<CommentVO> commentVOList = comments.stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment);
                    // ユーザー情報を設定
                    vo.setUserInfo(userMap.get(comment.getUserId()));
                    // 返信先ユーザー情報を設定
                    if (comment.getReplyToUserId() != null) {
                        vo.setReplyToUserInfo(userMap.get(comment.getReplyToUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        
        // コメントツリーを構築
        return buildCommentTree(commentVOList);
    }
    
    /**
     * コメントをVOに変換
     */
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }
    
    /**
     * コメントツリーを構築
     */
    private List<CommentVO> buildCommentTree(List<CommentVO> commentList) {
        // トップレベルコメントの抽出
        List<CommentVO> rootComments = commentList.stream()
                .filter(comment -> comment.getParentId() == null)
                .collect(Collectors.toList());
        
        // 返信コメントの抽出とグループ化
        Map<Long, List<CommentVO>> replyMap = commentList.stream()
                .filter(comment -> comment.getParentId() != null)
                .collect(Collectors.groupingBy(CommentVO::getParentId));
        
        // ツリー構造の組み立て
        rootComments.forEach(root -> {
            List<CommentVO> replies = replyMap.get(root.getId());
            if (replies != null) {
                // 時系列でソート（最新の返信を末尾にする）
                replies.sort((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()));
                root.setReplies(replies);
            } else {
                root.setReplies(new ArrayList<>());
            }
        });
        
        return rootComments;
    }
    
    /**
     * ユーザー情報を一括取得
     */
    private Map<Long, UserInfo> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // ユーザーを一括検索
        List<User> users = userService.listByIds(userIds);
        
        // UserInfoに変換してMapに格納
        Map<Long, UserInfo> userMap = new HashMap<>();
        users.forEach(user -> {
            UserInfo userInfo = new UserInfo(user, fileClient);
            userMap.put(user.getId(), userInfo);
        });
        
        return userMap;
    }
}