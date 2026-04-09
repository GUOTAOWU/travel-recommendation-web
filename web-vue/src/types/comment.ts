import type { UserInfo } from './user'

/**
 * コメントオブジェクト型（VO）
 */
export interface CommentVO {
  id: number
  userId: number
  userInfo: UserInfo
  itemId: number
  content: string
  parentId?: number            // 親コメントのID（スレッド用）
  replyToCommentId?: number    // 返信先のコメントID
  replyToUserId?: number       // 返信先のユーザーID
  replyToUserInfo?: UserInfo   // 返信先のユーザー情報
  replies?: CommentVO[]        // 返信リスト（ネストされたコメント）
  createTime: string
  updateTime: string
}

/**
 * コメント追加用DTO
 */
export interface CommentAddDTO {
  itemId: number
  content: string
  parentId?: number
  replyToCommentId?: number
  replyToUserId?: number
}

/**
 * コメント検索用DTO
 */
export interface CommentQueryDTO {
  itemId?: number
  userId?: number
  onlyParent?: boolean         // 親コメントのみを取得するかどうか
  pageNo?: number
  pageSize?: number
}