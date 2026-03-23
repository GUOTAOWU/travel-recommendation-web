<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '80px' : '240px'" class="aside-panel">
      <div class="brand-area" :class="{ 'brand-collapse': isCollapse }">
        <img src="/2.png" alt="System Logo" class="brand-logo" />
        <span v-show="!isCollapse" class="brand-text">Travel AI</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        class="custom-menu"
        :router="true"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="transparent"
        text-color="#a0aec0"
        active-text-color="#fff"
      >
        <template v-if="isAdmin">
          <div class="menu-group-title" v-if="!isCollapse">管理中心</div>
          
          <el-menu-item index="/admin">
            <el-icon><Monitor /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>
          
          <div class="menu-group-title" v-if="!isCollapse">数据与资源</div>
          
          <el-menu-item index="/admin/kgm">
            <el-icon><Star /></el-icon>
            <template #title>知识图谱</template>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Box /></el-icon>
            <template #title>类别管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/items">
            <el-icon><Files /></el-icon>
            <template #title>景点管理</template>
          </el-menu-item>
          <el-menu-item index="/admin/user-actions">
            <el-icon><Clock /></el-icon>
            <template #title>行为日志</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="main-wrapper">
      <el-header class="glass-header">
        <div class="header-left">
          <div class="toggle-btn" @click="toggleCollapse">
            <el-icon :class="{ 'is-active': !isCollapse }"><Expand /></el-icon>
          </div>
          
          <el-breadcrumb separator="/" class="custom-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/user' }">前台首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title || '管理后台' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <div class="status-pill">
            <AlgoHealthCheck />
          </div>

          <el-dropdown @command="handleCommand" trigger="click">
            <div class="admin-profile">
              <el-avatar :size="36" :src="userInfo?.avatarUrl" class="admin-avatar">
                {{ userInfo?.username?.charAt(0) }}
              </el-avatar>
              <div class="admin-info">
                <span class="admin-name">{{ userInfo?.username }}</span>
                <span class="admin-role">管理员</span>
              </div>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item command="logout" class="danger-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content-area">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <div :key="route.path" class="view-container">
              <component :is="Component" />
            </div>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  Monitor, User, Files, Box, Expand, Star, Clock, 
  SwitchButton, CaretBottom 
} from '@element-plus/icons-vue'
import AlgoHealthCheck from './AlgoHealthCheck.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin())
const isCollapse = ref(false)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style scoped>
/* 全局容器 */
.layout-container {
  height: 100vh;
  background-color: #f4f7f9; /* 柔和的浅灰背景 */
}

/* ============ 侧边栏风格重构 ============ */
.aside-panel {
  background: #1a1c23; /* 深空灰 */
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

/* Logo 区域 */
.brand-area {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10px;
  background: rgba(0, 0, 0, 0.1);
  margin-bottom: 10px;
  transition: all 0.3s;
  overflow: hidden;
  white-space: nowrap;
}

.brand-collapse {
  padding: 0;
}

.brand-logo {
  width: 32px;
  height: 32px;
  object-fit: contain;
  transition: transform 0.3s;
}

.brand-text {
  margin-left: 12px;
  /* 你的核心要求：红色字体 */
  color: #ff4d4f; 
  font-weight: 800;
  font-size: 16px;
  letter-spacing: 0.5px;
  font-family: 'Helvetica Neue', sans-serif;
  opacity: 1;
  transition: opacity 0.3s;
}

/* 菜单重构 */
.custom-menu {
  border: none;
  padding: 0 10px;
  flex: 1;
  overflow-y: auto;
}

.menu-group-title {
  padding: 10px 12px 5px;
  font-size: 12px;
  color: #586578;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 600;
}

/* 菜单项样式覆盖 */
:deep(.el-menu-item) {
  height: 48px;
  line-height: 48px;
  border-radius: 8px; /* 圆角胶囊 */
  margin-bottom: 4px;
  transition: all 0.2s;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #ff4d4f 0%, #ff7875 100%); /* 配合Logo的红色主题，或者用蓝色 */
  /* 如果不想用红色做菜单背景，可以改成蓝色: background: linear-gradient(90deg, #409EFF 0%, #66b1ff 100%); */
  background: linear-gradient(90deg, #409EFF 0%, #3a8ee6 100%); /* 保持Element蓝，避免太刺眼 */
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

:deep(.el-menu-item .el-icon) {
  font-size: 18px;
  margin-right: 10px;
}

/* ============ 主体区域 ============ */
.main-wrapper {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部玻璃拟态 */
.glass-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.02);
  z-index: 99;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.3s;
}

.toggle-btn:hover {
  background: rgba(0,0,0,0.05);
}

.toggle-btn .el-icon {
  transition: transform 0.3s;
}
.toggle-btn .el-icon.is-active {
  transform: rotate(180deg);
}

/* 面包屑 */
.custom-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #909399;
  font-weight: 400;
}
.custom-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133;
  font-weight: 600;
}

/* 头部右侧 */
.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-pill {
  padding: 4px 12px;
  background: #f0f9eb;
  border-radius: 20px;
  font-size: 12px;
}

.admin-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
}

.admin-profile:hover {
  background: rgba(0,0,0,0.03);
}

.admin-avatar {
  border: 2px solid #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  background-color: #409EFF;
}

.admin-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.admin-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.admin-role {
  font-size: 11px;
  color: #909399;
}

.dropdown-icon {
  font-size: 12px;
  color: #909399;
}

/* 内容区域 */
.content-area {
  padding: 24px;
  background-color: #f4f7f9;
  overflow-x: hidden;
}

.view-container {
  width: 100%;
  height: 100%;
}

/* 路由动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>