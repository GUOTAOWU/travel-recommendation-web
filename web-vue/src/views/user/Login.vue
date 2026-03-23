<template>
  <div class="login-wrapper">
    <div class="bg-overlay"></div>
    
    <div class="content-container">
      <div class="brand-section animate-slide-right">
        <div class="brand-header">
          <img src="/2.png" class="brand-logo" alt="Logo" />
          <span class="brand-name">Travel AI</span>
        </div>
        
        <div class="brand-slogan">
          <h1 class="main-title">探索<br>未知的边界</h1>
          <p class="sub-title">让每一次出发，都成为期待已久的相遇。</p>
        </div>

        <div class="tags-container">
          <div class="glass-tag">
            <span class="hash">#</span> 深度定制
          </div>
          <div class="glass-tag">
            <span class="hash">#</span> 灵感规划
          </div>
          <div class="glass-tag">
            <span class="hash">#</span> 智能伴游
          </div>
        </div>
      </div>

      <div class="form-section animate-slide-left">
        <div class="login-card glass-panel">
          <div class="card-header">
            <h3>Welcome Back</h3>
            <p>登录账号，同步您的专属行程</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            size="large"
          >
            <el-form-item prop="username">
              <div class="input-group">
                <span class="input-label">账号</span>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  class="minimal-input"
                >
                  <template #suffix>
                    <el-icon class="input-icon"><User /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-group">
                <span class="input-label">密码</span>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  show-password
                  class="minimal-input"
                  @keyup.enter="handleLogin"
                >
                  <template #suffix>
                    <el-icon class="input-icon"><Lock /></el-icon>
                  </template>
                </el-input>
              </div>
            </el-form-item>

            <div class="form-footer">
              <el-button
                type="primary"
                :loading="loading"
                class="submit-btn"
                @click="handleLogin"
                round
              >
                立即登录 <el-icon class="el-icon--right"><Right /></el-icon>
              </el-button>
            </div>
            
            <div class="bottom-links">
              <span>新用户？</span>
              <router-link to="/register" class="link-text">创建一个账号</router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { User, Lock, Right } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const loginForm = ref({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const loading = ref(false)
const loginFormRef = ref<FormInstance>()

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
    loading.value = true
    await userStore.login(loginForm.value.username, loginForm.value.password)
    const redirect = route.query.redirect as string
    router.replace(redirect || '/')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 全局容器 */
.login-wrapper {
  min-height: 100vh;
  width: 100%;
  position: relative;
  /* 替换背景图为 1.JPG */
  background: url('/1.JPG') no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* 黑色渐变遮罩，增加文字可读性和高级感 */
.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0.3) 100%);
  backdrop-filter: blur(2px);
  z-index: 1;
}

.content-container {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 80px;
  padding: 40px;
}

/* ============ 左侧：品牌展示 ============ */
.brand-section {
  color: #fff;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 40px;
}

.brand-logo {
  height: 50px;
  width: auto;
  filter: drop-shadow(0 4px 6px rgba(0,0,0,0.2));
}

.brand-name {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.main-title {
  font-size: 64px;
  line-height: 1.1;
  font-weight: 900;
  margin: 0 0 20px 0;
  background: linear-gradient(to right, #fff, #e0e0e0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sub-title {
  font-size: 18px;
  color: rgba(255,255,255,0.85);
  font-weight: 300;
  margin-bottom: 50px;
  letter-spacing: 1px;
}

.tags-container {
  display: flex;
  gap: 15px;
}

.glass-tag {
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.2);
  padding: 8px 16px;
  border-radius: 30px;
  font-size: 14px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
  cursor: default;
}

.glass-tag:hover {
  background: rgba(255,255,255,0.2);
  transform: translateY(-2px);
}

.hash {
  color: #409EFF;
  font-weight: bold;
  margin-right: 4px;
}

/* ============ 右侧：登录表单 ============ */
.form-section {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: 24px;
}

.glass-panel {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.card-header {
  margin-bottom: 30px;
  text-align: left;
}

.card-header h3 {
  font-size: 28px;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  font-weight: 700;
}

.card-header p {
  color: #666;
  font-size: 14px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.input-label {
  font-size: 12px;
  color: #333;
  font-weight: 600;
  margin-left: 4px;
}

/* 极简输入框样式重写 */
.minimal-input :deep(.el-input__wrapper) {
  background-color: #f3f4f6;
  box-shadow: none !important;
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 12px 15px;
  height: 50px;
  transition: all 0.3s;
}

.minimal-input :deep(.el-input__wrapper:hover),
.minimal-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #409EFF;
  box-shadow: 0 0 0 4px rgba(64, 158, 255, 0.1) !important;
}

.input-icon {
  color: #909399;
}

.form-footer {
  margin-top: 10px;
}

.submit-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  background: #1a1a1a; /* 黑色按钮，更显高级 */
  border-color: #1a1a1a;
  box-shadow: 0 4px 14px 0 rgba(0,0,0,0.39);
  transition: all 0.3s;
}

.submit-btn:hover {
  background: #333;
  border-color: #333;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.23);
}

.bottom-links {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.link-text {
  color: #409EFF;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}

.link-text:hover {
  text-decoration: underline;
}

/* 动画 */
.animate-slide-right {
  animation: slideInRight 0.8s ease-out;
}

.animate-slide-left {
  animation: slideInLeft 0.8s ease-out;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(-30px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

/* 响应式 */
@media (max-width: 900px) {
  .content-container {
    grid-template-columns: 1fr;
    gap: 40px;
    padding: 20px;
  }
  
  .brand-section {
    align-items: center;
    text-align: center;
  }

  .main-title {
    font-size: 42px;
  }
  
  .main-title br {
    display: none;
  }
}
</style>