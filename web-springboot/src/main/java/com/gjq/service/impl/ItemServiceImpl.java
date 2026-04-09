package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gjq.client.FileClient;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.item.ItemAddDTO;
import com.gjq.dto.item.ItemQueryDTO;
import com.gjq.dto.item.ItemUpdateDTO;
import com.gjq.entity.Category;
import com.gjq.entity.Item;
import com.gjq.entity.User;
import com.gjq.mapper.CategoryMapper;
import com.gjq.mapper.ItemMapper;
import com.gjq.mapper.UserMapper;
import com.gjq.service.CategoryService;
import com.gjq.service.ItemService;
import com.gjq.vo.item.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * アイテム（スポット）サービス実装クラス
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileClient fileClient;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(ItemAddDTO dto) {
        // カテゴリの存在チェック
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException("カテゴリが存在しません");
        }

        // エンティティを作成して保存
        Item entity = new Item();
        BeanUtils.copyProperties(dto, entity);
        itemMapper.insert(entity);
        
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ItemUpdateDTO dto) {
        // IDの存在チェック
        Item entity = itemMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("アイテムが存在しません");
        }

        // カテゴリIDを更新する場合、カテゴリの存在チェック
        if (dto.getCategoryId() != null && !dto.getCategoryId().equals(entity.getCategoryId())) {
            Category category = categoryMapper.selectById(dto.getCategoryId());
            if (category == null) {
                throw new BusinessException("カテゴリが存在しません");
            }
        }

        // カバー画像を更新する場合、古いファイルを削除
        if (dto.getCoverObjectKey() != null && !dto.getCoverObjectKey().equals(entity.getCoverObjectKey()) && 
            StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            try {
                fileClient.delete(entity.getCoverBucket(), entity.getCoverObjectKey());
            } catch (Exception e) {
                // ファイル削除失敗。ログのみ記録し、後続の処理には影響させない
                System.err.println("旧アイテムのカバーファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // 添付ファイルを更新する場合、古いファイルを削除
        if (dto.getFileObjectKey() != null && !dto.getFileObjectKey().equals(entity.getFileObjectKey()) && 
            StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            try {
                fileClient.delete(entity.getFileBucket(), entity.getFileObjectKey());
            } catch (Exception e) {
                // ファイル削除失敗。ログのみ記録し、後続の処理には影響させない
                System.err.println("旧アイテムのファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // プロパティの更新
        if (StringUtils.hasText(dto.getTitle())) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            entity.setCategoryId(dto.getCategoryId());
        }
        if (dto.getCoverBucket() != null) {
            entity.setCoverBucket(dto.getCoverBucket());
        }
        if (dto.getCoverObjectKey() != null) {
            entity.setCoverObjectKey(dto.getCoverObjectKey());
        }
        if (dto.getFileBucket() != null) {
            entity.setFileBucket(dto.getFileBucket());
        }
        if (dto.getFileObjectKey() != null) {
            entity.setFileObjectKey(dto.getFileObjectKey());
        }
        if (dto.getTags() != null) {
            entity.setTags(dto.getTags());
        }
        if (dto.getExtraData() != null) {
            entity.setExtraData(dto.getExtraData());
        }

        itemMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // IDの存在チェック
        Item entity = itemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("アイテムが存在しません");
        }

        // カバー画像を削除
        if (StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            try {
                fileClient.delete(entity.getCoverBucket(), entity.getCoverObjectKey());
            } catch (Exception e) {
                System.err.println("アイテムのカバーファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // 関連ファイルを削除
        if (StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            try {
                fileClient.delete(entity.getFileBucket(), entity.getFileObjectKey());
            } catch (Exception e) {
                System.err.println("アイテムの関連ファイル削除に失敗しました: " + e.getMessage());
            }
        }

        // アイテムを削除
        itemMapper.deleteById(id);
    }

    @Override
    public ItemVO getById(Long id) {
        Item entity = itemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("アイテムが存在しません");
        }
        return toVO(entity);
    }

    @Override
    public Page<ItemVO> page(ItemQueryDTO dto) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        
        // 検索条件の追加
        if (StringUtils.hasText(dto.getTitle())) {
            queryWrapper.like(Item::getTitle, dto.getTitle());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            queryWrapper.like(Item::getDescription, dto.getDescription());
        }
        if (dto.getCategoryId() != null) {
            queryWrapper.eq(Item::getCategoryId, dto.getCategoryId());
        }
        if (StringUtils.hasText(dto.getTag())) {
            // タグ検索の処理 - 複数タグに対応
            String tagQuery = dto.getTag().trim();
            
            // カンマが含まれる場合、複数タグとして処理
            if (tagQuery.contains(",")) {
                String[] tags = tagQuery.split(",");
                for (String tag : tags) {
                    if (StringUtils.hasText(tag)) {
                        // 部分一致を防ぐため、カンマの有無を考慮したLIKE検索を実施
                        // 例: "科学,技術" というタグに対し、"科"はヒットするが "学技" はヒットしないように制御
                        queryWrapper.and(q -> q.like(Item::getTags, "," + tag.trim() + ",")
                                .or()
                                .like(Item::getTags, tag.trim() + ",")
                                .or()
                                .like(Item::getTags, "," + tag.trim())
                                .or()
                                .eq(Item::getTags, tag.trim()));
                    }
                }
            } else {
                // 単一タグ検索
                queryWrapper.and(q -> q.like(Item::getTags, "," + tagQuery + ",")
                        .or()
                        .like(Item::getTags, tagQuery + ",")
                        .or()
                        .like(Item::getTags, "," + tagQuery)
                        .or()
                        .eq(Item::getTags, tagQuery));
            }
        }
        
        // 作成者の氏名で検索
        if (StringUtils.hasText(dto.getUserRealName())) {
            // 氏名条件に一致するユーザーIDリストを取得
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.like(User::getRealName, dto.getUserRealName());
            List<User> users = userMapper.selectList(userQuery);
            List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
            
            // 一致するユーザーがいない場合、空の結果を保証するために不可能な条件を追加
            if (userIds.isEmpty()) {
                queryWrapper.eq(Item::getUserId, -1L);
            } else {
                queryWrapper.in(Item::getUserId, userIds);
            }
        }
        
        // 作成日時の降順でソート
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        // ページング検索
        Page<Item> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = itemMapper.selectPage(page, queryWrapper);
        
        // VOへ変換
        Page<ItemVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        List<ItemVO> voList = new ArrayList<>();
        for (Item entity : page.getRecords()) {
            voList.add(toVO(entity));
        }
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<ItemVO> listByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getCategoryId, categoryId);
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        List<Item> list = itemMapper.selectList(queryWrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<ItemVO> listByTag(String tag) {
        if (!StringUtils.hasText(tag)) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Item::getTags, tag);
        queryWrapper.orderByDesc(Item::getCreateTime);
        
        List<Item> list = itemMapper.selectList(queryWrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ItemVO toVO(Item entity) {
        if (entity == null) {
            return null;
        }
        
        ItemVO vo = new ItemVO();
        BeanUtils.copyProperties(entity, vo);
        
        // カバー画像URLの設定
        if (StringUtils.hasText(entity.getCoverBucket()) && StringUtils.hasText(entity.getCoverObjectKey())) {
            vo.setCoverUrl(fileClient.getFileUrl(entity.getCoverBucket(), entity.getCoverObjectKey()));
        }
        
        // ファイルURLの設定
        if (StringUtils.hasText(entity.getFileBucket()) && StringUtils.hasText(entity.getFileObjectKey())) {
            vo.setFileUrl(fileClient.getFileUrl(entity.getFileBucket(), entity.getFileObjectKey()));
        }
        
        // タグリストの設定
        if (StringUtils.hasText(entity.getTags())) {
            vo.setTagList(entity.getTags().split(","));
        }
        
        // カテゴリ情報の設定
        Category category = categoryMapper.selectById(entity.getCategoryId());
        if (category != null) {
            vo.setCategory(categoryService.toVO(category));
        }
        
        // 投稿者の氏名を設定
        if (entity.getUserId() != null) {
            User user = userMapper.selectById(entity.getUserId());
            if (user != null) {
                vo.setUserRealName(user.getRealName());
            }
        }
        
        return vo;
    }
}