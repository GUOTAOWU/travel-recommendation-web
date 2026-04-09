package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.common.Result;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.service.UserActionService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.useraction.UserActionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * ユーザー行動コントローラー
 */
@Tag(name = "ユーザー行動", description = "ユーザー行動関連インターフェース")
@RestController
@RequestMapping("/user-action")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    /**
     * ユーザー行動ログを追加
     *
     * @param dto ユーザー行動DTO
     * @return 成功したかどうか
     */
    @Operation(summary = "ユーザー行動ログを追加", description = "ユーザーの閲覧/予約ログを追加します")
    @PostMapping
    public Result<Boolean> addUserAction(@RequestBody @Valid UserActionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        boolean success = userActionService.addUserAction(userId, dto);
        return Result.success(success);
    }

    /**
     * 現在のユーザーの行動ログをページング検索
     *
     * @param dto 検索条件
     * @return ページング結果
     */
    @Operation(summary = "現在のユーザーの行動ログをページング検索", description = "現在のユーザーの閲覧/予約ログをページング検索します")
    @GetMapping("/page")
    public Result<Page<UserActionVO>> pageMyActions(UserActionQueryDTO dto) {
        // 現在のユーザーIDを設定
        dto.setUserId(SecurityUtils.getUserId());
        Page<UserActionVO> page = userActionService.pageUserActions(dto);
        return Result.success(page);
    }

    /**
     * 全ユーザーの行動ログをページング検索（管理者用インターフェース）
     *
     * @param dto 検索条件
     * @return ページング結果
     */
    @Operation(summary = "全ユーザーの行動ログをページング検索", description = "全ユーザーの閲覧/予約ログをページング検索します（管理者用）")
    @GetMapping("/admin/page")
    public Result<Page<UserActionVO>> pageAllActions(UserActionQueryDTO dto) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        Page<UserActionVO> page = userActionService.pageUserActions(dto);
        return Result.success(page);
    }

    /**
     * ユーザー行動ログを一括削除（管理者用インターフェース）
     *
     * @param ids 削除対象のログIDリスト
     * @return 成功したかどうか
     */
    @Operation(summary = "ユーザー行動ログを一括削除", description = "ユーザー行動ログを一括削除します（管理者用）")
    @DeleteMapping("/batch")
    public Result<Boolean> batchDeleteActions(@RequestBody @Valid @Parameter(description = "ログIDリスト") 
                                             List<Long> ids) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        boolean success = userActionService.batchDeleteActions(ids);
        return Result.success(success);
    }
    
    /**
     * 現在のユーザーの行動ログを一括削除
     *
     * @param ids 削除対象のログIDリスト
     * @return 成功したかどうか
     */
    @Operation(summary = "現在のユーザーの行動ログを一括削除", description = "現在のユーザーの行動ログを一括削除します")
    @DeleteMapping("/my/batch")
    public Result<Boolean> batchDeleteMyActions(@RequestBody @Valid @Parameter(description = "ログIDリスト") 
                                              List<Long> ids) {
        Long userId = SecurityUtils.getUserId();
        boolean success = userActionService.batchDeleteMyActions(userId, ids);
        return Result.success(success);
    }
    
    /**
     * スポットの閲覧数を取得
     *
     * @param itemId アイテムID
     * @return 閲覧数
     */
    @Operation(summary = "スポットの閲覧数を取得", description = "指定されたスポットの閲覧数を取得します")
    @GetMapping("/view/count/{itemId}")
    public Result<Long> getViewCount(@PathVariable Long itemId) {
        // 閲覧アクションのタイプは0
        long count = userActionService.getActionCountByItemIdAndType(itemId, 0);
        return Result.success(count);
    }
    
    /**
     * スポットの予約数を取得
     *
     * @param itemId アイテムID
     * @return 予約数
     */
    @Operation(summary = "スポットの予約数を取得", description = "指定されたスポットの予約数を取得します")
    @GetMapping("/reservation/count/{itemId}")
    public Result<Long> getReservationCount(@PathVariable Long itemId) {
        // 予約アクションのタイプは1
        long count = userActionService.getActionCountByItemIdAndType(itemId, 1);
        return Result.success(count);
    }
}