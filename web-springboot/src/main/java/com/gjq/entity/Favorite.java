package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * お気に入りエンティティクラス
 */
@Data
@TableName("favorite")
public class Favorite {
    /**
     * お気に入りID
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
     * 作成日時
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}