package com.gjq.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * カテゴリ追加DTO
 */
@Data
@Schema(description = "カテゴリ追加DTO")
public class CategoryAddDTO {

    @Schema(description = "カテゴリ名", required = true)
    @NotBlank(message = "カテゴリ名は必須です")
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