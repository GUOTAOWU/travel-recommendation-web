from flask import Response, json
from typing import Any

def api_response(
    data: Any = None,
    code: int = 200,
    msg: str = "success"
) -> Response:
    """
    統一されたAPIレスポンス形式
    
    Args:
        data: レスポンスデータ
        code: ステータスコード
        msg: メッセージ
    
    Returns:
        統一形式のレスポンスオブジェクト
    """
    response = {
        'code': code,
        'msg': msg,
        'data': data
    }
    return Response(
        json.dumps(response, ensure_ascii=False),
        content_type='application/json; charset=utf-8'
    )

def success(data=None, msg='操作に成功しました'):
    """
    成功レスポンス
    :param data: レスポンスデータ
    :param msg: メッセージ
    :return: JSONレスポンス
    """
    return api_response(data=data, code=200, msg=msg)

def error(msg='操作に失敗しました', code=500, data=None):
    """
    エラーレスポンス
    :param msg: エラーメッセージ
    :param code: エラーコード
    :param data: レスポンスデータ
    :return: JSONレスポンス
    """
    return api_response(data=data, code=code, msg=msg) 