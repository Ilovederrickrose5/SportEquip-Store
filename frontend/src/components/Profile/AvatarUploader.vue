<template>
  <div class="avatar-upload-component">
    <div class="avatar-preview">
      <img :src="avatarUrl" alt="用户头像" class="avatar-preview-img">
      <div class="avatar-upload-overlay">
        <input 
          type="file" 
          id="avatar-upload" 
          accept="image/*" 
          @change="handleAvatarChange" 
          class="avatar-upload-input"
        >
        <label for="avatar-upload" class="avatar-upload-label">
          更换头像
        </label>
      </div>
    </div>
    <span v-if="errors.avatar" class="error-msg">{{ errors.avatar }}</span>
    <span v-if="uploading" class="uploading-msg">上传中...</span>
  </div>
</template>

<script>
import axiosInstance from '../../utils/axiosInstance'

export default {
  name: 'AvatarUploader',
  props: {
    initialAvatar: {
      type: String,
      default: null
    },
    errors: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      uploading: false,
      selectedAvatar: null
    }
  },
  computed: {
    avatarUrl() {
      if (this.selectedAvatar) {
        return URL.createObjectURL(this.selectedAvatar)
      }
      return this.initialAvatar || '/default-avatar.svg'
    }
  },
  methods: {
    async handleAvatarChange(event) {
      const file = event.target.files[0]
      if (file) {
        // 验证文件类型
        if (!file.type.startsWith('image/')) {
          this.$emit('error', { avatar: '只能上传图片文件' })
          return
        }
        // 验证文件大小（最大5MB）
        if (file.size > 5 * 1024 * 1024) {
          this.$emit('error', { avatar: '图片大小不能超过5MB' })
          return
        }
        
        // 清除错误信息
        this.$emit('error', {})
        
        // 保存选择的文件
        this.selectedAvatar = file
        
        // 上传头像
        const avatarUrl = await this.uploadAvatar()
        if (avatarUrl) {
          this.$emit('avatar-updated', avatarUrl)
        }
      }
    },
    
    async uploadAvatar() {
      if (!this.selectedAvatar) {
        return null
      }
      
      this.uploading = true
      try {
        const formData = new FormData()
        formData.append('file', this.selectedAvatar)
        
        const response = await axiosInstance.post('/files/upload/avatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
        
        return response.data.fileUrl
      } catch (error) {
        console.error('上传头像失败', error)
        this.$emit('error', { avatar: '上传头像失败，请稍后重试' })
        return null
      } finally {
        this.uploading = false
      }
    },
    
    reset() {
      this.selectedAvatar = null
    }
  },
  
  // 清理Blob对象引用，避免内存泄漏
  beforeUnmount() {
    if (this.selectedAvatar) {
      URL.revokeObjectURL(this.avatarUrl)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.avatar-upload-component {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  
  .avatar-preview {
    position: relative;
    display: inline-block;
    margin: 0 auto;
    
    .avatar-preview-img {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      object-fit: cover;
      border: 3px solid var(--border-color);
      box-shadow: var(--shadow-sm);
    }
    
    .avatar-upload-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity var(--transition-default);
      cursor: pointer;
      
      &:hover {
        opacity: 1;
      }
    }
    
    .avatar-upload-input {
      position: absolute;
      width: 100%;
      height: 100%;
      opacity: 0;
      cursor: pointer;
    }
    
    .avatar-upload-label {
      color: white;
      font-size: 14px;
      font-weight: 500;
      text-align: center;
    }
  }
  
  .uploading-msg {
    color: var(--success-color);
    font-size: 12px;
    margin-top: var(--spacing-xs);
  }
  
  .error-msg {
    color: var(--danger-color);
    font-size: 12px;
    margin-top: var(--spacing-xs);
  }
}
</style>