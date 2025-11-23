<template>
  <div class="ball-sports-page">
    <!-- 顶部搜索和返回按钮 -->
    <div class="page-header">
      <div class="header-container">
        <SearchBar 
          v-model="searchQuery"
          placeholder="搜索球类运动装备..."
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
              title="球类分类"
              :options="categoryOptions"
              type="checkbox"
              v-model="selectedCategories"
              @change="handleFilterChange"
            />

            <FilterSection 
              title="品牌"
              :options="brandOptions"
              type="radio"
              v-model="selectedBrands"
              @change="handleFilterChange"
            />
            

          </div>

          <!-- 固定在底部的区域 -->
          <div class="filter-footer">
            <button class="reset-filter-btn" @click="resetFilters">重置筛选</button>
            <PriceFilter 
              v-model="priceRange"
              @apply="handlePriceApply"
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
            @remove-value="removeFilterValue"
          />
          
          <!-- 筛选结果信息 -->
          <div class="filter-info">
            <span v-if="!loading">找到 {{ allFilteredProducts.length }} 件商品</span>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="loading" class="loading-state">
            <p>数据加载中...</p>
            <p class="loading-status">{{ loadingStatus }}</p>
          </div>
          
          <!-- 商品卡片网格 -->
          <div v-else class="products-grid">
            <ProductCard 
              v-for="product in paginatedProducts" 
              :key="product.id"
              :product="product"
              @add-to-cart="addToCart"
            />

            <!-- 如果没有商品时显示 -->
            <div class="no-products" v-if="paginatedProducts.length === 0">
              <p>暂无符合条件的商品</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  // 导入可复用组件
  import SearchBar from '../components/Search/SearchBar.vue';
  import Pagination from '../components/UI/Pagination.vue';
  import ProductCard from '../components/ProductDisplay/ProductCard.vue';
  import FilterSection from '../components/Filter/FilterSection.vue';
  import PriceFilter from '../components/Filter/PriceFilter.vue';
  import FilterTags from '../components/Filter/FilterTags.vue';
  import CartService from '../services/CartService';
  import { ArrowLeft } from '@element-plus/icons-vue';
  
  export default {
    name: 'BallSportsView',
    components: {
      SearchBar,
      Pagination,
      ProductCard,
      FilterSection,
      PriceFilter,
      FilterTags,
      ArrowLeft
    },
    data() {
      return {
        searchQuery: '',
        selectedCategories: [], // 选中的二级分类ID
        selectedBrands: '', // 选中的品牌ID (使用字符串而不是数组，因为品牌只能单选)

        priceRange: { min: '', max: '' },
        currentPage: 1,       // 当前页码
        itemsPerPage: 12,     // 每页显示的商品数量
        loading: false,       // 加载状态
        loadingStatus: '',    // 数据加载状态文本
        mainCategory: {
          id: 1,
          name: '球类运动',
          description: '各类球类运动相关装备'
        },
        subCategories: [],
        thirdCategories: [],
        products: [],

      };
    },
    
    computed: {
      // 分类选项
      categoryOptions() {
        return this.subCategories
          .filter(cat => cat && cat.main_category_id === 1)
          .map(cat => ({ value: cat.id.toString(), label: cat.name }));
      },
      
      // 品牌选项
      brandOptions() {
        const brands = this.filteredThirdCategories;
        return brands.map(brand => ({ value: brand.id.toString(), label: brand.name }));
      },
      
      // 活跃的筛选条件
      activeFilters() {
        const filters = {};
        
        if (this.searchQuery) {
          filters.search = this.searchQuery;
        }
        
        if (this.selectedCategories.length > 0) {
          filters.categories = this.selectedCategories;
        }
        
        if (this.selectedBrands) {
          filters.brand = [this.selectedBrands];
        }
        

        
        if (this.priceRange.min || this.priceRange.max) {
          filters.price = this.priceRange;
        }
        
        return filters;
      },
      
      // 筛选标签的显示名称映射
      filterLabels() {
        return {
          categories: this.getCategoryLabels(),
          brand: this.getBrandLabels(),
  
        };
      },
    
    // 筛选商品 - 获取所有符合条件的商品
    allFilteredProducts() {
      return this.products.filter(product => {
        // 只显示主类ID为1的商品（球类运动）
        if (product.mainCategoryId !== 1) return false;
        
        // 搜索筛选
        if (this.searchQuery && !product.name.toLowerCase().includes(this.searchQuery.toLowerCase())) {
          return false;
        }
        
        // 二级分类筛选（基于subCategoryId）
        if (this.selectedCategories.length > 0) {
          // 确保商品有二级分类ID
          if (!product.subCategoryId) return false;
          // 检查商品的二级分类ID是否在用户选择的分类中
          if (!this.selectedCategories.includes(product.subCategoryId.toString())) {
            return false;
          }
        }
        
        // 三级分类筛选（基于third_category_name，品牌筛选）
        if (this.selectedBrands) {
          // 获取选中的品牌对象
          const selectedBrand = this.thirdCategories.find(brand => 
            brand.id.toString() === this.selectedBrands
          );
          
          // 使用品牌名称进行筛选，确保能显示该品牌下的所有商品
          if (selectedBrand && product.thirdCategoryName !== selectedBrand.name) {
            return false;
          }
        }
        

        
        // 价格筛选
        const productPrice = parseFloat(product.price) || 0;
        const minPriceValue = parseFloat(this.priceRange.min) || 0;
        const maxPriceValue = parseFloat(this.priceRange.max) || Infinity;
        
        if (!isNaN(minPriceValue) && productPrice < minPriceValue) {
          return false;
        }
        if (!isNaN(maxPriceValue) && productPrice > maxPriceValue) {
          return false;
        }
        
        return true;
      });
    },
    
    // 计算总页数
    totalPages() {
      return Math.ceil(this.allFilteredProducts.length / this.itemsPerPage);
    },
    
    // 获取当前页显示的商品
    paginatedProducts() {
      const startIndex = (this.currentPage - 1) * this.itemsPerPage;
      const endIndex = startIndex + this.itemsPerPage;
      return this.allFilteredProducts.slice(startIndex, endIndex);
    },
    
    // 获取当前选中的二级分类下的三级分类（品牌），并去重
    filteredThirdCategories() {
      // 如果没有选中的二级分类，返回所有三级分类
      if (this.selectedCategories.length === 0) {
        // 对所有三级分类进行去重
        const uniqueBrands = {};
        this.thirdCategories.forEach(brand => {
          if (!uniqueBrands[brand.name]) {
            uniqueBrands[brand.name] = brand;
          }
        });
        return Object.values(uniqueBrands);
      }
      
      // 如果选中了二级分类，只返回对应的三级分类，确保层级关系正确
      const selectedSubIds = this.selectedCategories.map(id => parseInt(id));
      const filteredBrands = this.thirdCategories.filter(brand => {
        // 确保品牌有对应的二级分类
        return brand && brand.sub_category_id && selectedSubIds.includes(brand.sub_category_id);
      });
      
      // 对筛选后的三级分类按品牌名称去重
      const uniqueBrands = {};
      filteredBrands.forEach(brand => {
        if (!uniqueBrands[brand.name]) {
          uniqueBrands[brand.name] = brand;
        }
      });
      
      return Object.values(uniqueBrands);
    }
  },
  
  methods: {
    // 获取分类标签映射
    getCategoryLabels() {
      const labels = {};
      this.subCategories.forEach(cat => {
        if (cat) {
          labels[cat.id.toString()] = cat.name;
        }
      });
      return labels;
    },
    
    // 获取品牌标签映射
    getBrandLabels() {
      const labels = {};
      this.thirdCategories.forEach(brand => {
        if (brand) {
          labels[brand.id.toString()] = brand.name;
        }
      });
      return labels;
    },
    

    
    // 处理搜索
    handleSearch(query) {
      console.log('搜索:', query);
    },
    
    // 处理页码变化
    handlePageChange(page) {
      this.currentPage = page;
    },
    
    // 处理筛选条件变化
    handleFilterChange() {
      this.currentPage = 1;
    },
    
    // 处理价格筛选应用
    handlePriceApply() {
      this.currentPage = 1;
    },
    
    // 移除整个筛选器
    removeFilter(filterType) {
      switch(filterType) {
        case 'search':
          this.searchQuery = '';
          break;
        case 'categories':
          this.selectedCategories = [];
          break;
        case 'brand':
          this.selectedBrands = '';
          break;

        case 'price':
          this.priceRange = { min: '', max: '' };
          break;
      }
      this.currentPage = 1;
    },
    
    // 移除特定值的筛选
    removeFilterValue(filterType, value) {
      switch(filterType) {
        case 'categories':
          const catIndex = this.selectedCategories.indexOf(value);
          if (catIndex > -1) {
            this.selectedCategories.splice(catIndex, 1);
          }
          break;
        case 'brand':
          this.selectedBrands = '';
          break;

      }
      this.currentPage = 1;
    },
    
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
        
        // 过滤出球类运动商品（主分类ID为1）
        this.products = Array.isArray(allProducts) 
          ? allProducts.filter(product => product.mainCategoryId === 1)
          : [];
        
        this.loadingStatus = `成功加载 ${this.products.length} 个球类运动商品`;
        
        // 处理分类数据
        this.processCategoryData(this.products);
      } catch (error) {
        this.loadingStatus = '数据加载失败';
        console.error('加载商品数据失败:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 处理分类数据
    processCategoryData(products) {
      if (!Array.isArray(products)) return;
      
      const uniqueSubCategories = {};
      const uniqueThirdCategories = {};
      
      products.forEach(product => {
        // 处理子分类 - 使用驼峰式命名匹配后端DTO
        if (product.subCategoryId && product.subCategoryName) {
          uniqueSubCategories[product.subCategoryId] = {
            id: product.subCategoryId,
            name: product.subCategoryName,
            main_category_id: product.mainCategoryId || 1 // 添加主分类ID，默认为1（球类运动）
          };
        }
        
        // 处理三级分类 - 使用驼峰式命名匹配后端DTO
        if (product.thirdCategoryId && product.thirdCategoryName) {
          uniqueThirdCategories[product.thirdCategoryId] = {
            id: product.thirdCategoryId,
            name: product.thirdCategoryName
          };
        }
      });
      
      this.subCategories = Object.values(uniqueSubCategories);
      this.thirdCategories = Object.values(uniqueThirdCategories);
    },
    
    goBack() {
      this.$router.push('/');
    },
    
    resetFilters() {
      this.searchQuery = '';
      this.selectedCategories = [];
      this.selectedBrands = '';

      this.priceRange = { min: '', max: '' };
      this.currentPage = 1;
    },
    
    // 添加商品到购物车 - 使用Element Plus消息提示
    async addToCart(productId, quantity) {
      console.log('添加到购物车开始，参数:', {productId, quantity});
      
      try {
        // 1. 验证参数
        if (!productId || typeof productId !== 'number' || !quantity || typeof quantity !== 'number' || quantity <= 0) {
          throw new Error('无效的商品参数');
        }
        
        // 2. 验证登录状态
        const token = localStorage.getItem('token');
        if (!token) {
          throw new Error('请先登录');
        }
        
        // 3. 确保CartService可用
        if (!CartService || typeof CartService.addToCart !== 'function') {
          throw new Error('购物车服务不可用');
        }
        
        // 4. 调用服务并等待结果
        const result = await CartService.addToCart(productId, quantity);
        
        // 5. 成功处理 - 使用Element Plus消息提示
        if (this.$message) {
          this.$message.success('添加到购物车成功');
        }
        console.log('添加到购物车成功，结果:', result);
      } catch (error) {
        // 6. 错误处理 - 修复undefined访问问题
        console.error('添加到购物车失败，完整错误信息:', error);
        
        // 安全提取错误消息，避免undefined访问
        let errorMessage = '添加失败，请稍后重试';
        
        // 检查error是否存在
        if (error) {
          // 优先使用error.message
          if (error.message && typeof error.message === 'string') {
            errorMessage = error.message;
          }
          // 然后检查error.response.data.message
          else if (error.response && error.response.data && typeof error.response.data === 'object') {
            if (error.response.data.message) {
              errorMessage = error.response.data.message;
            } else {
              // 转换整个data对象为字符串，避免访问不存在的属性
              errorMessage = JSON.stringify(error.response.data);
            }
          }
          // 简单检查error.response是否存在
          else if (error.response) {
            errorMessage = '网络请求失败: ' + (error.response.status || '未知错误');
          }
        }
        
        // 显示错误消息 - 使用Element Plus消息提示
        if (this.$message) {
          this.$message.error(errorMessage);
        }
        
        // 特别处理登录错误 - 使用Element Plus确认对话框
        if (errorMessage.includes('登录') && this.$confirm) {
          try {
            await this.$confirm('请先登录再添加商品到购物车，是否去登录？', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'info'
            });
            this.$router.push('/login');
          } catch (confirmError) {
            // 用户取消登录，不做处理
          }
        }
      } finally {
        console.log('添加到购物车操作结束');
      }
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
    
    // 当二级分类选择改变时，清空已选中的三级分类
    selectedCategories() {
      this.selectedBrands = '';
    },
    
    // 当筛选条件改变时，回到第一页
    allFilteredProducts() {
      if (this.currentPage > this.totalPages) {
        this.currentPage = Math.max(1, this.totalPages);
      }
    }
  },
  
  // 组件挂载时加载数据
  mounted() {
    // 组件挂载后自动加载数据
    this.loadData();
  }
}
</script>

<style lang="scss" scoped>
  // 导入全局CSS变量
  @import '../assets/css/variables.scss';

.ball-sports-page {
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

      .back-btn {
        transition: all var(--transition-normal);

        &:hover {
          border-color: var(--primary-color);
          color: var(--primary-color);
        }
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

        .products-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(var(--product-card-width), 1fr));
          gap: var(--spacing-xl);

          .no-products {
            grid-column: 1 / -1;
            padding: calc(var(--spacing-xl) * 2) 0;
          }
        }

        .loading-state {
          text-align: center;
          padding: calc(var(--spacing-xl) * 2) 0;
          color: var(--text-secondary);
          font-size: var(--font-size-md);

          .loading-status {
            margin-top: var(--spacing-sm);
          }

          .loading-empty {
            margin-bottom: var(--spacing-sm);
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