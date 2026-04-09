package com.gjq.vo.favorite;

import com.gjq.vo.item.ItemVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * お気に入りビューオブジェクト (VO)
 */
@Data
public class FavoriteVO {
    /**
     * お気に入りID
     */
    private Long id;

    /**
     * ユーザーID
     */
    private Long userId;

    /**
     * アイテムID
     */
    private Long itemId;

    /**
     * アイテム詳細情報
     */
    private ItemVO item;

    /**
     * お気に入り登録日時
     */
    private LocalDateTime createTime;
}