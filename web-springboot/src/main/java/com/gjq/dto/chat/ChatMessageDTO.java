package com.gjq.dto.chat;

import lombok.Data;

/**
 * チャットメッセージデータ転送オブジェクト (DTO)
 */
@Data
public class ChatMessageDTO {
    
    /**
     * セッションID
     */
    private Long sessionId;
    
    /**
     * メッセージ内容
     */
    private String content;
    
    /**
     * 使用するモデル名
     */
    private String model;
    
    /**
     * 追加データ。JSON形式。ファイル情報などの保存に使用
     */
    private String extraData;
}