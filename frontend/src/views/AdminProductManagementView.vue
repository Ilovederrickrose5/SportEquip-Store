<template>
  <div class="product-module">
    <div class="product-content">
      <div class="product-header">
        <h2 class="product-title">商品管理</h2>
        <div class="action-buttons">
          <el-button @click="$router.push({ name: 'home' })">
            返回首页
          </el-button>
        </div>
      </div>
      
      <!-- 商品统计信息 -->
      <div class="product-stats product-card">
        <div class="stats-content">
          <div class="stat-item">
            <span class="stat-label">商品总数：</span>
            <span class="stat-value">{{ totalProducts }}</span>
          </div>
          <div class="category-stats">
            <span class="stat-label">分类统计：</span>
            <div class="category-stats-list">
              <el-tag 
                v-for="(count, categoryName) in categoryProductCounts" 
                :key="categoryName" 
                effect="light"
              >
                {{ categoryName }}: {{ count }}个
              </el-tag>
            </div>
          </div>
        </div>
        <div class="stats-action">
          <el-button type="primary" @click="showAddModal = true" :disabled="isLoading">
            <el-icon><Plus /></el-icon>
            添加商品
          </el-button>
        </div>
      </div>
      
      <!-- 搜索组件 -->
      <ProductSearch
        v-model="searchQuery"
        :filter-category="filterCategory"
        @update:filter-category="filterCategory = $event"
        :categories="categories"
      />
      
      <!-- 商品列表表格 -->
      <ProductTable
        :products="filteredProducts"
        :loading="isLoading"
        :empty-text="isLoading ? '加载中...' : '暂无商品数据'"
        @edit="editProduct"
        @delete="confirmDelete"
      />
      
      <!-- 分页组件 -->
      <ProductPagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="filteredProducts.length"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
      />
      
      <!-- 添加/编辑商品弹窗 -->
      <ProductFormModal
        :visible="showAddModal || showEditModal"
        :product="editingProduct"
        :categories="allCategories"
        :loading="isLoading"
        :title="showAddModal ? '添加商品' : '编辑商品'"
        @update:visible="onModalVisibleChange"
        @submit="handleSubmit"
        @cancel="closeModal"
      />
      
      <!-- 删除确认弹窗 -->
      <ProductDeleteConfirmModal
        :visible="showDeleteConfirm"
        :product-id="productToDelete?.id"
        :product-name="productToDelete?.name"
        :loading="isLoading"
        @update:visible="showDeleteConfirm = $event"
        @confirm="deleteProduct"
        @cancel="cancelDelete"
      />
    </div>
  </div>
</template>

<style scoped lang="scss">
.product-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}

.stats-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.stat-item {
  white-space: nowrap;
}

.category-stats {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  flex: 1;
}

.category-stats-list {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  margin-left: 8px;
  gap: 8px;
}

.category-stats-list::-webkit-scrollbar {
  height: 4px;
}

.category-stats-list::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}

.stats-action {
  margin-left: 16px;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .product-stats {
    flex-direction: column;
    align-items: stretch;
  }
  
  .stats-action {
    margin-left: 0;
    margin-top: 16px;
    text-align: right;
  }
}
</style>

<script>
import { Plus } from '@element-plus/icons-vue';
import AuthService from '../services/AuthService';
import ProductService from '../services/ProductService';
import ProductTable from '../components/Product/ProductTable.vue';
import ProductSearch from '../components/Product/ProductSearch.vue';
import ProductFormModal from '../components/Product/ProductFormModal.vue';
import ProductDeleteConfirmModal from '../components/Product/ProductDeleteConfirmModal.vue';
import ProductPagination from '../components/Product/ProductPagination.vue';

