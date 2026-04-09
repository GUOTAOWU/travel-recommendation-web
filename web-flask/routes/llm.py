"""
LLM ルーティング
"""
import random
import re
from flask import Blueprint, request, jsonify, Response, stream_with_context
from typing import List
from algo.llm.llm_client import LLMClient
from algo.llm.config import AVAILABLE_MODELS, DEFAULT_MODEL
from utils.response import success, error
from algo.knowledge_graph.neo4j_client import neo4j_client

llm_bp = Blueprint('llm', __name__, url_prefix='/api/llm')

@llm_bp.route('/models', methods=['GET'])
def get_models():
    """利用可能なモデルの一覧を取得する"""
    return success(AVAILABLE_MODELS)

@llm_bp.route('/chat', methods=['POST'])
def chat():
    """標準的なチャットインターフェース"""
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        
        if not messages:
            return error('メッセージリストを空にすることはできません')
        
        client = LLMClient(model_name)
        response = client.chat(messages)
        return success(response)
    except Exception as e:
        return error(f'チャットに失敗しました: {str(e)}')

@llm_bp.route('/recommend', methods=['POST'])
def recommend_from_chat():
    """チャット内容に基づいた観光スポットの推薦"""
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        user_id = data.get('userId')
        limit = data.get('limit', 10)
        
        if not messages:
            return error('メッセージリストを空にすることはできません')
        
        if user_id is None:
            return error('ユーザーIDは必須です')
        
        client = LLMClient(model_name)
        keywords = client.extract_keywords(messages)
        
        # キーワードが抽出できない、または空の結果が返された場合のフォールバック
        if not keywords or (isinstance(keywords, list) and keywords[0] == '空字符串'):
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": [],
                "message": "対話から有効なスポットキーワードを抽出できなかったため、興味のありそうなスポットを推薦します",
                "isHistoryItems": True
            })
        
        query = """
        MATCH (i:Item)
        MATCH (creator:User)-[:CREATED]->(i)
        MATCH (i)-[:BELONGS_TO]->(c:Category)
        OPTIONAL MATCH (i)-[:HAS_TAG]->(t:Tag)
        WITH i, creator, c, COLLECT(DISTINCT t.name) AS tagNames
        WITH i, creator, c, tagNames,
             (
                 ANY(keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword)))
             ) AS keywordMatch
        WHERE keywordMatch
        WITH i, creator, c, tagNames,
             SIZE([(i)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
             (
                 SIZE([keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)]) * 3.0 +
                 SIZE([keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword))]) * 2.5
             ) AS matchScore
        WITH i, creator, c, tagNames, (matchScore * 10 + popularity * 0.1) AS totalScore
        RETURN i.id AS id, i.title AS title, i.description AS description, 
               i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
               creator.username AS username, creator.realName AS userRealName,
               c.name AS categoryName, tagNames, totalScore AS score
        ORDER BY score DESC
        LIMIT $limit
        """
        
        params = {
            "keywords": keywords,
            "userId": user_id,
            "limit": limit
        }
        
        items = neo4j_client.execute_query(query, params)
        
        if not items:
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": keywords,
                "message": f"{', '.join(keywords)} に関連する新しいスポットが見つからなかったため、興味のありそうなスポットを推薦します",
                "isHistoryItems": True
            })
        
        for item in items:
            original_tags = []
            if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
            
            tag_names = item.get("tagNames", [])
            all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
            item["tags"] = all_tags
            
            if "tagNames" in item:
                del item["tagNames"]
        
        return success({
            "items": items,
            "keywords": keywords,
            "message": "お客様の好みに基づいて、以下のスポットを推薦します",
            "isHistoryItems": False
        })
    except Exception as e:
        return error(f'対話ベースの推薦生成に失敗しました: {str(e)}')

