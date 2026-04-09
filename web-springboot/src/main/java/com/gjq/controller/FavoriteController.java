package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.PageDTO;
import com.gjq.dto.favorite.FavoriteDTO;
import com.gjq.entity.Favorite;
import com.gjq.service.FavoriteService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.favorite.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * お気に入りコントローラー
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * お気に入りに追加
     *
     * @param favoriteDTO お気に入りDTO
     * @return 実行結果
     */
    @PostMapping
    public Result<?> addFavorite(@RequestBody FavoriteDTO favoriteDTO) {
        Long userId = SecurityUtils.getUserId();
        boolean success = favoriteService.addFavorite(userId, favoriteDTO.getItemId());
        return success ? Result.success() : Result.error("お気に入り登録に失敗しました");
    }

    /**
     * お気に入りを解除
     *
     * @param itemId アイテムID
     * @return 実行結果
     */
    @DeleteMapping("/{itemId}")
    public Result<?> removeFavorite(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = favoriteService.removeFavorite(userId, itemId);
        return success ? Result.success() : Result.error("お気に入り解除に失敗しました");
    }

    /**
     * お気に入り状態を確認
     *
     * @param itemId アイテムID
     * @return お気に入り状態
     */
    @GetMapping("/status/{itemId}")
    public Result<Boolean> status(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, itemId);
        return Result.success(isFavorite);
    }

    /**
     * ユーザーがお気に入り登録したアイテムID一覧を取得
     *
     * @return アイテムID一覧
     */
    @GetMapping("/user/items")
    public Result<List<Long>> userFavoriteItems() {
        Long userId = SecurityUtils.getUserId();
        List<Long> itemIds = favoriteService.getUserFavoriteItemIds(userId);
        return Result.success(itemIds);
    }

    /**
     * ユーザーのお気に入り一覧をページング形式で取得
     *
     * @param pageDTO ページングパラメータ
     * @return お気に入り一覧
     */
    @GetMapping("/user/page")
    public Result<Page<FavoriteVO>> userFavoritePage(PageDTO pageDTO) {
        Long userId = SecurityUtils.getUserId();
        Page<Favorite> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<FavoriteVO> voPage = favoriteService.getUserFavorites(userId, page);
        return Result.success(voPage);
    }

    /**
     * アイテムのお気に入り数を取得
     *
     * @param itemId アイテムID
     * @return お気に入り数
     */
    @GetMapping("/count/{itemId}")
    public Result<Long> getFavoriteCount(@PathVariable Long itemId) {
        long count = favoriteService.getFavoriteCount(itemId);
        return Result.success(count);
    }
    
    /**
     * 管理者用：全お気に入りデータのページング取得
     * * @param pageDTO ページングパラメータ
     * @return お気に入りデータ一覧
     */
    @GetMapping("/admin/page")
    public Result<Page<Favorite>> adminFavoritePage(PageDTO pageDTO) {
        Page<Favorite> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Favorite> resultPage = favoriteService.page(page);
        return Result.success(resultPage);
    }
}