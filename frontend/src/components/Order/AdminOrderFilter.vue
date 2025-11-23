<template>
  <div class="order-filter">
    <el-select 
      v-model="localStatusFilter" 
      placeholder="筛选状态" 
      size="small"
      @change="handleStatusChange"
      style="width: 150px; margin-right: 10px;"
    >
      <el-option label="全部订单" value="" />
      <el-option label="待支付" value="PENDING" />
      <el-option label="已支付" value="PAID" />
      <el-option label="已发货" value="SHIPPED" />
      <el-option label="已完成" value="DELIVERED" />
      <el-option label="已取消" value="CANCELED" />
    </el-select>
    <el-input
      v-model="localSearchKeyword"
      placeholder="搜索订单号/用户名"
      size="small"
      @input="handleSearch"
      style="width: 200px;"
    >
      <template #prefix>
        <el-icon><search /></el-icon>
      </template>
    </el-input>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue';

export default {
  name: 'AdminOrderFilter',
  components: {
    Search
  },
  props: {
    statusFilter: {
      type: String,
      default: ''
    },
    searchKeyword: {
      type: String,
      default: ''
    }
  },
  emits: ['status-change', 'search-change'],
  computed: {
    localStatusFilter: {
      get() {
        return this.statusFilter;
      },
      set(value) {
        this.$emit('status-change', value);
      }
    },
    localSearchKeyword: {
      get() {
        return this.searchKeyword;
      },
      set(value) {
        this.$emit('search-change', value);
      }
    }
  },
  methods: {
    handleStatusChange(value) {
      this.$emit('status-change', value);
    },
    handleSearch() {
      this.$emit('search-change', this.localSearchKeyword);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.order-filter {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  
  @media (max-width: 768px) {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-sm);
    
    .el-select,
    .el-input {
      width: 100% !important;
    }
  }
}
</style>