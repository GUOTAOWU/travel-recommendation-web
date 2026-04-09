package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ユーザー行動記録エンティティクラス
 */
@Data
@TableName("user_action")
public class UserAction {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ユーザーID
     */
    private Long userId;

    /**
     * 観光スポットID
     */
    private Long itemId;

    /**
     * アクションタイプ（0: 閲覧、1: 予約）
     */
    private Integer actionType;

    /**
     * 拡張データ（JSON形式）
     */
    private String extraData;

    /**
     * 作成日時
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}