def get_user_history_items(user_id: int, limit: int = 10) -> list:
    """ユーザーの過去のインタラクション履歴からスポットを取得する"""
    query = """
    MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)-[:BELONGS_TO]->(c:Category)
    MATCH (creator:User)-[:CREATED]->(i)
    OPTIONAL MATCH (i)-[:HAS_TAG]->(tag:Tag)
    
    WITH i, creator, c, r, TYPE(r) AS interactionType,
         CASE TYPE(r)
           WHEN 'PURCHASED' THEN 4  
           WHEN 'FAVORITED' THEN 3  
           WHEN 'LIKED' THEN 2      
           WHEN 'VIEWED' THEN 1     
           ELSE 0
         END AS typeWeight,
         r.createTime AS interactionTime,
         COLLECT(DISTINCT tag.name) AS tagNames
    
    WITH i.id AS itemId, i, creator, c, tagNames,
         COLLECT({
           type: interactionType,
           weight: typeWeight,
           time: interactionTime,
           label: CASE interactionType
             WHEN 'PURCHASED' THEN '予約済み'
             WHEN 'FAVORITED' THEN 'お気に入り済み'
             WHEN 'LIKED' THEN 'いいね済み'
             WHEN 'VIEWED' THEN '閲覧済み'
             ELSE ''
           END
         }) AS interactions
    
    WITH itemId, i, creator, c, tagNames, interactions,
         REDUCE(maxWeight = 0, interaction IN interactions | 
           CASE WHEN interaction.weight > maxWeight THEN interaction.weight ELSE maxWeight END) AS maxWeight,
         REDUCE(latestTime = datetime(), interaction IN interactions | 
           CASE WHEN interaction.time > latestTime THEN interaction.time ELSE latestTime END) AS latestTime
    
    ORDER BY maxWeight DESC, latestTime DESC
    
    RETURN itemId AS id, i.title AS title, i.description AS description, 
           i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
           creator.username AS username, creator.realName AS userRealName,
           c.name AS categoryName, tagNames,
           interactions AS allInteractions,
           REDUCE(labels = [], interaction IN interactions | 
             CASE WHEN interaction.label IN labels THEN labels ELSE labels + [interaction.label] END
           ) AS interactionLabels
    LIMIT $limit
    """
    
    params = {"userId": user_id, "limit": limit}
    
    try:
        items = neo4j_client.execute_query(query, params)
        for item in items:
            original_tags = []
            if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
            
            tag_names = item.get("tagNames", [])
            all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
            item["tags"] = all_tags
            
            if "tagNames" in item:
                del item["tagNames"]
            
            all_interactions = item.get("allInteractions", [])
            interaction_labels = item.get("interactionLabels", [])
            
            if all_interactions:
                primary_interaction = max(all_interactions, key=lambda x: x.get("weight", 0))
                item["interactionType"] = primary_interaction.get("type", "")
                item["interactionLabel"] = primary_interaction.get("label", "")
            else:
                item["interactionType"] = ""
                item["interactionLabel"] = ""
            
            if len(interaction_labels) == 1:
                item["interactionDescription"] = f"以前、{interaction_labels[0]}"
            elif len(interaction_labels) > 1:
                # 複数のアクションがある場合にラベルを結合する（例：「予約・お気に入り・閲覧済み」）
                combined_labels = '・'.join([label.replace('済み', '') for label in interaction_labels])
                item["interactionDescription"] = f"以前、{combined_labels}済み"
            else:
                item["interactionDescription"] = "以前にインタラクションあり"
            
            if "allInteractions" in item:
                del item["allInteractions"]
            if "interactionLabels" in item:
                del item["interactionLabels"]
            item["score"] = 0
        
        return items
    except Exception as e:
        print(f"ユーザー履歴の取得に失敗しました: {str(e)}")
        return []

