"""
タグ抽出サービス - LLMを使用して観光スポットの説明からタグを抽出する
"""
import logging
from typing import List, Dict, Any, Optional
from algo.llm.llm_client import LLMClient
from algo.llm.config import DEFAULT_MODEL

logger = logging.getLogger(__name__)

class TagExtractorService:
    """タグ抽出サービス"""
    
    def __init__(self, model_name: str = DEFAULT_MODEL):
        """
        タグ抽出サービスの初期化
        
        Args:
            model_name: 使用するLLMモデル名
        """
        self.llm_client = LLMClient(model_name)
    
    def extract_tags_from_attraction(self, attraction_title: str, attraction_description: str, 
                                   category_name: str, category_description: str,
                                   original_tags: Optional[str] = None) -> List[str]:
        """
        観光スポット情報からタグを抽出する
        
        Args:
            attraction_title: スポットのタイトル
            attraction_description: スポットの説明
            category_name: カテゴリ名
            category_description: カテゴリの説明
            original_tags: 元のタグ文字列（カンマ区切り）
            
        Returns:
            抽出されたタグのリスト
        """
        try:
            # システムプロンプトを構築
            system_message = {
                "role": "system",
                "content": """你是旅游景点标签抽取专家。从景点信息中抽取5-12个关键标签，用于推荐和分类。

规则：
- 标签简洁（2-4字）
- 优先：地理位置、景点类型、特色、体验
- 避免重复
- 保留有价值的原始标签

只返回标签，用逗号分隔。"""
            }
            
            # ユーザーメッセージを構築
            user_content = f"""景点信息：
景点名称：{attraction_title}
景点描述：{attraction_description}
所属类别：{category_name}
类别描述：{category_description}"""
            
            if original_tags:
                user_content += f"\n原标签：{original_tags}"
            
            user_content += "\n\n抽取标签："
            
            user_message = {
                "role": "user",
                "content": user_content
            }
            
            # LLMを呼び出してタグを抽出
            messages = [system_message, user_message]
            response = self.llm_client.chat(messages)
            
            # タグを解析
            if response and isinstance(response, str):
                tags = [tag.strip() for tag in response.split(',') if tag.strip()]
                # 重複除去と件数制限
                unique_tags = list(dict.fromkeys(tags))[:12]  # 順序を保持したまま重複削除、最大12件
                
                logger.info(f"スポット '{attraction_title}' から {len(unique_tags)} 件のタグを抽出しました: {unique_tags}")
                return unique_tags
            else:
                logger.warning(f"LLMが空のレスポンスを返しました。スポット: {attraction_title}")
                return []
                
        except Exception as e:
            logger.error(f"タグ抽出に失敗しました。スポット: {attraction_title}, エラー: {str(e)}")
            return []
    
    def extract_tags_batch(self, attractions: List[Dict]) -> Dict[int, List[str]]:
        """
        タグを一括抽出する
        
        Args:
            attractions: スポットリスト。各要素は id, title, description, category_name, category_description, tags を含む
            
        Returns:
            スポットIDからタグリストへのマッピング
        """
        result = {}
        
        for attraction in attractions:
            attraction_id = attraction.get('id')
            if not attraction_id:
                continue
                
            tags = self.extract_tags_from_attraction(
                attraction_title=attraction.get('title', ''),
                attraction_description=attraction.get('description', ''),
                category_name=attraction.get('category_name', ''),
                category_description=attraction.get('category_description', ''),
                original_tags=attraction.get('tags', '')
            )
            
            if tags:
                result[attraction_id] = tags
        
        logger.info(f"一括抽出完了。{len(result)} 件のスポットを正常に処理しました")
        return result
    
    def create_tag_nodes_data(self, tag_mappings: Dict[int, List[str]]) -> tuple[List[Dict], List[Dict]]:
        """
        タグノードデータとリレーションシップデータを作成する
        
        Args:
            tag_mappings: スポットIDからタグリストへのマッピング
            
        Returns:
            (タグノードデータリスト, リレーションシップデータリスト)
        """
        tag_nodes = []
        relationships = []
        tag_id_counter = 1
        tag_name_to_id = {}
        
        for attraction_id, tags in tag_mappings.items():
            for tag_name in tags:
                # 各タグのノードを作成（存在しない場合）
                if tag_name not in tag_name_to_id:
                    tag_id = tag_id_counter
                    tag_name_to_id[tag_name] = tag_id
                    tag_id_counter += 1
                    
                    tag_nodes.append({
                        'id': tag_id,
                        'name': tag_name,
                        'type': 'llm_extracted',  # LLMによって抽出されたタグであることを示す
                        'createTime': 'datetime()',
                        'updateTime': 'datetime()'
                    })
                else:
                    tag_id = tag_name_to_id[tag_name]
                
                # スポットとタグのリレーションシップを作成
                relationships.append({
                    'type': 'HAS_TAG',
                    'itemId': attraction_id,
                    'tagId': tag_id,
                    'createTime': 'datetime()'
                })
        
        logger.info(f"{len(tag_nodes)} 件のタグノードと {len(relationships)} 件のリレーションシップを作成しました")
        return tag_nodes, relationships

# グローバルインスタンスを作成
tag_extractor_service = TagExtractorService()
