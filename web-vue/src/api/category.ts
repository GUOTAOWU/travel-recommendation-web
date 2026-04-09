import { request } from './request'
import { fileRequest } from './file_request'
import type { CategoryAddDTO, CategoryQueryDTO, CategoryUpdateDTO, CategoryVO, ItemAddDTO, ItemQueryDTO, ItemUpdateDTO, ItemVO } from '@/types/item'
import type { PageVO } from '@/types/common'

/**
 * カテゴリ関連API
 */
export const categoryApi = {
  /**
   * カテゴリを追加します
   * @param data カテゴリ追加DTO
   */
  add: (data: CategoryAddDTO) => {
    return request.post<number>('/category', data)
  },

  /**
   * カテゴリを更新します
   * @param data カテゴリ更新DTO
   */
  update: (data: CategoryUpdateDTO) => {
    return request.put('/category', data)
  },

  /**
   * カテゴリを削除します
   * @param id カテゴリID
   */
  delete: (id: number) => {
    return request.delete(`/category/${id}`)
  },

  /**
   * カテゴリの詳細を取得します
   * @param id カテゴリID
   */
  getById: (id: number) => {
    return request.get<CategoryVO>(`/category/${id}`)
  },

  /**
   * すべてのカテゴリを取得します（セレクトボックス等で使用）
   */
  list: () => {
    return request.get<CategoryVO[]>('/category/list')
  },

  /**
   * カテゴリをページング検索します
   * @param params 検索パラメータ（ページ番号、サイズ、名称など）
   */
  page: (params: CategoryQueryDTO) => {
    return request.get<PageVO<CategoryVO>>('/category/page', { params })
  }
}