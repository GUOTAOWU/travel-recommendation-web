import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: to => {
      const userStore = useUserStore();
      // 未ログインの場合はログイン画面へ
      if (!userStore.isLoggedIn()) {
        return '/login';
      }
      
      // 権限に応じてリダイレクト先を振り分け
      if (userStore.isAdmin()) {
        return '/admin';
      } else {
        return '/user/home';
      }
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: {
      title: 'ログイン',
      requiresAuth: false
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: {
      title: '新規登録',
      requiresAuth: false
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/admin/Dashboard.vue'),
    meta: {
      title: 'ダッシュボード'
    }
  },
  {
    path: '/user',
    component: () => import('@/components/UserLayout.vue'),
    redirect: '/user/home',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue'),
        meta: {
          title: 'ホーム',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: {
          title: 'マイページ',
          requiresAuth: true
        }
      },
      {
        path: 'favorites',
        name: 'UserFavorites',
        component: () => import('@/views/user/MyFavorites.vue'),
        meta: {
          title: 'お気に入り一覧',
          requiresAuth: true
        }
      },
      {
        path: 'history/browsing',
        name: 'UserBrowsingHistory',
        component: () => import('@/views/history/BrowsingHistory.vue'),
        meta: {
          title: '閲覧履歴',
          requiresAuth: true
        }
      },
      {
        path: 'history/reservation',
        name: 'UserReservationHistory',
        component: () => import('@/views/history/PurchaseHistory.vue'),
        meta: {
          title: '予約履歴',
          requiresAuth: true
        }
      },
      {
        path: 'categories',
        name: 'UserCategories',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: {
          title: 'カテゴリー検索',
          requiresAuth: true
        }
      },
      {
        path: 'category/:id',
        name: 'UserCategoryDetail',
        component: () => import('@/views/category/CategoryDetail.vue'),
        meta: {
          title: 'カテゴリー詳細',
          requiresAuth: true
        }
      },
      {
        path: 'items',
        name: 'UserItems',
        component: () => import('@/views/item/ItemList.vue'),
        meta: {
          title: 'スポット検索',
          requiresAuth: true
        }
      },
      {
        path: 'item/:id',
        name: 'UserItemDetail',
        component: () => import('@/views/item/ItemDetail.vue'),
        meta: {
          title: 'スポット詳細',
          requiresAuth: true
        }
      },
      {
        path: 'chat',
        name: 'UserChatSessionList',
        component: () => import('@/views/chat/ChatView.vue'),
        meta: {
          title: 'AIチャット',
          requiresAuth: true
        }
      },
    ]
  },
  {
    path: '/admin',
    component: () => import('@/components/Layout.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: '',
        name: 'Admin',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: {
          title: '管理コンソール',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'kgm',
        name: 'KnowledgeGraphManagement',
        component: () => import('@/views/admin/KnowledgeGraphManagement.vue'),
        meta: {
          title: '知識グラフ管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: 'ユーザー管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: {
          title: 'カテゴリー管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'items',
        name: 'ItemManagement',
        component: () => import('@/views/admin/ItemManagement.vue'),
        meta: {
          title: 'スポット管理',
          requiresAuth: true,
          requiresAdmin: true
        }
      },
      {
        path: 'user-actions',
        name: 'UserActionHistory',
        component: () => import('@/views/admin/UserActionHistory.vue'),
        meta: {
          title: 'ユーザー行動履歴',
          requiresAuth: true,
          requiresAdmin: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ルートガード（ナビゲーションガード）
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)

  // ページタイトルの設定
  document.title = `${to.meta.title} - インテリジェント観光推薦システム`

  if (requiresAuth) {
    // 認証が必要なページで未ログインの場合
    if (!userStore.isLoggedIn()) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    // 管理者権限が必要なページで権限がない場合
    if (requiresAdmin && !userStore.isAdmin()) {
      next({ name: 'UserHome' })
      return
    }
  }

  next()
})

export default router