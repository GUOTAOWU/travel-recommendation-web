package com.gjq.vo.useraction;

import com.gjq.vo.item.ItemVO;
import com.gjq.vo.user.UserInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ユーザー行動ビューオブジェクト
 */
@Data
@Schema(description = "ユーザー行動ビューオブジェクト")
public class UserActionVO {
    /**
     * ID
     */
    @Schema(description = "アクションID")
    private Long id;

    /**
     * ユーザーID
     */
    @Schema(description = "ユーザーID")
    private Long userId;

    /**
     * ユーザー情報
     */
    @Schema(description = "ユーザー詳細情報")
    private UserInfo userInfo;

    /**
     * アイテムID
     */
    @Schema(description = "アイテムID")
    private Long itemId;

    /**
     * アイテム情報
     */
    @Schema(description = "アイテム詳細情報")
    private ItemVO item;

    /**
     * 行動タイプ(0:閲覧 1:購入)
     */
    @Schema(description = "行動タイプ：0-閲覧、1-購入")
    private Integer actionType;

    /**
     * 拡張データ(JSON形式)
     */
    @Schema(description = "拡張データ(JSON形式)")
    private String extraData;

    /**
     * 作成時間
     */
    @Schema(description = "アクション発生日時")
    private LocalDateTime createTime;
}