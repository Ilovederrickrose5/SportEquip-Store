<template>
  <div class="product-search-container">
    <div class="search-filter-wrapper">
      <el-input
        v-model="localSearchQuery"
        placeholder="搜索商品名称..."
        class="search-input"
        @input="handleSearch"
        clearable
      >
        <template #prefix>
          <el-icon class="el-input__icon"><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select 
        v-model="localFilterCategory" 
        placeholder="所有分类" 
        class="filter-select"
        @change="handleCategoryChange"
      >
        <el-option value="" label="所有分类"></el-option>
        <el-option 
          v-for="category in categories" 
          :key="category.id" 
          :value="category.id" 
          :label="category.name"
        ></el-option>
      </el-select>
    </div>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue';

export default {
  name: 'ProductSearch',
  components: {
    Search
  },
  props: {
    searchQuery: {
      type: String,
      default: ''
    },
    filterCategory: {
      type: String,
      default: ''
    },
    categories: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:searchQuery', 'update:filterCategory'],
  data() {
    return {
      localSearchQuery: this.searchQuery,
      localFilterCategory: this.filterCategory
    };
  },
  watch: {
    searchQuery(newVal) {
      this.localSearchQuery = newVal;
    },
    filterCategory(newVal) {
      this.localFilterCategory = newVal;
    }
  },
  methods: {
    handleSearch() {
      this.$emit('update:searchQuery', this.localSearchQuery);
    },
    handleCategoryChange() {
      this.$emit('update:filterCategory', this.localFilterCategory);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.product-search-container {
  background-color: var(--bg-white);
  padding: var(--spacing-lg);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow);
  margin-bottom: var(--spacing-lg);
}

.search-filter-wrapper {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.search-input {
  flex: 1;
  max-width: 500px;
}

.filter-select {
  width: 200px;
}

@media (max-width: 768px) {
  .search-filter-wrapper {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-select {
    width: 100%;
  }
}
</style>