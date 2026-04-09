package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.dto.comment.CommentAddDTO;
import com.gjq.dto.comment.CommentQueryDTO;
import com.gjq.entity.Comment;
import com.gjq.vo.comment.CommentVO;

import java.util.List;

/**
 * コメントサービスインターフェース
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * コメントを追加します
     *
     * @param userId ユーザーID
     * @param commentAddDTO コメント追加DTO
     * @return コメントID
     */
    Long addComment(Long userId, CommentAddDTO commentAddDTO);
    
    /**
     * コメントを削除します（権限チェック付き）
     * - 管理者はすべてのコメントを削除可能
     * - アイテムの投稿者は、自身のアイテムに紐付くすべてのコメントを削除可能
     * - 一般ユーザーは、自身が投稿したコメントのみ削除可能
     *
     * @param userId ユーザーID
     * @param commentId コメントID
     * @param isAdmin 管理者かどうか
     * @param itemCreatorId アイテム作成者ID（現在のユーザーがアイテム作成者の場合に指定）
     * @return 成功した場合はtrue
     */
    boolean deleteCommentWithPermissionCheck(Long userId, Long commentId, boolean isAdmin, Long itemCreatorId);
    
    /**
     * コメントの詳細を取得します
     *
     * @param commentId コメントID
     * @return コメントVO
     */
    CommentVO getCommentById(Long commentId);
    
    /**
     * コメントをページング検索します
     *
     * @param queryDTO 検索条件
     * @return ページングされたコメントVO
     */
    Page<CommentVO> pageComments(CommentQueryDTO queryDTO);
    
    /**
     * アイテムIDに基づいてコメントツリーを取得します（トップレベルコメントとその返信）
     *
     * @param itemId アイテムID
     * @return コメントVOリスト（ツリー構造として構築済み）
     */
    List<CommentVO> getCommentTreeByItemId(Long itemId);
}