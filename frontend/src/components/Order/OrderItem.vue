<template>
  <div class="order-item">
    <div class="order-header">
      <div class="order-info">
        <span class="order-id">订单号: {{ order.id }}</span>
        <span class="order-date">{{ formatDate(order.createdAt) }}</span>
      </div>
      <el-tag :type="statusType" size="small">{{ statusText }}</el-tag>
    </div>
    
    <div class="order-products">
      <div 
        v-for="item in order.orderItems" 
        :key="`${order.id}-${item.productId}`"
        class="product-item"
        @click="handleViewDetails"
      >
        <div class="product-info">
          <span class="product-name">{{ item.productName }}</span>
          <div class="product-price-qty">
            <span class="product-price">¥{{ item.price.toFixed(2) }}</span>
            <span class="product-quantity">x{{ item.quantity }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="order-footer">
      <div class="order-total">
        <span>共 {{ itemCount }} 件商品</span>
        <span class="total-amount">合计: ¥{{ order.totalAmount.toFixed(2) }}</span>
      </div>
      <div class="order-actions">
        <el-button type="primary" size="small" @click="handleViewDetails">
          查看详情
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OrderItem',
  props: {
    order: {
      type: Object,
      required: true
    }
  },
  computed: {
    statusText() {
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
      const typeMap = {
        'PENDING': 'warning',
        'PAID': 'primary',
        'SHIPPED': 'info',
        'DELIVERED': 'success',
        'CANCELED': 'danger'
      };
      return typeMap[this.order.status] || 'info';
    },
    itemCount() {
      return this.order.orderItems?.reduce((total, item) => total + item.quantity, 0) || 0;
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
    },
    handleViewDetails() {
      this.$emit('view-details', this.order.id);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.order-item {
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  overflow: hidden;
  transition: box-shadow 0.3s;

  &:hover {
    box-shadow: var(--box-shadow-hover);
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-md);
    background-color: var(--bg-light);
    border-bottom: 1px solid var(--border-color);

    .order-info {
      display: flex;
      gap: var(--spacing-lg);

      .order-id {
        font-weight: 500;
      }

      .order-date {
        color: var(--text-secondary);
        font-size: var(--font-size-sm);
      }
    }
  }

  .order-products {
    padding: var(--spacing-md);

    .product-item {
      padding: var(--spacing-sm) 0;
      border-bottom: 1px dashed var(--border-color);
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background-color: var(--bg-light);
      }

      &:last-child {
        border-bottom: none;
      }

      .product-info {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .product-name {
          flex: 1;
          color: var(--text-primary);
        }

        .product-price-qty {
          display: flex;
          gap: var(--spacing-lg);

          .product-price {
            color: var(--primary-color);
            font-weight: 500;
          }
        }
      }
    }
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-md);
    background-color: var(--bg-hover);
    border-top: 1px solid var(--border-color);

    .order-total {
      display: flex;
      align-items: center;
      gap: var(--spacing-lg);

      .total-amount {
        color: var(--primary-color);
        font-size: var(--font-size-md);
        font-weight: bold;
      }
    }

    .order-actions {
      display: flex;
      gap: var(--spacing-sm);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-item {
    .order-header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-sm);

      .order-info {
        flex-direction: column;
        gap: var(--spacing-xs);
      }
    }

    .product-info {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-xs);

      .product-price-qty {
        align-self: flex-end;
      }
    }

    .order-footer {
      flex-direction: column;
      align-items: flex-end;
      gap: var(--spacing-sm);

      .order-total {
        align-self: flex-start;
      }
    }
  }
}
</style>