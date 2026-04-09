import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

// axiosインスタンスの作成
const service: AxiosInstance = axios.create({
  // 環境変数からアルゴリズムサーバーのURLを取得（例: http://localhost:5000/api）
  baseURL: import.meta.env.VITE_ALGO_URL,
  timeout: 60000 // 60秒でタイムアウト設定（重い計算処理を考慮）
})

// リクエストインターセプター（送信前の処理）
service.interceptors.request.use(
  (config) => {
    // localStorageからJWTトークンを取得
    const token = localStorage.getItem('token')
    if (token) {
      // リクエストヘッダーにBearerトークンを付与
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// レスポンスインターセプター（受信後の処理）
service.interceptors.response.use(
  (response) => {
    // サーバーからの共通レスポンス形式 { code, msg, data } を分解
    const { code, msg, data } = response.data

    // 成功（ステータスコード200）の場合
    if (code === 200) {
      return data
    }

    // 失敗（エラーメッセージを通知）
    ElMessage.error(msg || 'リクエストに失敗しました')
    return Promise.reject(new Error(msg || 'リクエストに失敗しました'))
  },
  (error) => {
    // ネットワークエラーやHTTPステータスエラーの処理
    ElMessage.error(error.message || 'リクエストに失敗しました')
    return Promise.reject(error)
  }
)

// リクエストメソッドのラップ（使いやすくカプセル化）
const algoRequest = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },
}

export { algoRequest }