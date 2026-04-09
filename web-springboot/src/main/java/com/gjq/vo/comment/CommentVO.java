package com.gjq.vo.comment;

import com.gjq.vo.user.UserInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * コメントビューオブジェクト (VO)
 */
@Data
public class CommentVO {
    /**
     * コメントID
     */
    private Long id;

    /**
     * ユーザーID
     */
    private Long userId;

    /**
     * ユーザー情報
     */
    private UserInfo userInfo;

    /**
     * アイテムID
     */
    private Long itemId;

    /**
     * コメント内容
     */
    private String content;

    /**
     * 親コメントID
     */
    private Long parentId;
    
    /**
     * 返信先のコメントID
     */
    private Long replyToCommentId;
    
    /**
     * 返信先のユーザーID
     */
    private Long replyToUserId;
    
    /**
     * 返信先のユーザー情報
     */
    private UserInfo replyToUserInfo;

    /**
     * 返信リスト
     */
    private List<CommentVO> replies;

    /**
     * 作成日時
     */
    private LocalDateTime createTime;

    /**
     * 更新日時
     */
    private LocalDateTime updateTime;
}