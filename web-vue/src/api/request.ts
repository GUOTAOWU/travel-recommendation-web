import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// axios インスタンスの作成
const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000000,
})

// リクエストインターセプター
instance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // localStorage からトークンを取得
    const userInfo = localStorage.getItem('userInfo')
    if (userInfo) {
      const { token } = JSON.parse(userInfo)
      if (token && config.headers) {
        // リクエストヘッダーにトークンを追加
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// レスポンスインターセプター
instance.interceptors.response.use(
  (response: AxiosResponse) => {

    // バイナリデータの場合は、そのまま返す
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const { code, msg, data } = response.data

    // リクエスト成功
    if (code === 200 || code === 0) {
      return data
    }

    // ログイン有効期限切れ
    if (code === 401) {
      const userStore = useUserStore()
      // バックエンドにリクエストを送信せず、ローカル状態のみをクリアする
      userStore.logout(false)
      return Promise.reject(new Error('ログインの有効期限が切れました。再度ログインしてください'))
    }

    // エラーメッセージを表示
    const error = new Error(msg || 'リクエストに失敗しました') as Error & { response?: any }
    error.response = response
    ElMessage.error(msg || 'リクエストに失敗しました')
    return Promise.reject(error)
  },
  (error) => {
    // ネットワークエラーの処理
    let message = 'ネットワークリクエストに失敗しました。ネットワーク接続を確認してください'
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = data.msg || 'リクエストパラメータエラー'
          break
        case 401:
          message = 'ログインの有効期限が切れました。再度ログインしてください'
          const userStore = useUserStore()
          // バックエンドにリクエストを送信せず、ローカル状態のみをクリアする
          userStore.logout(false)
          break
        case 403:
          message = 'このリソースにアクセスする権限がありません'
          break
        case 404:
          message = 'リクエストされたリソースは存在しません'
          break
        case 500:
          message = 'サーバー内部エラー'
          break
        default:
          message = data.msg || 'リクエストに失敗しました'
      }
    }
    if (!error.config?.silent) {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

// リクエストメソッドのラップ
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return instance.put(url, data, config)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return instance.delete(url, config)
  },
}

export { request }