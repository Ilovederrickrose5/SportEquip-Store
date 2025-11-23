<template>
  <div class="products-grid">
    <ProductCard 
      v-for="product in products" 
      :key="product.id"
      :product="product"
      @add-to-cart="handleAddToCart"
    />
    
    <!-- 如果没有商品时显示 -->
    <div class="no-products" v-if="products.length === 0">
      <p>暂无符合条件的商品</p>
    </div>
  </div>
</template>

<script>
import ProductCard from './ProductCard.vue';

export default {
  name: 'ProductGrid',
  components: {
    ProductCard
  },
  props: {
    products: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    handleAddToCart(productId, quantity) {
      this.$emit('add-to-cart', productId, quantity);
    }
  }
}
</script>

<style scoped lang="scss">
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 25px;

  @media (max-width: 768px) {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
}

.no-products {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px 0;
  color: #666;
  font-size: 16px;
}
</style>