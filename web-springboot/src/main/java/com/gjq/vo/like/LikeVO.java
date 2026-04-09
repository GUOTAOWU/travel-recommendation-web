package com.gjq.vo.like;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * いいねビューオブジェクト (VO)
 */
@Data
public class LikeVO {
    /**
     * いいねID
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
     * 作成日時
     */
    private LocalDateTime createTime;
}