import { algoRequest } from './algo_request'
import type { ItemVO, RecommendedItemVO } from '@/types/item'
import type { ChatMessage } from '@/types/chat'

// 推薦システムAPI
export const recommendationApi = {
  /**
   * ユーザーに観光スポットを推薦する（協調フィルタリングベース）
   * @param userId ユーザーID
   * @param limit 推薦数
   */
  getForUser: (userId: number, limit: number = 10) => {
    return algoRequest.get<ItemVO[]>(`/recommendation/user/${userId}`, { 
      params: { limit } 
    })
  },

  /**
   * ユーザーに観光スポットを推薦する（コンテンツベース）
   * @param userId ユーザーID
   * @param limit 推薦数
   */
  getContentForUser: (userId: number, limit: number = 10) => {
    return algoRequest.get<ItemVO[]>(`/recommendation/user/${userId}/content`, { 
      params: { limit } 
    })
  },

  /**
   * チャット内容に基づいて観光スポットを推薦する
   * @param messages チャットのメッセージ履歴
   * @param userId ユーザーID（オプション）
   * @param model LLMモデル名（オプション）
   * @param limit 推薦数
   */
  getFromChat: (messages: ChatMessage[], userId?: number, model?: string, limit: number = 10) => {
    return algoRequest.post<{
      items: RecommendedItemVO[], 
      keywords: string[],
      message?: string,
      isHistoryItems?: boolean
    }>('/llm/recommend', { 
      messages,
      userId,
      model,
      limit
    })
  }
}