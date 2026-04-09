package com.gjq.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * カテゴリビューオブジェクト
 */
@Data
@Schema(description = "カテゴリビューオブジェクト")
public class CategoryVO {

    @Schema(description = "カテゴリID")
    private Long id;

    @Schema(description = "カテゴリ名")
    private String name;

    @Schema(description = "カテゴリの説明")
    private String description;

    @Schema(description = "アイコンのストレージバケット")
    private String iconBucket;

    @Schema(description = "アイコンのオブジェクトキー")
    private String iconObjectKey;

    @Schema(description = "アイコンURL")
    private String iconUrl;

    @Schema(description = "作成日時")
    private LocalDateTime createTime;

    @Schema(description = "更新日時")
    private LocalDateTime updateTime;
}