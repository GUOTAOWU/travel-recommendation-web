package com.gjq.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ユーザーステータスDTO
 */
@Data
@Schema(description = "ユーザーステータスDTO")
public class UserStatusDTO {
    
    @NotNull(message = "ステータスは必須です")
    @Schema(description = "ステータス：0-無効、1-有効")
    private Integer status;
}