package com.gjq.vo.item;

import com.gjq.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * アイテムビューオブジェクト
 */
@Data
@Schema(description = "アイテムビューオブジェクト")
public class ItemVO {

    @Schema(description = "アイテムID")
    private Long id;

    @Schema(description = "アイテムのタイトル")
    private String title;

    @Schema(description = "アイテムの説明")
    private String description;

    @Schema(description = "カテゴリID")
    private Long categoryId;

    @Schema(description = "カテゴリ情報")
    private CategoryVO category;

    @Schema(description = "ユーザーID（作成者）")
    private Long userId;

    @Schema(description = "投稿者の氏名")
    private String userRealName;

    @Schema(description = "カバー画像のストレージバケット")
    private String coverBucket;

    @Schema(description = "カバー画像のオブジェクトキー")
    private String coverObjectKey;

    @Schema(description = "カバー画像URL")
    private String coverUrl;

    @Schema(description = "ファイルのストレージバケット")
    private String fileBucket;

    @Schema(description = "ファイルのオブジェクトキー")
    private String fileObjectKey;

    @Schema(description = "ファイルURL")
    private String fileUrl;

    @Schema(description = "タグ（カンマ区切り文字列）")
    private String tags;

    @Schema(description = "タグリスト（配列形式）")
    private String[] tagList;

    @Schema(description = "拡張データ（JSON形式）。将来的なフィールド拡張用")
    private String extraData;

    @Schema(description = "作成日時")
    private LocalDateTime createTime;

    @Schema(description = "更新日時")
    private LocalDateTime updateTime;
}