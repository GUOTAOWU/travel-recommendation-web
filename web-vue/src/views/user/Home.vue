<template>
  <div class="immersive-home">
    <div class="fixed-bg"></div>
    <div class="bg-overlay"></div>

    <div class="main-container">
      
      <div class="top-bar-container">
        <div class="spacer"></div>

        <div class="user-status-bar glass-panel">
          <div class="user-info-group">
            <el-avatar :size="36" :src="userInfo?.avatarUrl || defaultAvatar" class="header-avatar">
              {{ userInfo?.username?.substring(0, 1) }}
            </el-avatar>
            <div class="user-text">
              <span class="u-name">{{ userInfo?.realName || userInfo?.username || '请登录' }}</span>
            </div>
          </div>
          <div class="vertical-divider"></div>
          <div class="stats-group">
            <div class="stat-mini" title="收藏">
              <el-icon><Star /></el-icon> <span>{{ userStats.favorites }}</span>
            </div>
            <div class="stat-mini" title="足迹">
              <el-icon><Location /></el-icon> <span>{{ userStats.history }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="bento-grid">
        
        <div class="bento-item hero-banner">
          <el-carousel 
            height="100%" 
            :interval="4000" 
            :autoplay="true"
            arrow="hover"
            trigger="click"
          >
            <el-carousel-item v-for="(banner, index) in banners" :key="index">
              <div class="hero-slide" @click="handleBannerClick(banner)">
                <div class="slide-mask"></div>
                
                <el-image 
                  :src="banner.imageUrl" 
                  fit="cover" 
                  class="hero-img"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon> 无法加载图片
                    </div>
                  </template>
                </el-image>

                <div class="hero-text-overlay">
                  <h3>{{ banner.title }}</h3>
                  <div class="explore-btn">即刻探索 <el-icon><ArrowRight /></el-icon></div>
                </div>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="bento-item ai-assistant" @click="goToChat">
          <div class="ai-bg-particles"></div>
          <div class="ai-content-wrapper">
            <div class="ai-left">
               <img src="/2.png" class="ai-custom-icon" alt="AI" />
            </div>
            <div class="ai-right">
              <h2>AI 灵感向导</h2>
              <p>不知道去哪玩？点击这里，为您定制专属行程</p>
            </div>
            <div class="ai-action-icon">
              <el-icon><Right /></el-icon>
            </div>
          </div>
        </div>

        <div class="bento-item category-nav glass-panel">
          <div class="panel-header">
            <span>探索灵感</span>
            <el-icon class="link-icon" @click="router.push('/user/categories')"><ArrowRight /></el-icon>
          </div>
          <div class="category-scroll">
            <div 
              v-for="category in popularCategories" 
              :key="category.id" 
              class="cat-chip"
              @click="goToCategoryDetail(category.id)"
            >
              <span class="hash">#</span> {{ category.name }}
            </div>
          </div>
        </div>
      </div>

      <div class="content-stream">
        <div class="stream-section">
          <div class="stream-header">
            <div class="header-left">
              <div class="indicator-dot"></div>
              <h2>为您甄选</h2>
            </div>
            <div class="switch-capsule">
              <span 
                :class="{ active: recommendationType === 'user' }"
                @click="recommendationType = 'user'; fetchRecommendedItems()"
              >猜你喜欢</span>
              <span 
                :class="{ active: recommendationType === 'content' }"
                @click="recommendationType = 'content'; fetchRecommendedItems()"
              >相似推荐</span>
            </div>
          </div>

          <div class="masonry-layout">
            <div 
              v-for="item in recommendedItems" 
              :key="item.id" 
              class="masonry-card glass-panel"
              @click="goToItemDetail(item.id)"
            >
              <div class="card-media">
                <el-image :src="item.coverUrl || defaultCover" fit="cover" loading="lazy">
                   <template #error>
                      <div class="image-slot"><el-icon><Picture /></el-icon></div>
                   </template>
                </el-image>
                <div class="card-float-tag">{{ item.categoryName }}</div>
              </div>
              <div class="card-body">
                <h3>{{ item.title }}</h3>
                <div class="card-metrics">
                  <span><el-icon><View /></el-icon> {{ formatNumber(item.views) }}</span>
                  <span><el-icon><Star /></el-icon> {{ formatNumber(item.favorites) }}</span>
                </div>
              </div>
            </div>
             <div v-if="recommendedItems.length === 0 && !recommendationsLoading" class="empty-placeholder">
              <el-empty description="暂无推荐数据" :image-size="80" />
            </div>
          </div>
        </div>

        <div class="stream-section mt-40">
          <div class="stream-header">
            <div class="header-left">
              <div class="indicator-dot orange"></div>
              <h2>当季热度 TOP</h2>
            </div>
            <span class="view-all" @click="router.push('/user/items?sort=popular')">查看榜单</span>
          </div>

          <div class="list-layout">
            <div 
              v-for="(item, index) in popularItems" 
              :key="item.id" 
              class="list-card glass-panel"
              @click="goToItemDetail(item.id)"
            >
              <div class="rank-num">{{ index + 1 }}</div>
              <div class="list-img">
                <el-image :src="item.coverUrl || defaultCover" fit="cover" />
              </div>
              <div class="list-info">
                <h3>{{ item.title }}</h3>
                <div class="heat-bar">
                  <div class="heat-fill" :style="`width: ${Math.min((item.views || 0)/100, 100)}%`"></div>
                </div>
              </div>
              <div class="list-action">
                <el-button circle size="small" :icon="ArrowRight" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="brand-footer">
        <div class="footer-content">

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  ArrowRight, Picture, View, Star, Location, 
  Ticket, Right
} from '@element-plus/icons-vue'
import { itemApi } from '@/api/item'
import { categoryApi } from '@/api/category'
import { favoriteApi } from '@/api/favorite'
import { pageMyActions, getItemViewCount } from '@/api/userAction'
import { recommendationApi } from '@/api/recommendation'
import { fileRequest } from '@/api/file_request'

