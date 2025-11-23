<template>
  <div v-if="order" class="order-details">
    <div class="detail-section">
      <h3>订单信息</h3>
      <div class="detail-item">
        <label>订单号:</label>
        <span>{{ order.id }}</span>
      </div>
      <div class="detail-item">
        <label>创建时间:</label>
        <span>{{ formatDate(order.createdAt) }}</span>
      </div>
      <div class="detail-item">
        <label>订单状态:</label>
        <el-tag :type="statusType">{{ statusText }}</el-tag>
      </div>
      <div class="detail-item">
        <label>支付方式:</label>
        <span>{{ paymentText }}</span>
      </div>
    </div>
    
    <div class="detail-section">
      <h3>收货信息</h3>
      <div class="detail-item">
        <label>收货人:</label>
        <span>{{ order.recipientName }}</span>
      </div>
      <div class="detail-item">
        <label>手机号码:</label>
        <span>{{ order.phone }}</span>
      </div>
      <div class="detail-item">
        <label>收货地址:</label>
        <span>{{ order.address }}</span>
      </div>
      <div class="detail-item" v-if="order.remark">
        <label>订单备注:</label>
        <span>{{ order.remark }}</span>
      </div>
    </div>
    
    <div class="detail-section">
      <h3>商品信息</h3>
      <el-table :data="order.orderItems" style="width: 100%;">
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">¥{{ scope.row.price.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100">
          <template #default="scope">¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}</template>
        </el-table-column>
      </el-table>
    </div>
    
    <div class="detail-section">
      <h3>金额信息</h3>
      <div class="amount-summary">
        <div class="amount-item">
          <span>商品总价:</span>
          <span>¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="amount-item">
          <span>运费:</span>
          <span>¥0.00</span>
        </div>
        <div class="amount-total">
          <span>应付金额:</span>
          <span>¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderDetail',
  props: {
    order: {
      type: Object,
      required: false,
      default: null
    }
  },
  computed: {
    statusText() {
      if (!this.order) return '';
      const statusMap = {
        'PENDING': '待支付',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'DELIVERED': '已完成',
        'CANCELED': '已取消'
      };
      return statusMap[this.order.status] || this.order.status;
    },
    statusType() {
      if (!this.order) return 'info';
      const typeMap = {
        'PENDING': 'warning',
        'PAID': 'primary',
        'SHIPPED': 'info',
        'DELIVERED': 'success',
        'CANCELED': 'danger'
      };
      return typeMap[this.order.status] || 'info';
    },
    paymentText() {
      if (!this.order) return '';
      const paymentMap = {
        'WECHAT': '微信支付',
        'ALIPAY': '支付宝',
        'BANKCARD': '银行卡'
      };
      return paymentMap[this.order.paymentMethod] || this.order.paymentMethod;
    }
  },
  methods: {
    formatDate(dateString) {
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
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
    padding-bottom: var(--spacing-lg);

    &:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }

    h3 {
      margin: 0 0 var(--spacing-md) 0;
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-primary);
    }

    .detail-item {
      display: flex;
      margin-bottom: var(--spacing-sm);

      &:last-child {
        margin-bottom: 0;
      }

      label {
        width: 80px;
        color: var(--text-secondary);
        font-weight: 500;
      }

      span {
        flex: 1;
        color: var(--text-primary);
      }
    }

    .amount-summary {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: var(--spacing-sm);

      .amount-item {
        display: flex;
        gap: 40px;
        color: var(--text-secondary);
      }

      .amount-total {
        display: flex;
        gap: 40px;
        margin-top: var(--spacing-sm);
        padding-top: var(--spacing-sm);
        border-top: 1px dashed var(--border-color);
        color: var(--text-primary);
        font-size: var(--font-size-md);
        font-weight: bold;

        span:last-child {
          color: var(--primary-color);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-details {
    .detail-section {
      .detail-item {
        flex-direction: column;
        gap: var(--spacing-xs);

        label {
          width: auto;
        }
      }

      .amount-summary {
        .amount-item,
        .amount-total {
          gap: var(--spacing-lg);
        }
      }
    }
  }
}
</style>