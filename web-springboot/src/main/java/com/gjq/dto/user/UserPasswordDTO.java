package com.gjq.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ユーザーパスワード変更DTO
 */
@Data
public class UserPasswordDTO {
    /**
     * ユーザーID
     */
    @NotNull(message = "ユーザーIDは必須です")
    private Long id;

    /**
     * 現在のパスワード
     */
    @NotBlank(message = "現在のパスワードは必須です")
    private String oldPassword;

    /**
     * 新しいパスワード
     */
    @NotBlank(message = "新しいパスワードは必須です")
    private String newPassword;
}