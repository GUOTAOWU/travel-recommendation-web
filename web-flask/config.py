"""
配置文件
"""
import os
import sys

def get_application_root():
    """获取应用程序根目录"""
    if getattr(sys, 'frozen', False):
        # 如果是打包后的可执行文件
        return os.path.dirname(sys.executable)
    else:
        # 如果是开发环境
        return os.path.dirname(__file__)

# 服务配置
HOST = os.environ.get('FLASK_SERVICE_HOST', 'localhost')
PORT = int(os.environ.get('FLASK_SERVICE_PORT', 5000))

# 文件服务配置
FILE_SERVER_HOST = os.environ.get('FILE_SERVICE_HOST', 'localhost')
FILE_SERVER_PORT = int(os.environ.get('FILE_SERVICE_PORT', 5001))
FILE_SERVER_URL = f'http://{FILE_SERVER_HOST}:{FILE_SERVER_PORT}/api/file'

# 业务服务配置
SPRINGBOOT_HOST = os.environ.get('SPRINGBOOT_HOST', 'localhost')
SPRINGBOOT_PORT = int(os.environ.get('SPRINGBOOT_PORT', 8080))
SPRINGBOOT_SERVER_URL = f'http://{SPRINGBOOT_HOST}:{SPRINGBOOT_PORT}/api'

# 内部服务认证令牌
INTERNAL_AUTH_TOKEN = 'web-flask-internal-token'

TEMP_DIR = os.environ.get('FLASK_TEMP_DIR', os.path.join(get_application_root(), 'temp'))
os.makedirs(TEMP_DIR, exist_ok=True)  # 创建临时文件目录

