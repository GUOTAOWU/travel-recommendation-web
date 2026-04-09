package com.gjq.service;

import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;

import java.util.List;

/**
 * チャットサービスインターフェース
 */
public interface ChatService {
    
    /**
     * チャットセッションを作成します
     * * @param dto チャットセッション情報
     * @return 作成されたセッション
     */
    ChatSessionVO createSession(ChatSessionDTO dto);
    
    /**
     * ユーザーのすべてのチャットセッションを取得します
     * * @return チャットセッションリスト
     */
    List<ChatSessionVO> getUserSessions();
    
    /**
     * チャットセッションの詳細を取得します
     * * @param sessionId セッションID
     * @return セッション詳細
     */
    ChatSessionVO getSessionById(Long sessionId);
    
    /**
     * チャットセッションを更新します
     * * @param sessionId セッションID
     * @param dto セッション情報
     * @return 更新後のセッション
     */
    ChatSessionVO updateSession(Long sessionId, ChatSessionDTO dto);
    
    /**
     * チャットセッションを削除します
     * * @param sessionId セッションID
     */
    void deleteSession(Long sessionId);
    
    /**
     * セッション内のすべてのメッセージを取得します
     * * @param sessionId セッションID
     * @return メッセージリスト
     */
    List<ChatMessageVO> getSessionMessages(Long sessionId);
    
    /**
     * メッセージを送信します
     * * @param dto メッセージ情報
     * @return 送信されたメッセージ
     */
    ChatMessageVO sendMessage(ChatMessageDTO dto);
    
    /**
     * メッセージの詳細を取得します
     * * @param messageId メッセージID
     * @return メッセージ詳細
     */
    ChatMessageVO getMessageById(Long messageId);
    
    /**
     * メッセージを削除します
     * * @param messageId メッセージID
     */
    void deleteMessage(Long messageId);
    
    /**
     * セッション内のすべてのチャット記録を消去します
     * * @param sessionId セッションID
     */
    void clearSessionMessages(Long sessionId);
    
    /**
     * メッセージ内容を更新します
     * * @param messageId メッセージID
     * @param content 更新内容
     */
    void updateMessageContent(Long messageId, String content);
}