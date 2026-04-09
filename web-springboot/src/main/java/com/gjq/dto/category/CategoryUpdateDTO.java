package com.gjq.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * カテゴリ更新DTO
 */
@Data
@Schema(description = "カテゴリ更新DTO")
public class CategoryUpdateDTO {

    @Schema(description = "カテゴリID", required = true)
    @NotNull(message = "カテゴリIDは必須です")
    private Long id;

    @Schema(description = "カテゴリ名")
    @Size(max = 50, message = "カテゴリ名は50文字以内で入力してください")
    private String name;

    @Schema(description = "カテゴリ説明")
    @Size(max = 255, message = "カテゴリ説明は255文字以内で入力してください")
    private String description;

    @Schema(description = "アイコンのバケット名")
    private String iconBucket;

    @Schema(description = "アイコンのオブジェクトキー")
    private String iconObjectKey;
}