<template>
  <div class="empty-orders">
    <el-empty description="暂无订单" :image-size="200">
      <el-button type="primary" @click="handleGoShopping">去购物</el-button>
    </el-empty>
  </div>
</template>

<script>
export default {
  name: 'EmptyOrder',
  methods: {
    handleGoShopping() {
      this.$router.push('/ball-sports');
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.empty-orders {
  padding: 40px 0;
  text-align: center;

  :deep(.el-empty) {
    .el-empty__image {
      margin-bottom: var(--spacing-lg);
    }

    .el-empty__description {
      color: var(--text-secondary);
      font-size: var(--font-size-md);
      margin-bottom: var(--spacing-lg);
    }

    .el-empty__bottom {
      margin-top: var(--spacing-lg);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .empty-orders {
    padding: 30px 0 20px;

    :deep(.el-empty) {
      .el-empty__image img {
        width: 160px;
        height: 160px;
      }

      .el-empty__description {
        font-size: var(--font-size-sm);
      }
    }
  }
}
</style>