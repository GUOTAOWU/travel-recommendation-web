"""
推薦システムのルーティング
"""
from flask import Blueprint, request
from algo.knowledge_graph.recommender import kg_recommender
from utils.response import success, error

# URLプレフィックスには一律で /api を追加する必要があります
recommendation_bp = Blueprint('recommendation', __name__, url_prefix='/api/recommendation')

@recommendation_bp.route('/user/<int:user_id>', methods=['GET'])
def recommend_for_user(user_id: int):
    """
    ユーザーにスポットを推薦する（ユーザーベース）
    
    Args:
        user_id: ユーザーID
        
    Returns:
        推薦スポットリスト
    """
    try:
        # リクエストパラメータの取得
        limit = request.args.get('limit', default=10, type=int)
        
        # 推薦アルゴリズムの呼び出し
        items = kg_recommender.recommend_by_user_id(user_id, limit)
        
        return success(data=items, msg=f'ユーザー {user_id} に対して {len(items)} 件の協調フィルタリング推薦を生成しました')
    except Exception as e:
        return error(msg=f'推薦の生成に失敗しました: {str(e)}', code=500)

@recommendation_bp.route('/user/<int:user_id>/content', methods=['GET'])
def recommend_content_for_user(user_id: int):
    """
    ユーザーにスポットを推薦する（コンテンツベース）
    
    Args:
        user_id: ユーザーID
        
    Returns:
        推薦スポットリスト
    """
    try:
        # リクエストパラメータの取得
        limit = request.args.get('limit', default=10, type=int)
        
        # 推薦アルゴリズムの呼び出し
        items = kg_recommender.recommend_by_content(user_id, limit)
        
        return success(data=items, msg=f'ユーザー {user_id} に対して {len(items)} 件のコンテンツベースの推薦を生成しました')
    except Exception as e:
        return error(msg=f'推薦の生成に失敗しました: {str(e)}', code=500)
