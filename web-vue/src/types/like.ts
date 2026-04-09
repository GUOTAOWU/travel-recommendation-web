// いいね（点賛）関連の型定義

// いいねエンティティ
export interface Like {
  id: number;
  userId: number;
  itemId: number;
  createTime: string;
}

// いいねDTO（データ転送オブジェクト）
export interface LikeDTO {
  itemId: number;
}

// いいね状態レスポンス
export interface LikeStatusResponse {
  isLiked: boolean;
  count: number;
}

// いいね状態の一括レスポンス
export interface BatchLikeStatusResponse {
  [itemId: number]: {
    isLiked: boolean;
    count: number;
  };
}