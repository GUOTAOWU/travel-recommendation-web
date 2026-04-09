package com.gjq.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.Result;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.service.CategoryService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.category.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * カテゴリコントローラー
 */
@RestController
@RequestMapping("/category")
@Tag(name = "カテゴリ管理", description = "カテゴリ関連インターフェース")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileClient fileClient;

    /**
     * カテゴリを追加
     */
    @PostMapping
    @Operation(summary = "カテゴリを追加", description = "カテゴリ情報を追加します")
    public Result<Long> add(@RequestBody CategoryAddDTO dto) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        Long id = categoryService.add(dto);
        return Result.success(id);
    }

    /**
     * カテゴリを更新
     */
    @PutMapping
    @Operation(summary = "カテゴリを更新", description = "カテゴリ情報を更新します")
    public Result<Void> update(@RequestBody CategoryUpdateDTO dto) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        categoryService.update(dto);
        return Result.success();
    }

    /**
     * カテゴリを削除
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "カテゴリを削除", description = "IDに基づいてカテゴリを削除します")
    public Result<Void> delete(@Parameter(description = "カテゴリID", required = true) @PathVariable Long id) {
        // 権限チェック
        if (!SecurityUtils.isAdmin()) {
            return Result.error("権限が不足しています");
        }
        
        categoryService.delete(id);
        return Result.success();
    }

    /**
     * カテゴリ詳細を取得
     */
    @GetMapping("/{id}")
    @Operation(summary = "カテゴリ詳細を取得", description = "IDに基づいてカテゴリ詳細を取得します")
    public Result<CategoryVO> getById(@Parameter(description = "カテゴリID", required = true) @PathVariable Long id) {
        CategoryVO vo = categoryService.getById(id);
        return Result.success(vo);
    }

    /**
     * カテゴリ一覧を取得
     */
    @GetMapping("/list")
    @Operation(summary = "カテゴリ一覧を取得", description = "すべてのカテゴリ一覧を取得します")
    public Result<List<CategoryVO>> list() {
        List<CategoryVO> list = categoryService.list();
        return Result.success(list);
    }

    /**
     * カテゴリのページング検索
     */
    @GetMapping("/page")
    @Operation(summary = "カテゴリのページング検索", description = "カテゴリ情報をページング検索します")
    public Result<Page<CategoryVO>> page(CategoryQueryDTO dto) {
        Page<CategoryVO> page = categoryService.page(dto);
        return Result.success(page);
    }
}