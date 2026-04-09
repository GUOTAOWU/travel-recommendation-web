import { algoRequest } from './algo_request'

/**
 * ヘルスチェック応答の型定義
 */
export interface HealthResponse {
  status: string   // ステータス（例: "UP", "OK"）
  service: string  // サービス名
  version: string  // バージョン情報
}

/**
 * アルゴリズムサービスの稼働状況（ヘルスチェック）を確認します
 * * @returns {Promise<HealthResponse>} サーバーからの健康状態レスポンス
 */
export const checkAlgoHealth = () => {
  return algoRequest.get<HealthResponse>('/health/health_check')
}