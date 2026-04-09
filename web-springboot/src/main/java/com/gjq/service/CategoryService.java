package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.vo.category.CategoryVO;

import java.util.List;

/**
 * カテゴリサービスインターフェース
 */
public interface CategoryService {

    /**
     * カテゴリを追加します
     *
     * @param dto カテゴリ追加DTO
     * @return カテゴリID
     */
    Long add(CategoryAddDTO dto);

    /**
     * カテゴリを更新します
     *
     * @param dto カテゴリ更新DTO
     */
    void update(CategoryUpdateDTO dto);

    /**
     * カテゴリを削除します
     *
     * @param id カテゴリID
     */
    void delete(Long id);

    /**
     * IDに基づいてカテゴリを取得します
     *
     * @param id カテゴリID
     * @return カテゴリビューオブジェクト(VO)
     */
    CategoryVO getById(Long id);

    /**
     * カテゴリリストを取得します
     *
     * @return カテゴリビューオブジェクト(VO)のリスト
     */
    List<CategoryVO> list();

    /**
     * カテゴリをページング検索します
     *
     * @param dto カテゴリ検索DTO
     * @return ページングされたカテゴリビューオブジェクト(VO)
     */
    Page<CategoryVO> page(CategoryQueryDTO dto);

    /**
     * エンティティをビューオブジェクト(VO)に変換します
     *
     * @param entity カテゴリエンティティ
     * @return カテゴリビューオブジェクト(VO)
     */
    CategoryVO toVO(Category entity);
}