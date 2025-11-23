<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <el-card class="login-form-card">
        <template #header>
          <div class="logo-container">
            <div class="logo-icon">🏃</div>
            <h1 class="app-name">运动装备商城</h1>
          </div>
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">请登录您的账号继续购物体验</p>
        </template>
        
        <BaseForm 
          ref="baseFormRef"
          :formData="loginForm"
          :rules="rules"
          :label-position="'top'"
          :label-width="''"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              show-password
              clearable
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-password">忘记密码？</a>
          </div>
          
          <el-alert
            v-if="errorMessage"
            :message="errorMessage"
            type="error"
            show-icon
            :closable="false"
            class="global-error"
          />
          
          <el-button 
            type="primary" 
            class="btn-block"
            :loading="isLoading"
            @click="handleLogin"
          >
            <span slot="icon">🚪</span>
            登录
          </el-button>
        </BaseForm>
        
        <div class="card-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="link">立即注册</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import axiosInstance from '../utils/axiosInstance'
import { API_ENDPOINTS } from '../config/api'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseForm from '../components/BaseForm.vue'

export default {
  name: 'LoginView',
  components: {
    BaseForm
  },
  setup() {
    const router = useRouter()
    const baseFormRef = ref(null)
    const loginForm = ref({
      username: '',
      password: ''
    })
    const rememberMe = ref(false)
    const errorMessage = ref('')
    const isLoading = ref(false)
    
    const rules = {
      username: [
        { required: true, message: '用户名不能为空', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '密码不能为空', trigger: 'blur' },
        { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
      ]
    }
    
    const handleLogin = async () => {
      if (!baseFormRef.value) return
      
      try {
        // 使用BaseForm的验证方法
        await baseFormRef.value.validate()
        
        isLoading.value = true
        errorMessage.value = ''
        
        // 清除可能存在的旧token，避免干扰新登录
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        
        const response = await axiosInstance.post(API_ENDPOINTS.auth.login, loginForm.value)
        
        // 登录成功后，正确存储用户信息和token到localStorage
        const userData = {
          username: response.data.username || '未知用户',
          id: response.data.id || '未知ID',
          role: response.data.role || '普通用户',
          avatar: response.data.avatar || ''
        }
        
        // 适配后端返回的token字段名
        const token = response.data.accessToken || response.data.token
        
        // 存储登录信息到localStorage
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('loginTimestamp', Date.now().toString())
        
        // 如果选择记住我，可以设置更长的过期时间等
        if (rememberMe.value) {
          localStorage.setItem('rememberMe', 'true')
        }
        
        // 登录成功后跳转到首页
        router.push('/')
      } catch (error) {
        // 表单验证失败不会进入这里，因为BaseForm会处理验证提示
        if (error.response) {
          // 服务器返回了响应
          if (error.response.status === 401) {
            errorMessage.value = '用户名或密码错误'
          } else {
            errorMessage.value = error.response.data?.message || error.response.data || '登录失败，请稍后重试'
          }
        } else if (error.request) {
          // 请求已发送但未收到响应
          errorMessage.value = '服务器无响应，请检查后端服务是否正常运行'
        } else if (error !== 'validation-failed') {
          // 避免捕获到表单验证失败的错误
          errorMessage.value = '请求配置错误: ' + error.message
        }
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      baseFormRef,
      loginForm,
      rememberMe,
      rules,
      errorMessage,
      isLoading,
      handleLogin
    }
  }
}
</script>

<style scoped>
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #2196f3, #03a9f4);
  padding: 20px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 420px;
}

.login-form-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideInUp 0.6s ease-out;
}

/* 使用Element Plus的header插槽样式 */
.login-form-card >>> .el-card__header {
  background: linear-gradient(135deg, #1976d2, #2196f3);
  color: white;
  padding: 30px 20px;
  text-align: center;
  border-bottom: none;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 3rem;
  margin-right: 16px;
  animation: float 3s ease-in-out infinite;
}

.app-name {
  font-size: 1.6rem;
  font-weight: 700;
  margin: 0;
}

.login-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: white;
}

.login-subtitle {
  font-size: 1rem;
  opacity: 0.9;
  margin: 0;
  color: white;
}

/* Element Plus内容区域 */
.login-form-card >>> .el-card__body {
  padding: 30px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.forgot-password {
  color: #1976d2;
  text-decoration: none;
  font-size: 14px;
}

.forgot-password:hover {
  color: #1565c0;
  text-decoration: underline;
}

.global-error {
  margin-bottom: 20px;
}

.btn-block {
  width: 100%;
  margin-top: 10px;
}

.card-footer {
  background-color: #f8f9fa;
  padding: 20px;
  text-align: center;
  border-top: 1px solid #e9ecef;
}

.card-footer span {
  font-size: 14px;
  color: #6c757d;
}

.link {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
}

.link:hover {
  color: #1565c0;
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }
  
  .login-form-wrapper {
    max-width: 100%;
  }
  
  .login-form-card >>> .el-card__header,
  .login-form-card >>> .el-card__body {
    padding: 24px;
  }
  
  .login-title {
    font-size: 1.5rem;
  }
  
  .logo-icon {
    font-size: 2.5rem;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 12px;
  }
  
  .login-form-card >>> .el-card__header,
  .login-form-card >>> .el-card__body,
  .card-footer {
    padding: 20px;
  }
  
  .login-title {
    font-size: 1.3rem;
  }
  
  .app-name {
    font-size: 1.4rem;
  }
  
  .logo-icon {
    font-size: 2rem;
  }
}

/* 新增的入场动画 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>