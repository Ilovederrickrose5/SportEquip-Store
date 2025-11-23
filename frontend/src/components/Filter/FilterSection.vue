<template>
  <div class="filter-section">
    <h3>{{ title }}</h3>
    <div class="filter-options">
      <label 
        v-for="option in options" 
        :key="option.value" 
        class="filter-option"
      >
        <input 
          :type="type"
          :value="option.value"
          v-model="localSelected"
          @change="handleChange"
        >
        <span>{{ option.label }}</span>
      </label>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FilterSection',
  props: {
    title: {
      type: String,
      required: true
    },
    options: {
      type: Array,
      required: true
    },
    type: {
      type: String,
      default: 'checkbox',
      validator: value => ['checkbox', 'radio'].includes(value)
    },
    modelValue: {
      type: [Array, String, Number],
      default: () => []
    }
  },
  computed: {
    localSelected: {
      get() {
        return this.modelValue;
      },
      set(value) {
        this.$emit('update:modelValue', value);
      }
    }
  },
  methods: {
    handleChange() {
      this.$emit('change', this.localSelected);
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.filter-section {
  margin-bottom: var(--spacing-xl);
}

.filter-section h3 {
  font-size: var(--font-size-md);
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
  font-weight: 600;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.filter-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  transition: color var(--transition-normal);
  padding: 4px 0;

  &:hover {
    color: var(--primary-color);
  }

  input {
    margin-right: var(--spacing-sm);
    cursor: pointer;
    accent-color: var(--primary-color);
  }

  /* 筛选选项的选中状态样式优化 */
  input[type="checkbox"]:checked + span,
  input[type="radio"]:checked + span {
    color: var(--primary-color);
    font-weight: 600;
  }
}
</style>