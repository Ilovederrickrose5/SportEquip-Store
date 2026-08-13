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
        const rawOrders = await OrderService.getUserOrders();

        // ---- 核心兜底 1：后端返回值必须是数组，否则一律降级为空数组 ----
        // 防止接口调错 / 缓存脏数据 / 后端返回 PageResponse 对象时，后续 .sort/.filter 直接炸
        let orders = Array.isArray(rawOrders)
          ? rawOrders
          : (rawOrders && Array.isArray(rawOrders.content) ? rawOrders.content : []);

        // ---- 核心兜底 2：每个 order 必须是对象，orderItems 必须是数组（防止 OrderItem 渲染炸）----
        orders = orders.map(order => {
          if (!order || typeof order !== 'object') return null;
          return {
            ...order,
            orderItems: Array.isArray(order.orderItems) ? order.orderItems : []
          };
        }).filter(Boolean);

        // 根据状态筛选（筛选前再判一次，极端保护）
        if (this.statusFilter) {
          this.orders = orders.filter(order => order && order.status === this.statusFilter);
        } else {
          this.orders = orders;
        }

        // 按创建时间倒序排列（排序前强制再次保证 this.orders 是数组，杜绝 sort is not a function）
        if (Array.isArray(this.orders)) {
          this.orders.sort((a, b) => {
            const ta = new Date(a && a.createdAt ? a.createdAt : 0).getTime();
            const tb = new Date(b && b.createdAt ? b.createdAt : 0).getTime();
            return tb - ta;
          });
        } else {
          this.orders = [];
        }
      } catch (error) {
        console.error('获取订单失败:', error);
        this.orders = []; // 兜底：错误时置空数组，避免旧脏数据
        this.$message.error(error.response?.data?.message || '获取订单失败');
      } finally {
        // 无论成功/异常/中途 return，一律把 loading 置为 false，防止视觉永远转圈
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
@use '../assets/css/variables.scss' as *;

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