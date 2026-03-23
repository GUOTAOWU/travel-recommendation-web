"""
LLM路由
"""
from flask import Blueprint, request, jsonify, Response, stream_with_context
from typing import List
from algo.llm.llm_client import LLMClient
from algo.llm.config import AVAILABLE_MODELS, DEFAULT_MODEL
from utils.response import success, error
from algo.knowledge_graph.neo4j_client import neo4j_client


llm_bp = Blueprint('llm', __name__, url_prefix='/api/llm')

@llm_bp.route('/models', methods=['GET'])
def get_models():
    """获取可用模型列表
    
    Returns:
        模型列表
    """
    return success(AVAILABLE_MODELS)

@llm_bp.route('/chat', methods=['POST'])
def chat():
    """普通对话接口
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        
    Returns:
        模型返回的文本内容
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        
        if not messages:
            return error('消息列表不能为空')
        
        client = LLMClient(model_name)
        response = client.chat(messages)
        return success(response)
    except Exception as e:
        return error(f'聊天失败: {str(e)}')


@llm_bp.route('/recommend', methods=['POST'])
def recommend_from_chat():
    """基于聊天内容推荐景点
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        userId: 用户ID，必传
        limit: 返回的推荐数量，默认10
        
    Returns:
        推荐景点列表和提取的关键词
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        user_id = data.get('userId')
        limit = data.get('limit', 10)
        
        if not messages:
            return error('消息列表不能为空')
        
        if user_id is None:
            return error('用户ID不能为空')
        
        # 提取关键词
        client = LLMClient(model_name)
        keywords = client.extract_keywords(messages)
        
        # 如果关键词为空，返回用户历史交互景点
        if not keywords or (isinstance(keywords, list) and keywords[0] == '空字符串'):
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": [],
                "message": "未能从对话中提取到有效的景点关键词，为您推荐您可能感兴趣的景点",
                "isHistoryItems": True  # 标识这是历史交互景点
            })
        
        # 基于关键词构建Neo4j查询，利用Tag节点进行更精确的匹配
        query = """
        // 基于关键词匹配景点
        MATCH (i:Item)
        MATCH (creator:User)-[:CREATED]->(i)
        
        // 关联景点所属类别和标签
        MATCH (i)-[:BELONGS_TO]->(c:Category)
        OPTIONAL MATCH (i)-[:HAS_TAG]->(t:Tag)
        
        // 收集标签信息
        WITH i, creator, c, COLLECT(DISTINCT t.name) AS tagNames
        
        // 进行关键词匹配
        WITH i, creator, c, tagNames,
             // 匹配标题、描述、原始标签或Tag节点中包含关键词的景点
             (
                 ANY(keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)) OR
                 ANY(keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword)))
             ) AS keywordMatch
        
        // 只返回匹配关键词的景点
        WHERE keywordMatch
        
        // 计算热门度和匹配度
        WITH i, creator, c, tagNames,
             SIZE([(i)<-[:VIEWED|PURCHASED|FAVORITED|LIKED]-() | true]) AS popularity,
             // 计算关键词匹配度得分
             (
                 SIZE([keyword IN $keywords WHERE toLower(i.title) CONTAINS toLower(keyword)]) * 3.0 +
                 SIZE([keyword IN $keywords WHERE toLower(i.description) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE toLower(coalesce(i.tags, '')) CONTAINS toLower(keyword)]) * 2.0 +
                 SIZE([keyword IN $keywords WHERE ANY(tag IN tagNames WHERE toLower(tag) CONTAINS toLower(keyword))]) * 2.5
             ) AS matchScore
        
        // 计算综合得分
        WITH i, creator, c, tagNames, (matchScore * 10 + popularity * 0.1) AS totalScore
        
        // 返回匹配结果
        RETURN i.id AS id, i.title AS title, i.description AS description, 
               i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
               creator.username AS username, creator.realName AS userRealName,
               c.name AS categoryName, tagNames, totalScore AS score
        ORDER BY score DESC
        LIMIT $limit
        """
        
        # 执行查询
        params = {
            "keywords": keywords,
            "userId": user_id,
            "limit": limit
        }
        
        items = neo4j_client.execute_query(query, params)
        
        # 如果没有找到匹配的景点，返回用户历史交互景点
        if not items:
            history_items = get_user_history_items(user_id, limit)
            return success({
                "items": history_items,
                "keywords": keywords,
                "message": f"没有找到与 {', '.join(keywords)} 相关的新景点，为您推荐了一些您可能感兴趣的景点",
                "isHistoryItems": True  # 标识这是历史交互景点
            })
        
        # 处理标签字段，合并原始标签和Tag节点标签
        for item in items:
            original_tags = []
            if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
            
            # 合并原始标签和Tag节点标签
            tag_names = item.get("tagNames", [])
            all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
            item["tags"] = all_tags
            
            # 清理tagNames字段
            if "tagNames" in item:
                del item["tagNames"]
        
        return success({
            "items": items,
            "keywords": keywords,
            "message": "根据您的偏好，为您推荐以下景点",
            "isHistoryItems": False  # 标识这是关键词匹配景点
        })
    except Exception as e:
        return error(f'基于对话生成景点推荐失败: {str(e)}')

