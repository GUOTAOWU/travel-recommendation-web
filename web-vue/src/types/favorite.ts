// お気に入り関連の型定義
import type { ItemVO } from './item'

// お気に入りエンティティ
export interface Favorite {
  id: number;
  userId: number;
  itemId: number;
  createTime: string;
}

// お気に入りDTO
export interface FavoriteDTO {
  itemId: number;
}

// お気に入り状態レスポンス
export interface FavoriteStatusResponse {
  isFavorite: boolean;
}

// お気に入りVO
export interface FavoriteVO {
  id: number;
  userId: number;
  itemId: number;
  item: ItemVO;
  createTime: string;
}