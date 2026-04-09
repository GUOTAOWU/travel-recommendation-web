package com.gjq.vo.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * チャットセッションビューオブジェクト
 */
@Data
public class ChatSessionVO {
    
    /**
     * セッションID
     */
    private Long id;
    
    /**
     * ユーザーID
     */
    private Long userId;
    
    /**
     * セッション名
     */
    private String sessionName;
    
    /**
     * 作成日時
     */
    private LocalDateTime createTime;
    
    /**
     * 更新日時
     */
    private LocalDateTime updateTime;
    
    /**
     * 最新メッセージのプレビュー
     */
    private String latestMessage;
}