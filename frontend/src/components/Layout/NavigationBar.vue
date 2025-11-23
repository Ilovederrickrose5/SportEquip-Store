<template>
  <header class="main-header">
    <nav class="top-nav">
      <div class="container">
        <div class="nav-left">
          <ul class="nav-list">
            <li class="nav-item active"><router-link to="/" class="nav-link active-link">首页</router-link></li>
            <li class="nav-item"><router-link to="/ball-sports" class="nav-link">球类运动</router-link></li>
            <li class="nav-item"><router-link to="/outdoor-adventure" class="nav-link">户外探险</router-link></li>
            <li class="nav-item"><router-link to="/fitness-training" class="nav-link">健身训练</router-link></li>
            <li class="nav-item"><router-link to="/cycling-sports" class="nav-link">骑行运动</router-link></li>
          </ul>
        </div>
        
        <div class="nav-right">
          <!-- 未登录状态显示登录按钮 -->
          <button v-if="!isLoggedIn" class="login-btn" @click="goToLogin">
            登录
          </button>
          
          <!-- 已登录状态显示个人中心 -->
          <UserProfileDropdown 
            v-else
            :user="user"
            :user-avatar="userAvatar"
            :username="username"
            @logout="handleLogout"
          />
        </div>
      </div>
    </nav>
  </header>
</template>

<script>
import UserProfileDropdown from '../UI/UserProfileDropdown.vue';

export default {
  name: 'NavigationBar',
  components: {
    UserProfileDropdown
  },
  data() {
    return {
      isLoggedIn: false,
      userAvatar: '/default-avatar.svg',
      username: '个人中心',
      user: {}
    }
  },
  computed: {
    hasToken() {
      return localStorage.getItem('token') && localStorage.getItem('token').length > 0
    }
  },
  mounted() {
    this.checkLoginStatus();
    window.addEventListener('storage', this.handleStorageChange);
    window.addEventListener('userAvatarUpdated', this.handleAvatarUpdated);
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    window.removeEventListener('storage', this.handleStorageChange);
    window.removeEventListener('userAvatarUpdated', this.handleAvatarUpdated);
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    checkLoginStatus() {
      const token = localStorage.getItem('token')
      const user = localStorage.getItem('user')
      
      if (token && user) {
        try {
          const userData = JSON.parse(user)
          this.isLoggedIn = true
          this.username = userData.username || '用户'
          this.user = userData
          this.userAvatar = userData.avatar || '/default-avatar.svg'
        } catch (e) {
          console.error('解析用户信息失败:', e)
        }
      }
    },
    
    handleStorageChange(event) {
      if (event.key === 'user') {
        try {
          const updatedUser = JSON.parse(event.newValue)
          this.user = updatedUser
          this.username = updatedUser.username || '用户'
          this.userAvatar = updatedUser.avatar || '/default-avatar.svg'
          this.isLoggedIn = true
        } catch (e) {
          console.error('解析更新后的用户信息失败:', e)
        }
      }
    },
    
    handleAvatarUpdated(event) {
      try {
        const storedUser = localStorage.getItem('user')
        if (storedUser) {
          const updatedUser = JSON.parse(storedUser)
          this.user = updatedUser
          this.username = updatedUser.username || '用户'
          this.userAvatar = updatedUser.avatar || '/default-avatar.svg'
          this.isLoggedIn = true
        }
      } catch (e) {
        console.error('更新头像失败:', e)
      }
    },
    
    goToProfile() {
      this.showDropdown = false;
      this.$router.push('/profile');
    },
    
    goToLogin() {
      this.$router.push('/login');
    },
    
    goToCart() {
      this.showDropdown = false;
      this.$router.push('/cart');
    },
    
    goToOrders() {
      this.showDropdown = false;
      this.$router.push('/orders');
    },
    
    goToOrderManagement() {
      this.showDropdown = false;
      this.$router.push('/admin/orders');
    },
    

    
    handleLogout(event) {
      if (event) {
        event.stopPropagation();
        event.preventDefault();
      }
      
      this.showDropdown = false;
      // 触发自定义事件，由父组件处理退出登录
      this.$emit('logout');
    },
    
    handleClickOutside(event) {
      const dropdown = this.$el.querySelector('.user-profile-dropdown');
      if (dropdown && !dropdown.contains(event.target)) {
        this.showDropdown = false;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.main-header {
  background-color: #fff;
  box-shadow: var(--box-shadow);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.top-nav {
  padding: 0;
}

.top-nav .container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.nav-left {
  flex: 1;
}

.nav-list {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-right: 1px;
}

.nav-link {
  display: block;
  padding: 18px 20px;
  color: var(--text-primary);
  text-decoration: none;
  transition: var(--transition-default);
  font-size: 16px;
  background-color: #fff;
  border-radius: var(--border-radius-large);
  margin: 4px 0;
}

.nav-link:hover {
  background-color: var(--primary-hover);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(30, 144, 255, 0.3);
}

.active-link {
  background-color: var(--primary-color);
  color: white;
  box-shadow: 0 4px 8px rgba(30, 144, 255, 0.3);
}

.nav-link:active {
  background-color: var(--primary-dark);
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(30, 144, 255, 0.3);
}

/* 个人中心相关样式已移至UserProfileDropdown组件 */

.login-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--border-radius-round);
  cursor: pointer;
  transition: var(--transition-default);
  box-shadow: 0 4px 8px rgba(30, 144, 255, 0.3);
}

.login-btn:hover {
  background-color: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(30, 144, 255, 0.4);
}

.login-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(30, 144, 255, 0.3);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .nav-link {
    padding: 18px 15px;
    font-size: 15px;
  }
}

@media (max-width: 768px) {
  .top-nav .container {
    flex-direction: column;
    padding: 10px;
  }
  
  .nav-list {
    flex-wrap: wrap;
    justify-content: center;
    margin-bottom: 15px;
  }
  
  .nav-link {
    padding: 12px 10px;
    font-size: 14px;
  }
}

@media (max-width: 576px) {
  .nav-link {
    padding: 10px 8px;
    font-size: 13px;
  }
  
  .user-profile {
    padding: 8px 15px;
  }
  
  .avatar {
    width: 30px;
    height: 30px;
    margin-right: 8px;
  }
  
  .username {
    font-size: 13px;
  }
}
</style>