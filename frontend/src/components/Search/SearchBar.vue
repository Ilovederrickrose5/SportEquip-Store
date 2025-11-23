<template>
  <div class="search-section">
    <input 
      type="text" 
      v-model="searchQuery"
      :placeholder="placeholder"
      class="search-input"
    >
    <button class="search-btn" @click="handleSearch">🔍</button>
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  props: {
    placeholder: {
      type: String,
      default: '搜索商品...'
    },
    modelValue: {
      type: String,
      default: ''
    }
  },
  computed: {
    searchQuery: {
      get() {
        return this.modelValue;
      },
      set(value) {
        this.$emit('update:modelValue', value);
      }
    }
  },
  methods: {
    handleSearch() {
      this.$emit('search', this.searchQuery);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.search-section {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 300px;
  border-radius: var(--border-radius);
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: box-shadow var(--transition-fast);

  &:focus-within {
    box-shadow: 0 2px 6px rgba(76, 175, 80, 0.2);
  }

  .search-input {
    flex: 1;
    padding: 10px 15px;
    border: 1px solid var(--border-color);
    border-right: none;
    border-radius: var(--border-radius) 0 0 var(--border-radius);
    font-size: var(--font-size-sm);
    outline: none;
    transition: border-color var(--transition-normal);
    background-color: var(--bg-white);
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-muted);
    }

    &:focus {
      border-color: var(--primary-color);
    }
  }

  .search-btn {
    padding: 10px 20px;
    background-color: var(--primary-color);
    color: var(--text-light);
    border: none;
    border-radius: 0 var(--border-radius) var(--border-radius) 0;
    cursor: pointer;
    font-size: var(--font-size-sm);
    transition: background-color var(--transition-normal);
    display: flex;
    align-items: center;
    justify-content: center;
    min-width: 50px;

    &:hover {
      background-color: var(--primary-hover);
    }
  }
}

// 响应式调整
@media (max-width: 480px) {
  .search-section {
    min-width: 200px;

    .search-input {
      padding: 8px 12px;
    }

    .search-btn {
      padding: 8px 16px;
      min-width: 45px;
    }
  }
}
</style>