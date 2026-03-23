<template>
  <div class="user-layout">
    <header class="main-header glass-nav">
      <div class="header-container">
        <div class="header-left">
          <div class="logo" @click="router.push('/user/home')">
            <img src="/2.png" alt="Travel AI Logo" />
            <span>Travel AI</span>
          </div>
          <nav class="primary-nav">
            <router-link to="/user/home" class="nav-item">探索</router-link>
            <router-link to="/user/items" class="nav-item">景点库</router-link>
            <router-link to="/user/categories" class="nav-item">分类</router-link>
            <router-link to="/user/chat" class="nav-item ai-link">
              <el-icon><MagicStick /></el-icon> AI向导
            </router-link>
          </nav>
        </div>

        <div class="header-center">
          <div class="search-box">
            <el-input
              v-model="searchText"
              placeholder="搜索心仪的目的地..."
              class="custom-search-input"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix>
                <el-dropdown trigger="click" @command="handleSearchModeChange">
                  <div class="search-mode">
                    {{ searchMode === 'title' ? '景点' : '标签' }}
                    <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="title">景点名称</el-dropdown-item>
                      <el-dropdown-item command="tag">个性标签</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template>
              <template #suffix>
                <el-icon class="search-icon" @click="handleSearch"><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>

        <div class="header-right">
          <div class="status-pill">
            <AlgoHealthCheck />
          </div>

          <div class="icon-actions">
            <el-tooltip content="我的收藏" placement="bottom">
              <router-link to="/user/favorites" class="icon-btn">
                <el-icon><Star /></el-icon>
              </router-link>
            </el-tooltip>
            <el-tooltip content="足迹" placement="bottom">
              <router-link to="/user/history/browsing" class="icon-btn">
                <el-icon><View /></el-icon>
              </router-link>
            </el-tooltip>
          </div>

          <el-divider direction="vertical" class="header-divider" />

          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-profile-trigger">
              <el-avatar :size="36" :src="userInfo?.avatarUrl || defaultAvatar" class="user-avatar">
                {{ userInfo?.username?.substring(0, 1) }}
              </el-avatar>
              <span class="user-name">{{ userInfo?.username || '未登录' }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="purchases">
                  <el-icon><Ticket /></el-icon> 预约记录
                </el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin" divided>
                  <el-icon><Monitor /></el-icon> 管理后台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided class="danger-text">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view v-slot="{ Component, route }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>

    <el-backtop :right="30" :bottom="30" class="custom-backtop" />

    <footer class="main-footer">
      <div class="footer-container">
        <div class="footer-brand">
          <img src="/2.png" alt="logo" class="footer-logo-img" />
          <span class="footer-title">Travel AI System</span>
        </div>
        <div class="footer-links">
          
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AlgoHealthCheck from './AlgoHealthCheck.vue'
import { categoryApi } from '@/api/category'
import { 
  Search, Star, View, User, ArrowDown, 
  MagicStick, Ticket, Monitor, SwitchButton, CaretBottom 
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const searchText = ref('')
const searchMode = ref('title')

const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin())
const popularCategories = ref<any[]>([])
const defaultAvatar = 'https://pic.imgdb.cn/item/65ae0c899f345e8d03301865.jpg'

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile': router.push('/user/profile'); break
    case 'purchases': router.push('/user/history/reservation'); break
    case 'admin': router.push('/admin'); break
    case 'logout': userStore.logout(); break
  }
}

const handleSearch = () => {
  if (searchText.value.trim()) {
    const query: Record<string, string | number> = {}
    if (searchMode.value === 'title') query.keyword = searchText.value.trim()
    else if (searchMode.value === 'tag') query.tag = searchText.value.trim()
    router.push({ path: '/user/items', query })
  }
}

const handleSearchModeChange = (command: string) => {
  searchMode.value = command
}

onMounted(async () => {
  try {
    const res = await categoryApi.page({ current: 1, size: 6 })
    if (res && res.records) popularCategories.value = res.records
  } catch (error) { console.error(error) }
})
</script>

<style scoped>
/* 全局布局 */
.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  /* 关键修改：背景色改为透明，去掉 z-index，让首页的背景图透出来 */
  background-color: transparent; 
  position: relative;
}

/* 导航栏重构 */
.main-header {
  height: 72px; 
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.85); /* 半透明白 */
  backdrop-filter: blur(12px); /* 毛玻璃 */
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

/* 左侧 Logo & Nav */
.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-size: 20px;
  font-weight: 800;
  color: #1a1a1a;
  letter-spacing: -0.5px;
}

.logo img {
  height: 36px;
  width: auto;
}

.primary-nav {
  display: flex;
  gap: 8px;
}

.nav-item {
  padding: 8px 16px;
  color: #666;
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-item:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #1a1a1a;
}

.nav-item.router-link-active {
  background: #1a1a1a;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 特殊处理 AI 链接 */
.ai-link {
  color: #409EFF;
}
.nav-item.ai-link:hover {
  background: #ecf5ff;
  color: #409EFF;
}

/* 中间搜索框 */
.header-center {
  flex: 1;
  max-width: 480px;
  margin: 0 40px;
}

/* 深度定制 Element Input */
.custom-search-input :deep(.el-input__wrapper) {
  border-radius: 50px;
  background-color: #f3f4f6;
  box-shadow: none !important;
  padding-left: 20px;
  transition: all 0.3s;
}

.custom-search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 2px #409EFF !important; /* 聚焦光环 */
}

.search-mode {
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding-right: 12px;
  border-right: 1px solid #dcdfe6;
  margin-right: 8px;
  font-weight: 600;
}

.search-icon {
  font-size: 18px;
  color: #909399;
  cursor: pointer;
  margin-right: 8px;
}
.search-icon:hover { color: #409EFF; }

/* 右侧功能区 */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-pill {
  padding: 4px 12px;
  background: #f0f9eb;
  border-radius: 20px;
  font-size: 12px;
}

.icon-actions {
  display: flex;
  gap: 4px;
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #606266;
  transition: all 0.2s;
  font-size: 18px;
}

.icon-btn:hover {
  background: rgba(0,0,0,0.05);
  color: #1a1a1a;
}

.header-divider {
  height: 24px;
  border-color: #e4e7ed;
  margin: 0 4px;
}

/* 用户头像区 */
.user-profile-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.2s;
}

.user-profile-trigger:hover {
  background: rgba(0,0,0,0.03);
}

.user-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 主内容 */
.main-content {
  flex: 1;
  width: 100%;
  position: relative;
}

/* 页脚重构 */
.main-footer {
  background: #fff;
  border-top: 1px solid #f0f0f0;
  padding: 40px 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  opacity: 0.8;
}

.footer-logo-img {
  height: 28px;
  filter: grayscale(100%);
  transition: filter 0.3s;
}

.footer-brand:hover .footer-logo-img {
  filter: grayscale(0%);
}

.footer-title {
  font-weight: 700;
  color: #333;
  font-size: 16px;
}

.footer-links {
  display: flex;
  gap: 30px;
  font-size: 13px;
  color: #909399;
}

/* 路由动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 自定义下拉菜单样式 */
.danger-text {
  color: #f56c6c;
}
</style>