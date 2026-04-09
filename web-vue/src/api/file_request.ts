import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// axios インスタンスの作成
const service: AxiosInstance = axios.create({
  // 環境変数からファイルサーバーのURLを取得（例: http://localhost:5001/api/file）
  baseURL: import.meta.env.VITE_FILE_URL,
  timeout: 60000, // 60秒でタイムアウト設定
})

// リクエストインターセプター
service.interceptors.request.use(
  (config) => {
    // localStorageからJWTトークンを取得してヘッダーにセット
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// レスポンスインターセプター
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // レスポンスがバイナリデータ（Blob）の場合は、そのままデータを返す
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const { code, msg, data } = response.data

    if (code === 200) {
      return data
    }

    ElMessage.error(msg || 'リクエストに失敗しました')
    return Promise.reject(new Error(msg || 'リクエストに失敗しました'))
  },
  (error) => {
    // ファイルダウンロード失敗時の特殊なハンドリング
    // responseTypeが'blob'の場合、エラー内容もBlobとして返ってくるため、FileReaderでテキスト化して読み取る
    if (error.config?.responseType === 'blob' && error.response?.data) {
      return new Promise((_, reject) => {
        const reader = new FileReader()
        reader.onload = () => {
          try {
            // Blobの中身をJSONとして解析し、サーバーからのエラーメッセージを表示
            const errorData = JSON.parse(reader.result as string)
            ElMessage.error(errorData.msg || 'ダウンロードに失敗しました')
            reject(new Error(errorData.msg || 'ダウンロードに失敗しました'))
          } catch (e) {
            ElMessage.error('ダウンロードに失敗しました')
            reject(new Error('ダウンロードに失敗しました'))
          }
        }
        reader.onerror = () => {
          ElMessage.error('ダウンロードに失敗しました')
          reject(new Error('ダウンロードに失敗しました'))
        }
        reader.readAsText(error.response.data)
      })
    }
    
    ElMessage.error(error.message || 'リクエストに失敗しました')
    return Promise.reject(error)
  }
)

/**
 * ファイル操作用リクエストメソッド群
 */
const fileRequest = {
  /**
   * ファイルをアップロードします
   * @param bucket ストレージバケット名
   * @param file ファイルオブジェクト
   * @param isCache キャッシュファイルとして扱うかどうか
   * @param config 追加のAxios設定
   * @returns { url: string, bucket: string, objectKey: string } アップロード成功後の情報
   */
  upload<T = { url: string, bucket: string, objectKey: string }>(bucket: string, file: File, isCache?: boolean, config?: AxiosRequestConfig): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)
    if (isCache) {
      formData.append('is_cache', 'true')
    }
    return service.post(`/file/upload/${bucket}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data', // マルチパート形式で送信
      },
      ...config,
    })
  },

  /**
   * ファイルを取得（ダウンロード）します
   * @param bucket ストレージバケット名
   * @param objectKey ファイル名（オブジェクトキー）
   * @param config 追加のAxios設定
   * @returns ファイル内容（Blob）
   */
  get(bucket: string, objectKey: string, config?: AxiosRequestConfig): Promise<Blob> {
    return service.get(`/file/${bucket}/${objectKey}`, {
      responseType: 'blob', // バイナリデータとして受け取る
      ...config,
    })
  },

  /**
   * ファイルを削除します
   * @param bucket ストレージバケット名
   * @param objectKey ファイル名（オブジェクトキー）
   * @param config 追加のAxios設定
   */
  delete<T = any>(bucket: string, objectKey: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(`/file/${bucket}/${objectKey}`, config)
  },

  /**
   * ファイルのアクセスURLを取得します（フロントエンドでの表示用）
   * @param bucket ストレージバケット名
   * @param objectKey ファイル名（オブジェクトキー）
   * @returns ファイルのフルURL
   */
  getFileUrl(bucket: string, objectKey: string): string {
    return `${import.meta.env.VITE_FILE_URL}/file/${bucket}/${objectKey}`
  }
}

export { fileRequest }