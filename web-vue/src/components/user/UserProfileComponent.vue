<template>
  <div class="user-profile-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else>
      <div v-if="error" class="error-container">
        <el-empty description="ユーザープロファイルの取得に失敗しました。後でもう一度お試しください">
          <el-button type="primary" @click="loadUserProfile">再試行</el-button>
        </el-empty>
      </div>
      
      <div v-else-if="!profileData" class="empty-profile">
        <el-empty description="ユーザープロファイルデータがまだありません。システムを利用してプロファイルを構築してください">
          <el-button type="primary" @click="goToHome">観光スポットを見る</el-button>
        </el-empty>
      </div>
      
      <div v-else class="profile-content">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>アクティビティ統計</h3>
            </div>
          </template>
          
          <div class="activity-stats">
            <div class="chart-container">
              <el-progress 
                v-for="(count, type) in profileData.activity_stats" 
                :key="type" 
                :percentage="getActivityPercentage(type)" 
                :color="getColorForActivity(type)"
                :stroke-width="18"
                :format="(percentage) => `${getInteractionLabel(type)}: ${count}回`"
                class="activity-progress"
              />
            </div>
            
            <div v-if="Object.keys(profileData.activity_stats).length === 0" class="no-data">
              <span>アクティビティデータがありません</span>
            </div>
          </div>
        </el-card>
        
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>お気に入りのカテゴリ</h3>
            </div>
          </template>
          
          <div class="category-list" v-if="profileData.preferred_categories.length > 0">
            <div 
              v-for="category in profileData.preferred_categories" 
              :key="category.id" 
              class="category-item"
            >
              <div class="category-info">
                <span class="category-name">{{ category.name }}</span>
                <el-tag type="success" size="small" effect="plain">
                  {{ category.count }} 回のインタラクション
                </el-tag>
              </div>
              
              <div class="interaction-types">
                <el-tag 
                  v-for="type in category.types" 
                  :key="type" 
                  size="small" 
                  type="info"
                  effect="plain"
                  class="interaction-tag"
                >
                  {{ getInteractionLabel(type) }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="カテゴリの好みに関するデータがありません" />
        </el-card>
        
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>タグの好み</h3>
            </div>
          </template>
          
          <div class="tag-cloud" v-if="profileData.preferred_tags.length > 0">
            <el-tag
              v-for="tag in profileData.preferred_tags"
              :key="tag.tag"
              type="info"
              effect="light"
              :style="{ fontSize: getTagSize(tag.count) + 'px' }"
              class="tag-item"
            >
              {{ tag.tag }} ({{ tag.count }})
            </el-tag>
          </div>
          
          <el-empty v-else description="タグの好みに関するデータがありません" />
        </el-card>
        
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3>興味が似ているユーザー</h3>
            </div>
          </template>
          
          <div class="similar-users" v-if="profileData.similar_users.length > 0">
            <div 
              v-for="user in profileData.similar_users" 
              :key="user.userId" 
              class="similar-user-item"
            >
              <el-avatar :size="40" :src="getUserAvatar(user)"></el-avatar>
              <div class="user-info">
                <span class="username">{{ user.username }}</span>
                <span class="common-items">{{ user.commonItems }} 件の共通の関心スポットがあります</span>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="似ているユーザーのデータがありません" />
        </el-card>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import type { UserProfile } from '@/types/user'
import { fileRequest } from '@/api/file_request'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(true)
const error = ref(false)
const profileData = ref<UserProfile | null>(null)

// ユーザープロファイルデータの読み込み
const loadUserProfile = async () => {
  if (!userStore.isLoggedIn()) {
    ElMessage.warning('先にログインしてください')
    return
  }
  
  const userId = userStore.userInfo?.id
  if (!userId) {
    ElMessage.warning('ユーザー情報が不完全です')
    return
  }
  
  loading.value = true
  error.value = false
  
  try {
    const res = await getUserProfile(userId)
    
    // 返却されたデータが空（インタラクション記録なし）か確認
    const hasData = res.preferred_categories.length > 0 || 
                     res.preferred_tags.length > 0 || 
                     Object.keys(res.activity_stats).length > 0 ||
                     res.similar_users.length > 0
    
    profileData.value = hasData ? res : null
  } catch (err) {
    console.error('ユーザープロファイルの取得に失敗しました:', err)
    error.value = true
    ElMessage.error('ユーザープロファイルの取得に失敗しました')
  } finally {
    loading.value = false
  }
}

// アクティビティの割合を取得
const getActivityPercentage = (type: string) => {
  if (!profileData.value?.activity_stats) return 0
  
  const stats = profileData.value.activity_stats
  const total = Object.values(stats).reduce((sum, count) => sum + count, 0)
  
  if (total === 0) return 0
  return Math.round((stats[type] / total) * 100)
}

// アクティビティの色を取得
const getColorForActivity = (type: string) => {
  const colorMap: Record<string, string> = {
    'VIEWED': '#409EFF',
    'FAVORITED': '#F56C6C',
    'PURCHASED': '#67C23A',
    'LIKED': '#E6A23C',
    'CREATED': '#9C27B0',
    'view': '#409EFF',
    'favorite': '#F56C6C',
    'purchase': '#67C23A',
    'like': '#E6A23C',
    'created': '#9C27B0',
    '浏览': '#409EFF',
    '收藏': '#F56C6C',
    '预约': '#67C23A',
    '点赞': '#E6A23C',
    '发布': '#9C27B0'
  }
  
  return colorMap[type] || '#909399'
}

// インタラクションタグの表示テキストを取得
const getInteractionLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    'VIEWED': '閲覧',
    'FAVORITED': 'お気に入り',
    'PURCHASED': '予約',
    'LIKED': 'いいね',
    'CREATED': '投稿',
    'view': '閲覧',
    'favorite': 'お気に入り',
    'purchase': '予約',
    'like': 'いいね',
    'created': '投稿',
    '浏览': '閲覧',
    '收藏': 'お気に入り',
    '预约': '予約',
    '点赞': 'いいね',
    '发布': '投稿'
  }
  
  return labelMap[type] || type
}

