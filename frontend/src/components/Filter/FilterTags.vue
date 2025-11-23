<template>
  <div class="filter-tags" v-if="Object.keys(filters).length > 0">
    <div class="filter-tag" v-if="filters.search">
      <span>搜索: {{ filters.search }}</span>
      <button class="remove-filter" @click="removeFilter('search')">×</button>
    </div>
    
    <template v-for="(values, type) in getArrayFilters" :key="type">
      <div 
        v-for="value in values" 
        :key="`${type}-${value}`" 
        class="filter-tag"
      >
        <span>{{ getFilterLabel(type, value) }}</span>
        <button class="remove-filter" @click="removeValueFilter(type, value)">×</button>
      </div>
    </template>
    
    <div class="filter-tag" v-if="filters.price && (filters.price.min || filters.price.max)">
      <span>价格: {{ filters.price.min || '0' }} - {{ filters.price.max || '不限' }}</span>
      <button class="remove-filter" @click="removeFilter('price')">×</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FilterTags',
  props: {
    filters: {
      type: Object,
      required: true
    },
    filterLabels: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    getArrayFilters() {
      const result = {};
      for (const [key, value] of Object.entries(this.filters)) {
        if (Array.isArray(value) && value.length > 0 && key !== 'search' && key !== 'price') {
          result[key] = value;
        }
      }
      return result;
    }
  },
  methods: {
    getFilterLabel(type, value) {
      const typeLabels = this.filterLabels[type] || {};
      return typeLabels[value] || `${type}: ${value}`;
    },
    removeFilter(type) {
      this.$emit('remove-filter', type);
    },
    removeValueFilter(type, value) {
      this.$emit('remove-value', type, value);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--border-light);
}

.filter-tag {
  display: inline-flex;
  align-items: center;
  background-color: #e8f5e9;
  color: var(--text-success);
  padding: 6px 12px;
  border-radius: var(--border-radius-xl);
  font-size: var(--font-size-sm);
  animation: fadeIn var(--transition-normal) ease-in-out;
  transition: transform var(--transition-fast) ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--box-shadow);
  }

  .remove-filter {
    background: none;
    border: none;
    color: var(--text-success);
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    margin-left: 8px;
    padding: 0;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    transition: background-color var(--transition-fast);

    &:hover {
      background-color: rgba(46, 125, 50, 0.2);
    }
  }
}

/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>