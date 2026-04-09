package com.gjq.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * ユーザー更新DTO
 */
@Data
public class UserUpdateDTO {
    /**
     * ユーザーID
     */
    @NotNull(message = "ユーザーIDは必須です")
    private Long id;

    /**
     * 氏名
     */
    private String realName;

    /**
     * 電話番号
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "電話番号の形式が正しくありません")
    private String phone;

    /**
     * メールアドレス
     */
    @Email(message = "メールアドレスの形式が正しくありません")
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
}