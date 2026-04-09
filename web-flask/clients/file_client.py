"""
ファイルサービスクライアント
"""
import os
import requests
import mimetypes
from config import FILE_SERVER_URL

class FileClient:
    """ファイルサービスクライアント"""
    
    def __init__(self):
        self.server_url = FILE_SERVER_URL
        
    def upload(self, bucket: str, file_path: str, is_cache: bool = False) -> dict:
        """
        ファイルをアップロードする
        
        Args:
            bucket: バケット名
            file_path: ファイルパス
            is_cache: キャッシュするかどうか
            
        Returns:
            dict: {
                'url': ファイルURL,
                'bucket': バケット名,
                'objectKey': オブジェクトキー名
            }
            
        Raises:
            Exception: アップロード失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}/upload/{bucket}"
            
            # ファイルのMIMEタイプを取得
            mime_type, _ = mimetypes.guess_type(file_path)
            # ファイル拡張子とMIMEタイプを出力
            print(f"ファイル拡張子: {os.path.splitext(file_path)[1].lower()}")
            print(f"MIMEタイプ: {mime_type}")
            if not mime_type:
                # ファイル拡張子に基づいてMIMEタイプを設定
                ext = os.path.splitext(file_path)[1].lower()
                mime_map = {
                    '.tif': 'image/tiff',
                    '.tiff': 'image/tiff',
                    '.png': 'image/png',
                    '.jpg': 'image/jpeg',
                    '.jpeg': 'image/jpeg',
                    '.gif': 'image/gif',
                    '.bmp': 'image/bmp',
                    '.pdf': 'application/pdf',
                    '.json': 'application/json',
                    '.txt': 'text/plain'
                }
                mime_type = mime_map.get(ext, 'application/octet-stream')
            
            # ファイルを読み込む
            with open(file_path, 'rb') as f:
                files = {
                    'file': (os.path.basename(file_path), f, mime_type)
                }
                data = {'is_cache': is_cache}
                
                # リクエストを送信
                response = requests.post(url, files=files, data=data)
                
            # レスポンスを確認
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 200:
                    return result['data']
                raise Exception(result['msg'])
            raise Exception(f"アップロード失敗: {response.status_code}")
            
        except Exception as e:
            raise Exception(f"ファイルのアップロードに失敗しました: {str(e)}")
            
    def delete(self, bucket: str, object_key: str) -> None:
        """
        ファイルを削除する
        
        Args:
            bucket: バケット名
            object_key: オブジェクトキー名
            
        Raises:
            Exception: 削除失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}/{bucket}/{object_key}"
            
            # リクエストを送信
            response = requests.delete(url)
            
            # レスポンスを確認
            if response.status_code == 200:
                result = response.json()
                if result['code'] == 200:
                    return
                raise Exception(result['msg'])
            raise Exception(f"削除失敗: {response.status_code}")
            
        except Exception as e:
            raise Exception(f"ファイルの削除に失敗しました: {str(e)}")
            
    def get(self, bucket: str, object_key: str) -> bytes:
        """
        ファイルを取得する
        
        Args:
            bucket: バケット名
            object_key: オブジェクトキー名
            
        Returns:
            bytes: ファイルの内容
            
        Raises:
            Exception: 取得失敗時に例外をスロー
        """
        try:
            url = f"{self.server_url}/{bucket}/{object_key}"
            
            # リクエストを送信
            response = requests.get(url)
            
            # レスポンスを確認
            if response.status_code == 200:
                return response.content
            raise Exception(f"取得失敗: {response.status_code}")
            
        except Exception as e:
            raise Exception(f"ファイルの取得に失敗しました: {str(e)}")
            
    def get_file_url(self, bucket: str, object_key: str) -> str:
        """
        ファイルURLを取得する
        
        Args:
            bucket: バケット名
            object_key: オブジェクトキー名
            
        Returns:
            str: ファイルURL
        """
        return f"{self.server_url}/{bucket}/{object_key}"
        
    def download(self, bucket: str, object_key: str, save_path: str) -> None:
        """
        ファイルをダウンロードする
        
        Args:
            bucket: バケット名
            object_key: オブジェクトキー名
            save_path: 保存先パス
            
        Raises:
            Exception: ダウンロード失敗時に例外をスロー
        """
        try:
            # ファイル内容を取得
            content = self.get(bucket, object_key)
            
            # ディレクトリを作成
            os.makedirs(os.path.dirname(save_path), exist_ok=True)
            
            # ファイルを保存
            with open(save_path, 'wb') as f:
                f.write(content)
                
        except Exception as e:
            raise Exception(f"ファイルのダウンロードに失敗しました: {str(e)}")

# グローバルインスタンスの作成
file_client = FileClient()