<template>
  <div class="profile-display">
    <!-- 头像展示 -->
    <div class="profile-avatar">
      <img :src="avatarUrl" alt="用户头像" class="avatar-img">
    </div>
    
    <!-- 个人信息展示 -->
    <div class="profile-info">
      <div class="profile-item">
        <span class="label">用户名:</span>
        <span class="value">{{ userInfo.username }}</span>
      </div>
      
      <div class="profile-item">
        <span class="label">邮箱:</span>
        <span class="value">{{ userInfo.email }}</span>
      </div>
      
      <div v-if="userInfo.phone" class="profile-item">
        <span class="label">手机号:</span>
        <span class="value">{{ userInfo.phone }}</span>
      </div>
      
      <div v-if="userInfo.address" class="profile-item">
        <span class="label">地址:</span>
        <span class="value">{{ userInfo.address }}</span>
      </div>
      
      <div class="profile-item">
        <span class="label">注册时间:</span>
        <span class="value">{{ formatDate(userInfo.createdAt) }}</span>
      </div>
      
      <div class="profile-item">
        <span class="label">最近更新:</span>
        <span class="value">{{ formatDate(userInfo.updatedAt) }}</span>
      </div>
      
      <div class="profile-item">
        <span class="label">用户身份:</span>
        <span class="value user-role" :class="getRoleClass(userInfo.role)">
          {{ getUserRole(userInfo.role) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileDisplay',
  props: {
    userInfo: {
      type: Object,
      required: true
    }
  },
  computed: {
    avatarUrl() {
      return this.userInfo.avatar || '/default-avatar.svg'
    }
  },
  methods: {
    // 获取用户角色中文显示
    getUserRole(role) {
      if (!role) return '未知';
      // 根据角色代码返回对应的中文名称
      if (role === 'ADMIN' || role === '管理员') {
        return '管理员';
      } else if (role === 'USER' || role === '普通用户') {
        return '普通用户';
      }
      return role;
    },
    
    // 获取角色对应的CSS类名
    getRoleClass(role) {
      if (role === 'ADMIN' || role === '管理员') {
        return 'role-admin';
      }
      return 'role-user';
    },
    
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.profile-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .profile-avatar {
    display: flex;
    justify-content: center;
    margin-bottom: var(--spacing-lg);
    
    .avatar-img {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      object-fit: cover;
      border: 3px solid var(--border-color);
      box-shadow: var(--shadow-sm);
    }
  }
  
  .profile-info {
    width: 100%;
    max-width: 500px;
  }
  
  .profile-item {
    display: flex;
    align-items: center;
    padding: var(--spacing-sm) 0;
    border-bottom: 1px solid var(--border-color);
    
    .label {
      font-weight: 600;
      color: var(--text-secondary);
      min-width: 80px;
      margin-right: var(--spacing-md);
    }
    
    .value {
      color: var(--text-primary);
      flex: 1;
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .user-role {
    padding: 4px 8px;
    border-radius: var(--border-radius);
    font-size: var(--font-size-xs);
    font-weight: 500;
    
    &.role-admin {
      background-color: var(--primary-light);
      color: var(--primary-color);
    }
    
    &.role-user {
      background-color: var(--success-light);
      color: var(--success-color);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile-display {
    .profile-info {
      width: 100%;
    }
    
    .profile-item {
      flex-direction: column;
      align-items: flex-start;
      
      .label {
        min-width: auto;
        margin-right: 0;
        margin-bottom: 4px;
      }
    }
  }
}
</style>