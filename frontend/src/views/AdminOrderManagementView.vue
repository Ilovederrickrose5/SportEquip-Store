<template>
  <div class="admin-orders-container">
    <div class="header-wrapper">
      <h1>订单管理</h1>
      <el-button type="primary" @click="goToHome" icon="el-icon-back" size="medium" class="back-to-home-btn">
      返回首页
      </el-button>
    </div>
    
    <el-card class="orders-card">
      <template #header>
        <div class="card-header">
          <span>所有订单</span>
          <!-- 使用独立的筛选组件 -->
          <admin-order-filter
            :status="statusFilter"
            :keyword="searchKeyword"
            @search="handleSearch"
            @reset="resetSearch"
          />
        </div>
      </template>
      
      <el-table 
        :data="orders" 
        style="width: 100%;"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="id" label="订单号" width="70" />
        <el-table-column label="用户信息" width="90">
          <template #default="scope">
            <span>{{ scope.row.userId || '未知用户' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="收货人" width="70">
          <template #default="scope">
            <span>{{ scope.row.recipientName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="手机号码" width="110">
          <template #default="scope">
            <span>{{ scope.row.phone }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="85" align="right">
          <template #default="scope">
            <span class="price">¥{{ scope.row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="90">
          <template #default="scope">
            <span>{{ getPaymentText(scope.row.paymentMethod) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="90">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="scope">
            <span>{{ formatDate(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <div class="action-buttons-vertical">
              <el-button type="primary" size="small" @click="viewOrderDetails(scope.row.id)">
                查看详情
              </el-button>
              <el-button 
                type="success" 
                size="small" 
                @click="updateOrderStatus(scope.row)"
                :disabled="scope.row.status === 'CANCELLED' || scope.row.status === 'DELIVERED'"
              >
                更新状态
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container" v-if="orders.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalOrders"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 订单详情对话框 - 使用独立组件 -->
    <el-dialog
      v-model="showDetailDialog"
      title="订单详情"
      width="700px"
      center
    >
      <order-details v-if="currentOrder" :order="currentOrder" />
    </el-dialog>
    
    <!-- 状态更新对话框 - 使用独立组件 -->
    <el-dialog
      v-model="showStatusDialog"
      title="更新订单状态"
      width="400px"
      center
    >
      <order-status-update
        ref="statusUpdateRef"
        v-if="orderToUpdate"
        :order="orderToUpdate"
        @confirm="handleStatusUpdateConfirm"
        @cancel="showStatusDialog = false"
      />
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStatusDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmStatusUpdate" :loading="statusUpdating">
            确认更新
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue';
import OrderService from '../services/OrderService';
import AdminOrderFilter from '../components/Order/AdminOrderFilter.vue';
import OrderDetails from '../components/Order/OrderDetails.vue';
import OrderStatusUpdate from '../components/Order/OrderStatusUpdate.vue';

export default {
  name: 'AdminOrderManagementView',
  components: {
    Search,
    AdminOrderFilter,
    OrderDetails,
    OrderStatusUpdate
  },
  data() {
      return {
        orders: [],
        totalOrders: 0,
        currentPage: 1,
        pageSize: 10,
        loading: true,
        statusFilter: '',
        searchKeyword: '',
        currentFilter: {},
        selectedOrder: null,
        showDetailDialog: false,
        showStatusDialog: false,
        currentOrder: null,
        orderToUpdate: null,
        statusForm: { newStatus: '' },
        statusUpdating: false
      };
    },
  created() {
      this.fetchAllOrders();
    },
  methods: {
      fetchAllOrders() {
        this.fetchOrders();
      },
      
      handleCurrentChange(currentPage) {
        this.currentPage = currentPage;
        this.handleSearch(this.currentFilter);
      },
      
     getStatusText(status) {
      const statusMap = {
        PENDING: '待支付',
        PAID: '已支付',
        PROCESSING: '处理中',
        SHIPPED: '已发货',
        DELIVERED: '已送达',
        CANCELLED: '已取消'
      };
      return statusMap[status] || status;
    },

    getStatusType(status) {
      const typeMap = {
        PENDING: 'warning',
        PAID: 'info',
        PROCESSING: 'primary',
        SHIPPED: 'success',
        DELIVERED: 'success',
        CANCELLED: 'danger'
      };
      return typeMap[status] || 'default';
    },

    getPaymentText(paymentMethod) {
      const paymentMap = {
        ONLINE_PAYMENT: '在线支付',
        CASH_ON_DELIVERY: '货到付款',
        PAYPAL: 'PayPal',
        CREDIT_CARD: '信用卡'
      };
      return paymentMap[paymentMethod] || paymentMethod;
    },
    
    goToHome() {
      this.$router.push('/');
    },
    async fetchAllOrders() {
      try {
        this.loading = true;
        const orders = await OrderService.getAllOrders();
        
        // 按创建时间倒序排列
        this.orders = orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      } catch (error) {
        console.error('获取所有订单失败:', error);
        this.$message.error(error.response?.data?.message || '获取订单失败');
      } finally {
        this.loading = false;
      }
    },
    
    handleSearch() {
      this.currentPage = 1; // 搜索时重置到第一页
    },
    
    handleSizeChange(size) {
      this.pageSize = size;
      this.handleSearch(this.currentFilter);
    },
  
      formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return d.toLocaleString('zh-CN', { 
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    
    handleCurrentChange(current) {
      this.currentPage = current;
    },
    
    async viewOrderDetails(orderId) {
      try {
        this.currentOrder = await OrderService.getOrderDetails(orderId);
        this.showDetailDialog = true;
      } catch (error) {
        console.error('获取订单详情失败:', error);
        this.$message.error(error.response?.data?.message || '获取订单详情失败');
      }
    },
    
    updateOrderStatus(order) {
      this.orderToUpdate = order;
      this.statusForm.newStatus = '';
      this.showStatusDialog = true;
    },
    
    confirmStatusUpdate() {
      if (this.$refs.statusUpdateRef) {
        this.$refs.statusUpdateRef.handleConfirm();
      }
    },
    
    async handleStatusUpdateConfirm(updateData) {
      try {
        this.statusUpdating = true;
        
        await OrderService.updateOrderStatus(this.orderToUpdate.id, updateData.newStatus);
        
        // 更新本地数据
        const index = this.orders.findIndex(o => o.id === this.orderToUpdate.id);
        if (index !== -1) {
          this.orders[index].status = updateData.newStatus;
        }
        
        this.$message.success('订单状态更新成功！');
        this.showStatusDialog = false;
      } catch (error) {
        console.error('更新订单状态失败:', error);
        this.$message.error(error.response?.data?.message || '更新订单状态失败');
      } finally {
        this.statusUpdating = false;
      }
    },
    
    resetSearch() {
      this.statusFilter = '';
      this.searchKeyword = '';
      this.currentPage = 1;
    },
    
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    

    
    canUpdateStatus(status) {
      // 已完成或已取消的订单不能再修改状态
      return status !== 'DELIVERED' && status !== 'CANCELED';
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../assets/css/variables.scss';

.admin-orders-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--spacing-lg);
  
  h1 {
    margin-bottom: var(--spacing-lg);
    color: var(--text-primary);
  }
  
  .header-wrapper {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-md);
    }
  }
  
  .orders-card {
    margin-bottom: var(--spacing-lg);
  }
  
  .pagination-container {
    margin-top: var(--spacing-lg);
    display: flex;
    justify-content: flex-end;
  }
  
  .price {
    color: var(--color-primary);
    font-weight: var(--font-weight-medium);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .admin-orders-container {
    padding: var(--spacing-sm);
  }
}
</style>