import { request } from './request'
import { algoRequest } from './algo_request'
import type { ChatSession, ChatMessage, ChatSessionCreateDTO, ChatSessionUpdateDTO, ChatMessageSendDTO, ChatMessageQueryDTO, ChatModel } from '@/types/chat'
// 💡 Piniaのuser storeからユーザー情報を取得
import { useUserStore } from '@/stores/user' 

/**
 * バックエンドAPI - セッション・メッセージ管理（Java側）
 * データの永続化（保存・削除）を担当します。
 */
export const chatApi = {
  // ユーザーの全セッションを取得
  getSessions() {
    return request.get<ChatSession[]>('/chat/sessions')
  },

  // セッション詳細を取得
  getSession(id: number) {
    return request.get<ChatSession>(`/chat/sessions/${id}`)
  },

  // 新規セッション作成
  createSession(data: ChatSessionCreateDTO) {
    return request.post<ChatSession>('/chat/sessions', data)
  },

  // セッション情報の更新（名前変更など）
  updateSession(data: ChatSessionUpdateDTO) {
    return request.put<ChatSession>(`/chat/sessions/${data.id}`, data)
  },

  // セッションを削除
  deleteSession(id: number) {
    return request.delete(`/chat/sessions/${id}`)
  },

  // セッション内の全メッセージ履歴を取得
  getMessages(params: ChatMessageQueryDTO) {
    return request.get<ChatMessage[]>('/chat/messages', { params })
  },

  // メッセージを送信し、DBに保存
  sendMessage(data: ChatMessageSendDTO) {
    return request.post<ChatMessage>('/chat/messages', data)
  },
  
  // セッション内の全チャット履歴を消去
  clearSessionMessages(sessionId: number) {
    return request.delete(`/chat/sessions/${sessionId}/messages`)
  },
  
  // 特定のメッセージを1件削除
  deleteMessage(messageId: number) {
    return request.delete(`/chat/messages/${messageId}`)
  },
  
  // メッセージ内容を編集・更新
  updateMessageContent(messageId: number, content: string) {
    return request.put(`/chat/messages/${messageId}/content`, { content })
  }
}

/**
 * アルゴリズムサーバーAPI - 生成AI/LLM関連（Python側）
 * 実際の推論や知識検索（GraphRAG）を担当します。
 */
export const llmApi = {
  // 利用可能なAIモデルの一覧を取得
  getModels() {
    return algoRequest.get<ChatModel[]>('/llm/models')
  },

  // 通常のチャット（非ストリーミング）
  chat(model: string, messages: ChatMessage[]) {
    return algoRequest.post<string>('/llm/chat', {
      model,
      messages: messages.map(msg => ({
        role: msg.role,
        content: msg.content
      }))
    })
  },

  /**
   * GraphRAGによる拡張チャット
   * グラフデータベースの知識を活用し、ユーザーに最適化された回答を生成します。
   */
  chatWithGraphRAG(model: string, messages: ChatMessage[]) {
    // 💡 現在ログイン中のユーザーIDを動的に取得
    let currentUserId = undefined;
    try {
      // 方法1：Pinia Storeからエレガントに取得
      const userStore = useUserStore();
      if (userStore.userInfo && userStore.userInfo.id) {
        currentUserId = userStore.userInfo.id;
      } else {
        // 方法2：Storeが未準備の場合、localStorageから安全にパースして取得
        const userStr = localStorage.getItem('userInfo');
        if (userStr) {
          const userObj = JSON.parse(userStr);
          // ✅ 修正済み：.userInfo レイヤーを介してIDを取得
          currentUserId = userObj.userInfo?.id; 
        }
      }
    } catch (error) {
      console.warn("userIdの取得に失敗しました。GraphRAGは匿名状態で実行されます。", error);
    }

    return algoRequest.post<{
      response: string;
      searchInfo: {
        keywords: string[];
        found_items: number;
        search_status: string;
        message: string;
      }
    }>('/llm/chat-with-graph-rag', {
      model,
      messages: messages.map(msg => ({
        role: msg.role,
        content: msg.content
      })),
      userId: currentUserId // 🚀 取得した実ユーザーIDをAIサーバーへ渡す
    })
  }
}