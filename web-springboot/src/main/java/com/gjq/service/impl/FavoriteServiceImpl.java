package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.entity.Favorite;
import com.gjq.entity.Item;
import com.gjq.mapper.FavoriteMapper;
import com.gjq.service.FavoriteService;
import com.gjq.service.ItemService;
import com.gjq.vo.favorite.FavoriteVO;
import com.gjq.vo.item.ItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * お気に入りサービス実装クラス
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ItemService itemService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long itemId) {
        // すでにお気に入り登録されているかチェック
        if (isFavorite(userId, itemId)) {
            return true;
        }

        // お気に入り記録を作成
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        return save(favorite);
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // お気に入り記録を削除
        return remove(queryWrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // 記録が存在するか確認
        return count(queryWrapper) > 0;
    }

    @Override
    public List<Long> getUserFavoriteItemIds(Long userId) {
        // 検索条件を構築
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .select(Favorite::getItemId);
        
        // ユーザーがお気に入り登録したアイテムIDリストを取得
        return list(queryWrapper).stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FavoriteVO> getUserFavorites(Long userId, Page<Favorite> page) {
        // 検索条件を構築
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        
        // お気に入り記録を検索
        Page<Favorite> favoriteList = page(page, queryWrapper);
        
        // お気に入り記録がない場合は、空のページを返す
        if (favoriteList.getRecords().isEmpty()) {
            return new Page<FavoriteVO>()
                    .setRecords(new ArrayList<>())
                    .setCurrent(favoriteList.getCurrent())
                    .setSize(favoriteList.getSize())
                    .setTotal(favoriteList.getTotal());
        }
        
        // VOの構築およびデータクリーンアップ（自己修復）処理
        List<FavoriteVO> voList = new ArrayList<>();
        
        for (Favorite favorite : favoriteList.getRecords()) {
            try {
                // スポットの詳細を取得
                ItemVO itemVO = itemService.getById(favorite.getItemId());
                
                if (itemVO == null) {
                    // 【コア修正・自己修復ロジック】：スポットが既に削除されている場合（null）、無効なお気に入り記録を削除
                    this.removeById(favorite.getId());
                    continue; // 無効なデータをスキップ
                }
                
                // 正常にデータを組み立て
                FavoriteVO vo = new FavoriteVO();
                BeanUtils.copyProperties(favorite, vo);
                vo.setItem(itemVO);
                voList.add(vo);
                
            } catch (Exception e) {
                // 【自己修復ロジック】：itemService.getById 内で「アイテムが存在しません」例外がスローされた場合の対応
                this.removeById(favorite.getId());
                continue; // 無効なデータをスキップ
            }
        }
        
        // 分ページ結果を構築
        Page<FavoriteVO> voPage = new Page<>();
        BeanUtils.copyProperties(favoriteList, voPage, "records");
        // クリーンアップ後の正常な記録を格納
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public long getFavoriteCount(Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getItemId, itemId);
        
        // お気に入り数を取得して返す
        return count(queryWrapper);
    }
}