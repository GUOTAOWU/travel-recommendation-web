package com.gjq.dto.useraction;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * ユーザー行動DTO
 */
@Data
@Schema(description = "ユーザー行動DTO")
public class UserActionDTO {
    
    /**
     * アイテムID
     */
    @NotNull(message = "アイテムIDは必須です")
    @Schema(description = "アイテムID", required = true)
    private Long itemId;
    
    /**
     * アクションタイプ (0: 閲覧, 1: 購入)
     */
    @NotNull(message = "アクションタイプは必須です")
    @Schema(description = "アクションタイプ：0-閲覧、1-購入", required = true)
    private Integer actionType;
    
    /**
     * 拡張データ (JSON形式)
     */
    @Schema(description = "拡張データ (JSON形式)。異なるシナリオ固有のデータを保存するために使用")
    private String extraData;
}