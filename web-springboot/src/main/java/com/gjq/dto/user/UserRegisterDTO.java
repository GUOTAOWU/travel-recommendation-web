package com.gjq.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * ユーザー登録DTO
 */
@Data
public class UserRegisterDTO {
    /**
     * ユーザー名
     */
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    /**
     * パスワード
     */
    @NotBlank(message = "パスワードは必須です")
    private String password;

    /**
     * 氏名
     */
    @NotBlank(message = "氏名は必須です")
    private String realName;

    /**
     * 電話番号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "電話番号の形式が正しくありません")
    private String phone;

    /**
     * メールアドレス
     */
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;
}