package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティクラス
 */
@Data
@TableName("user")
public class User {
    /**
     * ユーザーID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ユーザー名
     */
    private String username;

    /**
     * パスワード
     */
    private String password;

    /**
     * 氏名
     */
    private String realName;

    /**
     * 電話番号
     */
    private String phone;

    /**
     * メールアドレス
     */
    private String email;

    /**
     * アバターのバケット名
     */
    private String avatarBucket;

    /**
     * アバターのオブジェクトキー
     */
    private String avatarObjectKey;

    /**
     * ロール（0: 一般ユーザー、1: 管理者）
     */
    private Integer role;

    /**
     * ステータス（0: 無効、1: 有効）
     */
    private Integer status;

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