// 资源路径
const defaultAvatar = 'https://pic.imgdb.cn/item/65ae0c899f345e8d03301865.jpg'
const defaultCover = 'https://pic.imgdb.cn/item/65ae0c899f345e8d033018ab.jpg'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const recommendationsLoading = ref(false)
const userInfo = computed(() => userStore.userInfo)
const recommendationType = ref('user')

const userStats = ref({ favorites: 0, history: 0, reservations: 0 })

// 轮播图数据：使用本地图片
const banners = ref([
  { 
    id: 1, 
    title: '探索未知的世界', 
    imageUrl: '/3.jpg', 
    link: '/user/items' 
  },
  { 
    id: 2, 
    title: '发现身边的美好', 
    imageUrl: '/4.jpg', 
    link: '/user/categories' 
  },
  { 
    id: 3, 
    title: 'AI 智能向导', 
    imageUrl: '/5.jpg', 
    link: '/user/chat' 
  }
])

const recommendedItems = ref<any[]>([])
const popularItems = ref<any[]>([])
const popularCategories = ref<any[]>([])

// 格式化数字
const formatNumber = (num: any) => {
  const n = Number(num) || 0
  if (n < 1000) return n
  if (n < 10000) return (n / 1000).toFixed(1) + 'k'
  return (n / 10000).toFixed(1) + 'w'
}

// 路由跳转
const handleBannerClick = (banner: any) => router.push(banner.link)
const goToItemDetail = (id: number) => router.push(`/user/item/${id}`)
const goToCategoryDetail = (id: number) => router.push(`/user/category/${id}`)
const goToChat = () => router.push('/user/chat')

