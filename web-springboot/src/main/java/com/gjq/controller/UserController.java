package com.gjq.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjq.common.Result;
import com.gjq.dto.user.*;
import com.gjq.vo.user.*;
import com.gjq.entity.User;
import com.gjq.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * ユーザーコントローラー
 */
@Tag(name = "ユーザー管理", description = "ユーザー管理関連インターフェース")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * ユーザーログイン
     */
    @Operation(summary = "ユーザーログイン", description = "ユーザーログインインターフェース")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * ユーザー登録
     */
    @Operation(summary = "ユーザー登録", description = "ユーザー登録インターフェース")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * パスワード変更
     */
    @Operation(summary = "パスワード変更", description = "ユーザーパスワード変更インターフェース")
    @PostMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid UserPasswordDTO dto) {
        userService.updatePassword(dto);
        return Result.success();
    }

    /**
     * ユーザー情報の更新
     */
    @Operation(summary = "ユーザー情報の更新", description = "ユーザー基本情報更新インターフェース")
    @PutMapping
    public Result<Void> updateUser(@RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    /**
     * ユーザー一覧のページング検索
     */
    @Operation(summary = "ユーザー一覧のページング検索", description = "ユーザー一覧ページング検索インターフェース")
    @GetMapping("/page")
    public Result<IPage<User>> getUserPage(UserQueryDTO dto) {
        return Result.success(userService.getUserPage(dto));
    }

    /**
     * 現在のログインユーザー情報を取得
     */
    @Operation(summary = "現在のユーザー情報を取得", description = "現在のログインユーザー情報取得インターフェース")
    @GetMapping("/current")
    public Result<UserLoginVO> getCurrentUser() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * ログアウト
     */
    @Operation(summary = "ログアウト", description = "ユーザーログアウトインターフェース")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    /**
     * ユーザー詳細を取得
     */
    @Operation(summary = "ユーザー詳細を取得", description = "指定されたユーザーの詳細情報を取得します")
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * ユーザーを新規追加
     */
    @Operation(summary = "ユーザーを新規追加", description = "ユーザー新規追加インターフェース")
    @PostMapping
    public Result<Void> addUser(@RequestBody @Valid UserAddDTO dto) {
        userService.addUser(dto);
        return Result.success();
    }

    /**
     * ユーザーを削除
     */
    @Operation(summary = "ユーザーを削除", description = "指定されたユーザーを削除します")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        if (!userService.removeById(id)) {
            return Result.error("削除に失敗しました");
        }
        return Result.success();
    }

    /**
     * パスワードのリセット
     */
    @Operation(summary = "パスワードのリセット", description = "指定されたユーザーのパスワードをリセットします")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    /**
     * ユーザーステータスの更新
     */
    @Operation(summary = "ユーザーステータスの更新", description = "指定されたユーザーのステータスを更新します")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody @Valid UserStatusDTO dto) {
        userService.updateStatus(id, dto.getStatus());
        return Result.success();
    }
}