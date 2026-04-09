package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.entity.Like;
import com.gjq.mapper.LikeMapper;
import com.gjq.service.LikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * いいねサービス実装クラス
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Override
    @Transactional
    public boolean like(Long userId, Long itemId) {
        // 既にいいね済みかチェック
        if (isLiked(userId, itemId)) {
            return true;
        }

        // いいね記録を作成
        Like like = new Like();
        like.setUserId(userId);
        like.setItemId(itemId);
        return save(like);
    }

    @Override
    @Transactional
    public boolean unlike(Long userId, Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .eq(Like::getItemId, itemId);
        
        // いいね記録を削除
        return remove(queryWrapper);
    }

    @Override
    public boolean isLiked(Long userId, Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .eq(Like::getItemId, itemId);
        
        // 記録が存在するか確認
        return count(queryWrapper) > 0;
    }

    @Override
    public long getLikeCount(Long itemId) {
        // 検索条件を構築
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getItemId, itemId);
        
        // いいね数を取得
        return count(queryWrapper);
    }

    @Override
    public List<Long> getUserLikedItemIds(Long userId) {
        // 検索条件を構築
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getUserId, userId)
                .select(Like::getItemId);
        
        // ユーザーがいいねしたアイテムIDリストを取得
        return list(queryWrapper).stream()
                .map(Like::getItemId)
                .collect(Collectors.toList());
    }
}