// 数据获取逻辑
const fetchRecommendedItems = async () => {
  try {
    recommendationsLoading.value = true
    recommendedItems.value = []
    
    if (!userStore.userInfo?.id) {
      const res = await itemApi.page({ current: 1, size: 6 }) 
      recommendedItems.value = res.records
    } else {
      let items = []
      if (recommendationType.value === 'user') {
        items = await recommendationApi.getForUser(userStore.userInfo.id, 6)
      } else {
        items = await recommendationApi.getContentForUser(userStore.userInfo.id, 6)
      }
      
      if (items && items.length > 0) {
        recommendedItems.value = items
        for (const item of recommendedItems.value) {
          if (item.coverBucket && item.coverObjectKey) {
            item.coverUrl = fileRequest.getFileUrl(item.coverBucket, item.coverObjectKey)
          }
        }
      } else {
        const res = await itemApi.page({ current: 1, size: 6 })
        recommendedItems.value = res.records
      }
    }
    
    for (const item of recommendedItems.value) {
      try {
        const favoriteCount = await favoriteApi.getItemFavoriteCount(item.id)
        item.favorites = favoriteCount
        const viewCount = await getItemViewCount(item.id)
        item.views = viewCount
      } catch (error) {}
    }
  } catch (error) {
    console.error(error)
  } finally {
    recommendationsLoading.value = false
  }
}

const fetchPopularItems = async () => {
  try {
    const res = await itemApi.page({ current: 1, size: 100 })
    let items = res.records
    for (const item of items) {
        item.views = item.views || 0 
    }
    items.sort((a, b) => ((b.views || 0) - (a.views || 0)))
    popularItems.value = items.slice(0, 5)
  } catch (error) {}
}

const fetchPopularCategories = async () => {
  try {
    const res = await categoryApi.page({ current: 1, size: 10 })
    popularCategories.value = res.records
  } catch (error) {}
}

const fetchUserStats = async () => {
  try {
    const favoriteRes = await favoriteApi.getUserFavoriteItemIds();
    userStats.value.favorites = favoriteRes.length;
    const historyRes = await pageMyActions({ current: 1, size: 1, actionType: 0 });
    userStats.value.history = historyRes.total;
    const purchaseRes = await pageMyActions({ current: 1, size: 1, actionType: 1 });
    userStats.value.reservations = purchaseRes.total;
  } catch (error) {}
}

onMounted(async () => {
  loading.value = true
  await Promise.all([
    fetchRecommendedItems(),
    fetchPopularItems(),
    fetchPopularCategories(),
    fetchUserStats()
  ])
  loading.value = false
})
</script>

<style scoped>
/* Immersive Dashboard Style */

.immersive-home {
  position: relative;
  min-height: 100vh;
  /* 确保背景不被父级遮挡，强制透明 */
  background-color: transparent; 
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  color: #333;
}

/* 1. 背景图层 */
.fixed-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 你的背景图 */
  background-image: url('/1.JPG');
  background-size: cover;
  background-position: center;
  background-attachment: fixed; /* 视差滚动效果 */
  z-index: -2;
}

/* 2. 遮罩层 - 调节这里的背景色透明度来控制图片的显示程度 */
.bg-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  /* 0.85 的透明度让图片隐隐约约可见，不会太花 */
  background: rgba(255, 255, 255, 0.85); 
  backdrop-filter: blur(5px); /* 轻微模糊背景图 */
  z-index: -1;
}

.main-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 40px 60px;
  position: relative;
  z-index: 1;
}

/* ================= 1. 顶部栏 (简化版) ================= */
.top-bar-container {
  display: flex;
  justify-content: flex-end; /* 靠右对齐 */
  align-items: center;
  margin-bottom: 30px;
  padding: 15px 0;
}

.user-status-bar {
  display: flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: 50px;
  gap: 20px;
  background: rgba(255,255,255,0.7); /* 稍微透明一点 */
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  backdrop-filter: blur(10px);
}