@llm_bp.route('/chat-with-graph-rag', methods=['POST'])
def chat_with_graph_rag():
    """GraphRAGを利用した拡張対話インターフェース"""
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        user_id = data.get('userId')
        
        if not messages:
            return error('メッセージリストを空にすることはできません')
        
        client = LLMClient(model_name)
        
        # デフォルトの推奨件数
        target_count = 5 
        last_content = messages[-1].get('content', '') if messages else ''
        
        # ユーザーのリクエストから件数を抽出（正規表現による数字と単位の検索）
        num_match = re.search(r'(\d+)\s*[個处家条座名大]', last_content)
        if not num_match:
            num_match = re.search(r'推薦\s*(\d+)', last_content)
            
        if num_match:
            target_count = min(int(num_match.group(1)), 20)
        else:
            # 漢数字の処理（中国語入力への後方互換性維持のためキーは維持）
            cn_nums = {'一':1, '两':2, '二':2, '三':3, '四':4, '五':5, '六':6, '七':7, '八':8, '九':9, '十':10, '十五':15, '二十':20}
            for cn, val in cn_nums.items():
                if f"{cn}个" in last_content or f"{cn}处" in last_content or f"推薦{cn}" in last_content:
                    target_count = val
                    break
        
        extracted = client.extract_keywords(messages)
        if isinstance(extracted, str):
            extracted = [extracted]
        
        # 検索ノイズとなるストップワードをフィルタリング
        stop_words = {'景点', '地方', '推荐', '旅游', '游玩', '打卡', '空字符串', '日本', '哪里', '好玩'}
        original_keywords = [k for k in (extracted or []) if k and k not in stop_words]
        
        user_preferences = ""
        top_tags = []
        
        # ユーザー履歴に基づくパーソナライズ
        if user_id and str(user_id).strip() != '':
            user_id = int(user_id)
            history_items = get_user_history_items(user_id, limit=20)
            
            if history_items:
                all_tags = []
                for item in history_items:
                    if item.get('tags'):
                        all_tags.extend(item['tags'])
                
                if all_tags:
                    from collections import Counter
                    top_tags = [tag for tag, count in Counter(all_tags).most_common(2)]
                    user_preferences = f"【現在のユーザーの履歴に基づく好み】: {', '.join(top_tags)}"
        
        # グラフコンテキストの構築
        graph_context, search_info = _build_graph_context_with_info(
            original_keywords=original_keywords, 
            top_tags=top_tags,
            user_preferences=user_preferences, 
            target_count=target_count,
            user_id=user_id
        )
        
        search_info['original_keywords'] = original_keywords
        search_info['injected_tags'] = top_tags
        
        # RAGコンテキストを注入してLLMを実行
        response = client.chat_with_graph_rag(messages, graph_context)
        
        return success({
            'response': response,
            'searchInfo': search_info
        })
    
    except Exception as e:
        return error(f'GraphRAG対話に失敗しました: {str(e)}')

