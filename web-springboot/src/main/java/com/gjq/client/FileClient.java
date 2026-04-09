package com.gjq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.LinkedHashMap;

/**
 * ファイルオブジェクトストレージサービスクライアント
 */
@Component
public class FileClient {
    
    private static final Logger logger = LoggerFactory.getLogger(FileClient.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${file.server.url}")
    private String serverUrl;

    /**
     * 汎用レスポンス形式
     */
    public static class Response<T> {
        private int code;
        private String msg;
        private T data;
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
        
        public String getMsg() {
            return msg;
        }
        
        public void setMsg(String msg) {
            this.msg = msg;
        }
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
    }
    
    /**
     * ファイルアップロードレスポンスデータ
     */
    public static class UploadResult {
        private String url;
        private String bucket;
        private String objectKey;
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getBucket() {
            return bucket;
        }
        
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
        
        public String getObjectKey() {
            return objectKey;
        }
        
        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }
    }
    
    /**
     * ファイルをアップロードする
     *
     * @param bucket バケット名
     * @param file ファイル
     * @param isCache キャッシュするかどうか（デフォルト：false）
     * @return アップロード結果
     * @throws RuntimeException アップロード失敗時に例外をスロー
     */
    public UploadResult upload(String bucket, MultipartFile file, boolean isCache) {
        try {
            String url = serverUrl + "/upload/" + bucket;
            
            // リクエストヘッダーの設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // リクエストボディの設定
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("is_cache", isCache);
            
            // リクエストの送信
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Response<LinkedHashMap<String, Object>> response = (Response<LinkedHashMap<String, Object>>) restTemplate.postForObject(
                url,
                requestEntity,
                Response.class
            );
            
            // レスポンスの確認
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                LinkedHashMap<String, Object> data = response.getData();
                UploadResult result = new UploadResult();
                result.setUrl((String) data.get("url"));
                result.setBucket((String) data.get("bucket"));
                result.setObjectKey((String) data.get("objectKey"));
                return result;
            }
            throw new RuntimeException(response != null ? response.getMsg() : "アップロード失敗");
            
        } catch (Exception e) {
            logger.error("ファイルのアップロードに失敗しました", e);
            throw new RuntimeException("ファイルのアップロードに失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * ファイルをアップロードする
     *
     * @param bucket バケット名
     * @param fileContent ファイル内容
     * @param fileName ファイル名
     * @param isCache キャッシュするかどうか（デフォルト：false）
     * @return アップロード結果
     * @throws RuntimeException アップロード失敗時に例外をスロー
     */
    public UploadResult upload(String bucket, byte[] fileContent, String fileName, boolean isCache) {
        try {
            String url = serverUrl + "/upload/" + bucket;
            
            // リクエストヘッダーの設定
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            // リクエストボディの設定
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            body.add("is_cache", isCache);
            
            // リクエストの送信
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            @SuppressWarnings("unchecked")
            Response<LinkedHashMap<String, Object>> response = (Response<LinkedHashMap<String, Object>>) restTemplate.postForObject(
                url,
                requestEntity,
                Response.class
            );
            
            // レスポンスの確認
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                LinkedHashMap<String, Object> data = response.getData();
                UploadResult result = new UploadResult();
                result.setUrl((String) data.get("url"));
                result.setBucket((String) data.get("bucket"));
                result.setObjectKey((String) data.get("objectKey"));
                return result;
            }
            throw new RuntimeException(response != null ? response.getMsg() : "アップロード失敗");
            
        } catch (Exception e) {
            logger.error("ファイルのアップロードに失敗しました", e);
            throw new RuntimeException("ファイルのアップロードに失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * ファイルを削除する
     *
     * @param bucket バケット名
     * @param objectKey オブジェクトキー
     * @throws RuntimeException 削除失敗時に例外をスロー
     */
    public void delete(String bucket, String objectKey) {
        String url = serverUrl + "/" + bucket + "/" + objectKey;
        Response<?> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, null, Response.class).getBody();
        
        // レスポンスの確認
        if (response == null || response.getCode() != 200) {
            throw new RuntimeException(response != null ? response.getMsg() : "削除失敗");
        }
    }
    
    /**
     * ファイルを取得する
     *
     * @param bucket バケット名
     * @param objectKey オブジェクトキー
     * @return ファイルのバイト配列
     * @throws RuntimeException 取得失敗時に例外をスロー
     */
    public byte[] get(String bucket, String objectKey) {
        try {
            String url = serverUrl + "/" + bucket + "/" + objectKey;
            return restTemplate.getForObject(url, byte[].class);
        } catch (Exception e) {
            logger.error("ファイルの取得に失敗しました", e);
            throw new RuntimeException("ファイルの取得に失敗しました: " + e.getMessage());
        }
    }
    
    /**
     * ファイルのURLを取得する
     *
     * @param bucket バケット名
     * @param objectKey オブジェクトキー
     * @return ファイルのURL
     */
    public String getFileUrl(String bucket, String objectKey) {
        return String.format("%s/%s/%s", serverUrl, bucket, objectKey);
    }
}