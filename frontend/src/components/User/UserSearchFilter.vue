<template>
  <div class="user-search-filter">
    <el-input
      v-model="searchQuery"
      placeholder="搜索用户名或邮箱..."
      clearable
      class="search-input"
      @input="onSearchChange"
    />
    <el-select
      v-model="filterRole"
      placeholder="选择角色"
      class="filter-select"
      @change="onRoleChange"
      size="default"
    >
      <el-option label="所有角色" value="" />
      <el-option label="普通用户" value="USER" />
      <el-option label="管理员" value="ADMIN" />
    </el-select>
  </div>
</template>

<script>
export default {
  name: 'UserSearchFilter',
  props: {
    modelValue: {
      type: Object,
      default: () => ({ searchQuery: '', filterRole: '' })
    }
  },
  computed: {
    searchQuery: {
      get() {
        return this.modelValue.searchQuery
      },
      set(value) {
        this.$emit('update:modelValue', {
          ...this.modelValue,
          searchQuery: value
        })
      }
    },
    filterRole: {
      get() {
        return this.modelValue.filterRole
      },
      set(value) {
        this.$emit('update:modelValue', {
          ...this.modelValue,
          filterRole: value
        })
      }
    }
  },
  methods: {
    onSearchChange() {
      this.$emit('search-change')
    },
    onRoleChange() {
      this.$emit('role-change')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.user-search-filter {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
  align-items: center;

  .search-input {
    flex: 1;
    max-width: 300px;
  }

  .filter-select {
    min-width: 150px;
  }

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: stretch;

    .search-input {
      max-width: none;
    }
  }
}

// 隐藏选择角色下拉框左侧的空白复选框
::v-deep(.el-select__input.is-focus) {
  position: relative;
  &::before {
    display: none;
  }
}

::v-deep(.el-select__caret) {
  z-index: 1;
}

::v-deep(.el-input__inner) {
  padding-left: 15px;
}
</style>