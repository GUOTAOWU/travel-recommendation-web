package com.gjq.utils;

import com.gjq.common.exception.BusinessException;
import com.gjq.entity.User;
import com.gjq.service.UserService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * セキュリティユーティリティクラス
 */
@Component
public class SecurityUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 現在ログイン中のユーザーを取得します
     */
    public static User getCurrentUser() {
        Long userId = getUserId();
        UserService userService = applicationContext.getBean(UserService.class);
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("ユーザーが存在しません");
        }
        return user;
    }

    /**
     * 現在ログイン中のユーザーIDを取得します
     */
    public static Long getUserId() {
        // RequestContextHolderから現在のリクエスト属性を取得し、"userId"属性を抽出します
        Long userId = (Long) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getAttribute("userId");
        if (userId == null) {
            throw new BusinessException(401, "ユーザーがログインしていません");
        }
        return userId;
    }

    /**
     * 現在のユーザーが管理者かどうかを判定します
     */
    public static boolean isAdmin() {
        User user = getCurrentUser();
        // ロールが1の場合を管理者とみなします
        return user != null && user.getRole() == 1;
    }
}