def _build_graph_context_with_info(original_keywords: List[str], top_tags: List[str], user_preferences: str = "", target_count: int = 5, user_id: int = None) -> tuple[str, dict]:
    """グラフデータベースからコンテキスト情報を構築し、検索詳細を返す"""
    context_parts = []
    
    search_info = {
        'keywords': original_keywords,                                  
        'user_preferences': user_preferences,                  
        'found_items_count': 0,
        'search_status': 'no_keywords',
        'message': '',
        'raw_graph_data': [],                                  
        'final_injected_context': ''                            
    }
    
    # ユーザーが既にお気に入りに登録しているスポットを除外または低優先にするため取得
    favorited_item_ids = set()
    if user_id:
        try:
            fav_query = "MATCH (u:User {id: $userId})-[r:FAVORITED]->(i:Item) RETURN i.id AS itemId"
            fav_results = neo4j_client.execute_query(fav_query, {"userId": int(user_id)})
            if fav_results:
                favorited_item_ids = {str(res.get('itemId')) for res in fav_results if res.get('itemId') is not None}
        except Exception as e:
            print(f"お気に入り記録の照会に失敗しました: {e}")

    # ==========================================
    # 🚀 分離型グラフ検索戦略（AND検索による結果ゼロを回避）
    # ==========================================
    related_items = []
    
    # 戦略1：明確な検索ワードがある場合、それを強条件として優先（現在の意図を反映）
    if original_keywords:
        related_items = neo4j_client.search_related_items(original_keywords, limit=50)
        
    # 戦略2：検索ワードがない、または結果が得られない場合、履歴の好みから個別に取得
    if (not original_keywords or not related_items) and top_tags:
        for tag in top_tags:
            tag_items = neo4j_client.search_related_items([tag], limit=30)
            if tag_items:
                related_items.extend(tag_items)
                
    # 戦略3：フォールバック：何も見つからない場合、一般的な観光ワードで補完
    if not related_items:
        related_items = neo4j_client.search_related_items(["日本", "風景", "人気"], limit=30)

    # 重複排除処理
    unique_items = {}
    if related_items:
        for item in related_items:
            item_id = str(item.get('id'))
            if item_id and item_id not in unique_items:
                unique_items[item_id] = item
        candidate_pool = list(unique_items.values())
    else:
        candidate_pool = []
    
    search_info['found_items_count'] = len(candidate_pool)

    if user_preferences:
        context_parts.append("### 💡 ユーザープロファイルとパーソナライズされた好み ###")
        context_parts.append(user_preferences)
        context_parts.append("【推薦の原則】: 今回の明示的なリクエストを最優先し、歴史的嗜好をプラスアルファの加点要素として考慮しています。\n")
    
    if candidate_pool:
        valid_original_kws = [k.lower() for k in original_keywords if k.strip()]
        valid_top_tags = [t.lower() for t in top_tags if t.strip()]
        
        for item in candidate_pool:
            item_text = f"{item.get('title','')} {item.get('description','')} {item.get('categoryName','')} {' '.join(item.get('tags',[]))}".lower()
            
            # 1. コア意図のスコアリング
            current_score = 0
            for kw in valid_original_kws:
                if kw in item_text:
                    current_score += 100
                    
            # 2. 補助的な好みのスコアリング
            pref_score = 0
            for tag in valid_top_tags:
                if tag in item_text:
                    pref_score += 15
            
            base_score = item.get('relevanceScore', 0)
            
            # 3. キーワード不一致のフィルタリング
            if valid_original_kws and current_score == 0:
                final_weight = 0.1
            else:
                final_weight = current_score + pref_score + base_score
                
            # 4. 既にお気に入り済みのスポットは優先度を下げる
            if str(item.get('id')) in favorited_item_ids:
                final_weight = final_weight * 0.1
                
            item['custom_weight'] = final_weight
        
        # 加重サンプリングによる選出
        sample_size = min(target_count, len(candidate_pool))
        selected_items = []
        
        for _ in range(sample_size):
            if not candidate_pool:
                break
            weights = [max(item.get('custom_weight', 0.1), 0.1) for item in candidate_pool]
            chosen_item = random.choices(candidate_pool, weights=weights, k=1)[0]
            selected_items.append(chosen_item)
            candidate_pool.remove(chosen_item)
            
        search_info['raw_graph_data'] = selected_items
        search_info['search_status'] = 'found'
        search_info['message'] = f"意図加重アルゴリズムに基づき、{search_info['found_items_count']} 件がヒット。その中から {len(selected_items)} 件を厳選。"
        
        context_parts.append("### 関連観光スポット情報 (ローカルナレッジグラフより) ###")
        for i, item in enumerate(selected_items, 1):
            context_parts.append(f"{i}. {item['title']}")
            context_parts.append(f"   カテゴリ: {item.get('categoryName', '未分類')}")
            context_parts.append(f"   説明: {item.get('description', '説明なし')[:100]}...")
            if item.get('tags'):
                context_parts.append(f"   タグ: {', '.join(item['tags'][:5])}")
            context_parts.append("")
            
        context_parts.append("【出力指示】（厳守すること）:")
        context_parts.append(f"1. 【正確な件数の報告】: システムは正確に {len(selected_items)} 件のスポットを検索しました。回答の冒頭で「{len(selected_items)} 件のスポットを厳選しました」と明記し、リストにあるものをすべて紹介してください。")
        context_parts.append(f"2. 【過度な拡張の禁止】: ユーザーの希望件数は {target_count} 件です。提供されたリストの件数が希望件数以上であれば、自分の知識から新しいスポットを補完してはいけません。")
        context_parts.append("3. 【パーソナライズ紹介】: リストのスポットを紹介する際は、今回のリクエストに対する魅力を重点的に伝え、好みに無理やり結びつけないようにしてください。")
        context_parts.append(f"4. 【不足時のみ補完】: 提供リストが {target_count} 件に満たない場合のみ、自身の知識で不足分を補い、それを【AIによる追加推薦】として明記してください。")
        
    else:
        search_info['search_status'] = 'not_found_fallback'
        search_info['message'] = "グラフデータベースに関連記録が見つかりませんでした"
        
        context_parts.append("### システム通知 ###")
        context_parts.append("注意：システムのナレッジグラフに該当する記録が見つかりませんでした。")
        
        context_parts.append("【指示】:")
        context_parts.append("まず丁寧に「申し訳ございませんが、システムのナレッジグラフにはご要望に合致する記録がありませんでした」と伝えてください。")
        context_parts.append("その上で、自身の一般的な知識（General Knowledge）を活用して、ユーザーのニーズに合う最適なスポットを推薦してください。")
        context_parts.append("")
        
    if original_keywords:
        context_parts.append("### 今回の検索キーワード ###")
        context_parts.append(f"抽出された検索意図: {', '.join(original_keywords)}")
        context_parts.append("")
    
    context = "\n".join(context_parts)
    search_info['final_injected_context'] = context
    
    return context, search_info