package com.gjq.dto.favorite;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * お気に入りDTO
 */
@Data
public class FavoriteDTO {
    
    /**
     * アイテムID
     */
    @NotNull(message = "アイテムIDは必須です")
    private Long itemId;
}