def get_user_history_items(user_id: int, limit: int = 10) -> list:
    """获取用户历史交互的景点
    
    Args:
        user_id: 用户ID
        limit: 返回数量限制
        
    Returns:
        用户交互过的景点列表，按景点ID去重，合并交互类型
    """
    # 查询用户交互过的景点，按景点ID分组，合并交互类型
    query = """
    // 查找用户交互过的景点
    MATCH (u:User {id: $userId})-[r:VIEWED|PURCHASED|FAVORITED|LIKED]->(i:Item)-[:BELONGS_TO]->(c:Category)
    MATCH (creator:User)-[:CREATED]->(i)
    
    // 获取Tag节点信息
    OPTIONAL MATCH (i)-[:HAS_TAG]->(tag:Tag)
    
    WITH i, creator, c, r, TYPE(r) AS interactionType,
         CASE TYPE(r)
           WHEN 'PURCHASED' THEN 4  // 预约权重最高
           WHEN 'FAVORITED' THEN 3  // 收藏次之
           WHEN 'LIKED' THEN 2      // 点赞再次
           WHEN 'VIEWED' THEN 1     // 浏览权重最低
           ELSE 0
         END AS typeWeight,
         r.createTime AS interactionTime,
         COLLECT(DISTINCT tag.name) AS tagNames
    
    // 按景点ID分组，收集所有交互类型
    WITH i.id AS itemId, i, creator, c, tagNames,
         COLLECT({
           type: interactionType,
           weight: typeWeight,
           time: interactionTime,
           label: CASE interactionType
             WHEN 'PURCHASED' THEN '您预约过'
             WHEN 'FAVORITED' THEN '您收藏过'
             WHEN 'LIKED' THEN '您点赞过'
             WHEN 'VIEWED' THEN '您浏览过'
             ELSE ''
           END
         }) AS interactions
    
    // 计算最高权重和最新时间
    WITH itemId, i, creator, c, tagNames, interactions,
         REDUCE(maxWeight = 0, interaction IN interactions | 
           CASE WHEN interaction.weight > maxWeight THEN interaction.weight ELSE maxWeight END) AS maxWeight,
         REDUCE(latestTime = datetime(), interaction IN interactions | 
           CASE WHEN interaction.time > latestTime THEN interaction.time ELSE latestTime END) AS latestTime
    
    // 按最高权重和最新时间排序
    ORDER BY maxWeight DESC, latestTime DESC
    
    // 返回结果，合并交互类型信息
    RETURN itemId AS id, i.title AS title, i.description AS description, 
           i.tags AS tags, i.coverBucket AS coverBucket, i.coverObjectKey AS coverObjectKey,
           creator.username AS username, creator.realName AS userRealName,
           c.name AS categoryName, tagNames,
           interactions AS allInteractions,
           // 生成合并的交互标签
           REDUCE(labels = [], interaction IN interactions | 
             CASE WHEN interaction.label IN labels THEN labels ELSE labels + [interaction.label] END
           ) AS interactionLabels
    LIMIT $limit
    """
    
    params = {"userId": user_id, "limit": limit}
    
    try:
        items = neo4j_client.execute_query(query, params)
        
        # 处理标签字段，合并原始标签和Tag节点标签
        for item in items:
            original_tags = []
            if "tags" in item and item["tags"] and isinstance(item["tags"], str):
                original_tags = [tag.strip() for tag in item["tags"].split(",") if tag.strip()]
            
            # 合并原始标签和Tag节点标签
            tag_names = item.get("tagNames", [])
            all_tags = list(set(original_tags + [tag for tag in tag_names if tag]))
            item["tags"] = all_tags
            
            # 清理tagNames字段
            if "tagNames" in item:
                del item["tagNames"]
            
            # 处理交互类型信息
            all_interactions = item.get("allInteractions", [])
            interaction_labels = item.get("interactionLabels", [])
            
            # 设置主要交互类型（权重最高的）
            if all_interactions:
                primary_interaction = max(all_interactions, key=lambda x: x.get("weight", 0))
                item["interactionType"] = primary_interaction.get("type", "")
                item["interactionLabel"] = primary_interaction.get("label", "")
            else:
                item["interactionType"] = ""
                item["interactionLabel"] = ""
            
            # 生成合并的交互描述
            if len(interaction_labels) == 1:
                item["interactionDescription"] = interaction_labels[0]
            elif len(interaction_labels) > 1:
                # 将多个交互类型合并为一个描述
                item["interactionDescription"] = f"您{'、'.join([label.replace('您', '').replace('过', '') for label in interaction_labels])}过"
            else:
                item["interactionDescription"] = "您交互过"
            
            # 清理临时字段
            if "allInteractions" in item:
                del item["allInteractions"]
            if "interactionLabels" in item:
                del item["interactionLabels"]
            
            # 添加一个score字段，保持与推荐结果格式一致
            item["score"] = 0
        
        return items
    except Exception as e:
        print(f"获取用户历史交互景点失败: {str(e)}")
        return []


