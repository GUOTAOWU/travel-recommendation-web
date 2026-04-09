import { request } from './request'
import type { CategoryAddDTO, CategoryQueryDTO, CategoryUpdateDTO, CategoryVO, ItemAddDTO, ItemQueryDTO, ItemUpdateDTO, ItemVO } from '@/types/item'
import type { PageVO } from '@/types/common'

/**
 * 観光スポット（アイテム）関連API
 */
export const itemApi = {
  /**
   * 観光スポットを追加します
   * @param data 観光スポット追加用DTO
   */
  add: (data: ItemAddDTO) => {
    return request.post<number>('/item', data)
  },

  /**
   * 観光スポット情報を更新します
   * @param data 観光スポット更新用DTO
   */
  update: (data: ItemUpdateDTO) => {
    return request.put('/item', data)
  },

  /**
   * 観光スポットを削除します
   * @param id 観光スポットID
   */
  delete: (id: number) => {
    return request.delete(`/item/${id}`)
  },

  /**
   * 観光スポットの詳細情報を取得します
   * @param id 観光スポットID
   */
  getById: (id: number) => {
    return request.get<ItemVO>(`/item/${id}`)
  },

  /**
   * 観光スポットをページング検索します
   * @param params 検索パラメータ（キーワード、ページ番号など）
   */
  page: (params: ItemQueryDTO) => {
    return request.get<PageVO<ItemVO>>('/item/page', { params })
  },

  /**
   * カテゴリIDに基づいて観光スポット一覧を取得します
   * @param categoryId カテゴリID
   */
  listByCategoryId: (categoryId: number) => {
    return request.get<ItemVO[]>(`/item/list/category/${categoryId}`)
  },

  /**
   * タグに基づいて観光スポット一覧を取得します
   * @param tag タグ名
   */
  listByTag: (tag: string) => {
    return request.get<ItemVO[]>(`/item/list/tag/${tag}`)
  }
}