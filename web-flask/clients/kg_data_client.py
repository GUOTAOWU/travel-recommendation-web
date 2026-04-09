"""
知識グラフデータ同期クライアント - springboot_clientベースのラップ
"""
from clients.springboot_client import springboot_client
from typing import List, Dict, Any
import logging

logger = logging.getLogger(__name__)

class KGDataClient:
    """知識グラフデータクライアント"""
    
    def __init__(self):
        self.client = springboot_client
    
    def get_all_users(self) -> List[Dict]:
        """すべてのユーザーデータを取得する"""
        try:
            all_users = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/user/page", params)
                
                if not result or not result.get("records"):
                    break
                
                users = result["records"]
                all_users.extend(users)
                
                # さらなるデータがあるか確認
                if len(users) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_users)} 件のユーザーを取得しました")
            return all_users
            
        except Exception as e:
            logger.error(f"ユーザーデータの取得に失敗しました: {str(e)}")
            return []
    
    def get_all_categories(self) -> List[Dict]:
        """すべてのカテゴリデータを取得する"""
        try:
            all_categories = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/category/page", params)
                
                if not result or not result.get("records"):
                    break
                
                categories = result["records"]
                all_categories.extend(categories)
                
                # さらなるデータがあるか確認
                if len(categories) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_categories)} 件のカテゴリを取得しました")
            return all_categories
            
        except Exception as e:
            logger.error(f"カテゴリデータの取得に失敗しました: {str(e)}")
            return []
    
    def get_all_items(self) -> List[Dict]:
        """すべてのアイテム（物品）データを取得する"""
        try:
            all_items = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/item/page", params)
                
                if not result or not result.get("records"):
                    break
                
                items = result["records"]
                all_items.extend(items)
                
                # さらなるデータがあるか確認
                if len(items) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_items)} 件のアイテムを取得しました")
            return all_items
            
        except Exception as e:
            logger.error(f"アイテムデータの取得に失敗しました: {str(e)}")
            return []
    
    def get_all_user_actions(self) -> List[Dict]:
        """すべてのユーザー行動データを取得する"""
        try:
            all_actions = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/user-action/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                actions = result["records"]
                all_actions.extend(actions)
                
                # さらなるデータがあるか確認
                if len(actions) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_actions)} 件のユーザー行動を取得しました")
            return all_actions
            
        except Exception as e:
            logger.error(f"ユーザー行動データの取得に失敗しました: {str(e)}")
            return []
    
    def get_all_favorites(self) -> List[Dict]:
        """すべてのお気に入りデータを取得する"""
        try:
            all_favorites = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/favorite/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                favorites = result["records"]
                all_favorites.extend(favorites)
                
                # さらなるデータがあるか確認
                if len(favorites) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_favorites)} 件のお気に入りを取得しました")
            return all_favorites
            
        except Exception as e:
            logger.error(f"お気に入りデータの取得に失敗しました: {str(e)}")
            return []
    
    def get_all_likes(self) -> List[Dict]:
        """すべての「いいね」データを取得する"""
        try:
            all_likes = []
            current = 1
            size = 100
            
            while True:
                params = {"current": current, "size": size}
                result = self.client.get("/like/admin/page", params)
                
                if not result or not result.get("records"):
                    break
                
                likes = result["records"]
                all_likes.extend(likes)
                
                # さらなるデータがあるか確認
                if len(likes) < size:
                    break
                
                current += 1
            
            logger.info(f"{len(all_likes)} 件の「いいね」を取得しました")
            return all_likes
            
        except Exception as e:
            logger.error(f"「いいね」データの取得に失敗しました: {str(e)}")
            return []

# グローバルインスタンスの作成
kg_data_client = KGDataClient()