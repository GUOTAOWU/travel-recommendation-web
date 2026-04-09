package com.gjq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.entity.Like;

import java.util.List;

/**
 * いいねサービス
 */
public interface LikeService extends IService<Like> {

    /**
     * いいねを登録します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return 成功した場合はtrue
     */
    boolean like(Long userId, Long itemId);

    /**
     * いいねを取り消します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return 成功した場合はtrue
     */
    boolean unlike(Long userId, Long itemId);

    /**
     * ユーザーがいいね済みかどうかを判定します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return いいね済みの場合はtrue
     */
    boolean isLiked(Long userId, Long itemId);

    /**
     * アイテムのいいね数を取得します
     *
     * @param itemId アイテムID
     * @return いいね数
     */
    long getLikeCount(Long itemId);

    /**
     * ユーザーがいいねしたアイテムIDのリストを取得します
     *
     * @param userId ユーザーID
     * @return アイテムIDリスト
     */
    List<Long> getUserLikedItemIds(Long userId);
}