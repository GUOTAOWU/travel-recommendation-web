package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * コメントエンティティクラス
 */
@Data
@TableName("comment")
public class Comment {
    /**
     * コメントID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ユーザーID
     */
    private Long userId;

    /**
     * アイテムID
     */
    private Long itemId;

    /**
     * コメント内容
     */
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

    /**
     * 作成日時
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新日時
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}