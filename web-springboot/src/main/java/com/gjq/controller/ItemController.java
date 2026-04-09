package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.Result;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.service.ItemService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.item.ItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * スポットコントローラー
 */
@RestController
@RequestMapping("/item")
@Tag(name = "スポット管理", description = "スポット関連インターフェース")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private FileClient fileClient;

    /**
     * スポットを追加
     */
    @PostMapping
    @Operation(summary = "スポットを追加", description = "スポット情報を追加します")
    public Result<Long> add(@RequestBody ItemAddDTO dto) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        // 作成者を現在ログイン中のユーザーに設定
        dto.setUserId(SecurityUtils.getUserId());
        
        Long id = itemService.add(dto);
        return Result.success(id);
    }

    /**
     * スポットを更新
     */
    @PutMapping
    @Operation(summary = "スポットを更新", description = "スポット情報を更新します")
    public Result<Void> update(@RequestBody ItemUpdateDTO dto) {
        itemService.update(dto);
        return Result.success();
    }

    /**
     * スポットを削除
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "スポットを削除", description = "IDに基づいてスポットを削除します")
    public Result<Void> delete(@Parameter(description = "スポットID", required = true) @PathVariable Long id) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        itemService.delete(id);
        return Result.success();
    }

    /**
     * スポット詳細を取得
     */
    @GetMapping("/{id}")
    @Operation(summary = "スポット詳細を取得", description = "IDに基づいてスポット詳細を取得します")
    public Result<ItemVO> getById(@Parameter(description = "スポットID", required = true) @PathVariable Long id) {
        ItemVO vo = itemService.getById(id);
        return Result.success(vo);
    }

    /**
     * スポットのページング検索
     */
    @GetMapping("/page")
    @Operation(summary = "スポットのページング検索", description = "スポット情報をページング検索します")
    public Result<Page<ItemVO>> page(ItemQueryDTO dto) {
        Page<ItemVO> page = itemService.page(dto);
        return Result.success(page);
    }

    /**
     * カテゴリIDに基づいてスポット一覧を取得
     */
    @GetMapping("/list/category/{categoryId}")
    @Operation(summary = "カテゴリIDに基づいてスポット一覧を取得", description = "カテゴリIDに基づいてスポット一覧を取得します")
    public Result<List<ItemVO>> listByCategoryId(
            @Parameter(description = "カテゴリID", required = true) @PathVariable Long categoryId) {
        List<ItemVO> list = itemService.listByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * タグに基づいてスポット一覧を取得
     */
    @GetMapping("/list/tag/{tag}")
    @Operation(summary = "タグに基づいてスポット一覧を取得", description = "タグに基づいてスポット一覧を取得します")
    public Result<List<ItemVO>> listByTag(
            @Parameter(description = "タグ", required = true) @PathVariable String tag) {
        List<ItemVO> list = itemService.listByTag(tag);
        return Result.success(list);
    }
}