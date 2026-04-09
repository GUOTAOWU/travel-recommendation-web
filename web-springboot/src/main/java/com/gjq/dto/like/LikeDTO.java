package com.gjq.dto.like;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * いいねDTO
 */
@Data
public class LikeDTO {
    /**
     * アイテムID
     */
    @NotNull(message = "アイテムIDは必須です")
    private Long itemId;
}