package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.entity.Favorite;
import com.gjq.vo.favorite.FavoriteVO;

import java.util.List;

/**
 * お気に入りサービス
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * お気に入りに追加します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return 成功した場合はtrue
     */
    boolean addFavorite(Long userId, Long itemId);

    /**
     * お気に入りを取り消します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return 成功した場合はtrue
     */
    boolean removeFavorite(Long userId, Long itemId);

    /**
     * ユーザーがお気に入り登録しているか確認します
     *
     * @param userId ユーザーID
     * @param itemId アイテムID
     * @return お気に入り登録されている場合はtrue
     */
    boolean isFavorite(Long userId, Long itemId);

    /**
     * ユーザーがお気に入り登録したアイテムIDリストを取得します
     *
     * @param userId ユーザーID
     * @return アイテムIDリスト
     */
    List<Long> getUserFavoriteItemIds(Long userId);

    /**
     * ユーザーのお気に入りリストをページングして取得します
     *
     * @param userId ユーザーID
     * @param page ページングパラメータ
     * @return お気に入りリスト
     */
    Page<FavoriteVO> getUserFavorites(Long userId, Page<Favorite> page);

    /**
     * アイテムのお気に入り登録数を取得します
     *
     * @param itemId アイテムID
     * @return お気に入り数
     */
    long getFavoriteCount(Long itemId);
}