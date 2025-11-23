<template>
  <div class="user-profile-dropdown">
    <div class="user-profile" @click="toggleDropdown">
      <div class="avatar">
        <img :src="userAvatar" alt="用户头像" class="avatar-img">
      </div>
      <span class="username">{{ username }}</span>
      <span class="dropdown-arrow" :class="{ 'rotate': showDropdown }">▼</span>
    </div>
    <!-- 下拉菜单 -->
    <div v-if="showDropdown" class="dropdown-menu">
      <div class="dropdown-item" @click.stop="goToProfile">
        <el-icon><User /></el-icon>
        <span>个人资料</span>
      </div>
      <div class="dropdown-item" @click.stop="goToCart">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
      </div>
      <div class="dropdown-item" @click.stop="goToOrders">
        <el-icon><Document /></el-icon>
        <span>我的订单</span>
      </div>
      <template v-if="isAdmin">
        <div class="dropdown-item" @click.stop="goToUserManagement">
          <el-icon><UserFilled /></el-icon>
          <span>用户管理</span>
        </div>
        <div class="dropdown-item" @click.stop="goToProductManagement">
          <el-icon><Box /></el-icon>
          <span>商品管理</span>
        </div>
        <div class="dropdown-item" @click.stop="goToOrderManagement">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </div>
      </template>
      <div class="dropdown-divider"></div>
      <div class="dropdown-item logout-item" @click.stop="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </div>
    </div>
  </div>
</template>

<script>
import { User, ShoppingCart, Document, UserFilled, Box, Tickets, SwitchButton } from '@element-plus/icons-vue';

export default {
  name: 'UserProfileDropdown',
  components: {
    User,
    ShoppingCart,
    Document,
    UserFilled,
    Box,
    Tickets,
    SwitchButton
  },
  props: {
    user: {
      type: Object,
      required: true
    },
    userAvatar: {
      type: String,
      default: '/default-avatar.svg'
    },
    username: {
      type: String,
      default: '个人中心'
    }
  },
  data() {
    return {
      showDropdown: false
    };
  },
  computed: {
    isAdmin() {
      return this.user.role === 'ADMIN' || this.user.role === '管理员';
    }
  },
  mounted() {
    document.addEventListener('click', this.handleClickOutside);
  },
  beforeUnmount() {
    document.removeEventListener('click', this.handleClickOutside);
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    goToProfile() {
      this.closeDropdown();
      this.$router.push('/profile');
    },
    goToCart() {
      this.closeDropdown();
      this.$router.push('/cart');
    },
    goToOrders() {
      this.closeDropdown();
      this.$router.push('/orders');
    },
    goToUserManagement() {
      this.closeDropdown();
      this.$router.push('/admin/users');
    },
    goToProductManagement() {
      this.closeDropdown();
      this.$router.push('/admin/products');
    },
    goToOrderManagement() {
      this.closeDropdown();
      this.$router.push('/admin/orders');
    },
    handleLogout() {
      this.closeDropdown();
      this.$emit('logout');
    },
    handleClickOutside(event) {
      const dropdown = this.$el.querySelector('.user-profile-dropdown');
      if (dropdown && !dropdown.contains(event.target)) {
        this.showDropdown = false;
      }
    },
    closeDropdown() {
      this.showDropdown = false;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.user-profile-dropdown {
  position: relative;
  display: inline-block;
  
  .user-profile {
    display: flex;
    align-items: center;
    padding: 10px 20px;
    cursor: pointer;
    transition: background-color var(--transition-normal);
    border-radius: var(--border-radius);
    
    &:hover {
      background-color: var(--primary-lighter);
    }
    
    .avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      overflow: hidden;
      margin-right: 10px;
      border: 2px solid var(--primary-color);
      
      .avatar-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }
    
    .username {
      color: var(--text-primary);
      font-size: 14px;
    }
    
    .dropdown-arrow {
      margin-left: 5px;
      font-size: 12px;
      transition: transform var(--transition-normal);
      
      &.rotate {
        transform: rotate(180deg);
      }
    }
  }
  
  .dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: var(--border-radius);
    box-shadow: var(--box-shadow);
    min-width: 180px;
    z-index: 1000;
    margin-top: 5px;
    
    .dropdown-item {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      color: var(--text-primary);
      cursor: pointer;
      transition: background-color var(--transition-normal);
      
      &:hover {
        background-color: var(--primary-lighter);
      }
      
      .el-icon {
        margin-right: 10px;
        font-size: 16px;
      }
    }
    
    .dropdown-divider {
      height: 1px;
      background-color: #ddd;
      margin: 4px 0;
    }
    
    .logout-item {
      color: var(--danger-color);
      
      &:hover {
        background-color: rgba(255, 71, 87, 0.1);
      }
    }
  }
}
</style>