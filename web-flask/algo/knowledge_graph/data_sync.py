"""
データ同期サービス - MySQLからNeo4jへデータを同期する
"""
import logging
from typing import List, Dict, Any
from clients.kg_data_client import kg_data_client
from algo.knowledge_graph.neo4j_client import neo4j_client
from algo.knowledge_graph.tag_extractor import tag_extractor_service

logger = logging.getLogger(__name__)

class DataSyncService:
    """データ同期サービス"""
    
    def __init__(self):
        self.data_client = kg_data_client
        self.neo4j_client = neo4j_client
    
    def sync_all_data(self) -> Dict[str, Any]:
        """
        すべてのデータをグラフデータベースに同期する
        
        Returns:
            同期結果の統計情報
        """
        try:
            logger.info("グラフデータベースへのデータ同期を開始します")
            
            # 1. グラフデータベースをクリア
            self.neo4j_client.clear_database()
            
            # 2. 制約とインデックスを作成
            self.neo4j_client.create_constraints()
            
            # 3. ユーザーデータを取得して同期
            users = self.data_client.get_all_users()
            if users:
                self.neo4j_client.create_user_nodes(users)
            
            # 4. カテゴリデータを取得して同期
            categories = self.data_client.get_all_categories()
            if categories:
                self.neo4j_client.create_category_nodes(categories)
            
            # 5. アイテムデータを取得して同期
            items = self.data_client.get_all_items()
            if items:
                self.neo4j_client.create_item_nodes(items)
            
            # 6. タグとリレーションシップを作成
            tags, tag_relationships = self._build_tags_and_relationships(items, categories)
            if tags:
                self.neo4j_client.create_tag_nodes(tags)
            
            # 7. リレーションシップを作成
            relationships = self._build_relationships(users, categories, items)
            if tag_relationships:
                relationships.extend(tag_relationships)
            if relationships:
                self.neo4j_client.create_relationships(relationships)
            
            # 8. 統計情報を取得
            stats = self.neo4j_client.get_statistics()
            
            logger.info("データ同期が完了しました")
            return {
                "success": True,
                "message": f"同期完了！ユーザー: {stats.get('users', 0)}, カテゴリ: {stats.get('categories', 0)}, アイテム: {stats.get('items', 0)}, タグ: {stats.get('tags', 0)}",
                "statistics": stats
            }
            
        except Exception as e:
            logger.error(f"データ同期に失敗しました: {str(e)}")
            return {
                "success": False,
                "message": f"同期失敗: {str(e)}",
                "statistics": {}
            }
    
    def _build_relationships(self, users: List[Dict], categories: List[Dict], items: List[Dict]) -> List[Dict]:
        """リレーションシップデータを構築する"""
        relationships = []
        
        try:
            # 1. ユーザーがアイテムを作成したリレーションシップ
            for item in items:
                relationships.append({
                    "type": "CREATED",
                    "userId": item.get("userId"),
                    "itemId": item.get("id"),
                    "createTime": item.get("createTime")
                })
            
            # 2. アイテムがカテゴリに属するリレーションシップ
            for item in items:
                relationships.append({
                    "type": "BELONGS_TO",
                    "itemId": item.get("id"),
                    "categoryId": item.get("categoryId")
                })
            
            # 3. ユーザー行動リレーションシップを取得
            user_actions = self.data_client.get_all_user_actions()
            for action in user_actions:
                action_type = action.get("actionType")
                if action_type == 0:  # 閲覧
                    relationships.append({
                        "type": "VIEWED",
                        "userId": action.get("userId"),
                        "itemId": action.get("itemId"),
                        "createTime": action.get("createTime"),
                        "extraData": action.get("extraData")
                    })
                elif action_type == 1:  # 購入
                    relationships.append({
                        "type": "PURCHASED",
                        "userId": action.get("userId"),
                        "itemId": action.get("itemId"),
                        "createTime": action.get("createTime"),
                        "extraData": action.get("extraData")
                    })
            
            # 4. お気に入りリレーションシップを取得
            favorites = self.data_client.get_all_favorites()
            for favorite in favorites:
                relationships.append({
                    "type": "FAVORITED",
                    "userId": favorite.get("userId"),
                    "itemId": favorite.get("itemId"),
                    "createTime": favorite.get("createTime")
                })
            
            # 5. いいねリレーションシップを取得
            likes = self.data_client.get_all_likes()
            for like in likes:
                relationships.append({
                    "type": "LIKED",
                    "userId": like.get("userId"),
                    "itemId": like.get("itemId"),
                    "createTime": like.get("createTime")
                })
            
            logger.info(f"{len(relationships)} 件のリレーションシップを構築しました")
            return relationships
            
        except Exception as e:
            logger.error(f"リレーションシップの構築に失敗しました: {str(e)}")
            return []
    
    def _build_tags_and_relationships(self, items: List[Dict], categories: List[Dict]) -> tuple[List[Dict], List[Dict]]:
        """タグデータとリレーションシップを構築する"""
        try:
            logger.info("开始使用数据库原生标签数据，跳过LLM提取...")
            
            # 直接通过拆分数据库原生 tags 字段来构建 tag_mappings
            tag_mappings = {}
            for item in items:
                item_id = item.get('id')
                tags_str = item.get('tags', '')
                
                if tags_str:
                    # 按照逗号分割，并去掉首尾多余空格
                    tag_list = [t.strip() for t in tags_str.split(',') if t.strip()]
                    tag_mappings[item_id] = tag_list
                else:
                    tag_mappings[item_id] = []
            
            # タグノードとリレーションシップデータを作成 (直接传给创建节点的方法)
            tag_nodes, tag_relationships = tag_extractor_service.create_tag_nodes_data(tag_mappings)
            
            logger.info(f"{len(tag_nodes)} 件のタグノードと {len(tag_relationships)} 件のタグリレーションシップを作成しました")
            return tag_nodes, tag_relationships
            
        except Exception as e:
            logger.error(f"タグとリレーションシップの構築に失敗しました: {str(e)}")
            return [], []

# グローバルインスタンスを作成
data_sync_service = DataSyncService()
