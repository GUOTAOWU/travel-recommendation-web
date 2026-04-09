"""
知識グラフ（ナレッジグラフ）ルーティング
"""
from flask import Blueprint, request
from algo.knowledge_graph.data_sync import data_sync_service
from algo.knowledge_graph.neo4j_client import neo4j_client
from utils.response import success, error


knowledge_graph_bp = Blueprint('knowledge_graph', __name__, url_prefix='/api/knowledge-graph')

@knowledge_graph_bp.route('/sync', methods=['POST'])
def sync_data():
    """
    グラフデータベースへのデータ同期
    
    Returns:
        同期結果
    """
    try:
        result = data_sync_service.sync_all_data()
        
        if result.get("success"):
            return success(
                data=result.get("statistics", {}),
                msg=result.get("message", "データ同期に成功しました")
            )
        else:
            return error(
                msg=result.get("message", "データ同期に失敗しました"),
                code=500
            )
            
    except Exception as e:
        return error(f'データ同期に失敗しました: {str(e)}', code=500)

@knowledge_graph_bp.route('/stats', methods=['GET'])
def get_stats():
    """
    グラフデータベースの統計情報を取得
    
    Returns:
        統計情報
    """
    try:
        stats = neo4j_client.get_statistics()
        
        # 統計データの組織化処理を追加
        result = {
            "nodes": {},
            "relationships": {}
        }
        
        # ノード統計の処理
        if "users" in stats:
            result["nodes"]["User"] = stats["users"]
        if "categories" in stats:
            result["nodes"]["Category"] = stats["categories"]
        if "items" in stats:
            result["nodes"]["Item"] = stats["items"]
        if "tags" in stats:
            result["nodes"]["Tag"] = stats["tags"]
            
        # リレーション統計の処理
        if "created" in stats:
            result["relationships"]["CREATED"] = stats["created"]
        if "belongs_to" in stats:
            result["relationships"]["BELONGS_TO"] = stats["belongs_to"]
        if "favorited" in stats:
            result["relationships"]["FAVORITED"] = stats["favorited"]
        if "liked" in stats:
            result["relationships"]["LIKED"] = stats["liked"]
        if "viewed" in stats:
            result["relationships"]["VIEWED"] = stats["viewed"]
        if "purchased" in stats:
            result["relationships"]["PURCHASED"] = stats["purchased"]
        if "has_tag" in stats:
            result["relationships"]["HAS_TAG"] = stats["has_tag"]
            
        return success(result, msg='統計情報の取得に成功しました')
    except Exception as e:
        return error(f'統計情報の取得に失敗しました: {str(e)}', code=500)

def serialize_neo4j_data(obj):
    """
    Neo4j データを再帰的にシリアライズし、DateTime などの特殊オブジェクトを文字列に変換する
    
    Args:
        obj: シリアライズ対象のオブジェクト
        
    Returns:
        シリアライズ後のオブジェクト
    """
    # 基本型の処理
    if isinstance(obj, (str, int, float, bool, type(None))):
        return obj
    
    # DateTime オブジェクトの処理
    if hasattr(obj, 'isoformat'):
        return obj.isoformat()
    
    # 辞書の処理
    if isinstance(obj, dict):
        return {key: serialize_neo4j_data(value) for key, value in obj.items()}
    
    # リストの処理
    if isinstance(obj, list):
        return [serialize_neo4j_data(item) for item in obj]
    
    # その他の反復可能オブジェクトの処理
    if hasattr(obj, '__iter__'):
        try:
            return str(obj)
        except:
            return obj
    
    # その他の場合は直接文字列に変換
    return str(obj)

