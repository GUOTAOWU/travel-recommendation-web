"""
ビジネスサービスクライアント
"""
import requests
from config import SPRINGBOOT_SERVER_URL, INTERNAL_AUTH_TOKEN
from typing import Optional, Any, Dict

class SpringbootClient:
    """ビジネスサービスクライアント"""
    
    def __init__(self):
        # バックエンドサービスのアドレス（バックエンドとの結合テスト済み、変更禁止）
        self.server_url = SPRINGBOOT_SERVER_URL
        # 内部認証トークン（バックエンドとの結合テスト済み、変更禁止）
        self.headers = {
            'X-Internal-Auth': INTERNAL_AUTH_TOKEN,
            'Content-Type': 'application/json'
        }
    
    def _handle_response(self, response: requests.Response) -> Any:
        """
        レスポンス結果の処理
        
        Args:
            response: レスポンスオブジェクト
            
        Returns:
            Any: レスポンスデータ
            
        Raises:
            Exception: リクエスト失敗時に例外をスロー
        """
        if response.status_code == 200:
            result = response.json()
            if result['code'] == 200:
                return result.get('data')
            raise Exception(result['msg'])
        raise Exception(f"リクエスト失敗: HTTP {response.status_code}")

    def get(self, path: str, params: Optional[Dict] = None) -> Any:
        """
        GETリクエストを送信
        
        Args:
            path: リクエストパス
            params: クエリパラメータ
            
        Returns:
            Any: レスポンスデータ
            
        Raises:
            Exception: リクエスト失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}{path}"
            response = requests.get(url, params=params, headers=self.headers)
            return self._handle_response(response)
        except Exception as e:
            raise Exception(f"GETリクエスト失敗: {str(e)}")

    def post(self, path: str, json: Optional[Dict] = None, data: Optional[Dict] = None) -> Any:
        """
        POSTリクエストを送信
        
        Args:
            path: リクエストパス
            json: JSONデータ
            data: フォームデータ
            
        Returns:
            Any: レスポンスデータ
            
        Raises:
            Exception: リクエスト失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}{path}"
            response = requests.post(url, json=json, data=data, headers=self.headers)
            return self._handle_response(response)
        except Exception as e:
            raise Exception(f"POSTリクエスト失敗: {str(e)}")

    def put(self, path: str, json: Optional[Dict] = None) -> Any:
        """
        PUTリクエストを送信
        
        Args:
            path: リクエストパス
            json: JSONデータ
            
        Returns:
            Any: レスポンスデータ
            
        Raises:
            Exception: リクエスト失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}{path}"
            response = requests.put(url, json=json, headers=self.headers)
            return self._handle_response(response)
        except Exception as e:
            raise Exception(f"PUTリクエスト失敗: {str(e)}")

    def delete(self, path: str, json: Optional[Dict] = None) -> Any:
        """
        DELETEリクエストを送信
        
        Args:
            path: リクエストパス
            json: JSONデータ
            
        Returns:
            Any: レスポンスデータ
            
        Raises:
            Exception: リクエスト失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}{path}"
            response = requests.delete(url, json=json, headers=self.headers)
            return self._handle_response(response)
        except Exception as e:
            raise Exception(f"DELETEリクエスト失敗: {str(e)}")

# グローバルインスタンスの作成
springboot_client = SpringbootClient()