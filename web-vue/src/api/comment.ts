import { request } from './request'
import type { CommentAddDTO, CommentQueryDTO, CommentVO } from '@/types/comment'
import type { PageVO } from '@/types/common'

/**
 * コメント関連API
 */
export const commentApi = {
  /**
   * コメントを追加します
   * @param data コメントデータ（内容、対象ID、親コメントIDなど）
   * @returns 作成されたコメントのID
   */
  add: (data: CommentAddDTO) => {
    return request.post<number>('/comment', data)
  },

  /**
   * コメントを削除します
   * @param commentId コメントID
   * @returns 成功した場合はtrue
   */
  delete: (commentId: number) => {
    return request.delete<boolean>(`/comment/${commentId}`)
  },

  /**
   * コメントの詳細を取得します
   * @param commentId コメントID
   * @returns コメントの詳細情報（VO）
   */
  get: (commentId: number) => {
    return request.get<CommentVO>(`/comment/${commentId}`)
  },

  /**
   * コメントをページング検索します
   * @param params 検索パラメータ（ページ番号、サイズなど）
   * @returns ページングされたコメントリスト
   */
  page: (params: CommentQueryDTO) => {
    return request.get<PageVO<CommentVO>>('/comment/page', { params })
  },

  /**
   * 観光スポットのコメントツリーを取得します
   * （親コメントとその返信を階層構造で取得）
   * @param itemId 観光スポット（アイテム）ID
   * @returns 階層化されたコメントリスト
   */
  getTree: (itemId: number) => {
    return request.get<CommentVO[]>(`/comment/tree/${itemId}`)
  }
}