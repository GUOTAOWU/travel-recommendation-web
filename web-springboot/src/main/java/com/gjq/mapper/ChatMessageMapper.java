package com.gjq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gjq.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * チャットメッセージMapperインターフェース
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * セッション内のすべてのメッセージを取得します
     * * @param sessionId セッションID
     * @return メッセージリスト
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} ORDER BY message_time ASC")
    List<ChatMessage> selectMessagesBySessionId(@Param("sessionId") Long sessionId);
}