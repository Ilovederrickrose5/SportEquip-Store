<template>
  <el-dialog
    :model-value="visible"
    title="确认删除"
    width="400px"
    :close-on-click-modal="false"
    @update:model-value="handleUpdateVisible"
  >
    <div class="delete-confirm-content">
      <div class="warning-icon">
        <el-icon class="el-icon--warning"><WarningFilled /></el-icon>
      </div>
      <div class="confirm-message">
        <p>
          确定要删除用户 <strong>"{{ user?.username }}"</strong> 吗？
        </p>
        <p class="danger-tip">此操作不可撤销。</p>
        <p v-if="isAdminUser" class="admin-warning">
          管理员账户不允许删除！
        </p>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        type="danger"
        @click="handleConfirm"
        :disabled="isAdminUser || isSubmitting"
        :loading="isSubmitting"
      >
        {{ isSubmitting ? '删除中...' : '确认删除' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import { WarningFilled } from '@element-plus/icons-vue'

export default {
  name: 'DeleteConfirmModal',
  components: {
    WarningFilled
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    user: {
      type: Object,
      default: null
    },
    isSubmitting: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    isAdminUser() {
      return this.user?.role === 'ADMIN'
    }
  },
  methods: {
    handleUpdateVisible(value) {
      if (!value) {
        this.handleCancel()
      }
    },
    handleCancel() {
      this.$emit('cancel')
    },
    handleConfirm() {
      if (!this.isAdminUser) {
        this.$emit('confirm', this.user)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.delete-confirm-content {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;

  .warning-icon {
    color: var(--danger-color);
    font-size: 24px;
    margin-top: 2px;
  }

  .confirm-message {
    flex: 1;

    p {
      margin-bottom: var(--spacing-sm);
      line-height: 1.5;
    }

    .danger-tip {
      color: var(--text-danger);
      font-weight: 500;
    }

    .admin-warning {
      color: var(--text-danger);
      font-size: var(--font-size-sm);
      margin-top: var(--spacing-sm);
    }
  }
}

@media (max-width: 768px) {
  ::v-deep(.el-dialog__wrapper) {
    .el-dialog {
      width: 85% !important;
      max-width: 400px;
    }
  }

  .delete-confirm-content {
    flex-direction: column;
    align-items: center;
    text-align: center;

    .confirm-message {
      text-align: center;
    }
  }
}
</style>