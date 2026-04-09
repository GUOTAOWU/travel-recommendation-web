package com.gjq.dto.comment;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * コメント追加DTO
 */
@Data
public class CommentAddDTO {
    /**
     * アイテムID
     */
    @NotNull(message = "アイテムIDは必須です")
    private Long itemId;

    /**
     * コメント内容
     */
    @NotBlank(message = "コメント内容は必須です")
    private String content;

    /**
     * 親コメントID（nullの場合はトップレベルコメント、それ以外は返信を意味します）
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
}