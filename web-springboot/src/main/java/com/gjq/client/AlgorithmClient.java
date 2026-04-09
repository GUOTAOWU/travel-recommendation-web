package com.gjq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * アルゴリズムサービス・クライアント
 */
@Component
public class AlgorithmClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    

    @Value("${algorithm.server.url}")
    private String serverUrl;

    /**
     * 汎用レスポンス形式
     */
    private static class Response<T> {
        private int code;
        private String msg;
        private T data;
        
        public int getCode() {
            return code;
        }
        
        public String getMsg() {
            return msg;
        }
        
        public T getData() {
            return data;
        }
    }

    /**
     * レスポンス結果の処理
     *
     * @param response レスポンスオブジェクト
     * @param <T> データ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    private <T> T handleResponse(Response<T> response) {
        if (response != null && response.getCode() == 200) {
            return response.getData();
        }
        throw new RuntimeException(response != null ? response.getMsg() : "リクエスト失敗");
    }

    /**
     * GETリクエストを送信
     *
     * @param path リクエストパス
     * @param params クエリパラメータ
     * @param <T> 期待されるデータ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    public <T> T get(String path, Map<String, Object> params) {
        try {
            String url = serverUrl + path;
            ResponseEntity<Response<T>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Response<T>>() {},
                params
            );
            return handleResponse(response.getBody());
        } catch (Exception e) {
            logger.error("GETリクエスト失敗", e);
            throw new RuntimeException("GETリクエスト失敗: " + e.getMessage());
        }
    }

    /**
     * POSTリクエストを送信
     *
     * @param path リクエストパス
     * @param body リクエストボディ
     * @param <T> 期待されるデータ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    public <T> T post(String path, Object body) {
        try {
            String url = serverUrl + path;
            ResponseEntity<Response<T>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body),
                new ParameterizedTypeReference<Response<T>>() {}
            );
            return handleResponse(response.getBody());
        } catch (Exception e) {
            logger.error("POSTリクエスト失敗", e);
            throw new RuntimeException("POSTリクエスト失敗: " + e.getMessage());
        }
    }

    /**
     * ファイル付きのPOSTリクエストを送信
     *
     * @param path リクエストパス
     * @param file ファイル
     * @param params その他のパラメータ
     * @param <T> 期待されるデータ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    public <T> T postWithFile(String path, MultipartFile file, Map<String, Object> params) {
        try {
            String url = serverUrl + path;
            
            // フォームデータの構築
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            if (file != null) {
                body.add("file", file.getResource());
            }
            if (params != null) {
                params.forEach((key, value) -> body.add(key, value));
            }

            ResponseEntity<Response<T>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body),
                new ParameterizedTypeReference<Response<T>>() {}
            );
            return handleResponse(response.getBody());
        } catch (Exception e) {
            logger.error("POSTファイルリクエスト失敗", e);
            throw new RuntimeException("POSTファイルリクエスト失敗: " + e.getMessage());
        }
    }

    /**
     * PUTリクエストを送信
     *
     * @param path リクエストパス
     * @param body リクエストボディ
     * @param <T> 期待されるデータ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    public <T> T put(String path, Object body) {
        try {
            String url = serverUrl + path;
            ResponseEntity<Response<T>> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(body),
                new ParameterizedTypeReference<Response<T>>() {}
            );
            return handleResponse(response.getBody());
        } catch (Exception e) {
            logger.error("PUTリクエスト失敗", e);
            throw new RuntimeException("PUTリクエスト失敗: " + e.getMessage());
        }
    }

    /**
     * DELETEリクエストを送信
     *
     * @param path リクエストパス
     * @param body リクエストボディ
     * @param <T> 期待されるデータ型
     * @return レスポンスデータ
     * @throws RuntimeException リクエスト失敗時に例外をスロー
     */
    public <T> T delete(String path, Object body) {
        try {
            String url = serverUrl + path;
            ResponseEntity<Response<T>> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(body),
                new ParameterizedTypeReference<Response<T>>() {}
            );
            return handleResponse(response.getBody());
        } catch (Exception e) {
            logger.error("DELETEリクエスト失敗", e);
            throw new RuntimeException("DELETEリクエスト失敗: " + e.getMessage());
        }
    }
    
}