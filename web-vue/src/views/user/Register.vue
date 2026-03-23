<template>
  <div class="register-wrapper">
    <div class="bg-overlay"></div>
    
    <div class="content-container">
      <div class="brand-section animate-slide-right">
        <div class="brand-header">
          <img src="/2.png" class="brand-logo" alt="Logo" />
          <span class="brand-name">Travel AI</span>
        </div>
        
        <div class="brand-slogan">
          <h1 class="main-title">成为<br>探索者</h1>
          <p class="sub-title">注册账号，解锁 AI 专属行程规划与个性化推荐。</p>
        </div>

        <div class="tags-container">
          <div class="glass-tag">
            <span class="hash">#</span> 专属主页
          </div>
          <div class="glass-tag">
            <span class="hash">#</span> 收藏云同步
          </div>
          <div class="glass-tag">
            <span class="hash">#</span> 优先体验
          </div>
        </div>
      </div>

      <div class="form-section animate-slide-left">
        <div class="register-card glass-panel">
          <div class="card-header">
            <h3>Create Account</h3>
            <p>填写以下信息，开启您的智能之旅</p>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="设置用户名 (3-20字符)"
                class="minimal-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-row">
              <el-form-item prop="password" class="half-width">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="设置密码"
                  show-password
                  class="minimal-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item prop="confirmPassword" class="half-width">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  show-password
                  class="minimal-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><CircleCheck /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item prop="realName" class="half-width">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="真实姓名"
                  class="minimal-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><UserFilled /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="phone" class="half-width">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="手机号码"
                  class="minimal-input"
                >
                  <template #prefix>
                    <el-icon class="input-icon"><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
            </div>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="电子邮箱地址"
                class="minimal-input"
              >
                <template #prefix>
                  <el-icon class="input-icon"><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-footer">
              <el-button
                type="primary"
                :loading="loading"
                class="submit-btn"
                @click="handleRegister"
                round
              >
                立即注册 <el-icon class="el-icon--right"><Right /></el-icon>
              </el-button>
            </div>
            
            <div class="bottom-links">
              <span>已有账号？</span>
              <router-link to="/login" class="link-text">直接登录</router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue' // 引入 reactive
import type { FormInstance, FormRules } from 'element-plus' // 引入 FormRules 类型定义
import { User, Lock, Right, CircleCheck, UserFilled, Iphone, Message } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: ''
})

const validatePass2 = (rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.value.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 修复点：使用 reactive 并显式指定 <FormRules> 类型，解决 TypeScript 报错
const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '格式不正确', trigger: 'blur' }
  ]
})

const loading = ref(false)
const registerFormRef = ref<FormInstance>()

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 提交前移除 confirmPassword 字段
    const { confirmPassword, ...registerData } = registerForm.value
    await userStore.register(registerData)
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 全局容器 (复用登录页风格) */
.register-wrapper {
  min-height: 100vh;
  width: 100%;
  position: relative;
  /* 背景图使用 1.JPG */
  background: url('/1.JPG') no-repeat center center;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

/* 黑色渐变遮罩 */
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

/* ============ 右侧：注册表单 ============ */
.form-section {
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-card {
  width: 100%;
  max-width: 480px;
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
  margin-bottom: 25px;
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

/* 表单布局 */
.form-row {
  display: flex;
  gap: 15px;
}

.half-width {
  flex: 1;
}

/* 极简输入框样式 */
.minimal-input :deep(.el-input__wrapper) {
  background-color: #f3f4f6;
  box-shadow: none !important;
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 10px 15px;
  height: 48px;
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
  background: #1a1a1a;
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
.animate-slide-right { animation: slideInRight 0.8s ease-out; }
.animate-slide-left { animation: slideInLeft 0.8s ease-out; }

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
  
  /* 在移动端取消双列，防止太挤 */
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>