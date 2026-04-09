/**
 * 日付フォーマット用ユーティリティ関数
 */

/**
 * 日付をフォーマットする
 * @param date 日付オブジェクトまたは文字列
 * @param format フォーマット文字列、YYYY-MM-DD HH:mm:ss をサポート
 * @returns フォーマットされた日付文字列
 */
export function formatDate(date: Date | string, format: string = 'YYYY-MM-DD'): string {
  const d = typeof date === 'string' ? new Date(date) : date
  
  const year = d.getFullYear().toString()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const day = d.getDate().toString().padStart(2, '0')
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const seconds = d.getSeconds().toString().padStart(2, '0')
  
  return format
    .replace(/YYYY/g, year)
    .replace(/MM/g, month)
    .replace(/DD/g, day)
    .replace(/HH/g, hours)
    .replace(/mm/g, minutes)
    .replace(/ss/g, seconds)
}

/**
 * 相対時間を計算する（数分前、数時間前、数日前など）
 * @param date 日付オブジェクトまたは文字列
 * @returns 相対時間の文字列
 */
export function relativeTime(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  
  // 時間差の計算
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(months / 12)
  
  if (years > 0) {
    return `${years}年前`
  } else if (months > 0) {
    return `${months}ヶ月前`
  } else if (days > 0) {
    return `${days}日前`
  } else if (hours > 0) {
    return `${hours}時間前`
  } else if (minutes > 0) {
    return `${minutes}分前`
  } else {
    return 'たった今'
  }
}