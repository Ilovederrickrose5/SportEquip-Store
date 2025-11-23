<template>
  <div class="user-pagination" v-if="total > 0">
    <el-pagination
      background
      layout="prev, pager, next, jumper, ->, total"
      :total="total"
      :current-page="currentPage"
      :page-size="pageSize"
      :page-sizes="pageSizes"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script>
export default {
  name: 'UserPagination',
  props: {
    total: {
      type: Number,
      default: 0
    },
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    },
    pageSizes: {
      type: Array,
      default: () => [5, 10, 20, 50]
    }
  },
  methods: {
    handleCurrentChange(page) {
      this.$emit('current-change', page)
    },
    handleSizeChange(size) {
      this.$emit('size-change', size)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.user-pagination {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: flex-end;
  padding: var(--spacing-md);
  background: var(--bg-white);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);

  ::v-deep(.el-pagination) {
    margin: 0;
  }
}

@media (max-width: 768px) {
  .user-pagination {
    justify-content: center;
    padding: var(--spacing-sm);

    ::v-deep(.el-pagination) {
      .el-pagination__total {
        margin-right: var(--spacing-sm);
      }

      .el-pagination__jump {
        margin-left: var(--spacing-sm);
      }
    }
  }
}
</style>