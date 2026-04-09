package com.gjq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS（クロスオリジンリソース共有）設定
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // クロスオリジンを許可するオリジン（ドメイン）の設定
        config.addAllowedOriginPattern("*");
        
        // 許可するリクエストヘッダーの設定
        config.addAllowedHeader("*");
        
        // 許可するリクエストメソッド（GET, POSTなど）の設定
        config.addAllowedMethod("*");
        
        // 資格情報（Cookieなど）の送信を許可する
        config.setAllowCredentials(true);
        
        // クライアント側に公開するレスポンスヘッダーの設定
        config.addExposedHeader("*");
        
        // すべてのパス（エンドポイント）に対してCORS設定を登録
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}