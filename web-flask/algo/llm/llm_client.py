"""
LLM クライアント
"""
import json
import requests
from typing import Dict, Any, List, Optional
from algo.llm.config import MODEL_CONFIGS, DEFAULT_MODEL

class LLMClient:
    """LLM クライアント"""
    def __init__(self, model_name: str = DEFAULT_MODEL):
        """LLM クライアントを初期化する"""
        self.model_name = model_name
        self.config = MODEL_CONFIGS[model_name]
    
    def _make_headers(self) -> Dict:
        """リクエストヘッダーを生成する"""
        return {
            'Authorization': f'Bearer {self.config["api_key"]}',
            'Content-Type': 'application/json'
        }
    
    def chat(self, messages: List[Dict]) -> str:
        """非ストリーミングチャットインターフェース"""
        system_message = {
            "role": "system",
            "content": """あなたはプロの旅行コンサルタント助手です。以下の要件を遵守してください：

1. 旅行、観光スポット、旅行プラン、宿泊、交通、グルメなど、旅行に関連する質問にのみ回答してください。
2. 旅行に関連しない質問（医療、法律相談、技術的な問題、学習指導など）については、丁寧に断り、専門家に相談するよう誘導してください。
3. 政治的に敏感な内容、アダルト、暴力などの有害なコンテンツへの回答は拒否しなければなりません。"""
        }
        full_messages = [system_message] + messages
        
        data = {
            'model': self.config['model'],
            'messages': full_messages,
            'max_tokens': self.config['max_tokens'],
            'temperature': self.config['temperature'],
        }
        
        response = requests.post(self.config['api_url'], json=data, headers=self._make_headers())
        result = response.json()
        return result['choices'][0]['message']['content']
        
    def extract_keywords(self, messages: List[Dict], query: str = "おすすめを教えて") -> List[str]:
        """対話履歴からキーワードを抽出する"""
        
        # システムプロンプトを構築し、LLMにキーワード抽出を指示する（地名を優先）
        system_message = {
            "role": "system",
            "content": """あなたは観光スポット推薦システムのキーワード抽出助手です。ユーザーとの対話からデータベース検索用のキーワードを抽出してください。
            
以下の要件を厳守してください：
1. 【中心地名にフォーカス】：ユーザーの**最新の質問**に含まれる中心的な地理的位置（例：北海道、関西、日本、パリ）や、特定の主要施設（例：ユニバーサル・スタジオ、博物館）を最優先で抽出してください。これが最も重要です。
2. 【形容詞や履歴の優先度を下げる】：ユーザーの個人的な嗜好タグは補助として1〜2個抽出しても構いませんが、中心的な地名を排除することは**絶対に**避けてください。
3. 【数量の制御】：抽出する総数は 2〜4 個の単語が最適です。
4. 【汎用的な語彙の保持】：「観光」「スポット」など、意図を理解するのに役立つ単語の保持を許可します。

簡潔なキーワードのみを返し（括弧などは不要）、カンマで区切ってください。"""
        }
        
        user_content = f"以下は私と旅行助手との対話履歴です。ここから観光スポット推薦システムの検索に使用するキーワードを抽出してください：\n\n"
        
        filtered_messages = []
        for msg in messages:
            content = msg.get('content', '')
            if query in content:
                content_without_query = content.replace(query, '').strip()
                if len(content_without_query) > 2:
                    filtered_messages.append(msg)
            else:
                filtered_messages.append(msg)
        
        for msg in filtered_messages:
            user_content += f"{msg['role']}: {msg['content']}\n"
        
        user_content += "\n観光スポットの推薦に関連するキーワードを抽出し、キーワードのみを返してください（括弧などは不要）。カンマで区切り、他の内容は含めないでください。適切なキーワードが見つからない場合は、空文字列を返してください。"
        
        user_message = {
            "role": "user",
            "content": user_content
        }
        
        data = {
            'model': self.config['model'],
            'messages': [system_message, user_message],
            'max_tokens': 100
        }
        
        response = requests.post(self.config['api_url'], json=data, headers=self._make_headers())
        result = response.json()
        keywords_text = result['choices'][0]['message']['content']
        
        if not keywords_text or keywords_text.strip() == "":
            return []
        
        try:
            keywords = [keyword.strip() for keyword in keywords_text.split(',') if keyword.strip()]
        except Exception as e:
            print(f"キーワードの抽出に失敗しました: {e}")
            return []
        
        return keywords
    
    def chat_with_graph_rag(self, messages: List[Dict], graph_context: str) -> str:
        """グラフデータベースのコンテキストを使用した拡張対話（RAG）"""
        system_message = {
            "role": "system",
            "content": """あなたはプロの旅行コンサルタント助手です。

以下の基本規律に注意してください：
1. 回答は自然、専門的かつ友好的で、旅行相談の文脈に適したものにしてください。
2. 政治的に敏感な内容、アダルト、暴力、違法行為などの有害なコンテンツへの回答は拒否しなければなりません。
3. 旅行関連の内容に専念し、旅行以外の質問（医療、法律など）については専門家に相談するよう誘導してください。

【最高実行戦略】：
これ以降の対話では、システムから注入された【コンテキストと実行命令】を受け取ります。
注入された命令で要求される「段落構造」と「ヒントメッセージ（システム検索結果とあなたの一般的知識を明確に区別すること）」に**厳格に従って**回答してください。

{graph_context}
"""
        }
        
        formatted_context = self._format_graph_context(graph_context)
        system_message["content"] = system_message["content"].format(graph_context=formatted_context)
        
        full_messages = [system_message] + messages
        
        data = {
            'model': self.config['model'],
            'messages': full_messages,
            'max_tokens': self.config['max_tokens'],
            'temperature': self.config['temperature'],
        }
        
        response = requests.post(self.config['api_url'], json=data, headers=self._make_headers())
        result = response.json()
        return result['choices'][0]['message']['content']
    
    def _format_graph_context(self, graph_context: str) -> str:
        """グラフデータベースのコンテキスト情報をフォーマットする"""
        if not graph_context or "未找到与关键词" in graph_context:
            return "【データベース検索結果】：関連する観光スポット情報が見つかりませんでした。"
        
        return f"""
=== 関連観光スポット情報 ===
{graph_context}
"""