@llm_bp.route('/chat-with-graph-rag', methods=['POST'])
def chat_with_graph_rag():
    """使用GraphRAG增强的对话接口
    
    请求体:
        model: 模型名称，可选，默认为DEFAULT_MODEL
        messages: 消息列表，每个消息包含role和content
        
    Returns:
        基于图数据库上下文增强的模型回复和搜索信息
    """
    try:
        data = request.json
        model_name = data.get('model', DEFAULT_MODEL)
        messages = data.get('messages', [])
        
        if not messages:
            return error('消息列表不能为空')
        
        # 初始化LLM客户端
        client = LLMClient(model_name)
        
        # 从历史对话中提取关键词
        keywords = client.extract_keywords(messages)
        
        # 构建图数据库上下文并获取搜索结果信息
        graph_context, search_info = _build_graph_context_with_info(keywords)
        
        # 使用GraphRAG增强对话
        response = client.chat_with_graph_rag(messages, graph_context)
        
        return success({
            'response': response,
            'searchInfo': search_info
        })
    
    except Exception as e:
        return error(f'GraphRAG对话失败: {str(e)}')


def _build_graph_context_with_info(keywords: List[str]) -> tuple[str, dict]:
    """构建图数据库上下文信息并返回搜索详情"""
    context_parts = []
    search_info = {
        'keywords': keywords,
        'found_items': 0,
        'search_status': 'no_keywords',
        'message': ''
    }
    
    if keywords:
        related_items = neo4j_client.search_related_items(keywords, limit=8)
        search_info['found_items'] = len(related_items) if related_items else 0
        
        if related_items:
            # === 场景 1: 图谱中查到了数据（但需要 AI 自行判断是否真的相关） ===
            search_info['search_status'] = 'found'
            search_info['message'] = f"找到 {len(related_items)} 个相关景点"
            
            context_parts.append("### 相关景点信息(来自系统本地数据库) ###")
            for i, item in enumerate(related_items, 1):
                context_parts.append(f"{i}. {item['title']}")
                context_parts.append(f"   类别: {item.get('categoryName', '未分类')}")
                context_parts.append(f"   描述: {item.get('description', '暂无描述')[:100]}...")
                if item.get('tags'):
                    context_parts.append(f"   标签: {', '.join(item['tags'][:5])}")
                context_parts.append("")
                
            # 【终极防呆修改】：赋予大模型“自主判断相关性”的权力，避免强行推荐错误地点
            context_parts.append("【核心回复指令】：请你首先评估上方提供的【相关景点信息】是否与用户提问的真实意图（如特定的城市、国家、主题）高度匹配。")
            context_parts.append("然后，请严格按照以下两种情况之一进行回答：")
            context_parts.append("情况A（如果高度匹配）：")
            context_parts.append("  第一段：你必须说“本系统为您检索到以下本地景点：”，然后介绍高度匹配的本地景点。")
            context_parts.append("  第二段：你必须说“此外，根据我的通用知识，我还为您推荐：”，补充几个不在列表中的相关景点。")
            context_parts.append("情况B（如果不匹配，例如用户问巴黎，但系统提供的是日本或毫不相干的景点）：")
            context_parts.append("  请直接无视系统提供的错误信息，并告诉用户：“抱歉，本系统中暂时没有查到关于该景点的收录记录。不过根据我的知识库，我可以为您介绍：” 然后使用你的通用知识库进行解答。")
            
        else:
            # === 场景 2: 图谱中没找到 ===
            search_info['search_status'] = 'not_found_fallback'
            search_info['message'] = f"库内未收录 {', '.join(keywords)}，切换为通用知识回答"
            
            context_parts.append("### 系统提示 ###")
            context_parts.append(f"注意：系统数据库中暂时没有关于关键词 [{', '.join(keywords)}] 的具体景点记录。")
            
            # 【核心修改】：强制大模型承认系统没查到，然后用通用知识回答
            context_parts.append("【核心回复指令】：请你严格按照以下结构回答用户：")
            context_parts.append("开头第一句，你必须明确说明：“抱歉，本系统中暂时没有查到关于该景点的收录记录。不过根据我的知识库，我可以为您介绍：”")
            context_parts.append("说完这句话后，请直接使用你的通用知识库（General Knowledge）详细回答用户的问题。")
            context_parts.append("")
            
    else:
        # === 场景 3: 没提取到关键词 ===
        search_info['message'] = "未能从对话中提取到有效的旅游关键词"
        context_parts.append("用户似乎没有提到具体的景点或旅游需求，请自然地引导用户提供更多信息。")
    
    if keywords:
        context_parts.append("### 本次搜索关键词 ###")
        context_parts.append(f"提取的关键词: {', '.join(keywords)}")
        context_parts.append("")
    
    context = "\n".join(context_parts)
    return context, search_info