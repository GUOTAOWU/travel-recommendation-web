package com.gjq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gjq.common.exception.BusinessException;
import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.entity.ChatMessage;
import com.gjq.entity.ChatSession;
import com.gjq.mapper.ChatMessageMapper;
import com.gjq.mapper.ChatSessionMapper;
import com.gjq.service.ChatService;
import com.gjq.utils.SecurityUtils;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * チャットサービス実装クラス
 */
@Service
public class ChatServiceImpl implements ChatService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    /**
     * チャットセッションを作成する
     */
    @Override
    @Transactional
    public ChatSessionVO createSession(ChatSessionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionName(dto.getSessionName());
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        
        chatSessionMapper.insert(session);
        
        return convertToSessionVO(session);
    }
    
    /**
     * ユーザーのすべてのチャットセッションを取得する
     */
    @Override
    public List<ChatSessionVO> getUserSessions() {
        Long userId = SecurityUtils.getUserId();
        
        List<ChatSession> sessions = chatSessionMapper.selectSessionsWithLatestMessage(userId);
        
        // セッションIDをキーとしてMapを使用し、重複を排除
        Map<Long, ChatSession> sessionMap = new HashMap<>();
        for (ChatSession session : sessions) {
            sessionMap.put(session.getId(), session);
        }
        
        // ソートされたリストに変換
        return sessionMap.values().stream()
                .map(this::convertToSessionVO)
                .collect(Collectors.toList());
    }
    
    /**
     * チャットセッションの詳細を取得する
     */
    @Override
    public ChatSessionVO getSessionById(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        // 権限チェック
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションへのアクセス権限がありません");
        }
        
        return convertToSessionVO(session);
    }
    
    /**
     * チャットセッションを更新する
     */
    @Override
    @Transactional
    public ChatSessionVO updateSession(Long sessionId, ChatSessionDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        // 権限チェック
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションを修正する権限がありません");
        }
        
        session.setSessionName(dto.getSessionName());
        session.setUpdateTime(LocalDateTime.now());
        
        chatSessionMapper.updateById(session);
        
        return convertToSessionVO(session);
    }
    
    /**
     * チャットセッションを削除する
     */
    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        // 権限チェック
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションを削除する権限がありません");
        }
        
        // セッションおよび関連するすべてのメッセージを削除
        chatSessionMapper.deleteById(sessionId);
        
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(wrapper);
    }
    
    /**
     * セッション内のすべてのメッセージを取得する
     */
    @Override
    public List<ChatMessageVO> getSessionMessages(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        // セッションの存在確認およびアクセス権限の検証
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションへのアクセス権限がありません");
        }
        
        List<ChatMessage> messages = chatMessageMapper.selectMessagesBySessionId(sessionId);
        
        return messages.stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());
    }
    
    /**
     * メッセージを送信する
     */
    @Override
    @Transactional
    public ChatMessageVO sendMessage(ChatMessageDTO dto) {
        Long userId = SecurityUtils.getUserId();
        
        // セッションの存在確認およびアクセス権限の検証
        ChatSession session = chatSessionMapper.selectById(dto.getSessionId());
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションへのアクセス権限がありません");
        }
        
        // ユーザーメッセージの作成
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setRole("user");
        userMessage.setContent(dto.getContent());
        userMessage.setModel(dto.getModel());
        userMessage.setExtraData(dto.getExtraData());
        userMessage.setMessageTime(LocalDateTime.now());
        
        chatMessageMapper.insert(userMessage);
        
        // セッションの最終更新日時を更新
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
        
        // AIアシスタントの返信メッセージを作成（実際の返信内容はフロントエンドがアルゴリズムサービスを呼び出して取得）
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSessionId(dto.getSessionId());
        aiMessage.setRole("assistant");
        aiMessage.setContent(""); // 内容はフロントエンドで充填
        aiMessage.setModel(dto.getModel());
        aiMessage.setMessageTime(LocalDateTime.now());
        
        chatMessageMapper.insert(aiMessage);
        
        // ユーザーメッセージではなくAIメッセージを返却することで、フロントエンドが直接AIメッセージIDを取得できるようにする
        return convertToMessageVO(aiMessage);
    }
    
    /**
     * メッセージの詳細を取得する
     */
    @Override
    public ChatMessageVO getMessageById(Long messageId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("メッセージが存在しません");
        }
        
        // セッションの存在確認およびアクセス権限の検証
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このメッセージへのアクセス権限がありません");
        }
        
        return convertToMessageVO(message);
    }
    
    /**
     * メッセージを削除する
     */
    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        Long userId = SecurityUtils.getUserId();
        
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("メッセージが存在しません");
        }
        
        // セッションの存在確認およびアクセス権限の検証
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このメッセージを削除する権限がありません");
        }
        
        // メッセージを削除
        chatMessageMapper.deleteById(messageId);
    }
    
    /**
     * セッション内のすべてのチャット記録を消去する
     */
    @Override
    @Transactional
    public void clearSessionMessages(Long sessionId) {
        Long userId = SecurityUtils.getUserId();
        
        // セッションの存在確認およびアクセス権限の検証
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このセッションへのアクセス権限がありません");
        }
        
        // セッション内のすべてのメッセージを削除
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        chatMessageMapper.delete(wrapper);
    }
    
    /**
     * メッセージ内容を更新する
     */
    @Override
    public void updateMessageContent(Long messageId, String content) {
        Long userId = SecurityUtils.getUserId();
        
        // メッセージの取得
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("メッセージが存在しません");
        }
        
        // メッセージが属するセッションを取得
        ChatSession session = chatSessionMapper.selectById(message.getSessionId());
        if (session == null) {
            throw new BusinessException("セッションが存在しません");
        }
        
        // ユーザー権限の検証
        if (!userId.equals(session.getUserId()) && !SecurityUtils.isAdmin()) {
            throw new BusinessException("このメッセージを修正する権限がありません");
        }
        
        // メッセージ内容の更新
        message.setContent(content);
        chatMessageMapper.updateById(message);
    }
    
    /**
     * エンティティオブジェクトをVOに変換
     */
    private ChatSessionVO convertToSessionVO(ChatSession session) {
        if (session == null) {
            return null;
        }
        
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);
        
        return vo;
    }
    
    /**
     * エンティティオブジェクトをVOに変換
     */
    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        
        return vo;
    }
}