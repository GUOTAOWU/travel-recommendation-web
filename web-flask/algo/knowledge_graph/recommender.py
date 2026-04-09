"""
ナレッジグラフベースの推薦アルゴリズム（時間減衰と加重ランダムサンプリングを導入）
"""
import logging
from typing import List, Dict
from algo.knowledge_graph.neo4j_client import neo4j_client

logger = logging.getLogger(__name__)

class KGRecommender:
    """ナレッジグラフベースの推薦アルゴリズム"""
    
    def __init__(self):
        self.neo4j_client = neo4j_client
    
    def recommend_by_user_id(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        ユーザーIDに基づいたスポット推薦（協調フィルタリング＋時間減衰＋加重ランダム）
        """
        try:
            query = """
            MATCH (u:User {id: $userId})-[r1:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            MATCH (i1)<-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]-(u2:User)
            WHERE u.id <> u2.id
            MATCH (u2)-[r3:VIEWED|PURCHASED|FAVORITED|LIKED]->(i2:Item)
            MATCH (creator:User)-[:CREATED]->(i2)
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            OPTIONAL MATCH (i2)-[:BELONGS_TO]->(c:Category)
            
            // 指数時間減衰関数の導入
            WITH i2, creator, c, u2, r3, tag,
                 CASE WHEN r3.createTime IS NOT NULL 
                 THEN exp(-0.05 * duration.inDays(localdatetime(replace(toString(r3.createTime), ' ', 'T')), localdatetime()).days)
                 ELSE 1.0 END AS timeWeight
            
            WITH i2, creator, c, tag,
                 SUM(timeWeight) AS score, 
                 COLLECT(DISTINCT TYPE(r3)) AS relationTypes
                 
            WITH i2, creator, c.name AS categoryName, score, relationTypes, COLLECT(DISTINCT tag.name) AS tagNames
            
            RETURN i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   categoryName, score, size(relationTypes) AS relationDiversity,
                   tagNames
            
            // ⭐ 主要な変更点：加重ランダムアルゴリズム (Weighted Random) を使用
            // 数式：乱数 * (基礎スコア + リレーション多様性の重み + 基礎最低保証スコア)
            ORDER BY rand() * (score + size(relationTypes) * 0.5 + 0.1) DESC
            LIMIT $limit
            """
            
            result = self.neo4j_client.execute_query(query, {"userId": user_id, "limit": limit})
            
            for item in result:
                original_tags = []
                if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                    original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
                tag_names = item.get("tagNames", [])
                item["tags"] = list(set(original_tags + [tag for tag in tag_names if tag]))
                if "tagNames" in item:
                    del item["tagNames"]
            
            logger.info(f"ユーザー {user_id} に対して加重ランダム協調フィルタリング推薦を {len(result)} 件生成しました")
            return result
            
        except Exception as e:
            logger.error(f"ユーザー {user_id} の協調フィルタリング推薦生成に失敗しました: {str(e)}")
            return []
    
    def recommend_by_content(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        ユーザーの好みに基づくコンテンツ推薦（カテゴリ＋タグベース＋時間減衰＋加重ランダム）
        """
        try:
            content_query = """
            MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            WITH u, i1, r,
                 CASE WHEN r.createTime IS NOT NULL 
                 THEN exp(-0.05 * duration.inDays(localdatetime(replace(toString(r.createTime), ' ', 'T')), localdatetime()).days)
                 ELSE 1.0 END AS timeWeight
                 
            MATCH (i1)-[:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (i1)-[:HAS_TAG]->(t:Tag)
            
            MATCH (i2:Item)-[:BELONGS_TO]->(c)
            WHERE i1.id <> i2.id
            MATCH (creator:User)-[:CREATED]->(i2)
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            
            WITH u, i2, creator, c.name AS categoryName,
                 SUM(timeWeight) AS categoryMatch,
                 SIZE([(i2)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
                 COLLECT(DISTINCT tag.name) AS tagNames
            
            OPTIONAL MATCH (u)-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)-[:HAS_TAG]->(ut:Tag)
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 COLLECT(DISTINCT ut.name) AS userTags
            
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 SIZE([tag IN tagNames WHERE tag IN userTags]) AS tagMatch
            
            WITH i2, creator, categoryName, popularity, tagNames,
                 (categoryMatch * 3.0 + tagMatch * 2.0 + popularity * 0.1) AS score
            
            RETURN DISTINCT i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   categoryName, score, tagNames
                   
            // ⭐ 主要な変更点：加重ランダムアルゴリズム (Weighted Random) を使用
            // 数式：乱数 * (総合マッチングスコア + 基礎最低保証スコア)
            ORDER BY rand() * (score + 0.1) DESC
            LIMIT $limit
            """
            
            result = self.neo4j_client.execute_query(content_query, {"userId": user_id, "limit": limit})
            
            for item in result:
                original_tags = []
                if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                    original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
                tag_names = item.get("tagNames", [])
                item["tags"] = list(set(original_tags + [tag for tag in tag_names if tag]))
                if "tagNames" in item:
                    del item["tagNames"]
            
            logger.info(f"ユーザー {user_id} に対して加重ランダムコンテンツ推薦を {len(result)} 件生成しました")
            return result
            
        except Exception as e:
            logger.error(f"ユーザー {user_id} のコンテンツベース推薦生成に失敗しました: {str(e)}")
            return []

# グローバルインスタンスを作成
kg_recommender = KGRecommender()