export default {
  name: 'AdminProductManagementView',
  components: {
    Plus,
    ProductTable,
    ProductSearch,
    ProductFormModal,
    ProductDeleteConfirmModal,
    ProductPagination
  },
  data() {
    return {
      products: [],
      categories: [],
      allCategories: [],
      searchQuery: '',
      filterCategory: '',
      currentPage: 1,
      pageSize: 10,
      pageJumpInput: 1,
      showAddModal: false,
      showEditModal: false,
      showDeleteConfirm: false,
      editingProduct: {
        name: '',
        price: '',
        stock: '',
        description: '',
        imageUrl: '',
        third_category_id: ''
      },
      productToDelete: null,
      isLoading: false,
      errorMessage: ''
    };
  },
  computed: {
    totalProducts() {
      return this.products.length;
    },
    
    categoryProductCounts() {
      // 统计各分类的商品数量
      const counts = {};
      
      // 先初始化所有主分类的计数为0
      this.categories.forEach(category => {
        counts[category.name] = 0;
      });
      
      // 统计每个商品所属的主分类
      this.products.forEach(product => {
        // 找到商品对应的主分类
        const productCategory = this.categories.find(cat => cat.id === product.mainCategoryId);
        if (productCategory && counts.hasOwnProperty(productCategory.name)) {
          counts[productCategory.name]++;
        }
      });
      
      return counts;
    },
    
    filteredProducts() {
      let result = [...this.products];
      
      // 搜索过滤
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter(product => 
          product.name.toLowerCase().includes(query)
        );
      }
      
      // 分类过滤
      if (this.filterCategory) {
        const filterId = Number(this.filterCategory);
        
        // 检查是否为主分类ID
        if (filterId >= 1 && filterId <= 4) {
          // 如果是主分类，则只筛选主分类ID匹配的商品
          result = result.filter(product => product.mainCategoryId === filterId);
        } else {
          // 如果不是主分类，则继续使用原来的逻辑
          result = result.filter(product => 
            product.subCategoryId === filterId ||
            product.thirdCategoryId === filterId
          );
        }
      }
      
      // 分页
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return result.slice(start, end);
    }
  },
  watch: {
    // 当分类或搜索条件改变时，重置到第一页
    filterCategory() {
      this.currentPage = 1;
      this.pageJumpInput = 1;
    },
    searchQuery() {
      this.currentPage = 1;
      this.pageJumpInput = 1;
    },
    // 当当前页码改变时，同步更新输入框的值
    currentPage(newPage) {
      this.pageJumpInput = newPage;
    }
  },
  created() {
    this.loadInitialData();
  },
  methods: {
    async loadInitialData() {
      try {
        // 检查权限
        const currentUser = AuthService.getCurrentUser();
        if (!currentUser || !AuthService.hasRole('ADMIN')) {
          this.$message.error('您没有权限访问此功能');
          this.$router.push({ name: 'home' });
          return;
        }
        
        this.isLoading = true;
        
        // 并行加载商品和分类数据
        const [products, categoriesResponse] = await Promise.all([
          ProductService.getAllProducts(),
          ProductService.getAllCategories()
        ]);
        
        this.products = products;
        
        // 处理分类数据
        const allCategories = [];
        const mainCategories = [];
        
        if (categoriesResponse && Array.isArray(categoriesResponse)) {
          categoriesResponse.forEach(main => {
            mainCategories.push(main);
            
            if (main.subCategories && Array.isArray(main.subCategories)) {
              main.subCategories.forEach(sub => {
                sub.mainCategory = { id: main.id, name: main.name };
                allCategories.push(sub);
                
                if (sub.thirdCategories && Array.isArray(sub.thirdCategories)) {
                  sub.thirdCategories.forEach(third => {
                    third.subCategory = sub;
                    third.mainCategory = main;
                    allCategories.push(third);
                  });
                }
              });
            }
          });
        }
        
        this.categories = mainCategories;
        this.allCategories = allCategories.filter(cat => cat.thirdCategories === undefined);
        
      } catch (error) {
        console.error('Failed to load data:', error);
        this.errorMessage = error.response?.data || '加载数据失败';
        this.$message.error(this.errorMessage);
        
        if (error.response?.status === 403) {
          setTimeout(() => {
            this.$router.push({ name: 'home' });
          }, 1500);
        }
      } finally {
        this.isLoading = false;
      }
    },
    
    editProduct(product) {
      // 创建商品数据的深拷贝
      this.editingProduct = JSON.parse(JSON.stringify(product));
      this.showEditModal = true;
      this.showAddModal = false;
    },
    
    closeModal() {
      this.showAddModal = false;
      this.showEditModal = false;
      this.editingProduct = {
        name: '',
        price: '',
        stock: '',
        description: '',
        imageUrl: '',
        third_category_id: ''
      };
      this.errorMessage = '';
    },
    
    async handleSubmit(productData) {
      try {
        this.isLoading = true;
        
        // 准备商品数据
        const data = {
          name: productData.name,
          price: Number(productData.price),
          stock: Number(productData.stock),
          description: productData.description,
          imageUrl: productData.imageUrl,
          thirdCategory: {
            id: Number(productData.third_category_id || productData.categoryId)
          }
        };
        
        let updatedProduct;
        if (this.showAddModal) {
          // 添加新商品
          updatedProduct = await ProductService.createProduct(data);
          this.products.unshift(updatedProduct);
          this.$message.success('商品添加成功');
        } else {
          // 更新现有商品
          updatedProduct = await ProductService.updateProduct(productData.id, data);
          
          // 更新本地商品列表
          const index = this.products.findIndex(p => p.id === productData.id);
          if (index !== -1) {
            this.products[index] = updatedProduct;
          }
          this.$message.success('商品信息更新成功');
        }
        
        this.closeModal();
      } catch (error) {
        console.error('Failed to submit product:', error);
        this.$message.error('操作失败: ' + (error.response?.data || error.message));
      } finally {
        this.isLoading = false;
      }
    },
    
    confirmDelete(product) {
      this.productToDelete = product;
      this.showDeleteConfirm = true;
    },
    
    cancelDelete() {
      this.showDeleteConfirm = false;
      this.productToDelete = null;
    },
    
    async deleteProduct(productId) {
        try {
          this.isLoading = true;
          await ProductService.deleteProduct(productId);
          
          // 从本地列表中移除
          this.products = this.products.filter(p => p.id !== productId);
          
          this.cancelDelete();
          this.$message.success('商品删除成功');
        } catch (error) {
          console.error('Failed to delete product:', error);
          this.$message.error('删除失败: ' + (error.response?.data || error.message));
        } finally {
          this.isLoading = false;
        }
      },
      
      // 处理弹窗可见性变化
      onModalVisibleChange(visible) {
        if (!visible) {
          this.closeModal();
        }
      }
  }
};
</script>

<style lang="scss" scoped>
@import '../components/Product/product-common.scss';

// 商品统计样式增强
.product-stats {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--spacing-xl);
  padding: var(--spacing-lg);
  background-color: var(--bg-gray-light);
  
  .stat-item {
    display: flex;
    align-items: center;
    
    .stat-label {
      font-weight: 600;
      color: var(--text-secondary);
      margin-right: var(--spacing-xs);
    }
    
    .stat-value {
      font-size: var(--font-size-xl);
      font-weight: 700;
      color: var(--primary-color);
    }
  }
  
  .category-stats {
    display: flex;
    flex-direction: column;
    
    .stat-label {
      font-weight: 600;
      color: var(--text-secondary);
      margin-bottom: var(--spacing-xs);
    }
    
    .category-stats-list {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-sm);
    }
  }
}

// 响应式优化
@media (max-width: 768px) {
  .product-stats {
    flex-direction: column;
    align-items: stretch;
    gap: var(--spacing-md);
  }
  
  .category-stats {
    .category-stats-list {
      justify-content: center;
    }
  }
}
</style>