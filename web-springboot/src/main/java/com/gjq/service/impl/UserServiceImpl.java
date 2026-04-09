package com.gjq.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjq.client.FileClient;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.user.*;
import com.gjq.vo.user.*;
import com.gjq.entity.User;
import com.gjq.mapper.UserMapper;
import com.gjq.service.UserService;
import com.gjq.utils.SecurityUtils;
import com.gjq.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * ユーザーサービス実装クラス
 */
@Slf4j
@Service
@RequiredArgsConstructor  // finalが付与されたすべてのフィールドを対象に構造生成および依存性注入を自動で行う
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final FileClient fileClient;

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        // ユーザーを検索
        User user = lambdaQuery()
                .eq(User::getUsername, dto.getUsername())
                .one();
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }

        // パスワード照合
        if (!SecureUtil.md5(dto.getPassword()).equals(user.getPassword())) {
            throw new BusinessException("パスワードが間違っています");
        }

        // ステータス確認
        if (user.getStatus() == 0) {
            throw new BusinessException("アカウントが無効化されています");
        }

        // ログイン応答オブジェクトの作成
        UserLoginVO loginVO = new UserLoginVO(user, null, fileClient);
        // トークンの生成
        String token = JwtUtils.generateToken(loginVO.getUserInfo().getId());
        // トークンの設定
        loginVO.setToken(token);
        return loginVO;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        // ユーザー名の重複チェック
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException("ユーザー名が既に存在します");
        }

        // ユーザー作成
        User user = BeanUtil.copyProperties(dto, User.class);
        // パスワードの設定（MD5暗号化）
        user.setPassword(SecureUtil.md5(dto.getPassword()));
        // デフォルトロールの設定（0:一般ユーザー）
        user.setRole(0);
        // ステータスの設定（1:有効）
        user.setStatus(1);

        // ユーザー保存
        save(user);
    }

    @Override
    public void updatePassword(UserPasswordDTO dto) {
        // ユーザー確認
        User user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }

        // 元のパスワードの検証
        if (!SecureUtil.md5(dto.getOldPassword()).equals(user.getPassword())) {
            throw new BusinessException("元のパスワードが間違っています");
        }

        // パスワードの更新
        user.setPassword(SecureUtil.md5(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    public void updateUser(UserUpdateDTO dto) {
        // ユーザー確認
        User user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }

        // アバターが更新された場合、古いアバターファイルを削除
        if ((dto.getAvatarBucket() != null && !dto.getAvatarBucket().equals(user.getAvatarBucket())) ||
            (dto.getAvatarObjectKey() != null && !dto.getAvatarObjectKey().equals(user.getAvatarObjectKey()))) {
            if (StrUtil.isNotBlank(user.getAvatarBucket()) && StrUtil.isNotBlank(user.getAvatarObjectKey())) {
                try {
                    fileClient.delete(user.getAvatarBucket(), user.getAvatarObjectKey());
                } catch (Exception e) {
                    // 削除失敗は更新処理自体には影響させない
                    log.error("旧アバターの削除に失敗しました", e);
                }
            }
        }

        // ユーザー情報の更新
        BeanUtil.copyProperties(dto, user);
        updateById(user);
    }

    @Override
    public IPage<User> getUserPage(UserQueryDTO dto) {
        // 検索条件の構築
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StrUtil.isNotBlank(dto.getUsername()), User::getUsername, dto.getUsername())
                .like(StrUtil.isNotBlank(dto.getRealName()), User::getRealName, dto.getRealName())
                .like(StrUtil.isNotBlank(dto.getPhone()), User::getPhone, dto.getPhone())
                .like(StrUtil.isNotBlank(dto.getEmail()), User::getEmail, dto.getEmail())
                .eq(dto.getRole() != null, User::getRole, dto.getRole())
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus());

        // ページング検索の実行
        return page(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
    }

    @Override
    public UserLoginVO getCurrentUser() {
        // SecurityUtilsを使用して現在のユーザーを取得
        User user = SecurityUtils.getCurrentUser();
        // ユーザー情報を返す（トークンは既にクライアント側にあるためnullを設定）
        return new UserLoginVO(user, null, fileClient);
    }

    @Override
    public void logout() {
        // フロントエンド側でトークンを破棄するため、バックエンドでの処理は不要
    }

    @Override
    public void addUser(UserAddDTO dto) {
        // ユーザー名の重複チェック
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BusinessException("ユーザー名が既に存在します");
        }

        // ユーザー作成
        User user = BeanUtil.copyProperties(dto, User.class);
        // パスワードの設定（MD5暗号化）
        user.setPassword(SecureUtil.md5(dto.getPassword()));
        // ステータスを「有効」に設定
        user.setStatus(1);

        // ユーザー保存
        save(user);
    }

    @Override
    public void resetPassword(Long id) {
        // ユーザー確認
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }

        // パスワードを初期値「123456」にリセット
        user.setPassword(SecureUtil.md5("123456"));
        updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // ユーザー確認
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }

        // 管理者のステータスは変更不可
        if (user.getRole() == 1) {
            throw new BusinessException("管理者のステータスを変更することはできません");
        }

        // ステータス更新
        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        // ユーザー確認
        User user = getById(id);
        if (user == null) {
            return false;
        }

        // 管理者は削除不可
        if (user.getRole() == 1) {
            throw new BusinessException("管理者を削除することはできません");
        }

        // ユーザーのアバターファイルを削除
        if (StrUtil.isNotBlank(user.getAvatarBucket()) && StrUtil.isNotBlank(user.getAvatarObjectKey())) {
            try {
                fileClient.delete(user.getAvatarBucket(), user.getAvatarObjectKey());
            } catch (Exception e) {
                log.error("ユーザーアバターの削除に失敗しました", e);
            }
        }

        // ユーザーを削除
        return super.removeById(id);
    }
}