.user-info-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.u-name { font-weight: 700; font-size: 14px; color: #1a1a1a; }

.vertical-divider {
  width: 1px;
  height: 20px;
  background: rgba(0,0,0,0.1);
}

.stats-group {
  display: flex;
  gap: 15px;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #555;
  font-weight: 600;
}

/* ================= 2. Bento Grid 布局 ================= */
.bento-grid {
  display: grid;
  grid-template-columns: 2.2fr 1fr; /* 左侧更宽一点 */
  grid-template-rows: 260px 140px; /* 定义固定行高 */
  gap: 20px;
  margin-bottom: 40px;
}

.bento-item {
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  height: 100%; /* 填满格子 */
}

.bento-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.1);
}

/* 左侧 Banner (跨两行) */
.hero-banner {
  grid-column: 1 / 2;
  grid-row: 1 / 3;
  height: 100%; 
  background: rgba(255,255,255,0.5); /* 占位色 */
}

/* 轮播图样式强制修复 */
:deep(.el-carousel) {
  height: 100%;
}
:deep(.el-carousel__container) {
  height: 100% !important; /* 强制高度填满 */
}

.hero-slide {
  height: 100%;
  width: 100%;
  position: relative;
  cursor: pointer;
}

/* 图片遮罩，下半部分渐变黑 */
.slide-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 40%);
  z-index: 1;
}

.hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 关键：铺满 */
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

.hero-text-overlay {
  position: absolute;
  bottom: 40px;
  left: 40px;
  color: #fff;
  z-index: 2;
  text-shadow: 0 2px 8px rgba(0,0,0,0.5);
}

.hero-text-overlay h3 {
  font-size: 32px;
  margin-bottom: 15px;
  font-weight: 800;
  letter-spacing: 1px;
}

.explore-btn {
  background: #fff;
  color: #000;
  padding: 10px 24px;
  border-radius: 30px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  transition: all 0.2s;
}

.explore-btn:hover { background: #f0f0f0; }

/* 右上 AI 入口 */
.ai-assistant {
  grid-column: 2 / 3;
  grid-row: 1 / 2;
  background: #1e2129; /* 深色背景 */
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
  cursor: pointer;
  border: 1px solid rgba(255,255,255,0.1);
}

.ai-bg-particles {
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 90% 10%, rgba(64,158,255,0.2) 0%, transparent 60%);
  pointer-events: none;
}

.ai-content-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 2;
}

.ai-custom-icon {
  width: 56px;
  height: 56px;
  object-fit: contain;
  filter: drop-shadow(0 0 5px rgba(255,255,255,0.3));
}

.ai-right h2 {
  margin: 0 0 6px 0;
  font-size: 20px;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0,0,0,0.5);
}

.ai-right p {
  margin: 0;
  font-size: 12px;
  color: rgba(255,255,255,0.85);
  line-height: 1.4;
  text-shadow: 0 1px 2px rgba(0,0,0,0.5);
}

.ai-action-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 36px;
  height: 36px;
  background: rgba(255,255,255,0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255,255,255,0.3);
}

/* 右下 分类导航 */
.category-nav {
  grid-column: 2 / 3;
  grid-row: 2 / 3;
  display: flex;
  flex-direction: column;
  padding: 15px 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  margin-bottom: 12px;
  color: #333;
}

