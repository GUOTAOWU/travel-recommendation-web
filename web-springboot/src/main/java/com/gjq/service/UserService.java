package com.gjq.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjq.dto.user.*;
import com.gjq.vo.user.*;
import com.gjq.entity.User;

/**
 * ユーザーサービスインターフェース
 */
public interface UserService extends IService<User> {
    /**
     * ユーザーログイン
     *
     * @param dto ログインパラメータ
     * @return ログインユーザー情報
     */
    UserLoginVO login(UserLoginDTO dto);

    /**
     * ユーザー登録
     *
     * @param dto 登録パラメータ
     */
    void register(UserRegisterDTO dto);

    /**
     * パスワード変更
     *
     * @param dto パスワード変更パラメータ
     */
    void updatePassword(UserPasswordDTO dto);

    /**
     * ユーザー情報の更新
     *
     * @param dto ユーザー情報
     */
    void updateUser(UserUpdateDTO dto);

    /**
     * ユーザーリストのページング検索
     *
     * @param dto 検索パラメータ
     * @return ユーザーリスト
     */
    IPage<User> getUserPage(UserQueryDTO dto);

    /**
     * 現在のログインユーザー情報を取得
     *
     * @return ユーザー情報
     */
    UserLoginVO getCurrentUser();

    /**
     * ログアウト
     */
    void logout();

    /**
     * ユーザーの新規追加（管理者用など）
     */
    void addUser(UserAddDTO dto);

    /**
     * パスワードのリセット
     */
    void resetPassword(Long id);

    /**
     * ユーザーステータスの更新（有効/無効など）
     */
    void updateStatus(Long id, Integer status);
}