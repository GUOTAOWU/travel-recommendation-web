package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.PageDTO;
import com.gjq.dto.like.LikeDTO;
import com.gjq.entity.Like;
import com.gjq.service.LikeService;
import com.gjq.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * いいねコントローラー
 */
@RestController
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    /**
     * いいねを登録
     *
     * @param likeDTO いいねDTO
     * @return 実行結果
     */
    @PostMapping
    public Result<?> like(@RequestBody LikeDTO likeDTO) {
        Long userId = SecurityUtils.getUserId();
        boolean success = likeService.like(userId, likeDTO.getItemId());
        return success ? Result.success() : Result.error("いいねに失敗しました");
    }

    /**
     * いいねを解除
     *
     * @param itemId アイテムID
     * @return 実行結果
     */
    @DeleteMapping("/{itemId}")
    public Result<?> unlike(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean success = likeService.unlike(userId, itemId);
        return success ? Result.success() : Result.error("いいね解除に失敗しました");
    }

    /**
     * いいね状態を確認
     *
     * @param itemId アイテムID
     * @return いいね状態
     */
    @GetMapping("/status/{itemId}")
    public Result<Boolean> status(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getUserId();
        boolean isLiked = likeService.isLiked(userId, itemId);
        return Result.success(isLiked);
    }

    /**
     * アイテムのいいね数を取得
     *
     * @param itemId アイテムID
     * @return いいね数
     */
    @GetMapping("/count/{itemId}")
    public Result<Long> count(@PathVariable Long itemId) {
        long count = likeService.getLikeCount(itemId);
        return Result.success(count);
    }

    /**
     * アイテムのいいね状態といいね数を一括確認
     *
     * @param itemIds アイテムIDリスト
     * @return いいね状態といいね数のマップ
     */
    @GetMapping("/batch")
    public Result<Map<Long, Map<String, Object>>> batchStatus(@RequestParam List<Long> itemIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        Long userId = SecurityUtils.getUserId();

        for (Long itemId : itemIds) {
            Map<String, Object> itemResult = new HashMap<>();
            itemResult.put("isLiked", likeService.isLiked(userId, itemId));
            itemResult.put("count", likeService.getLikeCount(itemId));
            result.put(itemId, itemResult);
        }

        return Result.success(result);
    }

    /**
     * ユーザーがいいねしたアイテムIDリストを取得
     *
     * @return アイテムIDリスト
     */
    @GetMapping("/user/items")
    public Result<List<Long>> userLikedItems() {
        Long userId = SecurityUtils.getUserId();
        List<Long> itemIds = likeService.getUserLikedItemIds(userId);
        return Result.success(itemIds);
    }
    
    /**
     * 管理者用：全いいねデータのページング取得
     * * @param pageDTO ページングパラメータ
     * @return いいねデータ一覧
     */
    @GetMapping("/admin/page")
    public Result<Page<Like>> adminLikePage(PageDTO pageDTO) {
        Page<Like> page = new Page<>(pageDTO.getCurrent(), pageDTO.getSize());
        Page<Like> resultPage = likeService.page(page);
        return Result.success(resultPage);
    }
}