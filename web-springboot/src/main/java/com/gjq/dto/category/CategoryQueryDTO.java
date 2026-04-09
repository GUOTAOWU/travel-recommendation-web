package com.gjq.dto.category;

import com.gjq.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * カテゴリ検索DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "カテゴリ検索DTO")
public class CategoryQueryDTO extends PageDTO {

    @Schema(description = "カテゴリ名")
    private String name;

    @Schema(description = "カテゴリ説明")
    private String description;
}