<template>
  <div class="cart-summary">
    <div class="summary-header">
      <h2>结算</h2>
    </div>
    
    <div class="summary-content">
      <div class="summary-row">
        <span>商品总数:</span>
        <span>{{ totalItems }}</span>
      </div>
      <div class="summary-row">
        <span>商品总价:</span>
        <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
      </div>
    </div>
    
    <div class="checkout-actions">
      <el-button 
        @click="handleClearCart" 
        class="clear-cart-btn"
        :disabled="totalItems === 0"
      >
        清空购物车
      </el-button>
      <el-button 
        @click="handleCheckout" 
        class="checkout-btn" 
        type="primary"
        :disabled="totalItems === 0"
      >
        结算
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CartSummary',
  props: {
    totalItems: {
      type: Number,
      required: true
    },
    totalPrice: {
      type: Number,
      required: true
    }
  },
  emits: ['clear-cart', 'checkout'],
  methods: {
    handleClearCart() {
      this.$emit('clear-cart');
    },
    handleCheckout() {
      if (this.totalItems > 0) {
        this.$emit('checkout');
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import '../../assets/css/variables.scss';

.cart-summary {
  width: 350px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 25px;
  flex-shrink: 0;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color, #eee);

  h2 {
    font-size: 20px;
    color: var(--text-color-primary, #333);
    margin: 0;
  }
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;

  span:first-child {
    color: var(--text-color-secondary, #666);
  }
}

.total-price {
  font-size: 22px;
  font-weight: bold;
  color: var(--color-primary, #ff6700);
}

.checkout-actions {
  display: flex;
  gap: 10px;
}

.clear-cart-btn {
  flex: 1;
  border-color: var(--border-color, #ddd);
  color: var(--text-color-secondary, #666);

  &:hover:not(:disabled) {
    color: var(--color-danger, #ff4d4f);
    border-color: var(--color-danger, #ff4d4f);
    background-color: white;
  }
}

.checkout-btn {
  flex: 1;
  background-color: var(--color-primary, #ff6700);
  border-color: var(--color-primary, #ff6700);
  font-weight: bold;

  &:hover:not(:disabled) {
    background-color: var(--color-primary-hover, #e65c00);
    border-color: var(--color-primary-hover, #e65c00);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .cart-summary {
    width: 100%;
  }

  .checkout-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>