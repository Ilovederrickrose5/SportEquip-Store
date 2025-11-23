<template>
  <div class="outdoor-adventure-page">
    <!-- 顶部搜索和返回按钮 -->
    <div class="page-header">
      <div class="header-container">
        <SearchBar 
          v-model="searchQuery" 
          placeholder="搜索户外探险装备..." 
          @search="handleSearch"
        />
        
        <!-- 分页控件 -->
        <div class="pagination-header">
          <Pagination 
            :current-page="currentPage" 
            :total-pages="totalPages" 
            @page-change="handlePageChange"
          />
        </div>
        
        <el-button 
          class="back-btn" 
          @click="goBack"
          :icon="ArrowLeft"
        >
          返回
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="container">
        <!-- 左侧筛选功能 -->
        <div class="filter-sidebar">
          <!-- 可滚动内容区域 -->
          <div class="filter-content">
            <FilterSection 
              title="户外分类" 
              :options="categoryOptions" 
              v-model="selectedCategories" 
              type="checkbox"
            />

            <FilterSection 
              title="具体产品类型" 
              :options="filteredProductTypeOptions" 
              v-model="selectedProductTypes" 
              type="checkbox"
            />
          </div>

          <!-- 固定在底部的区域 -->
          <div class="filter-footer">
            <el-button 
              class="reset-filter-btn" 
              @click="resetFilters"
              plain
            >
              重置筛选
            </el-button>
            <PriceFilter 
              v-model="priceFilter" 
              @apply="handlePriceFilter"
            />
          </div>
        </div>

        <!-- 右侧商品列表容器 -->
        <div class="products-container">
          <!-- 筛选条件显示区域 -->
          <FilterTags 
            :filters="activeFilters" 
            :filter-labels="filterLabels" 
            @remove-filter="removeFilter" 
            @remove-value="removeValueFilter"
          />
          
          <!-- 筛选结果信息 -->
          <div class="filter-info">
            <span v-if="!loading">找到 {{ filteredProducts.length }} 件商品</span>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <el-empty description="数据加载中..."></el-empty>
            <p class="loading-status">{{ loadingStatus }}</p>
          </div>
          
          <!-- 商品卡片网格 -->
          <div v-else class="products-grid">
            <!-- 商品卡片 -->
            <ProductCard 
              v-for="product in paginatedProducts" 
              :key="product.id" 
              :product="product"
              @add-to-cart="addToCart"
            />

            <!-- 如果没有商品时显示 -->
            <el-empty 
              v-if="paginatedProducts.length === 0" 
              description="暂无符合条件的商品"
              class="no-products"
            ></el-empty>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ArrowLeft } from '@element-plus/icons-vue';
import SearchBar from '../components/Search/SearchBar.vue';
import Pagination from '../components/UI/Pagination.vue';
import FilterSection from '../components/Filter/FilterSection.vue';
import PriceFilter from '../components/Filter/PriceFilter.vue';
import FilterTags from '../components/Filter/FilterTags.vue';
import ProductCard from '../components/ProductDisplay/ProductCard.vue';

