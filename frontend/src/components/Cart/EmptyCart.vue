<template>
  <div class="empty-cart">
    <el-empty description="您的购物车是空的" :image-size="180">
      <el-button type="primary" class="continue-shopping-btn" @click="handleContinueShopping">
        <el-icon><ShoppingCart /></el-icon>
        继续购物
      </el-button>
    </el-empty>
  </div>
</template>

<script>
import { ShoppingCart } from '@element-plus/icons-vue';

export default {
  name: 'EmptyCart',
  components: {
    ShoppingCart
  },
  methods: {
    handleContinueShopping() {
      this.$router.push('/');
    }
  }
};
</script>

<style scoped lang="scss">
@import '../../assets/css/variables.scss';

.empty-cart {
  text-align: center;
  padding: 60px 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.continue-shopping-btn {
  background-color: var(--color-primary, #ff6700);
  border-color: var(--color-primary, #ff6700);
  margin-top: 20px;
  padding: 10px 20px;
  font-size: 16px;

  &:hover {
    background-color: var(--color-primary-hover, #e55a00);
    border-color: var(--color-primary-hover, #e55a00);
  }
}
</style>