// カウントに基づいてタグのサイズを取得
const getTagSize = (count: number) => {
  // 基本サイズは12px、カウントに応じてサイズを増やし、最大20px
  return Math.min(12 + Math.log2(count) * 2, 20)
}

// ユーザーアバターを取得 - 本物のアバターを優先し、なければデフォルトのアバターを使用
const getUserAvatar = (user: { userId: number, avatarBucket?: string, avatarObjectKey?: string }) => {
  // ユーザーにアバター情報がある場合、fileRequestを使用してURLを取得
  if (user.avatarBucket && user.avatarObjectKey) {
    return fileRequest.getFileUrl(user.avatarBucket, user.avatarObjectKey)
  }
  
  // それ以外の場合はデフォルトのアバターを使用
  return getDefaultAvatar(user.userId)
}

// デフォルトのアバターを取得
const getDefaultAvatar = (userId: number) => {
  return `https://randomuser.me/api/portraits/${userId % 2 === 0 ? 'men' : 'women'}/${userId % 70}.jpg`
}

// ホームへ移動
const goToHome = () => {
  router.push('/')
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.user-profile-container {
  width: 100%;
}

.loading-container {
  padding: 20px;
}

.error-container,
.empty-profile {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.profile-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.profile-card {
  height: 100%;
  transition: all 0.3s;
}

.profile-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.activity-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-progress {
  margin-bottom: 15px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-item {
  padding: 10px;
  border-radius: 6px;
  background-color: #f8f9fa;
  border: 1px solid #ebeef5;
}

.category-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-name {
  font-weight: 500;
  color: #303133;
}

.interaction-types {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.interaction-tag {
  margin-right: 0;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin-right: 0;
  transition: transform 0.2s;
}

.tag-item:hover {
  transform: scale(1.05);
}

.similar-users {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.similar-user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 6px;
  background-color: #f8f9fa;
  border: 1px solid #ebeef5;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  color: #303133;
}

.common-items {
  font-size: 12px;
  color: #909399;
}

.no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100px;
  color: #909399;
  font-size: 14px;
}

/* レスポンシブ対応 */
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
}
</style>