import { request } from '@/api/request'

/**
 * ユーザー行動データインターフェース
 */
export interface UserActionData {
  itemId: number;
  actionType: number; // 0:閲覧 1:予約
  extraData?: string;
}

/**
 * ユーザー行動検索パラメータインターフェース
 */
export interface UserActionQueryParams {
  current: number;
  size: number;
  userId?: number;
  itemId?: number;
  username?: string;
  itemTitle?: string;
  actionType?: number;
}

/**
 * ユーザー行動レスポンスデータインターフェース
 */
export interface UserActionResponse {
  records: any[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

/**
 * ユーザー行動記録を追加（閲覧/予約）
 * @param data ユーザー行動データ
 * @returns Promise
 */
export function addUserAction(data: UserActionData): Promise<boolean> {
  return request.post('/user-action', data)
}

/**
 * 現在のユーザーの行動記録をページング検索
 * @param params 検索パラメータ
 * @returns Promise
 */
export function pageMyActions(params: UserActionQueryParams): Promise<UserActionResponse> {
  return request.get('/user-action/page', { params })
}

/**
 * すべてのユーザーの行動記録をページング検索（管理者インターフェース）
 * @param params 検索パラメータ
 * @returns Promise
 */
export function pageAllActions(params: UserActionQueryParams): Promise<UserActionResponse> {
  return request.get('/user-action/admin/page', { params })
}

/**
 * ユーザー行動記録を一括削除（管理者インターフェース）
 * @param ids 記録IDリスト
 * @returns Promise
 */
export function batchDeleteActions(ids: number[]): Promise<boolean> {
  return request.delete('/user-action/batch', { data: ids })
}

/**
 * 現在のユーザーの行動記録を一括削除
 * @param ids 記録IDリスト
 * @returns Promise
 */
export function batchDeleteMyActions(ids: number[]): Promise<boolean> {
  return request.delete('/user-action/my/batch', { data: ids })
}

/**
 * 観光スポットの閲覧数を取得
 * @param itemId 観光スポットID
 * @returns Promise
 */
export function getItemViewCount(itemId: number): Promise<number> {
  return request.get(`/user-action/view/count/${itemId}`)
}

/**
 * 観光スポットの予約数を取得
 * @param itemId 観光スポットID
 * @returns Promise
 */
export function getItemReservationCount(itemId: number): Promise<number> {
  return request.get(`/user-action/reservation/count/${itemId}`)
}