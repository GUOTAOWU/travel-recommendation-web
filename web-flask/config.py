"""
設定ファイル
"""
import os
import sys

def get_application_root():
    """アプリケーションのルートディレクトリを取得する"""
    if getattr(sys, 'frozen', False):
        # パッケージ化された実行ファイルの場合
        return os.path.dirname(sys.executable)
    else:
        # 開発環境の場合
        return os.path.dirname(__file__)

# サービス設定
HOST = os.environ.get('FLASK_SERVICE_HOST', 'localhost')
PORT = int(os.environ.get('FLASK_SERVICE_PORT', 5000))

# ファイルサービス設定
FILE_SERVER_HOST = os.environ.get('FILE_SERVICE_HOST', 'localhost')
FILE_SERVER_PORT = int(os.environ.get('FILE_SERVICE_PORT', 5001))
FILE_SERVER_URL = f'http://{FILE_SERVER_HOST}:{FILE_SERVER_PORT}/api/file'

# ビジネスサービス設定
SPRINGBOOT_HOST = os.environ.get('SPRINGBOOT_HOST', 'localhost')
SPRINGBOOT_PORT = int(os.environ.get('SPRINGBOOT_PORT', 8080))
SPRINGBOOT_SERVER_URL = f'http://{SPRINGBOOT_HOST}:{SPRINGBOOT_PORT}/api'

# 内部サービス認証トークン
INTERNAL_AUTH_TOKEN = 'web-flask-internal-token'

TEMP_DIR = os.environ.get('FLASK_TEMP_DIR', os.path.join(get_application_root(), 'temp'))
os.makedirs(TEMP_DIR, exist_ok=True)  # 一時ファイルディレクトリを作成