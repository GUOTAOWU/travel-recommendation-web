package com.gjq.vo.user;

import com.gjq.client.FileClient;
import com.gjq.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * ユーザー情報
 */
@Data
@NoArgsConstructor
public class UserInfo {
    /**
     * ユーザーID
     */
    private Long id;

    /**
     * ユーザー名
     */
    private String username;

    /**
     * 本名
     */
    private String realName;

    /**
     * 電話番号
     */
    private String phone;

    /**
     * メールアドレス
     */
    private String email;

    /**
     * アバターのストレージバケット 
     */
    private String avatarBucket;

    /**
     * アバターのオブジェクトキー
     */
    private String avatarObjectKey;

    /**
     * アバターのURL
     */
    private String avatarUrl;

    /**
     * ロール（0:一般ユーザー 1:管理者）
     */
    private Integer role;

    /**
     * ステータス（0:無効 1:有効）
     */
    private Integer status;

    /**
     * UserエンティティからUserInfoを構築するコンストラクタ
     * * @param user エンティティ
     * @param fileClient ファイルサービスクライアント（URL生成用）
     */
    public UserInfo(User user, FileClient fileClient) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.phone = user.getPhone();
        this.email = user.getEmail();
        this.avatarBucket = user.getAvatarBucket();
        this.avatarObjectKey = user.getAvatarObjectKey();
        
        // バケット名とオブジェクトキーが存在する場合のみ、FileClientを使用してアクセス可能なURLを生成
        if (StringUtils.hasText(user.getAvatarBucket()) && StringUtils.hasText(user.getAvatarObjectKey())) {
            this.avatarUrl = fileClient.getFileUrl(user.getAvatarBucket(), user.getAvatarObjectKey());
        }
        
        this.role = user.getRole();
        this.status = user.getStatus();
    }
}