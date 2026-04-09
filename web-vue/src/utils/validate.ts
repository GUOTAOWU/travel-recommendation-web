// 携帯電話番号の検証（中国国内形式: 1から始まる11桁）
export const validatePhone = (phone: string) => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

// メールアドレスの検証
export const validateEmail = (email: string) => {
  const reg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  return reg.test(email)
}

// パスワードの検証（6〜20文字の英数字）
export const validatePassword = (password: string) => {
  const reg = /^[a-zA-Z0-9]{6,20}$/
  return reg.test(password)
}

// ユーザー名の検証（4〜16文字の英数字、ハイフン、アンダースコア）
export const validateUsername = (username: string) => {
  const reg = /^[a-zA-Z0-9_-]{4,16}$/
  return reg.test(username)
}

// IPアドレスの検証（IPv4）
export const validateIP = (ip: string) => {
  return /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/.test(ip)
}

// RTSPアドレスの検証
export const validateRTSP = (rtsp: string) => {
  return /^rtsp:\/\/[^\s]*$/.test(rtsp)
}

// 画像形式の検証
export const validateImageType = (type: string) => {
  return ['image/jpeg', 'image/png', 'image/gif'].includes(type)
}

// 動画形式の検証
export const validateVideoType = (type: string) => {
  return ['video/mp4'].includes(type)
}

// ファイルサイズの検証（単位：MB）
export const validateFileSize = (size: number, maxSize: number) => {
  return size <= maxSize * 1024 * 1024
}