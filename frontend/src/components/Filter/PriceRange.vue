<template>
  <div class="price-range-container">
    <div class="price-range">
      <input 
        type="number" 
        v-model.number="localMinPrice" 
        placeholder="最低价"
        @change="handleChange"
      >
      <span>-</span>
      <input 
        type="number" 
        v-model.number="localMaxPrice" 
        placeholder="最高价"
        @change="handleChange"
      >
      <button class="apply-price-btn" @click="handleApply">应用</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PriceRange',
  props: {
    modelValue: {
      type: Object,
      default: () => ({ min: null, max: null })
    }
  },
  data() {
    return {
      localMinPrice: this.modelValue.min,
      localMaxPrice: this.modelValue.max
    };
  },
  watch: {
    modelValue: {
      handler(newVal) {
        this.localMinPrice = newVal.min;
        this.localMaxPrice = newVal.max;
      },
      deep: true
    }
  },
  methods: {
    handleChange() {
      // 可以在这里添加即时更新
    },
    handleApply() {
      this.$emit('update:modelValue', {
        min: this.localMinPrice,
        max: this.localMaxPrice
      });
      this.$emit('apply', {
        min: this.localMinPrice,
        max: this.localMaxPrice
      });
    }
  }
};
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.price-range-container {
  .price-range {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    margin-bottom: var(--spacing-sm);

    input {
      width: calc(50% - 20px);
      padding: 8px var(--spacing-sm);
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      font-size: var(--font-size-sm);
      box-sizing: border-box;
      min-width: 80px;
      transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
      background-color: var(--bg-white);
      color: var(--text-primary);

      &::placeholder {
        color: var(--text-muted);
      }

      &:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
      }

      &:invalid {
        border-color: var(--text-danger);
      }
    }

    span {
      color: var(--text-secondary);
      font-size: var(--font-size-sm);
      margin: 0 4px;
      min-width: 10px;
      text-align: center;
    }

    .apply-price-btn {
      width: 100%;
      padding: 8px var(--spacing-md);
      margin-top: var(--spacing-sm);
      background-color: var(--primary-color);
      color: var(--text-light);
      border: none;
      border-radius: var(--border-radius);
      cursor: pointer;
      font-size: var(--font-size-sm);
      transition: background-color var(--transition-normal), transform var(--transition-fast);
      font-weight: 500;

      &:hover {
        background-color: var(--primary-hover);
        transform: translateY(-1px);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 响应式设计优化
@media (max-width: 768px) {
  .price-range-container {
    .price-range {
      gap: 6px;

      input {
        width: calc(50% - 15px);
        padding: 6px var(--spacing-sm);
      }

      .apply-price-btn {
        padding: 6px var(--spacing-md);
      }
    }
  }
}
</style>