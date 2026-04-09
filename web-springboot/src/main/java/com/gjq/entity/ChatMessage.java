package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * チャットメッセージエンティティクラス
 */
@Data
@TableName("chat_message")
public class ChatMessage {
    
    /**
     * メッセージID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * セッションID
     */
    private Long sessionId;
    
    /**
     * ロール（user / assistant）
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
     * 追加データ（JSON形式）。ファイル情報などの保存に使用
     */
    private String extraData;
    
    /**
     * メッセージ日時
     */
    private LocalDateTime messageTime;
}