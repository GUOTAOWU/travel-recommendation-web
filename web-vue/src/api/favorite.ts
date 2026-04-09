import { request } from './request'
import type { FavoriteDTO, FavoriteVO } from '@/types/favorite'
import type { PageVO } from '@/types/common'

/**
 * お気に入り関連API
 */
export const favoriteApi = {
  /**
   * お気に入りに追加します
   * @param data お気に入りデータ（アイテムIDなど）
   */
  add: (data: FavoriteDTO) => {
    return request.post('/favorite', data)
  },

  /**
   * お気に入りを取り消します
   * @param itemId 観光スポット（アイテム）ID
   */
  remove: (itemId: number) => {
    return request.delete(`/favorite/${itemId}`)
  },

  /**
   * お気に入り状態を確認します
   * @param itemId 観光スポット（アイテム）ID
   * @returns お気に入り登録済みの場合はtrue
   */
  status: (itemId: number) => {
    return request.get<boolean>(`/favorite/status/${itemId}`)
  },

  /**
   * ユーザーがお気に入り登録した全アイテムIDのリストを取得します
   */
  getUserFavoriteItemIds: () => {
    return request.get<number[]>('/favorite/user/items')
  },

  /**
   * ユーザーのお気に入り一覧をページング形式で取得します
   * @param current 現在のページ番号
   * @param size 1ページあたりの表示件数
   */
  page: (current: number = 1, size: number = 10) => {
    return request.get<PageVO<FavoriteVO>>('/favorite/user/page', {
      params: { current, size }
    })
  },

  /**
   * アイテムごとの総お気に入り数を取得します
   * @param itemId 観光スポット（アイテム）ID
   */
  getItemFavoriteCount: (itemId: number) => {
    return request.get<number>(`/favorite/count/${itemId}`)
  }
}