<template>
  <div class="profile-container">
    <header class="page-header">
      <button @click="$router.push('/')" class="btn-back">返回首页</button>
      <h1>个人中心</h1>
    </header>
    
    <div v-if="userInfo" class="profile-content">
      <!-- 个人资料展示和编辑 -->
      <div class="profile-section">
        <div class="section-header">
          <h2>个人资料</h2>
          <button @click="toggleEditMode" class="btn-action">
            {{ isEditMode ? '取消编辑' : '编辑资料' }}
          </button>
        </div>
        
        <!-- 资料展示视图 -->
        <div v-if="!isEditMode" class="profile-view">
          <ProfileDisplay :userInfo="userInfo" />
        </div>
        
        <!-- 资料编辑表单 -->
        <div v-else class="profile-edit">
          <!-- 头像上传组件 -->
          <AvatarUploader 
            :initialAvatar="userInfo.avatar" 
            :errors="errors"
            @avatar-updated="handleAvatarUpdated"
            @error="handleAvatarError"
          />
          
          <!-- 个人资料表单组件 -->
          <ProfileForm 
            :userData="userInfo"
            :isSubmitting="isSubmitting"
            :errors="errors"
            @submit="handleFormSubmit"
            @cancel="toggleEditMode"
            @validation-error="handleValidationError"
          />
        </div>
      </div>
    </div>
    
    <div v-else class="loading">加载中...</div>
  </div>
</template>

<script>
import axiosInstance from '../utils/axiosInstance'
import AvatarUploader from '../components/Profile/AvatarUploader.vue'
import ProfileForm from '../components/Profile/ProfileForm.vue'
import ProfileDisplay from '../components/Profile/ProfileDisplay.vue'

export default {
  name: 'UserProfileView',
  components: {
    AvatarUploader,
    ProfileForm,
    ProfileDisplay
  },
  data() {
    return {
      userInfo: null,
      isEditMode: false,
      isSubmitting: false,
      errors: {}
    }
  },
  mounted() {
    this.getUserInfo()
  },
  methods: {
    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await axiosInstance.get('/users/me')
        this.userInfo = response.data
      } catch (error) {
        console.error('获取用户信息失败', error)
        alert('获取用户信息失败，请稍后重试')
      }
    },
    
    // 切换编辑模式
    toggleEditMode() {
      if (this.isEditMode) {
        this.resetErrors()
      }
      this.isEditMode = !this.isEditMode
    },
    
    // 处理头像更新
    handleAvatarUpdated(avatarUrl) {
      // 更新用户头像信息
      this.userInfo.avatar = avatarUrl
      
      // 更新localStorage中的用户头像信息
      const savedUser = JSON.parse(localStorage.getItem('user'))
      if (savedUser) {
        savedUser.avatar = avatarUrl
        localStorage.setItem('user', JSON.stringify(savedUser))
      }
      
      // 触发自定义事件，通知其他组件（如首页导航栏）头像已更新
      window.dispatchEvent(new CustomEvent('userAvatarUpdated', { detail: { avatarUrl } }))
    },
    
    // 处理头像错误
    handleAvatarError(error) {
      this.errors = { ...this.errors, ...error }
    },
    
    // 处理表单提交
    async handleFormSubmit(formData) {
      this.isSubmitting = true
      try {
        let passwordChanged = false
        
        // 创建更新请求对象
        const updateRequest = {
          username: formData.username,
          email: formData.email,
          phone: formData.phone,
          address: formData.address
        }
        
        // 如果需要修改密码
        if (formData.needsPasswordChange) {
          await axiosInstance.put('/users/change-password', null, {
            params: {
              oldPassword: formData.oldPassword,
              newPassword: formData.newPassword
            }
          })
          passwordChanged = true
        }
        
        // 调用后端API更新用户信息
        const response = await axiosInstance.put(`/users/${this.userInfo.id}`, updateRequest)
        
        // 更新本地用户信息
        this.userInfo = response.data
        
        // 退出编辑模式
        this.isEditMode = false
        
        // 显示成功消息
        let message = '个人资料更新成功'
        if (passwordChanged) {
          message += '，密码已修改，请重新登录'
        }
        alert(message)
        
        // 如果修改了密码，提示用户重新登录
        if (passwordChanged) {
          // 这里可以添加自动登出逻辑
          // setTimeout(() => {
          //   window.location.reload()
          // }, 2000)
        }
      } catch (error) {
        console.error('更新个人资料失败', error)
        let errorMsg = '更新失败：'
        if (error.response?.data === 'Old password is incorrect') {
          errorMsg = '当前密码错误，请重新输入'
        } else if (typeof error.response?.data === 'string') {
          errorMsg = error.response.data
        } else {
          errorMsg += (error.response?.data || '请稍后重试')
        }
        alert(errorMsg)
      } finally {
        this.isSubmitting = false
        this.resetErrors()
      }
    },
    
    // 处理表单验证错误
    handleValidationError(errors) {
      this.errors = errors
    },
    
    // 重置错误信息
    resetErrors() {
      this.errors = {}
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../assets/css/variables.scss';

.profile-container {
  max-width: 800px;
  margin: 20px auto;
  padding: var(--spacing-lg);
  background-color: var(--background-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  
  .page-header {
    margin-bottom: var(--spacing-xl);
    
    .btn-back {
      background-color: var(--background-secondary);
      border: 1px solid var(--border-color);
      color: var(--text-secondary);
      padding: 8px 16px;
      border-radius: var(--border-radius);
      cursor: pointer;
      font-size: var(--font-size-sm);
      margin-bottom: var(--spacing-md);
      transition: all var(--transition-default);
      
      &:hover {
        background-color: var(--background-hover);
        border-color: var(--border-hover);
      }
    }
    
    h1 {
      color: var(--text-primary);
      margin-bottom: 0;
      text-align: center;
      font-size: var(--font-size-xl);
      font-weight: 600;
    }
  }
  
  .profile-content {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xl);
  }
  
  .profile-section {
    background-color: var(--background-card);
    border-radius: var(--border-radius-md);
    padding: var(--spacing-lg);
    box-shadow: var(--shadow-sm);
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-lg);
      padding-bottom: var(--spacing-md);
      border-bottom: 1px solid var(--border-color);
      
      h2 {
        color: var(--text-primary);
        margin: 0;
        font-size: var(--font-size-lg);
        font-weight: 600;
      }
      
      .btn-action {
        background-color: var(--primary-color);
        color: white;
        padding: 8px 16px;
        border: none;
        border-radius: var(--border-radius);
        font-size: var(--font-size-sm);
        cursor: pointer;
        transition: background-color var(--transition-default);
        
        &:hover {
          background-color: var(--primary-dark);
        }
      }
    }
    
    .profile-view,
    .profile-edit {
      width: 100%;
    }
  }
  
  .loading {
    text-align: center;
    padding: var(--spacing-xxl);
    color: var(--text-secondary);
    font-size: var(--font-size-md);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-container {
    padding: var(--spacing-md);
    margin: var(--spacing-md);
    
    .profile-section {
      .section-header {
        flex-direction: column;
        align-items: flex-start;
        gap: var(--spacing-md);
        
        .btn-action {
          width: 100%;
        }
      }
    }
  }
}
</style>