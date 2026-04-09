import { request } from './request'
import type { LikeDTO, LikeStatusResponse, BatchLikeStatusResponse } from '@/types/like'

// いいね関連API
export const likeApi = {
  /**
   * いいね
   * @param data いいねDTO
   */
  like: (data: LikeDTO) => {
    return request.post('/like', data)
  },

  /**
   * いいね取り消し
   * @param itemId 観光スポットID
   */
  unlike: (itemId: number) => {
    return request.delete(`/like/${itemId}`)
  },

  /**
   * いいね状態の照会
   * @param itemId 観光スポットID
   */
  status: (itemId: number) => {
    return request.get<boolean>(`/like/status/${itemId}`)
  },

  /**
   * 観光スポットのいいね数を照会
   * @param itemId 観光スポットID
   */
  count: (itemId: number) => {
    return request.get<number>(`/like/count/${itemId}`)
  },

  /**
   * 観光スポットのいいね状態といいね数を一括照会
   * @param itemIds 観光スポットIDリスト
   */
  batchStatus: (itemIds: number[]) => {
    return request.get<BatchLikeStatusResponse>('/like/batch', {
      params: { itemIds }
    })
  },

  /**
   * ユーザーがいいねした観光スポットIDリストを取得
   */
  userLikedItems: () => {
    return request.get<number[]>('/like/user/items')
  }
}