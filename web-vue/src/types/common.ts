// ページング検索パラメータ
export interface PageDTO {
  current: number // 現在のページ番号
  size: number // 1ページあたりの表示件数
}

// ページング返却結果
export interface PageVO<T> {
  records: T[] // データリスト
  total: number // 総件数
  size: number // 1ページあたりの表示件数
  current: number // 現在のページ番号
  pages: number // 総ページ数
}

// 共通レスポンス結果
export interface Result<T> {
  code: number // ステータスコード
  msg: string // メッセージ
  data: T // データ
}