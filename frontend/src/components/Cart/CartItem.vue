<template>
  <div class="cart-item">
    <!-- 商品图片 -->
    <div class="cart-item-image">
      <img :src="item.imageUrl || '/src/assets/default-product.png'" :alt="item.productName" />
    </div>
    
    <!-- 商品信息 -->
    <div class="cart-item-info">
      <h3 class="cart-item-name">{{ item.productName }}</h3>
      <p class="cart-item-price">¥{{ item.price }}</p>
      
      <!-- 数量控制 -->
      <div class="quantity-control">
        <el-button 
          @click="handleDecrease"
          :disabled="item.quantity <= 1"
          size="small"
          circle
          class="quantity-btn decrease-btn"
        >
          <el-icon><Minus /></el-icon>
        </el-button>
        <span class="quantity">{{ item.quantity }}</span>
        <el-button 
          @click="handleIncrease"
          size="small"
          circle
          class="quantity-btn increase-btn"
        >
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      
      <p class="cart-item-total">小计: ¥{{ item.itemTotal }}</p>
      <el-button @click="handleRemove" type="text" class="remove-btn">删除</el-button>
    </div>
  </div>
</template>

<script>
import { Minus, Plus } from '@element-plus/icons-vue';

export default {
  name: 'CartItem',
  components: {
    Minus,
    Plus
  },
  props: {
    item: {
      type: Object,
      required: true
    }
  },
  emits: ['decrease', 'increase', 'remove'],
  methods: {
    handleDecrease() {
      if (this.item.quantity > 1) {
        this.$emit('decrease', this.item);
      }
    },
    handleIncrease() {
      this.$emit('increase', this.item);
    },
    handleRemove() {
      this.$emit('remove', this.item.id);
    }
  }
};
</script>

<style scoped lang="scss">
@import '../../assets/css/variables.scss';

.cart-item {
  display: flex;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.cart-item-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  overflow: hidden;
  background-color: var(--bg-color-light, #f5f5f5);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
  }
}

.cart-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-left: 20px;
}

.cart-item-name {
  font-size: 18px;
  font-weight: 500;
  color: var(--text-color-primary, #333);
  margin: 0;
  line-height: 1.4;
}

.cart-item-price {
  font-size: 20px;
  font-weight: bold;
  color: var(--color-primary, #ff6700);
  margin: 0;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 15px;
  width: fit-content;
}

.quantity-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:not(:disabled):hover {
    background-color: var(--color-primary, #ff6700);
    color: white;
  }
}

.quantity {
  font-size: 18px;
  min-width: 40px;
  text-align: center;
  font-weight: 500;
}

.cart-item-total {
  font-size: 18px;
  color: var(--color-primary, #ff6700);
  font-weight: 600;
  margin: 0;
}

.remove-btn {
  width: fit-content;
  color: var(--color-primary, #ff6700);
  padding: 8px 16px;

  &:hover {
    background-color: rgba(255, 103, 0, 0.1);
    color: var(--color-primary, #ff6700);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .cart-item-info {
    margin-left: 0;
    align-items: center;
  }

  .quantity-control {
    margin: 0 auto;
  }
}
</style>