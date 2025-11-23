<template>
  <div class="product-pagination-container">
    <div class="pagination-info">
      <span>共 {{ total }} 条数据</span>
    </div>
    <el-pagination
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script>
export default {
  name: 'ProductPagination',
  props: {
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    },
    total: {
      type: Number,
      default: 0
    }
  },
  emits: ['update:currentPage', 'update:pageSize'],
  methods: {
    handleSizeChange(size) {
      this.$emit('update:page-size', size);
    },
    handleCurrentChange(current) {
      this.$emit('update:current-page', current);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.product-pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background-color: var(--bg-white);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow);
  margin-top: var(--spacing-lg);
}

.pagination-info {
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

@media (max-width: 768px) {
  .product-pagination-container {
    flex-direction: column;
    gap: var(--spacing-md);
    padding: var(--spacing-md);
  }
  
  .pagination-info {
    align-self: flex-start;
  }
}
</style>