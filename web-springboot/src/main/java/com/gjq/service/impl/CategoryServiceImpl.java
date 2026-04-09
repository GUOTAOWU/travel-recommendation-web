package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.category.CategoryAddDTO;
import com.gjq.dto.category.CategoryQueryDTO;
import com.gjq.dto.category.CategoryUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.mapper.CategoryMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.service.CategoryService;
import com.gjq.vo.category.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * カテゴリサービス実装クラス
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CategoryAddDTO dto) {
        // 名前の重複チェック
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, dto.getName());
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("カテゴリ名が既に存在します");
        }

        // エンティティを作成して保存
        Category entity = new Category();
        BeanUtils.copyProperties(dto, entity);
        categoryMapper.insert(entity);
        
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CategoryUpdateDTO dto) {
        // IDの存在チェック
        Category entity = categoryMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("カテゴリが存在しません");
        }

        // 名称を更新する場合、他のカテゴリと重複していないかチェック
        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(entity.getName())) {
            LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Category::getName, dto.getName());
            queryWrapper.ne(Category::getId, dto.getId());
            if (categoryMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("カテゴリ名が既に存在します");
            }
        }

        // アイコンを更新する場合、古いアイコンファイルを削除
        if (dto.getIconObjectKey() != null && !dto.getIconObjectKey().equals(entity.getIconObjectKey()) && 
            StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            try {
                fileClient.delete(entity.getIconBucket(), entity.getIconObjectKey());
            } catch (Exception e) {
                // ファイル削除失敗。ログのみ記録し、後続の処理には影響させない
                System.err.println("旧カテゴリのアイコンファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // プロパティの更新
        if (StringUtils.hasText(dto.getName())) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getIconBucket() != null) {
            entity.setIconBucket(dto.getIconBucket());
        }
        if (dto.getIconObjectKey() != null) {
            entity.setIconObjectKey(dto.getIconObjectKey());
        }

        categoryMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // IDの存在チェック
        Category entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("カテゴリが存在しません");
        }

        // 関連するアイテムの有無をチェック
        LambdaQueryWrapper<com.gjq.entity.Item> itemQueryWrapper = new LambdaQueryWrapper<>();
        itemQueryWrapper.eq(com.gjq.entity.Item::getCategoryId, id);
        if (itemMapper.selectCount(itemQueryWrapper) > 0) {
            throw new BusinessException("このカテゴリに関連付けられたアイテムが存在するため、削除できません");
        }

        // アイコンファイルを削除
        if (StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            try {
                fileClient.delete(entity.getIconBucket(), entity.getIconObjectKey());
            } catch (Exception e) {
                // ファイル削除失敗。ログのみ記録し、後続の処理には影響させない
                System.err.println("カテゴリのアイコンファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // カテゴリを削除
        categoryMapper.deleteById(id);
    }

    @Override
    public CategoryVO getById(Long id) {
        Category entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("カテゴリが存在しません");
        }
        return toVO(entity);
    }

    @Override
    public List<CategoryVO> list() {
        List<Category> list = categoryMapper.selectList(null);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Page<CategoryVO> page(CategoryQueryDTO dto) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        
        // 検索条件の追加
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like(Category::getName, dto.getName());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            queryWrapper.like(Category::getDescription, dto.getDescription());
        }
        
        // 作成日時の降順でソート
        queryWrapper.orderByDesc(Category::getCreateTime);
        
        // ページング検索
        Page<Category> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = categoryMapper.selectPage(page, queryWrapper);
        
        // VOへ変換
        Page<CategoryVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        List<CategoryVO> voList = new ArrayList<>();
        for (Category entity : page.getRecords()) {
            voList.add(toVO(entity));
        }
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public CategoryVO toVO(Category entity) {
        if (entity == null) {
            return null;
        }
        
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(entity, vo);
        
        // アイコンURLの設定
        if (StringUtils.hasText(entity.getIconBucket()) && StringUtils.hasText(entity.getIconObjectKey())) {
            vo.setIconUrl(fileClient.getFileUrl(entity.getIconBucket(), entity.getIconObjectKey()));
        }
        
        return vo;
    }
}