.link-icon { cursor: pointer; color: #999; }

.category-scroll {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  overflow-y: auto;
  align-content: flex-start;
}

.cat-chip {
  background: rgba(245, 247, 250, 0.7);
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  color: #555;
  border: 1px solid transparent;
}

.cat-chip:hover {
  background: #fff;
  color: #409EFF;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.hash { color: #409EFF; font-weight: bold; margin-right: 2px; }

/* ================= 3. 内容流布局 ================= */
.content-stream {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.stream-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.indicator-dot {
  width: 6px;
  height: 24px;
  background: #333;
  border-radius: 3px;
}

.indicator-dot.orange { background: #ff4d4f; }

.stream-header h2 {
  font-size: 22px;
  margin: 0;
  font-weight: 800;
  color: #1a1a1a;
}

.switch-capsule {
  background: rgba(255,255,255,0.6);
  padding: 4px;
  border-radius: 30px;
  display: flex;
  gap: 4px;
  border: 1px solid rgba(255,255,255,0.5);
}

.switch-capsule span {
  padding: 6px 18px;
  border-radius: 25px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
  font-weight: 600;
}

.switch-capsule span.active {
  background: #1a1a1a;
  color: #fff;
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

/* 瀑布流卡片 */
.masonry-layout {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.masonry-card {
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  background: rgba(255,255,255,0.9); /* 稍微透明一点 */
  border: 1px solid rgba(0,0,0,0.03);
}

.masonry-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
}

.card-media {
  height: 180px;
  position: relative;
  overflow: hidden;
}

.card-media .el-image { width: 100%; height: 100%; transition: transform 0.5s; }
.masonry-card:hover .card-media .el-image { transform: scale(1.05); }

.card-float-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255,255,255,0.95);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #333;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.card-body {
  padding: 16px;
}

.card-body h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-metrics {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #888;
}

.card-metrics span { display: flex; align-items: center; gap: 4px; }

/* 榜单列表 (横向) */
.list-layout {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list-card {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-radius: 12px;
  transition: all 0.2s;
  background: rgba(255,255,255,0.75); /* 毛玻璃 */
  border: 1px solid rgba(255,255,255,0.4);
  cursor: pointer;
}

.list-card:hover {
  background: #fff;
  transform: scale(1.01);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}

.rank-num {
  font-size: 20px;
  font-weight: 900;
  color: #ddd;
  width: 40px;
  text-align: center;
  margin-right: 10px;
  font-style: italic;
}

.list-card:nth-child(1) .rank-num { color: #ffcc00; }
.list-card:nth-child(2) .rank-num { color: #C0C0C0; } 
.list-card:nth-child(3) .rank-num { color: #cd7f32; }

.list-img {
  width: 80px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  margin-right: 20px;
}

.list-img .el-image { width: 100%; height: 100%; }

.list-info { flex: 1; }

.list-info h3 { margin: 0 0 6px 0; font-size: 15px; color: #333; font-weight: 600; }

.heat-bar {
  width: 120px;
  height: 4px;
  background: rgba(0,0,0,0.05);
  border-radius: 2px;
}

.heat-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff9c6e, #ff4d4f);
  border-radius: 2px;
}

.list-action {
  opacity: 0;
  transition: opacity 0.2s;
}

.list-card:hover .list-action { opacity: 1; }

/* ================= 4. 底部 ================= */
.brand-footer {
  margin-top: 60px;
  padding-top: 30px;
  border-top: 1px solid rgba(0,0,0,0.05);
  display: flex;
  justify-content: center;
}

.footer-content {
  display: flex;
  align-items: center;
  gap: 12px;
  opacity: 0.6;
  transition: opacity 0.3s;
}

.footer-content:hover { opacity: 1; }

.footer-logo {
  height: 28px;
  width: auto;
  filter: grayscale(100%);
  transition: filter 0.3s;
}

.footer-content:hover .footer-logo { filter: grayscale(0%); }

.footer-text {
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

/* 通用面板 - 增加毛玻璃效果，让背景透出来 */
.glass-panel {
  background: rgba(255,255,255,0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.5);
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .main-container { padding: 20px; }
  .masonry-layout { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 992px) {
  .bento-grid {
    grid-template-columns: 1fr;
    grid-template-rows: auto;
    gap: 15px;
  }
  .hero-banner { 
    grid-column: 1 / -1; 
    grid-row: auto; 
    height: 320px; /* 移动端高度 */
  }
  .ai-assistant { 
    grid-column: 1 / -1; 
    grid-row: auto; 
    height: auto;
    padding: 20px;
  }
  .category-nav { 
    grid-column: 1 / -1; 
    grid-row: auto; 
  }
}
</style>