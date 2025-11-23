<template>
  <div class="order-details">
    <!-- 订单信息 -->
    <section class="detail-section">
      <h3>订单信息</h3>
      <div class="detail-row">
        <div class="detail-item">
          <label>订单号:</label>
          <span>{{ order?.id }}</span>
        </div>
        <div class="detail-item">
          <label>用户ID:</label>
          <span>{{ order?.userId || '未知用户' }}</span>
        </div>
        <div class="detail-item">
          <label>创建时间:</label>
          <span>{{ formatDate(order?.createdAt) }}</span>
        </div>
      </div>
      <div class="detail-row">
        <div class="detail-item">
          <label>订单状态:</label>
          <el-tag :type="getStatusType(order?.status)">{{ getStatusText(order?.status) }}</el-tag>
        </div>
        <div class="detail-item">
          <label>支付方式:</label>
          <span>{{ getPaymentText(order?.paymentMethod) }}</span>
        </div>
      </div>
    </section>

    <!-- 收货信息 -->
    <section class="detail-section">
      <h3>收货信息</h3>
      <div class="detail-row">
        <div class="detail-item">
          <label>收货人:</label>
          <span>{{ order?.recipientName }}</span>
        </div>
        <div class="detail-item">
          <label>手机号码:</label>
          <span>{{ order?.phone }}</span>
        </div>
      </div>
      <div class="detail-item full-width">
        <label>收货地址:</label>
        <span>{{ order?.address }}</span>
      </div>
      <div class="detail-item full-width" v-if="order?.remark">
        <label>订单备注:</label>
        <span>{{ order?.remark }}</span>
      </div>
    </section>

    <!-- 商品信息 -->
    <section class="detail-section">
      <h3>商品信息</h3>
      <el-table :data="order?.orderItems || []" style="width: 100%;" border>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="scope">¥{{ scope.row.price.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="小计" width="100" align="right">
          <template #default="scope">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </section>

    <!-- 金额汇总 -->
    <section class="detail-section">
      <h3>金额信息</h3>
      <div class="amount-summary">
        <div class="amount-item">
          <span>商品总价:</span>
          <span>¥{{ (order?.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="amount-item">
          <span>运费:</span>
          <span>¥0.00</span>
        </div>
        <div class="amount-total">
          <span>应付金额:</span>
          <span>¥{{ (order?.totalAmount || 0).toFixed(2) }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'OrderDetails',
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  methods: {
    formatDate(dateString) {
      if (!dateString) return '-';
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
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'DELIVERED': '已完成',
        'CANCELED': '已取消'
      };
      return statusMap[status] || status;
    },
    getStatusType(status) {
      const typeMap = {
        'PENDING': 'warning',
        'PAID': 'primary',
        'SHIPPED': 'info',
        'DELIVERED': 'success',
        'CANCELED': 'danger'
      };
      return typeMap[status] || 'info';
    },
    getPaymentText(paymentMethod) {
      const paymentMap = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝',
        'BANKCARD': '银行卡'
      };
      return paymentMap[paymentMethod] || paymentMethod;
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.order-details {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  
  .detail-section {
    border-bottom: 1px solid var(--border-color);
    padding-bottom: var(--spacing-md);
    
    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
    
    h3 {
      margin: 0 0 var(--spacing-md) 0;
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-bold);
      color: var(--text-primary);
    }
  }
  
  .detail-row {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: var(--spacing-sm);
    gap: var(--spacing-md);
    
    @media (max-width: 768px) {
      flex-direction: column;
      gap: var(--spacing-sm);
    }
  }
  
  .detail-item {
    display: flex;
    align-items: flex-start;
    min-width: 250px;
    flex: 1;
    
    @media (max-width: 768px) {
      min-width: auto;
    }
    
    &.full-width {
      min-width: 100%;
    }
    
    label {
      width: 80px;
      color: var(--text-secondary);
      font-weight: var(--font-weight-medium);
      margin-right: var(--spacing-sm);
    }
    
    span {
      flex: 1;
      color: var(--text-primary);
    }
  }
  
  .price {
    color: var(--color-primary);
    font-weight: var(--font-weight-medium);
  }
  
  .amount-summary {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: var(--spacing-sm);
    
    .amount-item {
      display: flex;
      gap: 60px;
      color: var(--text-secondary);
    }
    
    .amount-total {
      display: flex;
      gap: 60px;
      margin-top: var(--spacing-sm);
      padding-top: var(--spacing-sm);
      border-top: 1px dashed var(--border-color);
      color: var(--text-primary);
      font-size: var(--font-size-lg);
      font-weight: var(--font-weight-bold);
      
      span:last-child {
        color: var(--color-primary);
      }
    }
  }
}
</style>