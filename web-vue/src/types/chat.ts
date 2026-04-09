// チャットモデルの定義
export interface ChatModel {
  key: string;
  name: string;
}

// チャットロール（役割）の定義
export type ChatRole = 'user' | 'assistant';

// チャットメッセージの定義
export interface ChatMessage {
  id?: number;
  sessionId?: number;
  role: ChatRole;
  content: string;
  model?: string;
  extraData?: string; // JSON 形式の追加データ
  messageTime?: string;
}

// チャットセッションの定義
export interface ChatSession {
  id: number;
  userId: number;
  sessionName: string;
  createTime: string;
  updateTime: string;
  latestMessage?: string; // 最新メッセージのプレビュー
}

// セッション作成リクエストのパラメータ
export interface ChatSessionCreateDTO {
  sessionName: string;
}

// セッション更新リクエストのパラメータ
export interface ChatSessionUpdateDTO {
  id: number;
  sessionName: string;
}

// メッセージ送信リクエストのパラメータ
export interface ChatMessageSendDTO {
  sessionId: number;
  content: string;
  model: string;
  extraData?: string;
}

// メッセージ照会リクエストのパラメータ
export interface ChatMessageQueryDTO {
  sessionId: number;
}