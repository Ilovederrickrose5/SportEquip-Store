<template>
  <div class="user-table-container">
    <el-table
      :data="users"
      style="width: 100%"
      :row-class-name="tableRowClassName"
      v-loading="isLoading"
    >
      <el-table-column prop="username" label="用户名" width="70" header-align="center" />
      <el-table-column prop="email" label="邮箱" width="180" header-align="center" />
      <el-table-column prop="role" label="角色" width="70" header-align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.role === 'ADMIN' ? 'primary' : 'success'"
            size="small"
          >
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="120" header-align="center">
        <template #default="{ row }">
          {{ row.phone || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="address" label="地址" header-align="center">
        <template #default="{ row }">
          {{ row.address || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" header-align="center">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right" header-align="center" align="right">
        <template #default="{ row }">
          <div class="action-buttons-vertical">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
              :disabled="row.role === 'ADMIN'"
              :loading="deletingUserId === row.id"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="users.length === 0 && !isLoading" class="empty-state">
      <el-empty description="暂无用户数据" />
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserTable',
  props: {
    users: {
      type: Array,
      default: () => []
    },
    isLoading: {
      type: Boolean,
      default: false
    },
    deletingUserId: {
      type: Number,
      default: null
    }
  },
  methods: {
    handleEdit(user) {
      this.$emit('edit', user)
    },
    handleDelete(user) {
      this.$emit('delete', user)
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN')
    },
    tableRowClassName({ row }) {
      return row.role === 'ADMIN' ? 'admin-row' : ''
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.user-table-container {
  background: var(--bg-white);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: var(--spacing-md);

  .empty-state {
    padding: var(--spacing-xl) 0;
    text-align: center;
  }
}

::v-deep(.admin-row) {
  background-color: var(--primary-lighter);
}

::v-deep(.el-button--small) {
  margin-right: var(--spacing-xs);
}

.action-buttons-vertical {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  
  .el-button--small {
    width: 60px;
    margin-bottom: 4px;
    margin-right: 0;
  }
}

@media (max-width: 768px) {
  .user-table-container {
    padding: var(--spacing-sm);
    overflow-x: auto;
  }

  ::v-deep(.el-table) {
    width: 100%;
    min-width: 600px;
  }
}
</style>