<template>
  <el-dialog
    :model-value="visible"
    title="编辑用户"
    width="500px"
    :close-on-click-modal="false"
    @update:model-value="handleUpdateVisible"
  >
    <el-form
      ref="editForm"
      :model="user"
      :rules="rules"
      label-width="80px"
      class="edit-user-form"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="user.username" placeholder="请输入用户名" />
      </el-form-item>
      
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="user.email" type="email" placeholder="请输入邮箱" />
      </el-form-item>
      
      <el-form-item label="电话" prop="phone">
        <el-input v-model="user.phone" placeholder="请输入电话" />
      </el-form-item>
      
      <el-form-item label="地址" prop="address">
        <el-input v-model="user.address" placeholder="请输入地址" />
      </el-form-item>
      
      <el-form-item label="角色" prop="role">
        <el-select
          v-model="user.role"
          placeholder="请选择角色"
          :disabled="isCurrentUser"
        >
          <el-option label="普通用户" value="USER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
        <div v-if="isCurrentUser" class="role-tip">
          不能修改当前登录用户的角色
        </div>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="isSubmitting"
      >
        {{ isSubmitting ? '更新中...' : '保存修改' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: 'EditUserModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userData: {
      type: Object,
      default: () => ({})
    },
    isSubmitting: {
      type: Boolean,
      default: false
    },
    isCurrentUser: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      user: {}
    }
  },
  watch: {
    userData: {
      handler(newData) {
        // 创建深拷贝，避免直接修改props
        this.user = JSON.parse(JSON.stringify(newData))
      },
      immediate: true
    }
  },
  computed: {
    rules() {
      return {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleUpdateVisible(value) {
      if (!value) {
        this.handleClose()
      }
    },
    handleClose() {
      this.$refs.editForm?.resetFields()
      this.$emit('close')
    },
    async handleSubmit() {
      try {
        await this.$refs.editForm.validate()
        this.$emit('submit', this.user)
      } catch (error) {
        // 表单验证失败，不做处理
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.edit-user-form {
  margin-bottom: 0;

  .role-tip {
    color: var(--text-muted);
    font-size: var(--font-size-xs);
    margin-top: var(--spacing-xs);
  }
}

@media (max-width: 768px) {
  ::v-deep(.el-dialog__wrapper) {
    .el-dialog {
      width: 90% !important;
      max-width: 500px;
    }
  }
}
</style>