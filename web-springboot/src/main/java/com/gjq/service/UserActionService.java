package com.gjq.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.dto.useraction.UserActionDTO;
import com.gjq.dto.useraction.UserActionQueryDTO;
import com.gjq.entity.UserAction;
import com.gjq.vo.useraction.UserActionVO;

import java.util.List;

/**
 * ユーザー行動サービスインターフェース
 */
public interface UserActionService extends IService<UserAction> {
    
    /**
     * ユーザー行動記録を追加します
     *
     * @param userId ユーザーID
     * @param dto ユーザー行動DTO
     * @return 成功した場合はtrue
     */
    boolean addUserAction(Long userId, UserActionDTO dto);
    
    /**
     * ユーザー行動をページング検索します
     *
     * @param dto 検索条件
     * @return ページング結果
     */
    Page<UserActionVO> pageUserActions(UserActionQueryDTO dto);
    
    /**
     * UserActionエンティティをVOに変換します
     *
     * @param userAction ユーザー行動エンティティ
     * @return ユーザー行動VO
     */
    UserActionVO toVO(UserAction userAction);
    
    /**
     * ユーザー行動記録を一括削除します
     *
     * @param ids 削除対象のレコードIDリスト
     * @return 成功した場合はtrue
     */
    boolean batchDeleteActions(List<Long> ids);
    
    /**
     * 現在のユーザーの行動記録を一括削除します
     *
     * @param userId ユーザーID
     * @param ids 削除対象のレコードIDリスト
     * @return 成功した場合はtrue
     */
    boolean batchDeleteMyActions(Long userId, List<Long> ids);
    
    /**
     * アイテムIDとアクションタイプに基づいてアクション数を集計します
     *
     * @param itemId アイテムID
     * @param actionType アクションタイプ（0-閲覧、1-購入）
     * @return アクション件数
     */
    long getActionCountByItemIdAndType(Long itemId, int actionType);
}