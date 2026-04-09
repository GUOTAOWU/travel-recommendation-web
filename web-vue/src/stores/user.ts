import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo, LoginResponse } from '@/types/user'
import { login as userLogin, logout as userLogout, updatePassword as updateUserPassword, register as userRegister } from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // ユーザー情報
  const userInfo = ref<UserInfo | null>(null)
  // トークン
  const token = ref<string | null>(null)

  // localStorageからユーザー情報を復元する
  const initUserInfo = () => {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        const data = JSON.parse(storedUserInfo)
        userInfo.value = data.userInfo
        token.value = data.token
      } catch (error) {
        console.error('ユーザー情報の解析に失敗しました:', error)
        localStorage.removeItem('userInfo')
      }
    }
  }

  // ログイン
  const login = async (username: string, password: string) => {
    try {
      const res = await userLogin(username, password)
      if (!res || !res.userInfo) {
        throw new Error('ログインに失敗しました。後でもう一度お試しください')
      }
      userInfo.value = res.userInfo
      token.value = res.token
      // ユーザー情報とトークンをlocalStorageに保存
      localStorage.setItem('userInfo', JSON.stringify({
        userInfo: res.userInfo,
        token: res.token
      }))
      ElMessage.success('ログインに成功しました')
      
      // ロール（権限）に応じて異なるページへ遷移
      if (res.userInfo.role === 1) {
        router.push('/admin')
      } else {
        router.push('/user')
        console.log('ログイン成功 - 一般ユーザー')
      }
    } catch (error) {
      ElMessage.error('ログインに失敗しました')
      throw error
    }
  }

  // ログアウト
  const logout = async (sendRequest: boolean = true) => {
    try {
      // 必要な場合のみログアウトAPIを呼び出し
      if (sendRequest) {
        try {
          await userLogout()
        } catch (error) {
          console.error('ログアウトリクエストに失敗しましたが、ローカルの状態クリアを続行します')
        }
      }
      
      // リクエストの成否に関わらず、ローカルの状態をクリア
      userInfo.value = null
      token.value = null
      // localStorageからユーザー情報を削除
      localStorage.removeItem('userInfo')
      
      if (sendRequest) {
        ElMessage.success('ログアウトしました')
      }
      
      router.push('/login')
    } catch (error) {
      // エラーが発生しても確実にローカル状態をクリアする
      userInfo.value = null
      token.value = null
      localStorage.removeItem('userInfo')
      router.push('/login')
      
      if (sendRequest) {
        ElMessage.error('ログアウトに失敗しました')
        throw error
      }
    }
  }

  // ログイン状態を判定
  const isLoggedIn = () => {
    try {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (!storedUserInfo) return false
      
      const data = JSON.parse(storedUserInfo)
      return !!(data.token && data.userInfo)
    } catch (error) {
      console.error('ユーザー情報の解析に失敗しました:', error)
      localStorage.removeItem('userInfo')
      return false
    }
  }

  // 管理者かどうかを判定
  const isAdmin = () => {
    try {
      const storedUserInfo = localStorage.getItem('userInfo')
      if (!storedUserInfo) return false
      
      const data = JSON.parse(storedUserInfo)
      return data.userInfo?.role === 1
    } catch (error) {
      console.error('ユーザー情報の解析に失敗しました:', error)
      localStorage.removeItem('userInfo')
      return false
    }
  }

  // パスワード変更
  const changePassword = async (data: { id: number; oldPassword: string; newPassword: string }) => {
    await updateUserPassword(data)
  }

  // ユーザー登録
  const register = async (data: { username: string; password: string; realName: string; phone: string; email: string }) => {
    return userRegister(data)
  }

  return {
    userInfo,
    token,
    initUserInfo,
    login,
    logout,
    isLoggedIn,
    isAdmin,
    changePassword,
    register
  }
})