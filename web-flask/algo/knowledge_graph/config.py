"""
ナレッジグラフ設定
"""
import os

# Neo4jデータベース設定
NEO4J_URI = os.getenv('NEO4J_URI', 'bolt://localhost:7687')
NEO4J_USER = os.getenv('NEO4J_USER', 'neo4j')
NEO4J_PASSWORD = os.getenv('NEO4J_PASSWORD', 'neo4j123')

# グラフデータベースのノードラベル
NODE_LABELS = {
    'USER': 'User',
    'CATEGORY': 'Category', 
    'ITEM': 'Item'
}

# グラフデータベースのリレーションシップタイプ
RELATIONSHIP_TYPES = {
    'CREATED': 'CREATED',           # ユーザーがアイテムを作成
    'BELONGS_TO': 'BELONGS_TO',     # アイテムがカテゴリに属する
    'FAVORITED': 'FAVORITED',       # ユーザーがアイテムをお気に入り登録
    'LIKED': 'LIKED',               # ユーザーがアイテムにいいねする
    'VIEWED': 'VIEWED',             # ユーザーがアイテムを閲覧
    'PURCHASED': 'PURCHASED'        # ユーザーがアイテムを購入
} 