export default {
  name: 'OutdoorAdventureView',
  components: {
    SearchBar,
    Pagination,
    FilterSection,
    PriceFilter,
    FilterTags,
    ProductCard,
    ArrowLeft
  },
  data() {
    return {
      loading: true,
      loadingStatus: '正在加载数据...',
      products: [],
      searchQuery: '',
      selectedCategories: [],
      selectedProductTypes: [],
      selectedOutdoorTypes: [],
      priceFilter: { min: '', max: '' },
      currentPage: 1,
      itemsPerPage: 20,
      subCategories: [],
      thirdCategories: [],
      brands: []
    };
  },
  computed: {
    // 分类选项格式转换
    categoryOptions() {
      return this.subCategories.map(cat => ({
        label: cat.name,
        value: cat.id.toString()
      }));
    },
    
    // 过滤后的产品类型选项
    filteredProductTypeOptions() {
      const productTypes = this.getFilteredThirdCategories();
      return productTypes.map(type => ({
        label: type.name,
        value: type.id.toString()
      }));
    },
    
    // 筛选后的产品
    filteredProducts() {
      let results = [...this.products];
      
      // 搜索筛选
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        results = results.filter(product => 
          product.name.toLowerCase().includes(query) || 
          product.description.toLowerCase().includes(query) ||
          (product.brand && product.brand.toLowerCase().includes(query))
        );
      }
      
      // 二级分类筛选
      if (this.selectedCategories.length > 0) {
        results = results.filter(product => 
          this.selectedCategories.includes(product.subCategoryId?.toString())
        );
      }
      
      // 三级分类(产品类型)筛选
      if (this.selectedProductTypes.length > 0) {
        results = results.filter(product => 
          this.selectedProductTypes.includes(product.thirdCategoryId?.toString())
        );
      }
      
      // 户外类型筛选（保留用于兼容）
      if (this.selectedOutdoorTypes.length > 0) {
        results = results.filter(product => 
          this.selectedOutdoorTypes.includes(product.thirdCategoryId?.toString())
        );
      }
      
      // 价格筛选
      if (this.priceFilter.min) {
        const min = parseFloat(this.priceFilter.min);
        results = results.filter(product => product.price >= min);
      }
      if (this.priceFilter.max) {
        const max = parseFloat(this.priceFilter.max);
        results = results.filter(product => product.price <= max);
      }
      
      return results;
    },
    
    // 分页后的产品
    paginatedProducts() {
      const start = (this.currentPage - 1) * this.itemsPerPage;
      const end = start + this.itemsPerPage;
      return this.filteredProducts.slice(start, end);
    },
    
    // 总页数
    totalPages() {
      return Math.max(1, Math.ceil(this.filteredProducts.length / this.itemsPerPage));
    },
    
    // 活动筛选条件
    activeFilters() {
      const filters = {};
      
      if (this.searchQuery) {
        filters.search = this.searchQuery;
      }
      
      if (this.selectedCategories.length > 0) {
        filters.categories = this.selectedCategories;
      }
      
      if (this.selectedProductTypes.length > 0) {
        filters.productTypes = this.selectedProductTypes;
      }
      
      if (this.priceFilter.min || this.priceFilter.max) {
        filters.price = this.priceFilter;
      }
      
      return filters;
    },
    
    // 筛选标签映射
    filterLabels() {
      const categoryMap = {};
      const productTypeMap = {};
      
      this.subCategories.forEach(cat => {
        categoryMap[cat.id.toString()] = cat.name;
      });
      
      this.thirdCategories.forEach(type => {
        productTypeMap[type.id.toString()] = type.name;
      });
      
      return {
        categories: categoryMap,
        productTypes: productTypeMap
      };
    }
  },
  watch: {
    // 监听路由参数中的search参数
    '$route.query.search': {
      handler(newSearch) {
        if (newSearch) {
          this.searchQuery = newSearch;
          this.currentPage = 1; // 搜索时重置到第一页
        }
      },
      immediate: true // 组件加载时立即执行
    },
    
    // 当选中分类改变时，重置产品类型选择和页码
    selectedCategories() {
      this.selectedProductTypes = [];
      this.selectedOutdoorTypes = [];
      this.currentPage = 1;
    },
    
    // 当筛选结果改变时，确保页码有效
    filteredProducts() {
      if (this.currentPage > this.totalPages && this.totalPages > 0) {
        this.currentPage = this.totalPages;
      }
    }
  },
  mounted() {
    this.loadData();
  },
  methods: {
    // 加载所有必要的数据
    async loadData() {
      this.loading = true;
      this.loadingStatus = '正在加载数据...';
      
      try {
        // 导入ProductService
        const { default: productService } = await import('../services/ProductService');
        
        // 调用API获取所有产品数据
        this.loadingStatus = '正在请求所有商品数据...';
        const allProducts = await productService.getAllProducts();
        
        // 过滤出户外探险商品（主分类ID为2）
        this.products = Array.isArray(allProducts) ? allProducts.filter(product => product.mainCategoryId === 2) : [];
          
        this.loadingStatus = `成功加载 ${this.products.length} 个户外探险商品`;
        
        // 处理分类数据
        this.processCategoryData(this.products);
      } catch (error) {
        this.loadingStatus = '数据加载失败';
        console.error('加载商品数据失败:', error);
        this.$message.error('数据加载失败，请稍后重试');
      } finally {
        this.loading = false;
      }
    },
    
    // 处理分类数据
    processCategoryData(products) {
      if (!Array.isArray(products)) return;
      
      const uniqueSubCategories = {};
      const uniqueThirdCategories = {};
      
      // 手动添加户外探险的二级分类
      uniqueSubCategories[11] = {id:11,name:'登山装备',main_category_id:2};
      uniqueSubCategories[12] = {id:12,name:'露营装备',main_category_id:2};
      uniqueSubCategories[13] = {id:13,name:'徒步装备',main_category_id:2};
      
      // 手动添加户外探险的三级分类
      const thirdCategoryData = [
        {id:24,name:'登山鞋',sub_category_id:11},
        {id:25,name:'冲锋衣',sub_category_id:11},
        {id:26,name:'登山背包',sub_category_id:11},
        {id:27,name:'帐篷',sub_category_id:12},
        {id:28,name:'睡袋炊具',sub_category_id:12},
        {id:29,name:'营地灯',sub_category_id:12},
        {id:30,name:'徒步鞋',sub_category_id:13},
        {id:31,name:'速干裤',sub_category_id:13},
        {id:32,name:'遮阳帽',sub_category_id:13}
      ];
      
      // 添加手动定义的三级分类
      thirdCategoryData.forEach(category => {
        uniqueThirdCategories[category.id] = category;
      });
      
      // 从产品数据中提取分类信息
      products.forEach(product => {
        if (product.subCategoryId && product.subCategoryName) {
          uniqueSubCategories[product.subCategoryId] = {
            id: product.subCategoryId,
            name: product.subCategoryName,
            main_category_id: product.mainCategoryId || 2
          };
        }
        
        if (product.thirdCategoryId && product.thirdCategoryName) {
          uniqueThirdCategories[product.thirdCategoryId] = {
            id: product.thirdCategoryId,
            name: product.thirdCategoryName,
            sub_category_id: product.subCategoryId
          };
        }
      });
      
      this.subCategories = Object.values(uniqueSubCategories);
      this.thirdCategories = Object.values(uniqueThirdCategories);
    },
    
    // 返回上一页
    goBack() {
      try {
        if (this.$router) {
          this.$router.push('/');
        } else {
          window.history.back();
        }
      } catch (error) {
        console.error('返回首页失败:', error);
        // 如果所有方法都失败，使用备用方案
        window.location.href = '/';
      }
    },
    
    // 重置所有筛选条件
    resetFilters() {
      this.searchQuery = '';
      this.selectedCategories = [];
      this.selectedProductTypes = [];
      this.selectedOutdoorTypes = [];
      this.priceFilter = { min: '', max: '' };
      this.currentPage = 1;
    },
    
    // 根据选中的二级分类过滤三级分类
    getFilteredThirdCategories() {
      if (!this.selectedCategories || this.selectedCategories.length === 0) {
        return this.thirdCategories;
      }
      
      return this.thirdCategories.filter(category => {
        if (!category) return false;
        return this.selectedCategories.includes(category.sub_category_id.toString());
      });
    },
    
    // 处理搜索
    handleSearch(query) {
      this.currentPage = 1;
      // 搜索逻辑已在计算属性中处理
    },
    
    // 处理页码变化
    handlePageChange(page) {
      this.currentPage = page;
    },
    
    // 处理价格筛选
    handlePriceFilter(filter) {
      this.priceFilter = { ...filter };
      this.currentPage = 1;
    },
    
    // 移除筛选
    removeFilter(type) {
      switch (type) {
        case 'search':
          this.searchQuery = '';
          break;
        case 'price':
          this.priceFilter = { min: '', max: '' };
          break;
      }
      this.currentPage = 1;
    },
    
    // 移除特定值筛选
    removeValueFilter(type, value) {
      switch (type) {
        case 'categories':
          this.selectedCategories = this.selectedCategories.filter(id => id !== value);
          break;
        case 'productTypes':
          this.selectedProductTypes = this.selectedProductTypes.filter(id => id !== value);
          break;
      }
      this.currentPage = 1;
    },
    
    // 添加商品到购物车
    async addToCart(productId, quantity) {
      try {
        if (!productId || typeof productId !== 'number' || !quantity || typeof quantity !== 'number' || quantity <= 0) {
          throw new Error('无效的商品参数');
        }
        
        const token = localStorage.getItem('token');
        if (!token) {
          throw new Error('请先登录');
        }
        
        // 导入CartService
        const { default: CartService } = await import('../services/CartService');
        
        const result = await CartService.addToCart(productId, quantity);
        
        // 使用Element Plus消息提示
        this.$message.success('添加到购物车成功');
      } catch (error) {
        let errorMessage = '添加失败，请稍后重试';
        
        if (error) {
          if (error.message && typeof error.message === 'string') {
            errorMessage = error.message;
          } else if (error.response && error.response.data && typeof error.response.data === 'object') {
            if (error.response.data.message) {
              errorMessage = error.response.data.message;
            } else {
              errorMessage = JSON.stringify(error.response.data);
            }
          } else if (error.response) {
            errorMessage = '网络请求失败: ' + (error.response.status || '未知错误');
          }
        }
        
        // 使用Element Plus消息提示
        this.$message.error(errorMessage);
        
        // 特别处理登录错误
        if (errorMessage.includes('登录')) {
          try {
            await this.$confirm('请先登录再添加商品到购物车，是否去登录？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'info'
            });
            this.$router.push('/login');
          } catch (confirmError) {
            // 用户取消登录
          }
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
  // 导入全局CSS变量
  @import '../assets/css/variables.scss';
  
  .outdoor-adventure-page {
    font-family: var(--font-family, 'Arial', sans-serif);
    background-color: var(--bg-color, #f5f5f5);
    min-height: 100vh;

    .page-header {
      background-color: var(--bg-white);
      padding: var(--spacing-lg) 0;
      box-shadow: var(--box-shadow);
      position: sticky;
      top: 0;
      z-index: 100;

      .header-container {
        margin: 0 auto;
        padding: 0 var(--spacing-lg);
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: var(--spacing-md);

        .pagination-header {
          display: flex;
          align-items: center;
        }
      }
    }

    .main-content {
      padding: var(--spacing-xl) 0;
      height: calc(100vh - 140px);
      overflow: hidden;

      // 确保左右容器高度一致
      .filter-sidebar,
      .products-container {
        height: 100%;
        box-sizing: border-box;
      }

      .container {
        margin: 0 auto;
        padding: 0 var(--spacing-lg);
        display: flex;
        gap: var(--spacing-xl);
        height: 100%;

        .filter-sidebar {
          width: var(--sidebar-width);
          background-color: var(--bg-white);
          border-radius: var(--border-radius-lg);
          overflow: hidden;
          box-shadow: var(--box-shadow);
          display: flex;
          flex-direction: column;
          height: 100%;
          box-sizing: border-box;

          .filter-content {
            flex: 1;
            padding: var(--spacing-lg);
            overflow-y: auto;
          }

          .filter-footer {
            padding: var(--spacing-md) var(--spacing-lg);
            border-top: 1px solid var(--border-light);
            background-color: var(--bg-light);

            .reset-filter-btn {
              width: 100%;
              margin-bottom: var(--spacing-lg);
              transition: all var(--transition-normal);
              
              &:hover {
                background-color: var(--bg-hover);
                color: var(--text-primary);
              }
            }
          }
        }

        .products-container {
          flex: 1;
          background-color: var(--bg-white);
          border-radius: var(--border-radius-lg);
          padding: var(--spacing-lg);
          box-shadow: var(--box-shadow);
          overflow-y: auto;
          height: 100%;
          box-sizing: border-box;

          .filter-info {
            margin-bottom: var(--spacing-lg);
            font-size: var(--font-size-sm);
            color: var(--text-secondary);
          }

          .loading-state {
            text-align: center;
            padding: calc(var(--spacing-xl) * 2) 0;
            color: var(--text-secondary);
            font-size: var(--font-size-md);

            .loading-status {
              margin-top: var(--spacing-sm);
            }
          }

          .products-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(var(--product-card-width), 1fr));
            gap: var(--spacing-xl);

            .no-products {
              grid-column: 1 / -1;
              padding: calc(var(--spacing-xl) * 2) 0;
            }
          }
        }
      }
    }

    // 响应式设计
    @media (max-width: 768px) {
      .main-content {
        .container {
          flex-direction: column;

          .filter-sidebar {
            width: 100%;
          }

          .products-container {
            .products-grid {
              grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
              gap: var(--spacing-md);
            }
          }
        }
      }

      .page-header {
        .header-container {
          flex-direction: column;
          align-items: stretch;
        }
      }
    }
  }
</style>