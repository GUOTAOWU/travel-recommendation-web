package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 観光スポットエンティティクラス
 */
@Data
@TableName("item")
public class Item {
    /**
     * スポットID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * スポット名称
     */
    private String title;

    /**
     * スポット説明
     */
    private String description;

    /**
     * カテゴリID
     */
    private Long categoryId;

    /**
     * ユーザーID（作成者）
     */
    private Long userId;

    /**
     * カバー画像のバケット名
     */
    private String coverBucket;

    /**
     * カバー画像のオブジェクトキー
     */
    private String coverObjectKey;

    /**
     * ファイルのバケット名（動画、音声、その他のファイル保存用）
     */
    private String fileBucket;

    /**
     * ファイルのオブジェクトキー（動画、音声、その他のファイル保存用）
     */
    private String fileObjectKey;

    /**
     * タグ（カンマ区切り）
     */
    private String tags;

    /**
     * 追加データ（JSON形式）。将来的に追加される可能性のあるフィールドの保存に使用
     */
    private String extraData;

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