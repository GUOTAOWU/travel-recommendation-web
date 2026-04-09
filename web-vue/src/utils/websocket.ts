export interface WebSocketMessage {
  type: 'fire_detected' | 'camera_status' | 'frame' | 'start_stream' | 'stop_stream' | 'check_camera' | 'stream_stopped'
  data: {
    wsId?: string      // WebSocket ID
    cameraId: number
    rtspUrl?: string   // RTSPアドレス
    frame?: string     // base64エンコードされた画像フレーム
    status?: number    // カメラのステータス
    has_fire?: boolean
    has_smoke?: boolean
    fire_confidence?: number
    smoke_confidence?: number
    fire_bbox?: number[]
    smoke_bbox?: number[]
    level?: number
    location?: string
    [key: string]: any
  }
}

// WebSocketサーバーのURL設定
const WS_URL = import.meta.env.VITE_ALGO_WS_URL || `ws://${window.location.hostname}:5000/ws`

class WebSocketClient {
  private ws: WebSocket | null = null
  private url: string
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5
  private reconnectTimeout = 3000
  private messageHandlers: ((message: WebSocketMessage) => void)[] = []
  private isActiveClose = false
  private reconnectTimer: number | null = null
  private messageQueue: any[] = []  // メッセージキュー
  private isConnecting = false      // 接続中フラグ

  constructor(url: string) {
    this.url = url
    console.log('WebSocket URL:', url)
  }

  // WebSocketに接続
  connect() {
    // すでに接続済みの場合は何もしない
    if (this.ws?.readyState === WebSocket.OPEN) {
      console.log('WebSocketはすでに接続されています')
      this.processMessageQueue()  // 待機中のキューを処理
      return
    }

    // 接続処理中の場合は待機
    if (this.ws?.readyState === WebSocket.CONNECTING || this.isConnecting) {
      console.log('WebSocket接続処理中です...')
      return
    }

    // 既存の古い接続があればクリーンアップ
    this.cleanup()

    // 状態をリセット
    this.isActiveClose = false
    this.isConnecting = true
    console.log('WebSocketに接続を試みています:', this.url)

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('WebSocket接続に成功しました')
        this.reconnectAttempts = 0
        this.clearReconnectTimer()
        this.isConnecting = false
        this.processMessageQueue()  // 接続成功後にキュー内のメッセージを送信
      }

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data)
          this.messageHandlers.forEach(handler => handler(message))
        } catch (error) {
          console.error('WebSocketメッセージの解析に失敗しました:', error)
        }
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket接続が閉じられました:', {
          code: event.code,
          reason: event.reason,
          wasClean: event.wasClean,
          isActiveClose: this.isActiveClose
        })

        this.isConnecting = false

        // 明示的なクローズでなく、正常終了でもない場合に再接続をスケジュール
        if (!this.isActiveClose && !event.wasClean) {
          this.scheduleReconnect()
        } else {
          console.log('WebSocketが正常に終了したため、再接続は行いません')
          this.cleanup()
        }
      }

      this.ws.onerror = (event) => {
        this.isConnecting = false
        // 開発環境のみ詳細なエラーを出力
        if (import.meta.env.DEV) {
          console.error('WebSocketエラー:', event)
        }
        console.error('WebSocket接続でエラーが発生しました')
      }
    } catch (error) {
      this.isConnecting = false
      console.error('WebSocket接続の作成に失敗しました:', error)
      this.scheduleReconnect()
    }
  }

  // メッセージキューの処理
  private processMessageQueue() {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()
      this.sendImmediate(message)
    }
  }

  // メッセージを即時送信
  private sendImmediate(message: any) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocketが接続されていないため、メッセージの送信に失敗しました')
      return
    }

    try {
      this.ws.send(JSON.stringify(message))
    } catch (error) {
      console.error('メッセージの送信に失敗しました:', error)
    }
  }

  // メッセージ送信（未接続時はキューに保存）
  send(message: any) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.log('WebSocket未接続のため、メッセージをキューに追加し、接続を試みます')
      this.messageQueue.push(message)
      this.connect()
      return
    }

    this.sendImmediate(message)
  }

  // リソースのクリーンアップ
  private cleanup() {
    if (this.ws) {
      // 全てのイベントリスナーを解除
      this.ws.onopen = null
      this.ws.onclose = null
      this.ws.onmessage = null
      this.ws.onerror = null

      // 接続が開いている場合は正常に閉じる
      if (this.ws.readyState === WebSocket.OPEN) {
        try {
          this.ws.close(1000, 'Normal closure')
        } catch (error) {
          console.error('WebSocket接続のクローズに失敗しました:', error)
        }
      }

      this.ws = null
    }

    this.clearReconnectTimer()
  }

  // 再接続タイマーのクリア
  private clearReconnectTimer() {
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  // 再接続のスケジュール
  private scheduleReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('WebSocketの再接続試行回数が上限に達しました')
      this.cleanup()
      return
    }

    this.reconnectAttempts++
    console.log(`WebSocket: ${this.reconnectAttempts}回目の再接続をスケジュールしています`)

    this.clearReconnectTimer()
    this.reconnectTimer = window.setTimeout(() => {
      console.log('再接続を実行中...')
      this.connect()
    }, this.reconnectTimeout)
  }

  // メッセージハンドラーの追加
  addMessageHandler(handler: (message: WebSocketMessage) => void) {
    this.messageHandlers.push(handler)
  }

  // メッセージハンドラーの削除
  removeMessageHandler(handler: (message: WebSocketMessage) => void) {
    const index = this.messageHandlers.indexOf(handler)
    if (index !== -1) {
      this.messageHandlers.splice(index, 1)
    }
  }

  // 接続を閉じる
  close() {
    console.log('WebSocket接続を能動的に閉じます')
    this.isActiveClose = true
    this.cleanup()
  }

  // 接続状態の確認
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }
}

// WebSocketクライアントのインスタンスを作成
const wsClient = new WebSocketClient(WS_URL)

export default wsClient