import { request } from './request'
import { algoRequest } from './algo_request'
import type { UserQueryParams, UserForm, UserInfo, LoginResponse, UserProfile } from '@/types/user'
import type { PageVO } from '@/types/common'

// ログイン
export const login = async (username: string, password: string) => {
  const res = await request.post<LoginResponse>('/user/login', { username, password })
  return res
}

// 新規登録
export const register = (data: {
  username: string
  password: string
  realName: string
  phone: string
  email: string
}) => {
  return request.post('/user/register', data)
}

// ログアウト
export const logout = () => {
  return request.post('/user/logout')
}

// パスワード変更
export const updatePassword = (data: {
  id: number
  oldPassword: string
  newPassword: string
}) => {
  return request.post('/user/password', data)
}

// ユーザー一覧を取得
export const getUserList = (params: UserQueryParams) => {
  return request.get<PageVO<UserInfo>>('/user/page', { params })
}

// ユーザー詳細を取得
export const getUserInfo = (id: number) => {
  return request.get<UserInfo>(`/user/${id}`)
}

// ユーザーを追加
export const addUser = (data: UserForm) => {
  return request.post<void>('/user', data)
}

// ユーザー情報を更新
export const updateUser = (data: UserForm) => {
  return request.put<void>('/user', data)
}

// ユーザーを削除
export const deleteUser = (id: number) => {
  return request.delete<void>(`/user/${id}`)
}

// パスワードをリセット
export const resetPassword = (id: number) => {
  return request.put<void>(`/user/${id}/reset-password`)
}

// ユーザーステータスを更新
export const updateUserStatus = (data: { id: number; status: number }) => {
  return request.put<void>(`/user/${data.id}/status`, { status: data.status })
}

// ユーザープロファイルを取得
export const getUserProfile = (userId: number) => {
  return algoRequest.get<UserProfile>(`/knowledge-graph/user-profile/${userId}`)
}