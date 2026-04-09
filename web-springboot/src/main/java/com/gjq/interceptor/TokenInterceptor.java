package com.gjq.interceptor;

import com.gjq.common.exception.BusinessException;
import com.gjq.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Value;

/**
 * トークンインターセプター
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);
    
    @Value("${system.internal-token}")
    private String internalToken;  // 内部サービス間通信用トークン
    
    @Value("${system.admin-user-id}")
    private Long systemUserId;  // システムユーザーID（管理者用）

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String remoteAddr = request.getRemoteAddr();
        String requestURI = request.getRequestURI();
        String internalAuth = request.getHeader("X-Internal-Auth");
        
        logger.debug("リクエスト送信元IP: {}, URI: {}, Internal-Auth: {}", remoteAddr, requestURI, internalAuth);
        
        // 内部サービス（アルゴリズムサービス等）からのリクエストの場合、内部認証トークンを検証
        if (internalAuth != null && internalToken.equals(internalAuth)) {
            // システムユーザーIDを設定
            request.setAttribute("userId", systemUserId);
            return true;
        }

        // トークンの取得
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未ログインです");
        }

        // Bearerトークンの処理
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            // トークンの検証
            if (!JwtUtils.validateToken(token)) {
                throw new BusinessException(401, "トークンが無効です");
            }
            // ユーザーIDを取得し、requestに格納
            Long userId = JwtUtils.getUserId(token);
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            throw new BusinessException(401, "トークンが無効です");
        }
    }
}