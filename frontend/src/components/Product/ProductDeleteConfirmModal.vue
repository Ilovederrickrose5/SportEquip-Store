<template>
  <el-dialog
    :model-value="visible"
    title="确认删除"
    width="400px"
    @update:model-value="handleUpdateVisible"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="confirm-content">
      <el-icon class="warning-icon"><WarningFilled /></el-icon>
      <p>确定要删除商品 "{{ productName }}" 吗？</p>
      <p class="danger-text">此操作不可撤销！</p>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="danger" @click="handleConfirm" :loading="loading">
          确认删除
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script>
import { WarningFilled } from '@element-plus/icons-vue';

export default {
  name: 'ProductDeleteConfirmModal',
  components: {
    WarningFilled
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    productId: {
      type: String,
      default: ''
    },
    productName: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:visible', 'confirm'],
  methods: {
    handleUpdateVisible(value) {
      this.$emit('update:visible', value);
      if (!value) {
        this.$emit('cancel');
      }
    },
    handleCancel() {
      this.$emit('update:visible', false);
      this.$emit('cancel');
    },
    handleConfirm() {
      this.$emit('confirm', this.productId);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.confirm-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg) 0;
  text-align: center;
}

.warning-icon {
  font-size: 48px;
  color: var(--warning-color);
  margin-bottom: var(--spacing-md);
}

.danger-text {
  color: var(--danger-color);
  font-weight: bold;
  margin-top: var(--spacing-md);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
}

p {
  margin: 0;
  line-height: 1.6;
}
</style>