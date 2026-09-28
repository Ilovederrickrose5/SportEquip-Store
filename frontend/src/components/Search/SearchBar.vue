<template>
  <div class="search-section">
    <input
      type="text"
      :value="inputValue"
      :placeholder="placeholder"
      class="search-input"
      @input="onInput"
      @keyup.enter="handleSearch"
    >
    <button class="search-btn" @click="handleSearch">🔍</button>
  </div>
</template>

<script>
import { debounce } from '../../utils/debounce'

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
  data() {
    return {
      // 本地输入值：与父组件 modelValue 解耦，输入过程只在本地流转
      inputValue: this.modelValue
    }
  },
  created() {
    // 输入防抖：静默 300ms 后才更新父组件的 modelValue，
    // 避免分类页的 computed 过滤链（分类+品牌+价格+关键词四重联动）在每次按键时都全量重算
    this.debouncedInput = debounce((value) => {
      this.$emit('update:modelValue', value)
      this.$emit('search', value)
    }, 300)
  },
  beforeUnmount() {
    // 组件销毁时取消尚未执行的防抖回调，避免操作已卸载实例
    this.debouncedInput.cancel()
  },
  watch: {
    // 外部重置（如"清空筛选"按钮）时，同步回显到输入框
    modelValue(newVal) {
      if (newVal !== this.inputValue) {
        this.inputValue = newVal
      }
    }
  },
  methods: {
    onInput(event) {
      this.inputValue = event.target.value
      this.debouncedInput(this.inputValue)
    },
    handleSearch() {
      // 按钮点击 / 回车为用户显式动作：立即生效，取消未执行的防抖任务避免重复
      this.debouncedInput.cancel()
      this.$emit('update:modelValue', this.inputValue)
      this.$emit('search', this.inputValue)
    }
  }
}
</script>

<style lang="scss" scoped>
@use '../../assets/css/variables.scss' as *;

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
    box-shadow: 0 2px 6px rgba(51, 51, 51, 0.2);
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