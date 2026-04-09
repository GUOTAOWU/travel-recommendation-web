// ユーザー情報
export interface UserInfo {
  id: number
  username: string
  realName: string
  phone: string | null
  email: string | null
  role: number
  status: number
  avatarBucket?: string
  avatarObjectKey?: string
  avatarUrl?: string
  createTime?: string
  updateTime?: string
}

// ログインレスポンス
export interface LoginResponse {
  userInfo: UserInfo
  token: string
}

// ユーザーロール（権限）
export enum UserRole {
  USER = 0,   // 一般ユーザー
  ADMIN = 1,  // 管理者
}

// ユーザーステータス
export enum UserStatus {
  DISABLED = 0, // 無効
  ENABLED = 1,  // 有効
}

// ユーザー一覧検索パラメータ
export interface UserListParams {
  page: number
  size: number
  username?: string
  realName?: string
  phone?: string
  email?: string
  role?: number
  status?: number
}

// ユーザー一覧レスポンス
export interface UserListResult {
  total: number
  list: UserInfo[]
}

// ユーザー追加パラメータ
export interface AddUserParams {
  username: string
  password: string
  realName: string
  phone: string
  email: string
  role: number
  status: number
}

// ユーザー更新パラメータ
export interface UpdateUserParams {
  id: number
  username?: string
  realName?: string
  phone?: string
  email?: string
  role?: number
  status?: number
  avatarBucket?: string
  avatarObjectKey?: string
}

// パスワード変更パラメータ
export interface UpdatePasswordParams {
  id: number
  oldPassword: string
  newPassword: string
}

// ログインパラメータ
export interface LoginParams {
  username: string
  password: string
}

// 新規登録パラメータ
export interface RegisterParams {
  username: string
  password: string
  realName: string
  phone: string
  email: string
}

// ユーザー検索パラメータ
export interface UserQueryParams {
  current: number
  size: number
  username?: string
  realName?: string
  phone?: string
  email?: string
  role?: number
  status?: number
}

// ユーザーフォームデータ
export interface UserForm {
  id?: number
  username?: string
  password?: string
  realName?: string
  phone?: string
  email?: string
  role?: number
  status?: number
  avatarBucket?: string
  avatarObjectKey?: string
}

// --- ユーザープロファイル（ユーザー画像）関連 ---

// ユーザープロファイル - カテゴリの好み
export interface UserPreferredCategory {
  id: number
  name: string
  count: number      // インタラクション回数
  types: string[]    // 行動タイプ（閲覧、いいね等）
  diversityScore: number // 多様性スコア
}

// ユーザープロファイル - タグの好み
export interface UserPreferredTag {
  tag: string
  count: number
}

// ユーザープロファイル - 似ているユーザー
export interface SimilarUser {
  userId: number
  username: string
  commonItems: number // 共通の関心スポット数
  avatarBucket?: string
  avatarObjectKey?: string
}

// ユーザープロファイル（全体データ）
export interface UserProfile {
  preferred_categories: UserPreferredCategory[] // お気に入りのカテゴリ
  preferred_tags: UserPreferredTag[]           // お気に入りのタグ
  activity_stats: Record<string, number>        // アクティビティ統計（行動タイプごとの回数）
  similar_users: SimilarUser[]                  // 似ているユーザーリスト
}