@knowledge_graph_bp.route('/visualization', methods=['GET'])
def get_visualization_data():
    """
    知識グラフの可視化データを取得
    
    Returns:
        ノードとエッジのデータ（グラフ可視化用）
    """
    try:
        # リクエストパラメータの取得
        limit = request.args.get('limit', default=300, type=int)
        
        print(f"可視化データの取得を開始します。ノード制限数: {limit}")
        
        # すべてのノードを取得
        nodes_query = f"""
        MATCH (n)
        RETURN n
        LIMIT {limit}
        """
        
        # すべてのリレーションを取得
        relationships_query = f"""
        MATCH (n)-[r]->(m)
        RETURN n, r, m, type(r) as relType
        LIMIT {limit * 2}
        """
        
        # クエリの実行
        node_results = neo4j_client.execute_query_raw(nodes_query)
        rel_results = neo4j_client.execute_query_raw(relationships_query)
        
        print(f"{len(node_results)} 個のノード、{len(rel_results)} 個のリレーションを取得しました")
        
        # ノードデータの処理
        nodes = {}
        for record in node_results:
            node = record['n']
            node_labels = list(node.labels)
            if not node_labels:
                continue
                
            node_type = node_labels[0]
            node_id = f"{node_type}-{node['id']}"
            
            # ノード表示名の決定
            display_name = ""
            if node_type == 'User':
                display_name = node.get('username', f"ユーザー{node['id']}")
            elif node_type == 'Item':
                display_name = node.get('title', f"スポット{node['id']}")
            elif node_type == 'Category':
                display_name = node.get('name', f"カテゴリ{node['id']}")
            elif node_type == 'Tag':
                display_name = node.get('name', f"タグ{node['id']}")
            else:
                display_name = f"{node_type}{node['id']}"
            
            nodes[node_id] = {
                'id': node_id,
                'name': display_name,
                'type': node_type,
                'properties': serialize_neo4j_data(dict(node))
            }
        
        # リレーションデータの処理 - ノードペアごとにグループ化し、同タイプのエッジを統合
        edge_groups = {}  # key: (source, target), value: list of edges
        
        for record in rel_results:
            source_node = record['n']
            target_node = record['m']
            rel_type = record['relType']
            
            source_labels = list(source_node.labels)
            target_labels = list(target_node.labels)
            
            if not source_labels or not target_labels:
                continue
                
            source_id = f"{source_labels[0]}-{source_node['id']}"
            target_id = f"{target_labels[0]}-{target_node['id']}"
            
            # source と target ノードが両方存在することを確認
            if source_id not in nodes or target_id not in nodes:
                continue
            
            # ノードペアのキーを作成
            node_pair_key = (source_id, target_id)
            
            if node_pair_key not in edge_groups:
                edge_groups[node_pair_key] = {}
            
            # リレーションタイプごとにグループ化
            if rel_type not in edge_groups[node_pair_key]:
                edge_groups[node_pair_key][rel_type] = 0
            edge_groups[node_pair_key][rel_type] += 1
        
        # 最終的なエッジデータの生成 - 1つのノードペアにつき1つのエッジ
        edges = []
        for (source_id, target_id), rel_types in edge_groups.items():
            # このノードペアの合計エッジ数を計算
            total_count = sum(rel_types.values())
            
            # デバッグ情報：ノードペアのエッジ情報を出力
            print(f"ノードペア {source_id} -> {target_id}: 合計エッジ数={total_count}, リレーションタイプ={list(rel_types.keys())}")
            
            # すべてのリレーションタイプを1つのラベルに統合
            if len(rel_types) == 1:
                # リレーションタイプが1種類のみの場合
                rel_type, count = list(rel_types.items())[0]
                if rel_type == 'CREATED':
                    japanese_label = "作成"
                elif rel_type == 'BELONGS_TO':
                    japanese_label = "所属"
                elif rel_type == 'VIEWED':
                    japanese_label = "閲覧"
                elif rel_type == 'PURCHASED':
                    japanese_label = "予約"
                elif rel_type == 'FAVORITED':
                    japanese_label = "お気に入り"
                elif rel_type == 'LIKED':
                    japanese_label = "いいね"
                elif rel_type == 'HAS_TAG':
                    japanese_label = "タグ"
                else:
                    japanese_label = rel_type
                
                if count > 1:
                    combined_label = f"{japanese_label} ×{count}"
                else:
                    combined_label = japanese_label
            else:
                # 複数のリレーションタイプがある場合
                type_labels = []
                for rel_type, count in rel_types.items():
                    if rel_type == 'CREATED':
                        type_name = "作成"
                    elif rel_type == 'BELONGS_TO':
                        type_name = "所属"
                    elif rel_type == 'VIEWED':
                        type_name = "閲覧"
                    elif rel_type == 'PURCHASED':
                        type_name = "予約"
                    elif rel_type == 'FAVORITED':
                        type_name = "お気に入り"
                    elif rel_type == 'LIKED':
                        type_name = "いいね"
                    elif rel_type == 'HAS_TAG':
                        type_name = "タグ"
                    else:
                        type_name = rel_type
                    
                    if count > 1:
                        type_labels.append(f"{type_name}×{count}")
                    else:
                        type_labels.append(type_name)
                
                combined_label = "、".join(type_labels)
            
            print(f"  エッジ統合: {combined_label}")
            
            # すべてのリレーション情報を含む単一のエッジを作成
            edges.append({
                'id': f"edge-{source_id}-{target_id}",
                'source': source_id,
                'target': target_id,
                'label': combined_label,
                'types': list(rel_types.keys()),  # 含まれるすべてのリレーションタイプ
                'type_counts': rel_types,         # 各タイプの数量
                'total_count': total_count,       # 合計数量
                'group_key': f"{source_id}-{target_id}"
            })
        
        # レスポンスデータの構築
        visualization_data = {
            'nodes': list(nodes.values()),
            'edges': edges
        }
        
        print(f"処理完了 - ノード: {len(nodes)}, エッジ: {len(edges)}")
        
        return success(visualization_data, msg='可視化データの取得に成功しました')
        
    except Exception as e:
        print(f"可視化データの取得に失敗しました: {str(e)}")
        import traceback
        traceback.print_exc()
        return error(f'可視化データの取得に失敗しました: {str(e)}', code=500)

