package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.entity.Item;
import com.gjq.mapper.CommentMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.service.CommentService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.comment.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * コメントコントローラー
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private ItemMapper itemMapper;

    /**
     * コメントを追加
     *
     * @param commentAddDTO コメント追加DTO
     * @return 実行結果
     */
    @PostMapping
    public Result<Long> addComment(@RequestBody @Valid CommentAddDTO commentAddDTO) {
        Long userId = SecurityUtils.getUserId();
        Long commentId = commentService.addComment(userId, commentAddDTO);
        return Result.success(commentId);
    }

    /**
     * コメントを削除
     *
     * @param commentId コメントID
     * @return 実行結果
     */
    @DeleteMapping("/{commentId}")
    public Result<?> deleteComment(@PathVariable Long commentId) {
        // 現在のユーザーIDを取得
        Long userId = SecurityUtils.getUserId();
        
        // コメント情報を取得
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("コメントが存在しません");
        }
        
        // アイテム作成者のIDを取得
        Long itemCreatorId = null;
        if (comment.getItemId() != null) {
            Item item = itemMapper.selectById(comment.getItemId());
            if (item != null) {
                itemCreatorId = item.getUserId();
            }
        }
        
        // 権限チェックを伴う削除を実行
        try {
            boolean success = commentService.deleteCommentWithPermissionCheck(userId, commentId, SecurityUtils.isAdmin(), itemCreatorId);
            return success ? Result.success() : Result.error("削除に失敗しました");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * コメント詳細を取得
     *
     * @param commentId コメントID
     * @return コメントVO
     */
    @GetMapping("/{commentId}")
    public Result<CommentVO> getComment(@PathVariable Long commentId) {
        CommentVO commentVO = commentService.getCommentById(commentId);
        return Result.success(commentVO);
    }

    /**
     * コメントをページング検索
     *
     * @param queryDTO 検索条件
     * @return コメントVOのページングオブジェクト
     */
    @GetMapping("/page")
    public Result<Page<CommentVO>> pageComments(CommentQueryDTO queryDTO) {
        Page<CommentVO> page = commentService.pageComments(queryDTO);
        return Result.success(page);
    }

    /**
     * アイテムのコメントツリーを取得
     *
     * @param itemId アイテムID
     * @return コメントVOリスト（ツリー構造）
     */
    @GetMapping("/tree/{itemId}")
    public Result<List<CommentVO>> getCommentTree(@PathVariable Long itemId) {
        List<CommentVO> commentTree = commentService.getCommentTreeByItemId(itemId);
        return Result.success(commentTree);
    }
}