<template>
  <div class="product-card">
    <div class="product-image">
      <img :src="product.imageUrl || '/default-product.png'" :alt="product.name">
    </div>
    <div class="product-info">
      <h4 class="product-name">{{ product.name }}</h4>
      <p class="product-description">{{ product.description || '暂无描述' }}</p>
      <p class="product-price">¥{{ product.price || '待定' }}</p>
      <button class="add-to-cart-btn" @click="addToCart">加入购物车</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true
    }
  },
  methods: {
    addToCart() {
      this.$emit('add-to-cart', this.product.id, 1);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.product-card {
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
  display: flex;
  flex-direction: column;
  height: 100%;

  &:hover {
    transform: translateY(-5px);
    box-shadow: var(--box-shadow-hover);
  }

  .product-image {
    width: 100%;
    height: var(--product-card-img-height);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: var(--bg-light);

    img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
      transition: transform var(--transition-normal);
    }
  }

  &:hover .product-image img {
    transform: scale(1.05);
  }

  .product-info {
    padding: var(--spacing-md);
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .product-name {
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 8px;
      line-height: 1.4;
      height: 45px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .product-description {
      font-size: var(--font-size-sm);
      color: var(--text-secondary);
      margin-bottom: var(--spacing-sm);
      height: 40px;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      line-height: 1.4;
    }

    .product-price {
      font-size: var(--font-size-lg);
      font-weight: 600;
      color: var(--text-danger);
      margin-bottom: var(--spacing-md);
    }

    .add-to-cart-btn {
      padding: 10px;
      background-color: var(--primary-color);
      color: var(--text-light);
      border: none;
      border-radius: var(--border-radius);
      cursor: pointer;
      font-size: var(--font-size-sm);
      transition: background-color var(--transition-normal);
      text-align: center;

      &:hover {
        background-color: var(--primary-hover);
      }
    }
  }
}
</style>