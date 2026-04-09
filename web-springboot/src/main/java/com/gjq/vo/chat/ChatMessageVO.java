package com.gjq.vo.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * チャットメッセージビューオブジェクト
 */
@Data
public class ChatMessageVO {
    
    /**
     * メッセージID
     */
    private Long id;
    
    /**
     * セッションID
     */
    private Long sessionId;
    
    /**
     * ロール（user/assistant）
     */
    private String role;
    
    /**
     * メッセージ内容
     */
    private String content;
    
    /**
     * 使用モデル名
     */
    private String model;
    
    /**
     * 拡張データ（JSON形式）。ファイル情報などの格納に使用
     */
    private String extraData;
    
    /**
     * メッセージ送信日時
     */
    private LocalDateTime messageTime;
}