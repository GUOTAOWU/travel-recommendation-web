package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.client.FileClient;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.entity.Item;
import com.gjq.entity.User;
import com.gjq.entity.UserAction;
import com.gjq.mapper.ItemMapper;
import com.gjq.mapper.UserActionMapper;
import com.gjq.mapper.UserMapper;
import com.gjq.service.CategoryService;
import com.gjq.service.ItemService;
import com.gjq.service.UserActionService;
import com.gjq.vo.item.ItemVO;
import com.gjq.vo.user.UserInfo;
import com.gjq.vo.useraction.UserActionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ユーザー行動サービス実装クラス
 */
@Service
public class UserActionServiceImpl extends ServiceImpl<UserActionMapper, UserAction> implements UserActionService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ItemMapper itemMapper;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private FileClient fileClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserAction(Long userId, UserActionDTO dto) {
        UserAction userAction = new UserAction();
        userAction.setUserId(userId);
        userAction.setItemId(dto.getItemId());
        userAction.setActionType(dto.getActionType());
        userAction.setExtraData(dto.getExtraData());
        
        // ユーザー行動記録を保存
        return save(userAction);
    }

    @Override
    public Page<UserActionVO> pageUserActions(UserActionQueryDTO dto) {
        // 検索条件を構築
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        
        // ユーザー名のあいまい検索を処理
        if (StringUtils.hasText(dto.getUsername())) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                    .like(User::getUsername, dto.getUsername()));
            if (!users.isEmpty()) {
                List<Long> userIdList = users.stream().map(User::getId).toList();
                queryWrapper.in(UserAction::getUserId, userIdList);
            } else {
                // 一致するユーザーがいない場合、空の結果を返す
                return new Page<>();
            }
        } else if (dto.getUserId() != null) {
            // ユーザーIDが指定されている場合
            queryWrapper.eq(UserAction::getUserId, dto.getUserId());
        }
        
        // 観光スポット名のあいまい検索を処理
        if (StringUtils.hasText(dto.getItemTitle())) {
            List<Item> items = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                    .like(Item::getTitle, dto.getItemTitle()));
            if (!items.isEmpty()) {
                List<Long> itemIdList = items.stream().map(Item::getId).toList();
                queryWrapper.in(UserAction::getItemId, itemIdList);
            } else {
                // 一致するスポットがない場合、空の結果を返す
                return new Page<>();
            }
        } else if (dto.getItemId() != null) {
            // スポットIDが指定されている場合
            queryWrapper.eq(UserAction::getItemId, dto.getItemId());
        }
        
        // アクションタイプが指定されている場合
        if (dto.getActionType() != null) {
            queryWrapper.eq(UserAction::getActionType, dto.getActionType());
        }
        
        // 日時の降順でソート
        queryWrapper.orderByDesc(UserAction::getCreateTime);
        
        // ページング検索
        Page<UserAction> page = new Page<>(dto.getCurrent(), dto.getSize());
        page = page(page, queryWrapper);
        
        // VOへ変換
        Page<UserActionVO> voPage = new Page<>();
        BeanUtils.copyProperties(page, voPage, "records");
        
        // レコードの処理
        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            // すべてのユーザーIDを取得
            List<Long> userIds = new ArrayList<>();
            // すべてのスポットIDを取得
            List<Long> itemIds = new ArrayList<>();
            
            // すべてのIDを収集
            for (UserAction userAction : page.getRecords()) {
                if (userAction.getUserId() != null) {
                    userIds.add(userAction.getUserId());
                }
                if (userAction.getItemId() != null) {
                    itemIds.add(userAction.getItemId());
                }
            }
            
            // ユーザー情報を一括取得
            Map<Long, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(userIds);
                users.forEach(user -> userMap.put(user.getId(), user));
            }
            
            // スポット情報を一括取得
            Map<Long, Item> itemMap = new HashMap<>();
            if (!itemIds.isEmpty()) {
                List<Item> items = itemMapper.selectBatchIds(itemIds);
                items.forEach(item -> itemMap.put(item.getId(), item));
            }
            
            // VOに変換
            List<UserActionVO> voList = new ArrayList<>();
            for (UserAction userAction : page.getRecords()) {
                UserActionVO vo = toVO(userAction);
                
                // ユーザー情報を設定
                User user = userMap.get(userAction.getUserId());
                if (user != null) {
                    vo.setUserInfo(new UserInfo(user, fileClient));
                }
                
                // スポット情報を設定
                Item item = itemMap.get(userAction.getItemId());
                if (item != null) {
                    vo.setItem(itemService.toVO(item));
                }
                
                voList.add(vo);
            }
            
            voPage.setRecords(voList);
        }
        
        return voPage;
    }

    @Override
    public UserActionVO toVO(UserAction userAction) {
        if (userAction == null) {
            return null;
        }
        
        UserActionVO vo = new UserActionVO();
        BeanUtils.copyProperties(userAction, vo);
        return vo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteActions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 記録を一括削除
        return removeBatchByIds(ids);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteMyActions(Long userId, List<Long> ids) {
        if (userId == null || ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 現在のユーザー自身の記録のみを削除するように検索条件を構築
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAction::getUserId, userId)
                    .in(UserAction::getId, ids);
        
        // 条件に一致する記録を削除
        return remove(queryWrapper);
    }
    
    @Override
    public long getActionCountByItemIdAndType(Long itemId, int actionType) {
        // 検索条件を構築
        LambdaQueryWrapper<UserAction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAction::getItemId, itemId)
                    .eq(UserAction::getActionType, actionType);
        
        // 記録件数を集計
        return count(queryWrapper);
    }
}