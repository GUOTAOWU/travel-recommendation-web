import dayjs from 'dayjs'

// 日時をフォーマットする
export function formatDateTime(date?: string) {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 日付をフォーマットする
export const formatDate = (date: string | number | Date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return year + '-' + month + '-' + day
}

// ファイルサイズをフォーマットする（B, KB, MB, GB）
export const formatFileSize = (size: number) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  }
}

// ユーザー権限（ロール）をフォーマットする
export const formatRole = (role: number) => {
  const roles = ['一般ユーザー', '管理者']
  return roles[role] || '不明'
}