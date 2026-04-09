package com.gjq.dto.item;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * アイテム検索DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "アイテム検索DTO")
public class ItemQueryDTO extends PageDTO {

    @Schema(description = "アイテムのタイトル")
    private String title;

    @Schema(description = "アイテムの説明")
    private String description;

    @Schema(description = "カテゴリID")
    private Long categoryId;

    @Schema(description = "タグ")
    private String tag;
    
    @Schema(description = "投稿者の氏名")
    private String userRealName;
}