<template>
  <div class="admin-user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button 
          type="primary" 
          @click="$router.push('/admin/products')"
          class="header-btn"
        >
          商品管理
        </el-button>
        <el-button 
          @click="$router.push({ name: 'home' })"
          class="header-btn"
        >
          返回首页
        </el-button>
      </div>
    </div>
    
    <!-- 搜索和筛选 -->
    <user-search-filter 
      v-model="filterModel"
      @search-change="handleSearchChange"
      @role-change="handleRoleChange"
    />
    
    <!-- 用户列表 -->
    <user-table 
      :users="filteredUsers"
      :is-loading="isLoading"
      :deleting-user-id="deletingUserId"
      @edit="handleEdit"
      @delete="handleDeleteConfirm"
    />
    
    <!-- 分页 -->
    <user-pagination 
      :total="filteredUsersTotal"
      :current-page="currentPage"
      :page-size="pageSize"
      @current-change="handlePageChange"
      @size-change="handlePageSizeChange"
    />
    
    <!-- 编辑用户弹窗 -->
    <edit-user-modal 
      :visible="showEditModal"
      :user-data="editingUser"
      :is-submitting="isLoading"
      :is-current-user="isCurrentUser(editingUser)"
      @close="closeEditModal"
      @submit="handleUpdateUser"
    />
    
    <!-- 删除确认弹窗 -->
    <delete-confirm-modal 
      :visible="showDeleteConfirm"
      :user="userToDelete"
      :is-submitting="isLoading"
      @cancel="cancelDelete"
      @confirm="handleDeleteUser"
    />
  </div>
</template>

<script>
import axiosInstance from '../utils/axiosInstance'
import AuthService from '../services/AuthService'
import { API_ENDPOINTS } from '../config/api'
import UserSearchFilter from '../components/User/UserSearchFilter.vue'
import UserTable from '../components/User/UserTable.vue'
import UserPagination from '../components/User/UserPagination.vue'
import EditUserModal from '../components/User/EditUserModal.vue'
import DeleteConfirmModal from '../components/User/DeleteConfirmModal.vue'

