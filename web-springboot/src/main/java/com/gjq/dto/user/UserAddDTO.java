package com.gjq.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * ユーザー新規追加DTO
 */
@Data
@Schema(description = "ユーザー新規追加DTO")
public class UserAddDTO {
    
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 20, message = "ユーザー名は3文字から20文字の間で入力してください")
    @Schema(description = "ユーザー名")
    private String username;
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 6, max = 20, message = "パスワードは6文字から20文字の間で入力してください")
    @Schema(description = "パスワード")
    private String password;
    
    @NotBlank(message = "氏名は必須です")
    @Size(min = 2, max = 20, message = "氏名は2文字から20文字の間で入力してください")
    @Schema(description = "氏名")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "電話番号の形式が正しくありません")
    @Schema(description = "電話番号")
    private String phone;
    
    @Email(message = "メールアドレスの形式が正しくありません")
    @Schema(description = "メールアドレス")
    private String email;
    
    @NotNull(message = "ロールは必須です")
    @Schema(description = "ロール：0-一般ユーザー、1-管理者")
    private Integer role;
}