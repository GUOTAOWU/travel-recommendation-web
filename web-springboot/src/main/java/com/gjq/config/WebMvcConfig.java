package com.gjq.config;

import com.gjq.interceptor.TokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 設定
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TokenInterceptor tokenInterceptor;

    public WebMvcConfig(TokenInterceptor tokenInterceptor) {
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")  // すべてのリクエストをインターセプトする
                .excludePathPatterns(    // インターセプトを除外するパスを指定
                        "/user/login",    // ログイン
                        "/user/register", // 新規登録
                        "/swagger-ui/**", // Swagger UI
                        "/v3/api-docs/**" // OpenAPI
                );
    }
}