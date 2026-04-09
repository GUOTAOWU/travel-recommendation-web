package com.gjq.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ユーザーログインDTO
 */
@Data
public class UserLoginDTO {
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
}