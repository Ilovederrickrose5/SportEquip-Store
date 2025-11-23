<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <el-card class="register-form-card">
        <template #header>
          <div class="logo-container">
            <div class="logo-icon">🏃</div>
            <h1 class="app-name">运动装备商城</h1>
          </div>
          <h2 class="register-title">用户注册</h2>
        </template>
        
        <BaseForm 
          ref="baseFormRef"
          :formData="registerForm"
          :rules="rules"
          :label-position="'top'"
          :label-width="''"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              type="email"
              placeholder="请输入邮箱"
              prefix-icon="el-icon-message"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              show-password
              clearable
            />
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              type="tel"
              placeholder="请输入手机号（选填）"
              prefix-icon="el-icon-phone"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="地址">
            <el-input
              v-model="registerForm.address"
              placeholder="请输入地址（选填）"
              prefix-icon="el-icon-location"
              clearable
            />
          </el-form-item>
          
          <!-- 错误提示现在通过ElMessage弹窗显示 -->
          
          <el-alert
            v-if="successMessage"
            :message="successMessage"
            type="success"
            show-icon
            :closable="false"
            class="global-message"
          />
          
          <el-button 
            type="primary" 
            class="btn-center"
            :loading="isLoading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </BaseForm>
        
        <div class="card-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BaseForm from '../components/BaseForm.vue'

export default {
  name: 'RegisterView',
  components: {
    BaseForm
  },
  setup() {
    const router = useRouter()
    const baseFormRef = ref(null)
    const registerForm = ref({
      username: '',
      email: '',
      password: '',
      phone: '',
      address: ''
    })
    // 不再使用Vue的响应式引用，而是直接操作DOM
    // const errorMessage = ref('')
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
      if (!baseFormRef.value) return
      
      try {
        // 使用BaseForm的验证方法
        await baseFormRef.value.validate()
        
        isLoading.value = true
        // ElMessage不需要手动清空容器
        successMessage.value = ''
        
        const response = await axios.post('http://localhost:8080/api/auth/register', registerForm.value)
        
        successMessage.value = '注册成功！即将跳转到登录页面'
        
        // 清空表单
        if (baseFormRef.value) {
          baseFormRef.value.resetFields()
        }
        
        // 2秒后跳转到登录页面
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } catch (error) {
        // 表单验证失败不会进入这里，因为BaseForm会处理验证提示
        if (error !== 'validation-failed') {
          if (error.response) {
            // 服务器返回错误响应
            console.log('错误响应:', error.response);
            console.log('错误数据:', error.response.data);
            
            // 简单直接的错误消息设置
            let finalErrorMessage = '';
            
            // 检查错误数据中是否包含邮箱已被使用的信息
            if (error.response.data && error.response.data.message) {
              const message = error.response.data.message;
              console.log('错误消息:', message);
              
              if (message.includes('邮箱已被使用') || message.includes('邮箱已被注册')) {
                finalErrorMessage = '邮箱已被注册，请更换其他邮箱';
              } else if (message.includes('已存在') || message.includes('exist')) {
                finalErrorMessage = '用户名或邮箱已存在，请更换后重试';
              } else {
                finalErrorMessage = message;
              }
            } else {
              // 默认错误消息
              finalErrorMessage = '注册失败，请稍后重试';
            }
            
            console.log('设置错误消息:', finalErrorMessage);
            
            // 使用Element Plus的ElMessage组件显示红色弹窗错误提示
            ElMessage.error({
              message: finalErrorMessage,
              duration: 3000, // 3秒后自动关闭
              showClose: true // 显示关闭按钮
            });
            console.log('通过ElMessage弹窗设置了错误消息:', finalErrorMessage);
          } else {
            // 使用Element Plus的ElMessage组件显示网络错误弹窗
            ElMessage.error({
              message: '网络错误，请检查您的网络连接',
              duration: 3000,
              showClose: true
            });
          }
        }
      } finally {
        isLoading.value = false
      }
    }
    
    return {
      baseFormRef,
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
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #2196f3, #03a9f4);
  padding: 20px;
}

.register-form-wrapper {
  width: 100%;
  max-width: 450px;
}

.register-form-card {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  animation: slideInUp 0.6s ease-out;
}

/* 使用Element Plus的header插槽样式 */
.register-form-card >>> .el-card__header {
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

.register-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
  color: white;
}

/* Element Plus内容区域 */
.register-form-card >>> .el-card__body {
  padding: 30px;
}

.global-message {
  margin-bottom: 20px;
}

/* 错误消息现在通过ElMessage弹窗显示，不再需要这些样式 */

.btn-center {
  display: block;
  margin: 10px auto 0;
  width: auto;
  min-width: 120px;
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
  .register-container {
    padding: 16px;
  }
  
  .register-form-wrapper {
    max-width: 100%;
  }
  
  .register-form-card >>> .el-card__header,
  .register-form-card >>> .el-card__body {
    padding: 24px;
  }
  
  .register-title {
    font-size: 1.5rem;
  }
  
  .logo-icon {
    font-size: 2.5rem;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 12px;
  }
  
  .register-form-card >>> .el-card__header,
  .register-form-card >>> .el-card__body,
  .card-footer {
    padding: 20px;
  }
  
  .register-title {
    font-size: 1.3rem;
  }
  
  .app-name {
    font-size: 1.4rem;
  }
  
  .logo-icon {
    font-size: 2rem;
  }
}

/* 入场动画 */
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