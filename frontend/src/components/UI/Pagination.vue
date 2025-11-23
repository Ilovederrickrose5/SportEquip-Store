<template>
  <div class="pagination-controls">
    <button 
      class="pagination-btn" 
      @click="prevPage" 
      :disabled="currentPage === 1"
    >
      ‹
    </button>
    <span class="pagination-info">
      第 {{ currentPage }} 页 / 共 {{ totalPages }} 页
    </span>
    <div class="page-jump">
      <span>跳至</span>
      <input 
        type="number" 
        min="1" 
        :max="totalPages"
        v-model="pageInput"
        @change="handlePageChange"
        @keydown.enter="handlePageChange"
        class="page-input"
      >
      <span>页</span>
    </div>
    <button 
      class="pagination-btn" 
      @click="nextPage" 
      :disabled="currentPage === totalPages"
    >
      ›
    </button>
  </div>
</template>

<script>
export default {
  name: 'Pagination',
  props: {
    currentPage: {
      type: Number,
      required: true
    },
    totalPages: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      pageInput: this.currentPage
    };
  },
  watch: {
    currentPage(newVal) {
      this.pageInput = newVal;
    }
  },
  methods: {
    prevPage() {
      if (this.currentPage > 1) {
        this.$emit('page-change', this.currentPage - 1);
      }
    },
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.$emit('page-change', this.currentPage + 1);
      }
    },
    handlePageChange() {
      const page = parseInt(this.pageInput);
      if (!isNaN(page) && page >= 1 && page <= this.totalPages) {
        this.$emit('page-change', page);
      } else {
        this.pageInput = this.currentPage;
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.pagination-controls {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;

  .pagination-btn {
    width: 32px;
    height: 32px;
    border: 1px solid var(--border-color);
    background-color: var(--bg-white);
    color: var(--text-primary);
    border-radius: var(--border-radius);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    transition: all var(--transition-normal);
    font-weight: 500;

    &:hover:not(:disabled) {
      background-color: var(--bg-hover);
      border-color: var(--primary-color);
      color: var(--primary-color);
      transform: translateY(-1px);
      box-shadow: var(--box-shadow);
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }

  .pagination-info {
    font-size: var(--font-size-sm);
    color: var(--text-secondary);
    min-width: 120px;
  }

  .page-jump {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: var(--font-size-sm);
    color: var(--text-secondary);

    .page-input {
      width: 60px;
      padding: 5px;
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius);
      text-align: center;
      font-size: var(--font-size-sm);
      transition: border-color var(--transition-fast);

      &:focus {
        outline: none;
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
      }
    }
  }
}

// 响应式调整
@media (max-width: 480px) {
  .pagination-controls {
    gap: 8px;

    .pagination-info {
      min-width: auto;
      margin-right: auto;
    }
  }
}
</style>