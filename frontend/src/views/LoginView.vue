<template>
  <div class="login-container">
    <!-- 左上角 Logo -->
    <div class="logo-corner">
      <div class="logo-text">Sport-Equiment</div>
    </div>
    
    <!-- 登录表单区域 -->
    <div class="login-wrapper">
      <h1 class="login-title">登录</h1>
      
      <el-form 
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
      >
        <!-- 用户名输入框 -->
        <div class="input-group">
          <input
            v-model="loginForm.username"
            type="text"
            placeholder="用户名"
            class="main-input"
          />
        </div>
        
        <!-- 密码输入框 -->
        <div class="input-group">
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            class="main-input"
          />
        </div>
        
        <!-- 忘记密码 -->
        <div class="forgot-password-wrapper">
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>
        
        <!-- 登录按钮 -->
        <el-button 
          type="primary" 
          class="login-btn"
          :loading="isLoading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      
      <!-- 注册链接 -->
      <div class="register-link">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axiosInstance from '../utils/axiosInstance'
import { API_ENDPOINTS } from '../config/api'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const loginForm = reactive({
      username: '',
      password: ''
    })
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
      if (!formRef.value) return
      
      try {
        isLoading.value = true
        
        const response = await axiosInstance.post(API_ENDPOINTS.auth.login, loginForm)
        
        const userData = {
          username: response.data.username || '未知用户',
          id: response.data.id || '未知ID',
          role: response.data.role || '普通用户',
          avatar: response.data.avatar || ''
        }
        
        const token = response.data.accessToken || response.data.token
        
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(userData))
        localStorage.setItem('loginTimestamp', Date.now().toString())
        
        router.push('/')
      } catch (error) {
        if (error.response) {
          if (error.response.status === 401) {
            alert('用户名或密码错误')
          } else {
            alert(error.response.data?.message || '登录失败，请稍后重试')
          }
        } else {
          alert('登录失败，请稍后重试')
        }
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      formRef,
      loginForm,
      isLoading,
      rules,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background-color: #1a1a1a;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

.logo-corner {
  position: absolute;
  top: 30px;
  left: 30px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
}

.login-wrapper {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 40px;
}

.login-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  width: 100%;
}

.main-input {
  width: 100%;
  height: 48px;
  background-color: #2d2d2d;
  border: 1px solid #3d3d3d;
  border-radius: 0;
  color: #ffffff;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  
  &::placeholder {
    color: #666666;
  }
  
  &:focus {
    border-color: #4CAF50;
    box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
  }
}

.forgot-password-wrapper {
  display: flex;
  justify-content: flex-end;
}

.forgot-password {
  color: #888888;
  font-size: 13px;
  text-decoration: none;
  
  &:hover {
    color: #4CAF50;
    text-decoration: underline;
  }
}

.login-btn {
  background-color: #2d5a2d !important;
  border: none !important;
  height: 48px !important;
  font-size: 16px !important;
  color: #ffffff !important;
  border-radius: 0 !important;
  
  &:hover:not(:disabled) {
    background-color: #3a6b3a !important;
  }
  
  &:disabled {
    background-color: #4a4a4a !important;
    cursor: not-allowed !important;
  }
  
  ::v-deep .el-button {
    border-radius: 0 !important;
  }
}

.register-link {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888888;
  font-size: 14px;
  
  .link {
    color: #4CAF50;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