export default {
  name: 'AdminUserManagementView',
  components: {
    UserSearchFilter,
    UserTable,
    UserPagination,
    EditUserModal,
    DeleteConfirmModal
  },
  data() {
    return {
      users: [],
      filterModel: {
        searchQuery: '',
        filterRole: ''
      },
      currentPage: 1,
      pageSize: 10,
      showEditModal: false,
      showDeleteConfirm: false,
      editingUser: {},
      userToDelete: null,
      deletingUserId: null,
      isLoading: false,
      errorMessage: ''
    }
  },
  computed: {
    filteredUsersList() {
      let result = [...this.users]
      
      // 搜索过滤
      if (this.filterModel.searchQuery) {
        const query = this.filterModel.searchQuery.toLowerCase()
        result = result.filter(user => 
          user.username.toLowerCase().includes(query) || 
          user.email.toLowerCase().includes(query)
        )
      }
      
      // 角色过滤
      if (this.filterModel.filterRole) {
        result = result.filter(user => user.role === this.filterModel.filterRole)
      }
      
      return result
    },
    filteredUsers() {
      // 分页
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredUsersList.slice(start, end)
    },
    filteredUsersTotal() {
      return this.filteredUsersList.length
    }
  },
  created() {
    this.loadUsers()
  },
  methods: {
    async loadUsers() {
      try {
        // 再次检查是否为管理员（额外安全措施）
        const currentUser = AuthService.getCurrentUser()
        if (!currentUser || !AuthService.hasRole('ADMIN')) {
          this.$message.error('您没有权限访问此功能')
          this.$router.push({ name: 'home' })
          return
        }
        
        this.isLoading = true
        const response = await axiosInstance.get(API_ENDPOINTS.user.getAll)
        this.users = response.data
      } catch (error) {
        console.error('Failed to load users:', error)
        
        // 详细的错误信息记录
        if (error.response) {
          console.error('响应状态:', error.response.status)
          console.error('响应数据:', error.response.data)
          console.error('请求URL:', error.config?.url)
        } else if (error.request) {
          console.error('请求已发送但未收到响应:', error.request)
        } else {
          console.error('请求配置错误:', error.message)
        }
        
        this.errorMessage = error.response?.data || '加载用户列表失败'
        this.$message.error(this.errorMessage)
        
        // 处理未授权错误 - 重定向到登录页
        if (error.response?.status === 401) {
          console.error('认证已过期，请重新登录')
          // 清除登录状态
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          setTimeout(() => {
            this.$router.push({ name: 'login' })
          }, 1000)
        } 
        // 如果是权限错误，重定向到仪表盘
        else if (error.response?.status === 403) {
          setTimeout(() => {
            this.$router.push({ name: 'home' })
          }, 1500)
        }
      } finally {
        this.isLoading = false
      }
    },
    handleSearchChange() {
      this.currentPage = 1 // 搜索时重置到第一页
    },
    handleRoleChange() {
      this.currentPage = 1 // 角色筛选时重置到第一页
    },
    handleEdit(user) {
      // 创建用户数据的深拷贝
      this.editingUser = JSON.parse(JSON.stringify(user))
      this.showEditModal = true
    },
    closeEditModal() {
      this.showEditModal = false
      this.editingUser = {}
      this.errorMessage = ''
    },
    async handleUpdateUser(userData) {
      try {
        this.isLoading = true
        
        // 创建符合后端DTO要求的数据结构
        const updateData = {
          username: userData.username,
          email: userData.email,
          phone: userData.phone || '',
          address: userData.address || '',
          avatar: userData.avatar
        }
        
        const response = await axiosInstance.put(API_ENDPOINTS.user.updateUser(userData.id), updateData)
        
        // 更新本地用户列表
        const index = this.users.findIndex(u => u.id === userData.id)
        if (index !== -1) {
          this.users[index] = response.data
        }
        
        this.closeEditModal()
        this.$message.success('用户信息更新成功')
      } catch (error) {
        console.error('Failed to update user:', error)
        this.errorMessage = error.response?.data || '更新用户信息失败'
        this.$message.error(`更新失败: ${this.errorMessage}`)
      } finally {
        this.isLoading = false
      }
    },
    handleDeleteConfirm(user) {
      this.userToDelete = user
      this.showDeleteConfirm = true
    },
    cancelDelete() {
      this.showDeleteConfirm = false
      this.userToDelete = null
      this.deletingUserId = null
    },
    async handleDeleteUser(user) {
      try {
        this.deletingUserId = user.id
        this.isLoading = true
        await axiosInstance.delete(API_ENDPOINTS.user.deleteUser(user.id))
        
        // 从本地列表中移除
        this.users = this.users.filter(u => u.id !== user.id)
        
        this.cancelDelete()
        this.$message.success('用户删除成功')
      } catch (error) {
        console.error('Failed to delete user:', error)
        const errorMsg = error.response?.data || '删除用户失败，请稍后重试'
        this.$message.error(errorMsg)
      } finally {
        this.isLoading = false
        this.deletingUserId = null
      }
    },
    handlePageChange(page) {
      this.currentPage = page
    },
    handlePageSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1 // 改变每页大小重置到第一页
    },
    isCurrentUser(user) {
      // 不允许修改当前登录用户的角色
      const currentUser = AuthService.getCurrentUser()
      return currentUser && currentUser.id === user.id
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../assets/css/variables.scss';

.admin-user-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-xl);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xl);
    padding-bottom: var(--spacing-md);
    border-bottom: 2px solid var(--border-color);

    h2 {
      margin: 0;
      color: var(--text-primary);
      font-size: var(--font-size-xl);
    }

    .header-actions {
      display: flex;
      gap: var(--spacing-md);

      .header-btn {
        min-width: 100px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-user-management {
    padding: var(--spacing-md);

    .page-header {
      flex-direction: column;
      align-items: stretch;
      gap: var(--spacing-md);

      .header-actions {
        justify-content: center;
      }
    }
  }
}

@media (max-width: 480px) {
  .admin-user-management {
    padding: var(--spacing-sm);
  }
}
</style>