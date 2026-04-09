// 観光スポット関連の型定義

// カテゴリエンティティ
export interface Category {
  id: number;
  name: string;
  iconUrl: string;
  iconObjectKey: string;
  iconBucket: string;
  description: string;
  createTime: string;
  updateTime: string;
}

// カテゴリ追加用DTO
export interface CategoryAddDTO {
  name: string;
  iconObjectKey: string;
  iconBucket: string;
  description: string;
}

// カテゴリ更新用DTO
export interface CategoryUpdateDTO {
  id: number;
  name?: string;
  iconObjectKey?: string;
  iconBucket?: string;
  description?: string;
}

// カテゴリ検索用DTO
export interface CategoryQueryDTO {
  name?: string;
  current?: number;
  size?: number;
}

// カテゴリVO
export interface CategoryVO {
  id: number;
  name: string;
  iconUrl: string;
  iconObjectKey?: string;
  iconBucket?: string;
  description: string;
  createTime: string;
  updateTime: string;
}

// 観光スポットエンティティ
export interface Item {
  id: number;
  title: string;
  description: string;
  coverUrl: string;
  coverObjectKey: string;
  coverBucket: string;
  fileUrl: string;
  fileObjectKey: string;
  fileBucket: string;
  tags: string;
  extraData?: string;
  categoryId: number;
  createTime: string;
  updateTime: string;
}

// 観光スポット追加用DTO
export interface ItemAddDTO {
  title: string;
  description: string;
  coverObjectKey: string;
  coverBucket: string;
  fileObjectKey: string;
  fileBucket: string;
  tags: string;
  extraData?: string;
  categoryId: number;
}

// 観光スポット更新用DTO
export interface ItemUpdateDTO {
  id: number;
  title?: string;
  description?: string;
  coverObjectKey?: string;
  coverBucket?: string;
  fileObjectKey?: string;
  fileBucket?: string;
  tags?: string;
  extraData?: string;
  categoryId?: number;
}

// 観光スポット検索用DTO
export interface ItemQueryDTO {
  title?: string;
  categoryId?: number;
  tag?: string;
  userRealName?: string;
  current?: number;
  size?: number;
}

// 観光スポットVO
export interface ItemVO {
  id: number;
  title: string;
  description: string;
  coverUrl: string;
  coverObjectKey?: string;
  coverBucket?: string;
  fileUrl: string;
  fileObjectKey?: string;
  fileBucket?: string;
  tags: string[];
  extraData?: string;
  category: CategoryVO;
  userId: number;
  userRealName?: string;
  createTime: string;
  updateTime: string;
  // 統計データ（フロントエンド側で補完）
  favorites?: number;
  views?: number;
}

// 推薦システムから返却される観光スポットVO
export interface RecommendedItemVO extends Partial<ItemVO> {
  id: number;
  title: string;
  description: string;
  tags?: string[];
  // 推薦システムが返却する可能性のある追加フィールド
  categoryName?: string;
  score?: number;
  relationDiversity?: number;
  popularity?: number;
  // 履歴インタラクション関連フィールド
  interactionType?: string;  // インタラクションタイプ：VIEWED、PURCHASED、FAVORITED、LIKED
  interactionLabel?: string; // インタラクションの説明：閲覧済み、予約済みなど
}