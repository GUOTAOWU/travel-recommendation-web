"""
LLM設定ファイル
"""

# モデル設定
MODEL_CONFIGS = {
    'qwen-plus': {
        'name': 'Qwen-Plus',
        'api_url': 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
        'api_key': 'sk-79a7c5a6c4e646608f56167c545b1cb1',
        'max_tokens': 2048,
        'temperature': 0.7,
        'model': 'qwen-plus'
    },
    'qwen-max': {
        'name': 'Qwen-Max',
        'api_url': 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
        'api_key': 'sk-79a7c5a6c4e646608f56167c545b1cb1',
        'max_tokens': 4096,
        'temperature': 0.7,
        'model': 'qwen-max'
    },
    'qwen-turbo': {
        'name': 'Qwen-Turbo',
        'api_url': 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
        'api_key': 'sk-79a7c5a6c4e646608f56167c545b1cb1',
        'max_tokens': 2048,
        'temperature': 0.7,
        'model': 'qwen-turbo'
    },
    'deepseek-r1': {
        'name': 'DeepSeek-R1',
        'api_url': 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
        'api_key': 'sk-79a7c5a6c4e646608f56167c545b1cb1',
        'max_tokens': 4096,
        'temperature': 0.7,
        'model': 'deepseek-r1'
    },
    'deepseek-v3': {
        'name': 'DeepSeek-V3',
        'api_url': 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions',
        'api_key': 'sk-79a7c5a6c4e646608f56167c545b1cb1',
        'max_tokens': 4096,
        'temperature': 0.7,
        'model': 'deepseek-v3'
    }
}

# デフォルトモデル
DEFAULT_MODEL = 'qwen-turbo' 

# 利用可能なモデル一覧
AVAILABLE_MODELS = [
    {'key': key, 'name': config['name']}
    for key, config in MODEL_CONFIGS.items()
] 
