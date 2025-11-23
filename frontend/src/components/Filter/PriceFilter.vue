<template>
  <div class="filter-section">
    <h3>价格区间</h3>
    <div class="price-range">
      <input 
        type="number" 
        v-model="minPriceInput" 
        placeholder="最低价"
        @input="handleInput"
      >
      <span>-</span>
      <input 
        type="number" 
        v-model="maxPriceInput" 
        placeholder="最高价"
        @input="handleInput"
      >
    </div>
    <button class="apply-price-btn" @click="applyFilter">应用</button>
  </div>
</template>

<script>
export default {
  name: 'PriceFilter',
  props: {
    modelValue: {
      type: Object,
      default: () => ({ min: '', max: '' })
    }
  },
  data() {
    return {
      minPriceInput: this.modelValue.min || '',
      maxPriceInput: this.modelValue.max || ''
    };
  },
  watch: {
    modelValue: {
      handler(newVal) {
        this.minPriceInput = newVal.min || '';
        this.maxPriceInput = newVal.max || '';
      },
      deep: true
    }
  },
  methods: {
    applyFilter() {
      const filterValue = {
        min: this.minPriceInput,
        max: this.maxPriceInput
      };
      this.$emit('update:modelValue', filterValue);
      this.$emit('apply', filterValue);
    },
    handleInput() {
      // 实时处理输入，但不触发应用
    }
  }
}
</script>

<style scoped>
.filter-section {
  margin-bottom: 30px;
}

.filter-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
  font-weight: 600;
}

.price-range {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.price-range input {
  width: calc(50% - 20px);
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  min-width: 80px;
}

.price-range span {
  color: #666;
  font-size: 14px;
  margin: 0 4px;
}

.apply-price-btn {
  width: 100%;
  padding: 8px 15px;
  margin-top: 10px;
  background-color: var(--primary-color, #4CAF50);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.apply-price-btn:hover {
  background-color: var(--primary-dark, #45a049);
}
</style>