from flask import Blueprint
from utils.response import success


health_bp = Blueprint('health_bp', __name__, url_prefix='/api/health')

@health_bp.route('/health_check', methods=['GET'])
def health_check():
    """ヘルスチェックインターフェース"""
    data = {
        'status': 'ok',
        'service': 'algorithm-service',
        'version': '1.0.0'
    }
    return success(data, msg='サービス状態チェックに成功しました')