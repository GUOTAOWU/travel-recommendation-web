package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * カテゴリエンティティクラス
 */
@Data
@TableName("category")
public class Category {
    /**
     * カテゴリID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * カテゴリ名
     */
    private String name;

    /**
     * カテゴリ説明
     */
    private String description;

    /**
     * アイコンのバケット名
     */
    private String iconBucket;

    /**
     * アイコンのオブジェクトキー
     */
    private String iconObjectKey;

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