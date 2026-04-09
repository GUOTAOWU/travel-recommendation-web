package com.gjq.controller;

import com.gjq.common.Result;
import com.gjq.dto.chat.ChatMessageDTO;
import com.gjq.dto.chat.ChatSessionDTO;
import com.gjq.service.ChatService;
import com.gjq.vo.chat.ChatMessageVO;
import com.gjq.vo.chat.ChatSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * チャットコントローラー
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "チャットAPI", description = "チャット関連インターフェース")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/sessions")
    @Operation(summary = "チャットセッションを作成")
    public Result<ChatSessionVO> createSession(@RequestBody ChatSessionDTO dto) {
        return Result.success(chatService.createSession(dto));
    }
    
    @GetMapping("/sessions")
    @Operation(summary = "ユーザーの全チャットセッションを取得")
    public Result<List<ChatSessionVO>> getUserSessions() {
        return Result.success(chatService.getUserSessions());
    }
    
    @GetMapping("/sessions/{id}")
    @Operation(summary = "チャットセッション詳細を取得")
    public Result<ChatSessionVO> getSessionById(@PathVariable("id") Long sessionId) {
        return Result.success(chatService.getSessionById(sessionId));
    }
    
    @PutMapping("/sessions/{id}")
    @Operation(summary = "チャットセッションを更新")
    public Result<ChatSessionVO> updateSession(@PathVariable("id") Long sessionId, 
                                                @RequestBody ChatSessionDTO dto) {
        return Result.success(chatService.updateSession(sessionId, dto));
    }
    
    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "チャットセッションを削除")
    public Result<Void> deleteSession(@PathVariable("id") Long sessionId) {
        chatService.deleteSession(sessionId);
        return Result.success();
    }
    
    @GetMapping("/messages")
    @Operation(summary = "セッション内の全メッセージを取得")
    public Result<List<ChatMessageVO>> getSessionMessages(@RequestParam("sessionId") Long sessionId) {
        return Result.success(chatService.getSessionMessages(sessionId));
    }
    
    @PostMapping("/messages")
    @Operation(summary = "メッセージを送信")
    public Result<ChatMessageVO> sendMessage(@RequestBody ChatMessageDTO dto) {
        return Result.success(chatService.sendMessage(dto));
    }
    
    @GetMapping("/messages/{id}")
    @Operation(summary = "メッセージ詳細を取得")
    public Result<ChatMessageVO> getMessageById(@PathVariable("id") Long messageId) {
        return Result.success(chatService.getMessageById(messageId));
    }
    
    @DeleteMapping("/messages/{id}")
    @Operation(summary = "メッセージを削除")
    public Result<Void> deleteMessage(@PathVariable("id") Long messageId) {
        chatService.deleteMessage(messageId);
        return Result.success();
    }
    
    @DeleteMapping("/sessions/{id}/messages")
    @Operation(summary = "セッションの全チャット履歴をクリア")
    public Result<Void> clearSessionMessages(@PathVariable("id") Long sessionId) {
        chatService.clearSessionMessages(sessionId);
        return Result.success();
    }
    
    @PutMapping("/messages/{id}/content")
    @Operation(summary = "メッセージ内容を更新")
    public Result<Void> updateMessageContent(@PathVariable("id") Long messageId, @RequestBody Map<String, String> params) {
        String content = params.get("content");
        chatService.updateMessageContent(messageId, content);
        return Result.success();
    }
}