"""
Neo4j グラフデータベースクライアント
"""
from neo4j import GraphDatabase
import logging
from typing import List, Dict, Optional
from algo.knowledge_graph.config import NEO4J_URI, NEO4J_USER, NEO4J_PASSWORD

logger = logging.getLogger(__name__)

class Neo4jClient:
    """Neo4j グラフデータベースクライアント"""
    
    def __init__(self, uri: str = NEO4J_URI, user: str = NEO4J_USER, password: str = NEO4J_PASSWORD):
        """
        Neo4j クライアントを初期化する
        """
        self.driver = GraphDatabase.driver(uri, auth=(user, password))
        
    def close(self):
        """データベース接続をクローズする"""
        if self.driver:
            self.driver.close()
            
    def execute_query(self, query: str, parameters: Optional[Dict] = None) -> List[Dict]:
        """Cypher クエリを実行する"""
        with self.driver.session() as session:
            result = session.run(query, parameters or {})
            return [record.data() for record in result]
    
    def execute_query_raw(self, query: str, parameters: Optional[Dict] = None) -> List:
        """Cypher クエリを実行し、生のレコードを返す"""
        with self.driver.session() as session:
            result = session.run(query, parameters or {})
            return list(result)
    
    def clear_database(self):
        """データベースをクリアする"""
        query = "MATCH (n) DETACH DELETE n"
        self.execute_query(query)
        logger.info("データベースがクリアされました")
    
    def create_constraints(self):
        """制約とインデックスを作成する"""
        constraints = [
            "CREATE CONSTRAINT user_id IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE",
            "CREATE CONSTRAINT category_id IF NOT EXISTS FOR (c:Category) REQUIRE c.id IS UNIQUE", 
            "CREATE CONSTRAINT item_id IF NOT EXISTS FOR (i:Item) REQUIRE i.id IS UNIQUE",
            "CREATE CONSTRAINT tag_id IF NOT EXISTS FOR (t:Tag) REQUIRE t.id IS UNIQUE",
            "CREATE INDEX user_username IF NOT EXISTS FOR (u:User) ON (u.username)",
            "CREATE INDEX item_title IF NOT EXISTS FOR (i:Item) ON (i.title)",
            "CREATE INDEX category_name IF NOT EXISTS FOR (c:Category) ON (c.name)",
            "CREATE INDEX tag_name IF NOT EXISTS FOR (t:Tag) ON (t.name)"
        ]
        
        for constraint in constraints:
            try:
                self.execute_query(constraint)
            except Exception as e:
                logger.debug(f"制約の作成に失敗しました（既に存在している可能性があります）: {e}")
        
        logger.info("制約とインデックスの作成が完了しました")
    
    def create_user_nodes(self, users: List[Dict]):
        """ユーザーノードを作成する"""
        query = """
        UNWIND $users AS user
        MERGE (u:User {id: user.id})
        SET u.username = user.username,
            u.realName = user.realName,
            u.phone = user.phone,
            u.email = user.email,
            u.role = user.role,
            u.status = user.status,
            u.avatarBucket = user.avatarBucket,
            u.avatarObjectKey = user.avatarObjectKey,
            u.createTime = user.createTime,
            u.updateTime = user.updateTime
        """
        self.execute_query(query, {"users": users})
        logger.info(f"{len(users)} 個のユーザーノードの作成に成功しました")
    
    def create_category_nodes(self, categories: List[Dict]):
        """カテゴリノードを作成する"""
        query = """
        UNWIND $categories AS category
        MERGE (c:Category {id: category.id})
        SET c.name = category.name,
            c.description = category.description,
            c.createTime = category.createTime,
            c.updateTime = category.updateTime
        """
        self.execute_query(query, {"categories": categories})
        logger.info(f"{len(categories)} 個のカテゴリノードの作成に成功しました")
    
    def create_item_nodes(self, items: List[Dict]):
        """スポット（アイテム）ノードを作成する"""
        query = """
        UNWIND $items AS item
        MERGE (i:Item {id: item.id})
        SET i.title = item.title,
            i.description = item.description,
            i.tags = item.tags,
            i.coverBucket = item.coverBucket,
            i.coverObjectKey = item.coverObjectKey,
            i.createTime = item.createTime,
            i.updateTime = item.updateTime
        """
        self.execute_query(query, {"items": items})
        logger.info(f"{len(items)} 個のスポットノードの作成に成功しました")
    
    def create_tag_nodes(self, tags: List[Dict]):
        """タグノードを作成する"""
        query = """
        UNWIND $tags AS tag
        MERGE (t:Tag {id: tag.id})
        SET t.name = tag.name,
            t.type = tag.type,
            t.createTime = datetime(),
            t.updateTime = datetime()
        """
        self.execute_query(query, {"tags": tags})
        logger.info(f"{len(tags)} 個のタグノードの作成に成功しました")
    
    def create_relationships(self, relationships: List[Dict]):
        """リレーションシップ（関連）を作成する"""
        user_create_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'CREATED'
        MERGE (u)-[:CREATED {createTime: rel.createTime}]->(i)
        """
        
        item_belongs_category_query = """
        UNWIND $relationships AS rel
        MATCH (i:Item {id: rel.itemId}), (c:Category {id: rel.categoryId})
        WHERE rel.type = 'BELONGS_TO'
        MERGE (i)-[:BELONGS_TO]->(c)
        """
        
        user_favorite_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'FAVORITED'
        MERGE (u)-[:FAVORITED {createTime: rel.createTime}]->(i)
        """
        
        user_like_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'LIKED'
        MERGE (u)-[:LIKED {createTime: rel.createTime}]->(i)
        """
        
        user_view_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'VIEWED'
        MERGE (u)-[:VIEWED {createTime: rel.createTime, extraData: rel.extraData}]->(i)
        """
        
        user_purchase_item_query = """
        UNWIND $relationships AS rel
        MATCH (u:User {id: rel.userId}), (i:Item {id: rel.itemId})
        WHERE rel.type = 'PURCHASED'
        MERGE (u)-[:PURCHASED {createTime: rel.createTime, extraData: rel.extraData}]->(i)
        """
        
        item_has_tag_query = """
        UNWIND $relationships AS rel
        MATCH (i:Item {id: rel.itemId}), (t:Tag {id: rel.tagId})
        WHERE rel.type = 'HAS_TAG'
        MERGE (i)-[:HAS_TAG {createTime: datetime()}]->(t)
        """
        
        queries = [
            user_create_item_query,
            item_belongs_category_query,
            user_favorite_item_query,
            user_like_item_query,
            user_view_item_query,
            user_purchase_item_query,
            item_has_tag_query
        ]
        
        for query in queries:
            self.execute_query(query, {"relationships": relationships})
        
        logger.info("リレーションシップの作成が完了しました")
    
    def get_statistics(self) -> Dict[str, int]:
        """グラフデータベースの統計情報を取得する"""
        stats = {}
        node_queries = {
            "users": "MATCH (u:User) RETURN count(u) as count",
            "categories": "MATCH (c:Category) RETURN count(c) as count",
            "items": "MATCH (i:Item) RETURN count(i) as count",
            "tags": "MATCH (t:Tag) RETURN count(t) as count"
        }
        for key, query in node_queries.items():
            result = self.execute_query(query)
            stats[key] = result[0]["count"] if result else 0
            
        relationship_queries = {
            "created": "MATCH ()-[r:CREATED]->() RETURN count(r) as count",
            "belongs_to": "MATCH ()-[r:BELONGS_TO]->() RETURN count(r) as count",
            "favorited": "MATCH ()-[r:FAVORITED]->() RETURN count(r) as count",
            "liked": "MATCH ()-[r:LIKED]->() RETURN count(r) as count",
            "viewed": "MATCH ()-[r:VIEWED]->() RETURN count(r) as count",
            "purchased": "MATCH ()-[r:PURCHASED]->() RETURN count(r) as count",
            "has_tag": "MATCH ()-[r:HAS_TAG]->() RETURN count(r) as count"
        }
        for key, query in relationship_queries.items():
            result = self.execute_query(query)
            stats[key] = result[0]["count"] if result else 0
            
        return stats
    
    def search_related_items(self, keywords: List[str], limit: int = 10) -> List[Dict]:
        """
        キーワードに基づいて関連スポットを検索する（動的な関連度により厳格にフィルタリング）
        """
        if not keywords:
            return []
            
        query = """
        MATCH (i:Item)
        OPTIONAL MATCH (i)-[:BELONGS_TO]->(c:Category)
        OPTIONAL MATCH (i)-[:HAS_TAG]->(t:Tag)
        WITH i, c, COLLECT(DISTINCT t.name) as tags
        
        // toLower と coalesce を使用してマッチングの堅牢性を確保（大文字小文字を区別せず、null を防止）
        WITH i, c, tags,
             // 第1：絶対ヒット単語数を計算（マッチした一意のキーワード数）
             SIZE([keyword IN $keywordList WHERE 
                toLower(coalesce(i.title, '')) CONTAINS toLower(keyword) OR 
                toLower(coalesce(i.description, '')) CONTAINS toLower(keyword) OR 
                toLower(coalesce(c.name, '')) CONTAINS toLower(keyword) OR 
                ANY(tag IN tags WHERE toLower(coalesce(tag, '')) CONTAINS toLower(keyword))
             ]) AS hitCount,
             
             // 第2：スコアリング戦略（タイトルヒット+10点、タグ+5点、説明+1点）
             REDUCE(s = 0, keyword IN $keywordList | s + 
                 CASE WHEN toLower(coalesce(i.title, '')) CONTAINS toLower(keyword) THEN 10 ELSE 0 END +
                 CASE WHEN ANY(tag IN tags WHERE toLower(coalesce(tag, '')) CONTAINS toLower(keyword)) THEN 5 ELSE 0 END +
                 CASE WHEN toLower(coalesce(c.name, '')) CONTAINS toLower(keyword) THEN 5 ELSE 0 END +
                 CASE WHEN toLower(coalesce(i.description, '')) CONTAINS toLower(keyword) THEN 1 ELSE 0 END
             ) AS relevanceScore
             
        // フィルタリング：少なくとも1つのキーワードにマッチするスポットのみを保持
        WHERE hitCount > 0
        
        RETURN DISTINCT i.id as id, 
                i.title as title, 
                i.description as description,
                c.name as categoryName,
                tags,
                i.createTime as createTime,
                hitCount,
                relevanceScore
               
        // ヒット単語数(hitCount)を優先し、同じ場合は重み付けスコア(relevanceScore)を比較
        ORDER BY hitCount DESC, relevanceScore DESC, i.createTime DESC
        // グラフ層の LIMIT を緩和し、Python メモリ内でのインテリジェントな切り捨てを容易にする
        LIMIT 50  
        """
        
        parameters = {
            "keywordList": keywords
        }
        
        results = self.execute_query(query, parameters)
        
        if not results:
            logger.info(f"キーワード {keywords} を使用した関連スポットは見つかりませんでした")
            return []
            
        # 2. インテリジェント動的切り捨てアルゴリズム
        max_hit_count = results[0]["hitCount"]
        
        # 動的な閾値ロジック：
        if len(keywords) <= 2:
            # キーワードが1-2個の場合、厳格なマッチングを要求
            threshold = max_hit_count
        else:
            # 語彙が多い（地名＋複数の形容詞）場合、許容範囲を広げる
            # コアワード（通常は地名＋国）が2つヒットすれば、候補に入れ、最終判断をLLMに委ねる
            threshold = max(2, max_hit_count - 2)
        
        # 閾値を下回る低精度なデータをフィルタリング
        strict_results = [r for r in results if r["hitCount"] >= threshold]
        
        # 3. 最終的に返す件数を取得
        final_results = strict_results[:limit]
        
        # 結果のフォーマット
        formatted_results = []
        for result in final_results:
            formatted_results.append({
                "id": result["id"],
                "title": result["title"],
                "description": result["description"],
                "categoryName": result["categoryName"],
                "tags": result["tags"],
                "createTime": result["createTime"],
                "hitCount": result["hitCount"],
                "relevanceScore": result["relevanceScore"],
                "source": "graph_search"
            })
        
        logger.info(f"キーワード {keywords} で検索し、インテリジェントな切り捨てを経て、{len(formatted_results)} 個の高精度なスポットを返しました")
        return formatted_results

# グローバルインスタンスの作成
neo4j_client = Neo4jClient()