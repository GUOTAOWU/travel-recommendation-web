// 高徳地図（AMap）の設定
export const mapConfig = {
  // 高徳地図 Web端 APIキー
  // 高徳オープンプラットフォームで申請してください：https://console.amap.com/dev/key/app
  key: '3d4c3bf2ed77b37809aefa1e9b3b64ec',
  
  // 地図バージョン
  version: '2.0',
  
  // 地図プラグイン
  plugins: [
    'AMap.Geocoder',    // ジオコーディング（住所検索）プラグイン
    'AMap.InfoWindow',  // 情報ウィンドウプラグイン
    'AMap.ToolBar',     // ツールバープラグイン
    'AMap.Scale'        // スケール（比例尺）プラグイン
  ],
  
  // デフォルトのズームレベル
  defaultZoom: 10,
  
  // 地図のスタイル
  mapStyle: 'amap://styles/normal'
}