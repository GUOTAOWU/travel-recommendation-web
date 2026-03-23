"""
基于知识图谱的推荐算法 (已引入时间衰减动态计算)
"""
import logging
from typing import List, Dict
from algo.knowledge_graph.neo4j_client import neo4j_client

logger = logging.getLogger(__name__)

class KGRecommender:
    """基于知识图谱的推荐算法"""
    
    def __init__(self):
        self.neo4j_client = neo4j_client
    
    def recommend_by_user_id(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        基于用户ID推荐景点（协同过滤 + 时间衰减）
        
        Args:
            user_id: 用户ID
            limit: 推荐结果数量限制
            
        Returns:
            推荐景点列表
        """
        try:
            query = """
            MATCH (u:User {id: $userId})-[r1:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            MATCH (i1)<-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]-(u2:User)
            WHERE u.id <> u2.id
            MATCH (u2)-[r3:VIEWED|PURCHASED|FAVORITED|LIKED]->(i2:Item)
            MATCH (creator:User)-[:CREATED]->(i2)
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            
            // 【核心创新点】：引入指数时间衰减函数
            // 提取交互时间，计算距离今天的天数，并应用 e^(-0.05 * t) 计算动态权重
            WITH i2, creator, u2, r3, tag,
                 CASE WHEN r3.createTime IS NOT NULL 
                 THEN exp(-0.05 * duration.inDays(localdatetime(replace(toString(r3.createTime), ' ', 'T')), localdatetime()).days)
                 ELSE 1.0 END AS timeWeight
            
            // 原来的 COUNT(DISTINCT u2) 改为 SUM(timeWeight) 累加时间权重
            WITH i2, creator, tag,
                 SUM(timeWeight) AS score, 
                 COLLECT(DISTINCT TYPE(r3)) AS relationTypes
                 
            WITH i2, creator, score, relationTypes, COLLECT(DISTINCT tag.name) AS tagNames
            RETURN i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   score, size(relationTypes) AS relationDiversity,
                   tagNames
            ORDER BY score DESC, relationDiversity DESC
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
            
            logger.info(f"为用户 {user_id} 生成了 {len(result)} 个基于时间衰减的协同过滤推荐")
            return result
            
        except Exception as e:
            logger.error(f"为用户 {user_id} 生成协同过滤推荐失败: {str(e)}")
            return []
    
    def recommend_by_content(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        基于用户喜好的内容推荐（基于类别和标签 + 时间衰减）
        
        Args:
            user_id: 用户ID
            limit: 推荐结果数量限制
            
        Returns:
            推荐景点列表
        """
        try:
            content_query = """
            // 1. 获取用户交互历史，并计算时间衰减权重
            MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)
            WITH u, i1, r,
                 CASE WHEN r.createTime IS NOT NULL 
                 THEN exp(-0.05 * duration.inDays(localdatetime(replace(toString(r.createTime), ' ', 'T')), localdatetime()).days)
                 ELSE 1.0 END AS timeWeight
                 
            MATCH (i1)-[:BELONGS_TO]->(c:Category)
            OPTIONAL MATCH (i1)-[:HAS_TAG]->(t:Tag)
            
            // 2. 基于类别找相似物品
            MATCH (i2:Item)-[:BELONGS_TO]->(c)
            WHERE i1.id <> i2.id
            MATCH (creator:User)-[:CREATED]->(i2)
            OPTIONAL MATCH (i2)-[:HAS_TAG]->(tag:Tag)
            
            // 3. 计算带有时效性的类别匹配度
            WITH u, i2, creator, c.name AS categoryName,
                 SUM(timeWeight) AS categoryMatch,
                 SIZE([(i2)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
                 COLLECT(DISTINCT tag.name) AS tagNames
            
            // 4. 基于标签的额外匹配
            OPTIONAL MATCH (u)-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]->(i1:Item)-[:HAS_TAG]->(ut:Tag)
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 COLLECT(DISTINCT ut.name) AS userTags
            
            WITH i2, creator, categoryName, categoryMatch, popularity, tagNames,
                 SIZE([tag IN tagNames WHERE tag IN userTags]) AS tagMatch
            
            // 5. 计算最终综合得分
            WITH i2, creator, categoryName, popularity, tagNames,
                 (categoryMatch * 3.0 + tagMatch * 2.0 + popularity * 0.1) AS score
            
            RETURN DISTINCT i2.id AS id, i2.title AS title, i2.description AS description, 
                   i2.tags AS tags, i2.coverBucket AS coverBucket, i2.coverObjectKey AS coverObjectKey,
                   creator.username AS username, creator.realName AS userRealName,
                   categoryName, score, tagNames
            ORDER BY score DESC
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
            
            logger.info(f"为用户 {user_id} 生成了 {len(result)} 个基于内容和时间衰减的推荐")
            return result
            
        except Exception as e:
            logger.error(f"为用户 {user_id} 生成基于内容的推荐失败: {str(e)}")
            return []

# 创建全局实例
kg_recommender = KGRecommender()