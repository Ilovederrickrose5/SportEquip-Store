<template>
  <div class="orders-container">
    <div class="page-header">
      <h1>我的订单</h1>
      <router-link to="/" class="back-to-home-btn">
        <el-button type="primary">返回首页</el-button>
      </router-link>
    </div>
    
    <el-card v-loading="loading" class="orders-card">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <el-select v-model="statusFilter" placeholder="筛选状态" size="small" @change="fetchOrders">
            <el-option label="全部订单" value="" />
            <el-option label="待支付" value="PENDING" />
            <el-option label="已支付" value="PAID" />
            <el-option label="已发货" value="SHIPPED" />
            <el-option label="已完成" value="DELIVERED" />
            <el-option label="已取消" value="CANCELED" />
          </el-select>
        </div>
      </template>
      
      <empty-order v-if="orders.length === 0 && !loading" />
      
      <div v-else class="orders-list">
        <order-item 
          v-for="order in orders" 
          :key="order.id" 
          :order="order"
          @view-details="viewOrderDetails"
        />
      </div>
    </el-card>
    
    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="订单详情"
      width="600px"
      center
    >
      <order-detail :order="currentOrder" />
    </el-dialog>
  </div>
</template>

<script>
import OrderService from '../services/OrderService';
import OrderItem from '../components/Order/OrderItem.vue';
import OrderDetail from '../components/Order/OrderDetail.vue';
import EmptyOrder from '../components/Order/EmptyOrder.vue';

export default {
  name: 'MyOrdersView',
  components: {
    OrderItem,
    OrderDetail,
    EmptyOrder
  },
  data() {
    return {
      orders: [],
      loading: false,
      statusFilter: '',
      showDetailDialog: false,
      currentOrder: null
    };
  },
  created() {
    this.fetchOrders();
  },
  methods: {
    async fetchOrders() {
      try {
        this.loading = true;
        const orders = await OrderService.getUserOrders();
        
        // 根据状态筛选
        if (this.statusFilter) {
          this.orders = orders.filter(order => order.status === this.statusFilter);
        } else {
          this.orders = orders;
        }
        
        // 按创建时间倒序排列
        this.orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      } catch (error) {
        console.error('获取订单失败:', error);
        this.$message.error(error.response?.data?.message || '获取订单失败');
      } finally {
        this.loading = false;
      }
    },
    
    async viewOrderDetails(orderId) {
      try {
        this.currentOrder = await OrderService.getOrderDetails(orderId);
        this.showDetailDialog = true;
      } catch (error) {
        console.error('获取订单详情失败:', error);
        this.$message.error(error.response?.data?.message || '获取订单详情失败');
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../assets/css/variables.scss';

.orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);

    h1 {
      margin: 0;
      color: var(--text-primary);
    }

    .back-to-home-btn {
      text-decoration: none;
    }
  }

  .orders-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      span {
        font-weight: 500;
        color: var(--text-primary);
      }
    }

    .orders-list {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-lg);
      padding: var(--spacing-sm) 0;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .orders-container {
    padding: var(--spacing-sm);

    .orders-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: var(--spacing-sm);
      }
    }
  }
}
</style>