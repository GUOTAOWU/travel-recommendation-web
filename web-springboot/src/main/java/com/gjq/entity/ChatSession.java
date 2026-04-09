package com.gjq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * チャットセッションエンティティクラス
 */
@Data
@TableName("chat_session")
public class ChatSession {
    
    /**
     * セッションID
     */
    @TableId(type = IdType.AUTO)
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
}