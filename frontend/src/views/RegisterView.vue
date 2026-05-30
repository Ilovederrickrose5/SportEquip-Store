<template>
  <div class="register-container">
    <!-- 左上角 Logo -->
    <div class="logo-corner">
      <div class="logo-text">Sport-Equiment</div>
    </div>
    
    <!-- 注册表单区域 -->
    <div class="register-wrapper">
      <h1 class="register-title">注册</h1>
      
      <el-form 
        ref="formRef"
        :model="registerForm"
        :rules="rules"
        class="register-form"
      >
        <!-- 用户名输入框 -->
        <div class="input-group">
          <input
            v-model="registerForm.username"
            type="text"
            placeholder="用户名"
            class="main-input"
          />
        </div>
        
        <!-- 邮箱输入框 -->
        <div class="input-group">
          <input
            v-model="registerForm.email"
            type="email"
            placeholder="邮箱"
            class="main-input"
          />
        </div>
        
        <!-- 密码输入框 -->
        <div class="input-group">
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            class="main-input"
          />
        </div>
        
        <!-- 手机号输入框 -->
        <div class="input-group">
          <input
            v-model="registerForm.phone"
            type="tel"
            placeholder="手机号（选填）"
            class="main-input"
          />
        </div>
        
        <!-- 地址输入框 -->
        <div class="input-group">
          <input
            v-model="registerForm.address"
            type="text"
            placeholder="地址（选填）"
            class="main-input"
          />
        </div>
        
        <!-- 成功提示 -->
        <el-alert
          v-if="successMessage"
          :message="successMessage"
          type="success"
          show-icon
          :closable="false"
          class="global-message"
        />
        
        <!-- 注册按钮 -->
        <el-button 
          type="primary" 
          class="register-btn"
          :loading="isLoading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>
      
      <!-- 登录链接 -->
      <div class="login-link">
        <span>已有账号？</span>
        <router-link to="/login" class="link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import axiosInstance from '../utils/axiosInstance'
import { API_ENDPOINTS } from '../config/api'
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const registerForm = reactive({
      username: '',
      email: '',
      password: '',
      phone: '',
      address: ''
    })
    const successMessage = ref('')
    const isLoading = ref(false)
    
    // 自定义手机号验证规则
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        return callback()
      }
      const phoneRegex = /^1[3-9]\d{9}$/
      if (!phoneRegex.test(value)) {
        return callback(new Error('请输入有效的手机号'))
      }
      callback()
    }
    
    const rules = {
      username: [
        { required: true, message: '用户名不能为空', trigger: 'blur' },
        { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '邮箱不能为空', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '密码不能为空', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
      ],
      phone: [
        { validator: validatePhone, trigger: 'blur' }
      ]
    }
    
    const handleRegister = async () => {
      if (!formRef.value) return
      
      try {
        isLoading.value = true
        successMessage.value = ''
        
        const response = await axiosInstance.post(API_ENDPOINTS.auth.register, registerForm)
        
        successMessage.value = '注册成功！即将跳转到登录页面'
        
        // 2秒后跳转到登录页面
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } catch (error) {
        let finalErrorMessage = '';
        
        // 检查是否有响应数据
        const response = error.response || (error.response && error.response);
        
        if (response) {
          // 有服务器响应
          if (response.data && response.data.message) {
            const message = response.data.message;
            
            if (message.includes('邮箱已被使用') || message.includes('邮箱已被注册')) {
              finalErrorMessage = '邮箱已被注册，请更换其他邮箱';
            } else if (message.includes('已存在') || message.includes('exist')) {
              finalErrorMessage = '用户名或邮箱已存在，请更换后重试';
            } else {
              finalErrorMessage = message;
            }
          } else if (response.status === 401) {
            finalErrorMessage = '注册接口未授权，请联系管理员';
          } else if (response.status === 500) {
            finalErrorMessage = '服务器内部错误，请稍后重试';
          } else {
            finalErrorMessage = `注册失败，状态码: ${response.status}`;
          }
        } else if (error.message) {
          // 检查错误消息
          if (error.message.includes('网络') || error.message.includes('Network')) {
            finalErrorMessage = '网络错误，请检查您的网络连接';
          } else if (error.message.includes('未授权')) {
            finalErrorMessage = '注册接口未授权，请联系管理员';
          } else {
            finalErrorMessage = error.message;
          }
        } else {
          finalErrorMessage = '注册失败，请稍后重试';
        }
        
        ElMessage.error({
          message: finalErrorMessage,
          duration: 3000,
          showClose: true
        });
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      formRef,
      registerForm,
      rules,
      successMessage,
      isLoading,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
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

.register-wrapper {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.register-title {
  font-size: 32px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 40px;
}

.register-form {
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
    border-color: #666;
    box-shadow: 0 0 0 2px rgba(102, 102, 102, 0.2);
  }
}

.global-message {
  margin-bottom: 10px;
}

.register-btn {
  background-color: #333 !important;
  border: none !important;
  height: 48px !important;
  font-size: 16px !important;
  color: #ffffff !important;
  border-radius: 0 !important;
  
  &:hover:not(:disabled) {
    background-color: #555 !important;
  }
  
  &:disabled {
    background-color: #4a4a4a !important;
    cursor: not-allowed !important;
  }
  
  ::v-deep .el-button {
    border-radius: 0 !important;
  }
}

.login-link {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888888;
  font-size: 14px;
  
  .link {
    color: #666;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
