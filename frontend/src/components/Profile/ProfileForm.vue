<template>
  <div class="profile-form">
    <!-- 基本信息表单 -->
    <div class="basic-info-section">
      <div class="form-group">
        <label for="username">用户名:</label>
        <input 
          type="text" 
          id="username" 
          v-model="form.username" 
          placeholder="用户名" 
          :class="{ 'error': errors.username }"
        >
        <span v-if="errors.username" class="error-msg">{{ errors.username }}</span>
      </div>
      
      <div class="form-group">
        <label for="email">邮箱:</label>
        <input 
          type="email" 
          id="email" 
          v-model="form.email" 
          placeholder="邮箱" 
          :class="{ 'error': errors.email }"
        >
        <span v-if="errors.email" class="error-msg">{{ errors.email }}</span>
      </div>
      
      <div class="form-group">
        <label for="phone">手机号:</label>
        <input 
          type="text" 
          id="phone" 
          v-model="form.phone" 
          placeholder="手机号（选填）" 
          :class="{ 'error': errors.phone }"
        >
        <span v-if="errors.phone" class="error-msg">{{ errors.phone }}</span>
      </div>
      
      <div class="form-group">
        <label for="address">地址:</label>
        <input 
          type="text" 
          id="address" 
          v-model="form.address" 
          placeholder="地址（选填）" 
          :class="{ 'error': errors.address }"
        >
        <span v-if="errors.address" class="error-msg">{{ errors.address }}</span>
      </div>
    </div>
    
    <!-- 密码修改部分 -->
    <div class="password-section">
      <h3>修改密码（选填）</h3>
      <div class="form-group">
        <label for="oldPassword">当前密码:</label>
        <input 
          type="password" 
          id="oldPassword" 
          v-model="form.oldPassword" 
          placeholder="请输入当前密码" 
          :class="{ 'error': errors.oldPassword }"
        >
        <span v-if="errors.oldPassword" class="error-msg">{{ errors.oldPassword }}</span>
      </div>
      
      <div class="form-group">
        <label for="newPassword">新密码:</label>
        <input 
          type="password" 
          id="newPassword" 
          v-model="form.newPassword" 
          placeholder="请输入新密码" 
          :class="{ 'error': errors.newPassword }"
        >
        <span 
          v-if="passwordStrengthMessage" 
          class="password-strength" 
          :class="passwordStrengthClass"
        >
          {{ passwordStrengthMessage }}
        </span>
        <span v-if="errors.newPassword" class="error-msg">{{ errors.newPassword }}</span>
      </div>
      
      <div class="form-group">
        <label for="confirmPassword">确认新密码:</label>
        <input 
          type="password" 
          id="confirmPassword" 
          v-model="form.confirmPassword" 
          placeholder="请再次输入新密码" 
          :class="{ 'error': errors.confirmPassword }"
        >
        <span v-if="errors.confirmPassword" class="error-msg">{{ errors.confirmPassword }}</span>
      </div>
    </div>
    
    <!-- 操作按钮 -->
    <div class="form-actions">
      <button 
        @click="handleSubmit" 
        class="btn btn-primary" 
        :disabled="isSubmitting"
      >
        {{ isSubmitting ? '保存中...' : '保存' }}
      </button>
      <button @click="handleCancel" class="btn btn-secondary">取消</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileForm',
  props: {
    userData: {
      type: Object,
      required: true
    },
    isSubmitting: {
      type: Boolean,
      default: false
    },
    errors: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      form: {
        username: '',
        email: '',
        phone: '',
        address: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordStrengthMessage: '',
      passwordStrengthClass: ''
    }
  },
  watch: {
    userData: {
      handler(newData) {
        this.initializeForm(newData)
      },
      immediate: true
    },
    // 监听新密码变化，更新密码强度提示
    'form.newPassword'(newValue) {
      this.checkPasswordStrength(newValue)
    }
  },
  methods: {
    initializeForm(userData) {
      this.form = {
        username: userData.username || '',
        email: userData.email || '',
        phone: userData.phone || '',
        address: userData.address || '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    },
    
    handleSubmit() {
      const validationErrors = this.validateForm()
      
      if (Object.keys(validationErrors).length === 0) {
        // 发送表单数据给父组件
        this.$emit('submit', {
          ...this.form,
          needsPasswordChange: !!(this.form.oldPassword || this.form.newPassword || this.form.confirmPassword)
        })
      } else {
        this.$emit('validation-error', validationErrors)
      }
    },
    
    handleCancel() {
      this.resetForm()
      this.$emit('cancel')
    },
    
    validateForm() {
      const errors = {}
      
      // 验证必填字段
      if (!this.form.username.trim()) {
        errors.username = '用户名不能为空'
      } else if (this.form.username.length < 3 || this.form.username.length > 20) {
        errors.username = '用户名长度必须在3-20个字符之间'
      }
      
      if (!this.form.email.trim()) {
        errors.email = '邮箱不能为空'
      } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
        if (!emailRegex.test(this.form.email)) {
          errors.email = '请输入有效的邮箱地址'
        }
      }
      
      // 验证可选字段的长度
      if (this.form.phone && this.form.phone.length > 20) {
        errors.phone = '手机号长度不能超过20个字符'
      }
      
      if (this.form.address && this.form.address.length > 255) {
        errors.address = '地址长度不能超过255个字符'
      }
      
      // 密码验证（如果填写了任何密码字段，都需要验证）
      if ((this.form.oldPassword || this.form.newPassword || this.form.confirmPassword)) {
        if (!this.form.oldPassword.trim()) {
          errors.oldPassword = '请输入当前密码'
        }
        
        if (!this.form.newPassword.trim()) {
          errors.newPassword = '请输入新密码'
        } else if (this.form.newPassword.length < 8) {
          errors.newPassword = '新密码长度不能少于8个字符'
        }
        
        if (!this.form.confirmPassword.trim()) {
          errors.confirmPassword = '请确认新密码'
        } else if (this.form.newPassword !== this.form.confirmPassword) {
          errors.confirmPassword = '两次输入的密码不一致'
        }
      }
      
      return errors
    },
    
    checkPasswordStrength(password) {
      if (!password) {
        this.passwordStrengthMessage = ''
        this.passwordStrengthClass = ''
        return
      }
      
      let strength = 0
      let message = ''
      let className = ''
      
      // 检查密码长度
      if (password.length >= 8) strength++
      if (password.length >= 12) strength++
      
      // 检查是否包含数字
      if (/\d/.test(password)) strength++
      
      // 检查是否包含小写字母
      if (/[a-z]/.test(password)) strength++
      
      // 检查是否包含大写字母
      if (/[A-Z]/.test(password)) strength++
      
      // 检查是否包含特殊字符
      if (/[^A-Za-z0-9]/.test(password)) strength++
      
      // 设置强度消息和样式
      if (strength <= 2) {
        message = '弱密码，请使用更复杂的密码'
        className = 'weak'
      } else if (strength <= 4) {
        message = '中等强度'
        className = 'medium'
      } else {
        message = '强密码，安全'
        className = 'strong'
      }
      
      this.passwordStrengthMessage = message
      this.passwordStrengthClass = className
    },
    
    resetForm() {
      this.initializeForm(this.userData)
      this.passwordStrengthMessage = ''
      this.passwordStrengthClass = ''
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.profile-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  
  .basic-info-section {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .password-section {
    margin-top: var(--spacing-md);
    padding-top: var(--spacing-md);
    border-top: 1px solid var(--border-color);
    
    h3 {
      margin: 0 0 var(--spacing-md) 0;
      color: var(--text-secondary);
      font-size: var(--font-size-md);
      font-weight: 600;
    }
    
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }
  
  .form-group {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xs);
    
    label {
      font-weight: 500;
      color: var(--text-secondary);
      font-size: var(--font-size-sm);
    }
    
    input {
      padding: 10px;
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      font-size: var(--font-size-sm);
      transition: all var(--transition-default);
      
      &:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: var(--shadow-sm);
      }
      
      &.error {
        border-color: var(--danger-color);
      }
    }
    
    .error-msg {
      color: var(--danger-color);
      font-size: var(--font-size-xs);
    }
    
    .password-strength {
      font-size: var(--font-size-xs);
      padding: 3px 0;
      
      &.weak {
        color: var(--danger-color);
      }
      
      &.medium {
        color: var(--warning-color);
      }
      
      &.strong {
        color: var(--success-color);
      }
    }
  }
  
  .form-actions {
    display: flex;
    gap: var(--spacing-md);
    margin-top: var(--spacing-lg);
    
    .btn {
      padding: 8px 16px;
      border: none;
      border-radius: var(--border-radius);
      font-size: var(--font-size-sm);
      cursor: pointer;
      transition: background-color var(--transition-default);
      
      &.btn-primary {
        background-color: var(--primary-color);
        color: white;
        
        &:hover:not(:disabled) {
          background-color: var(--primary-dark);
        }
        
        &:disabled {
          background-color: var(--text-disabled);
          cursor: not-allowed;
        }
      }
      
      &.btn-secondary {
        background-color: var(--background-secondary);
        color: var(--text-secondary);
        border: 1px solid var(--border-color);
        
        &:hover {
          background-color: var(--background-hover);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-form {
    .form-actions {
      flex-direction: column;
      
      .btn {
        width: 100%;
      }
    }
  }
}
</style>