@knowledge_graph_bp.route('/user-profile/<int:user_id>', methods=['GET'])
def get_user_profile(user_id):
    """
    ユーザープロファイル情報の取得
    
    Args:
        user_id: ユーザーID
        
    Returns:
        ユーザープロファイル情報
    """
    try:
        # ユーザーが好むカテゴリを取得
        category_query = """
        MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)-[:BELONGS_TO]->(c:Category)
        WITH c, COUNT(r) AS interactionCount, COLLECT(DISTINCT TYPE(r)) AS interactionTypes
        RETURN c.id AS id, c.name AS name, interactionCount AS count,
               interactionTypes AS types,
               SIZE(interactionTypes) AS diversityScore
        ORDER BY interactionCount DESC, diversityScore DESC
        LIMIT 5
        """
        
        # ユーザーが最も多くインタラクションしたタグを取得
        tag_query = """
        MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)
        WHERE i.tags IS NOT NULL
        UNWIND split(i.tags, ',') AS tag
        WITH TRIM(tag) AS cleanTag, COUNT(r) AS tagCount
        WHERE cleanTag <> ''
        RETURN cleanTag AS tag, tagCount AS count
        ORDER BY count DESC
        LIMIT 10
        """
        
        # ユーザーのアクティビティ統計を取得
        activity_query = """
        MATCH (u:User {id: $userId})-[r]->(i:Item)
        WITH TYPE(r) AS actionType, COUNT(r) AS actionCount
        RETURN actionType, actionCount
        ORDER BY actionCount DESC
        """
        
        # ユーザーのソーシャルネットワークを取得
        social_query = """
        MATCH (u1:User {id: $userId})-[r1:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)<-[r2:VIEWED|PURCHASED|FAVORITED|LIKED]-(u2:User)
        WHERE u1 <> u2
        WITH u2, COUNT(DISTINCT i) AS commonItems
        RETURN u2.id AS userId, u2.username AS username, 
               commonItems,
               u2.avatarBucket AS avatarBucket,
               u2.avatarObjectKey AS avatarObjectKey
        ORDER BY commonItems DESC
        LIMIT 5
        """
        
        # クエリの実行
        categories = neo4j_client.execute_query(category_query, {"userId": user_id})
        tags = neo4j_client.execute_query(tag_query, {"userId": user_id})
        activities = neo4j_client.execute_query(activity_query, {"userId": user_id})
        similar_users = neo4j_client.execute_query(social_query, {"userId": user_id})
        
        # アクティビティタイプ別のアクティビティ統計を整理
        activity_stats = {}
        for activity in activities:
            activity_stats[activity['actionType']] = activity['actionCount']
        
        # ユーザープロファイルデータの構築
        user_profile = {
            "preferred_categories": categories,
            "preferred_tags": tags,
            "activity_stats": activity_stats,
            "similar_users": similar_users
        }
        
        return success(user_profile, msg='ユーザープロファイルの取得に成功しました')
    except Exception as e:
        return error(f'ユーザープロファイルの取得に失敗しました: {str(e)}', code=500) 