package com.gjq.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * アイテム更新DTO
 */
@Data
@Schema(description = "アイテム更新DTO")
public class ItemUpdateDTO {

    @Schema(description = "アイテムID", required = true)
    @NotNull(message = "アイテムIDは必須です")
    private Long id;

    @Schema(description = "アイテムのタイトル")
    @Size(max = 100, message = "アイテムのタイトルは100文字以内で入力してください")
    private String title;

    @Schema(description = "アイテムの説明")
    private String description;

    @Schema(description = "カテゴリID")
    private Long categoryId;

    @Schema(description = "カバー画像のバケット名")
    private String coverBucket;

    @Schema(description = "カバー画像のオブジェクトキー")
    private String coverObjectKey;

    @Schema(description = "ファイルのバケット名")
    private String fileBucket;

    @Schema(description = "ファイルのオブジェクトキー")
    private String fileObjectKey;

    @Schema(description = "タグ（カンマ区切り）")
    @Size(max = 255, message = "タグは255文字以内で入力してください")
    private String tags;

    @Schema(description = "追加データ。JSON形式。将来的に追加される可能性のあるフィールドの保存に使用")
    private String extraData;
}