package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.entity.Item;
import com.gjq.vo.item.ItemVO;

import java.util.List;

/**
 * アイテムサービスインターフェース
 */
public interface ItemService {

    /**
     * アイテムを追加します
     *
     * @param dto アイテム追加DTO
     * @return アイテムID
     */
    Long add(ItemAddDTO dto);

    /**
     * アイテムを更新します
     *
     * @param dto アイテム更新DTO
     */
    void update(ItemUpdateDTO dto);

    /**
     * アイテムを削除します
     *
     * @param id アイテムID
     */
    void delete(Long id);

    /**
     * IDに基づいてアイテムを取得します
     *
     * @param id アイテムID
     * @return アイテムビューオブジェクト(VO)
     */
    ItemVO getById(Long id);

    /**
     * アイテムをページング検索します
     *
     * @param dto アイテム検索DTO
     * @return ページングされたアイテムビューオブジェクト(VO)
     */
    Page<ItemVO> page(ItemQueryDTO dto);

    /**
     * カテゴリIDに基づいてアイテムリストを取得します
     *
     * @param categoryId カテゴリID
     * @return アイテムビューオブジェクト(VO)のリスト
     */
    List<ItemVO> listByCategoryId(Long categoryId);

    /**
     * タグに基づいてアイテムリストを取得します
     *
     * @param tag タグ
     * @return アイテムビューオブジェクト(VO)のリスト
     */
    List<ItemVO> listByTag(String tag);

    /**
     * エンティティをビューオブジェクト(VO)に変換します
     *
     * @param entity アイテムエンティティ
     * @return アイテムビューオブジェクト(VO)
     */
    ItemVO toVO(Item entity);
}