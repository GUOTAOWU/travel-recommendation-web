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
 * 收藏Service实现
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private ItemService itemService;

    @Override
    @Transactional
    public boolean addFavorite(Long userId, Long itemId) {
        // 检查是否已收藏
        if (isFavorite(userId, itemId)) {
            return true;
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setItemId(itemId);
        return save(favorite);
    }

    @Override
    @Transactional
    public boolean removeFavorite(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // 删除收藏记录
        return remove(queryWrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId);
        
        // 查询是否存在记录
        return count(queryWrapper) > 0;
    }

    @Override
    public List<Long> getUserFavoriteItemIds(Long userId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .select(Favorite::getItemId);
        
        // 查询用户收藏的物品ID列表
        return list(queryWrapper).stream()
                .map(Favorite::getItemId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FavoriteVO> getUserFavorites(Long userId, Page<Favorite> page) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime);
        
        // 查询收藏记录
        Page<Favorite> favoriteList = page(page, queryWrapper);
        
        // 如果没有收藏记录，直接返回空页
        if (favoriteList.getRecords().isEmpty()) {
            return new Page<FavoriteVO>()
                    .setRecords(new ArrayList<>())
                    .setCurrent(favoriteList.getCurrent())
                    .setSize(favoriteList.getSize())
                    .setTotal(favoriteList.getTotal());
        }
        
        // 构建VO并进行脏数据自愈处理
        List<FavoriteVO> voList = new ArrayList<>();
        
        for (Favorite favorite : favoriteList.getRecords()) {
            try {
                // 查询景点详情
                ItemVO itemVO = itemService.getById(favorite.getItemId());
                
                if (itemVO == null) {
                    // 【核心修复与自愈逻辑】：景点已被删除（返回null），清理失效的收藏记录
                    this.removeById(favorite.getId());
                    continue; // 跳过该脏数据
                }
                
                // 正常组装数据
                FavoriteVO vo = new FavoriteVO();
                BeanUtils.copyProperties(favorite, vo);
                vo.setItem(itemVO);
                voList.add(vo);
                
            } catch (Exception e) {
                // 【自愈逻辑】：应对 itemService.getById 内部抛出"物品不存在"异常的情况
                this.removeById(favorite.getId());
                continue; // 跳过该脏数据
            }
        }
        
        // 构建分页结果
        Page<FavoriteVO> voPage = new Page<>();
        BeanUtils.copyProperties(favoriteList, voPage, "records");
        // 将清洗后正常的记录放进去
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public long getFavoriteCount(Long itemId) {
        // 构建查询条件
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getItemId, itemId);
        
        // 查询并返回收藏数量
        return count(queryWrapper);
    }
}