<template>
  <div class="product-table-container">
    <el-table 
      :data="products" 
      style="width: 100%"
      stripe
      :empty-text="emptyText"
    >
      <el-table-column prop="imageUrl" label="商品图片" width="100" header-align="center">
        <template #default="scope">
          <img 
            :src="scope.row.imageUrl || '/src/assets/default-product.png'" 
            :alt="scope.row.name" 
            class="product-image" 
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" header-align="center"></el-table-column>
      <el-table-column prop="price" label="价格" width="60" header-align="center">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="55" header-align="center"></el-table-column>
      <el-table-column label="分类" min-width="180" header-align="center">
        <template #default="scope">
          {{ formatCategory(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" min-width="160" header-align="center">
        <template #default="scope">
          {{ formatDate(scope.row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right" align="right" header-align="center">
        <template #default="scope">
          <div class="action-buttons-vertical">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(scope.row)"
              :disabled="loading"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
              :disabled="loading"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'ProductTable',
  props: {
    products: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    emptyText: {
      type: String,
      default: '暂无商品数据'
    }
  },
  emits: ['edit', 'delete'],
  methods: {
    handleEdit(product) {
      this.$emit('edit', product);
    },
    handleDelete(product) {
      this.$emit('delete', product);
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    },
    formatCategory(product) {
      if (product.thirdCategoryName) {
        return `${product.mainCategoryName || ''} / ${product.subCategoryName || ''} / ${product.thirdCategoryName}`;
      } else if (product.subCategoryName) {
        return `${product.mainCategoryName || ''} / ${product.subCategoryName}`;
      } else {
        return product.mainCategoryName || '-';
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../../assets/css/variables.scss';

.product-table-container {
  padding: var(--spacing-md);
  background-color: var(--bg-white);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--box-shadow);
  margin-bottom: var(--spacing-lg);
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: var(--border-radius);
}

.action-buttons-vertical {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-end;
}

::v-deep(.action-buttons-vertical .el-button--small) {
  margin-right: 0;
  margin-bottom: 4px;
  width: 60px;
}

::v-deep(.action-buttons-vertical .el-button--small:last-child) {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .product-table-container {
    padding: var(--spacing-sm);
    overflow-x: auto;
  }

  ::v-deep(.el-table) {
    width: 100%;
    min-width: 600px;
  }
}
</style>