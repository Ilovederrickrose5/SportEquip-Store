<template>
  <div class="cart-container">
    <div class="cart-header">
      <h1 class="cart-title">购物车</h1>
      <router-link to="/">
        <el-button type="primary" class="back-to-home-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回首页
        </el-button>
      </router-link>
    </div>
    
    <el-card v-loading="loading" shadow="hover" class="cart-card">
      <div class="cart-content">
        <!-- 购物车商品列表 -->
        <div class="cart-items-container">
          <template v-if="cart.cartItems && cart.cartItems.length > 0">
            <div class="cart-items">
              <CartItem 
                v-for="item in cart.cartItems" 
                :key="item.id" 
                :item="item"
                @decrease="handleDecreaseQuantity"
                @increase="handleIncreaseQuantity"
                @remove="handleRemoveItem"
              />
            </div>
          </template>
          
          <!-- 空购物车提示 -->
          <template v-else>
            <EmptyCart />
          </template>
        </div>
        
        <!-- 购物车结算区域 -->
        <div v-if="cart.cartItems && cart.cartItems.length > 0">
          <CartSummary
            :total-items="totalItems"
            :total-price="totalPrice"
            @clear-cart="handleClearCart"
            @checkout="handleCheckout"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 结算窗口 -->
    <CheckoutForm
      :visible="showCheckout"
      :cart-items="cart.cartItems"
      :total-price="totalPrice"
      @close="handleCheckoutClose"
      @order-created="handleOrderCreated"
    />
  </div>
</template>

<script>
import { ArrowLeft } from '@element-plus/icons-vue';
import CartService from '../services/CartService';
import CheckoutForm from '../components/Checkout/CheckoutForm.vue';
import CartItem from '../components/Cart/CartItem.vue';
import EmptyCart from '../components/Cart/EmptyCart.vue';
import CartSummary from '../components/Cart/CartSummary.vue';

export default {
  name: 'CartView',
  components: {
    ArrowLeft,
    CheckoutForm,
    CartItem,
    EmptyCart,
    CartSummary
  },
  data() {
    return {
      cart: {
        cartItems: []
      },
      loading: false,
      showCheckout: false
    };
  },
  computed: {
    // 计算商品总数
    totalItems() {
      return this.cart.cartItems.reduce((sum, item) => sum + item.quantity, 0);
    },
    // 计算商品总价
    totalPrice() {
      return this.cart.cartItems.reduce((sum, item) => {
        return sum + parseFloat(item.itemTotal || 0);
      }, 0);
    }
  },
  created() {
    this.fetchCart();
  },
  methods: {
    // 获取购物车数据
    async fetchCart() {
      try {
        this.loading = true;
        const data = await CartService.getCart();
        this.cart = data;
      } catch (error) {
        console.error('获取购物车失败:', error);
        let errorMessage = '获取购物车失败';
        if (error && error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        this.$message.error(errorMessage);
      } finally {
        this.loading = false;
      }
    },
    handleCheckout() {
      if (this.totalItems === 0) {
        this.$message.warning('购物车为空，无法结算');
        return;
      }
      this.showCheckout = true;
    },
    handleCheckoutClose() {
      this.showCheckout = false;
    },
    async handleOrderCreated() {
      // 订单创建成功后，清空购物车
      try {
        await CartService.clearCart();
        this.fetchCart(); // 重新获取购物车
        this.$router.push({ name: 'my-orders' });
      } catch (error) {
        console.error('清空购物车失败:', error);
      }
    },
    
    // 减少商品数量
    async handleDecreaseQuantity(item) {
      try {
        const data = await CartService.updateCartItem(item.id, item.quantity - 1);
        this.cart = data;
        this.$message.success('更新成功');
      } catch (error) {
        console.error('更新商品数量失败:', error);
        let errorMessage = '更新失败';
        if (error && error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        this.$message.error(errorMessage);
      }
    },
    
    // 增加商品数量
    async handleIncreaseQuantity(item) {
      try {
        const data = await CartService.updateCartItem(item.id, item.quantity + 1);
        this.cart = data;
        this.$message.success('更新成功');
      } catch (error) {
        console.error('更新商品数量失败:', error);
        let errorMessage = '更新失败';
        if (error && error.response && error.response.data && error.response.data.message) {
          errorMessage = error.response.data.message;
        }
        this.$message.error(errorMessage);
      }
    },
    
    // 删除商品
    async handleRemoveItem(cartItemId) {
      try {
        await this.$confirm('确定要删除这个商品吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const data = await CartService.removeFromCart(cartItemId);
        // 确保data存在才赋值
        if (data) {
          this.cart = data;
        } else {
          // 如果没有返回数据，重新获取购物车数据
          await this.fetchCart();
        }
        this.$message.success('删除成功');
      } catch (error) {
        // 如果是用户取消，不显示错误消息
        if (error !== 'cancel') {
          console.error('删除商品失败:', error);
          let errorMessage = '删除失败';
          if (error && error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          }
          this.$message.error(errorMessage);
        }
      }
    },
    
    // 清空购物车
    async handleClearCart() {
      try {
        await this.$confirm('确定要清空购物车吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        await CartService.clearCart();
        this.cart.cartItems = [];
        this.$message.success('购物车已清空');
      } catch (error) {
        // 如果是用户取消，不显示错误消息
        if (error !== 'cancel') {
          console.error('清空购物车失败:', error);
          let errorMessage = '清空购物车失败';
          if (error && error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          }
          this.$message.error(errorMessage);
        }
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import '../assets/css/variables.scss';

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background-color: var(--bg-color-light, #f5f5f5);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 10px 0;
}

.cart-title {
  text-align: center;
  color: var(--text-color-primary, #333);
  flex: 1;
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.back-to-home-btn {
  background-color: var(--color-success, #4CAF50);
  border-color: var(--color-success, #4CAF50);
  padding: 10px 20px;
  font-size: 14px;

  &:hover {
    background-color: var(--color-success-hover, #45a049);
    border-color: var(--color-success-hover, #45a049);
  }
}

.cart-card {
  margin-bottom: 30px;
}

.cart-content {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.cart-items-container {
  flex: 1;
  min-width: 300px;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 响应式设计
@media (max-width: 768px) {
  .cart-content {
    flex-direction: column;
  }

  .cart-title {
    font-size: 24px;
  }
}
</style>