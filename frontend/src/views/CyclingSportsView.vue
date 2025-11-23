<template>
  <div class="cycling-sports-page">
    <!-- 顶部搜索和返回按钮 -->
    <div class="page-header">
      <div class="header-container">
        <SearchBar 
          placeholder="搜索骑行装备..."
          v-model="searchQuery"
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
            <!-- 骑行车型筛选（二级分类ID为17） -->
            <FilterSection 
              title="骑行车型"
              :options="cycleTypeOptions"
              v-model="selectedCycleTypes"
            />

            <!-- 骑行装备筛选（二级分类ID为18） -->
            <FilterSection 
              title="骑行装备"
              :options="cyclingGearOptions"
              v-model="selectedCyclingGear"
            />
          </div>

          <!-- 固定在底部的区域 -->
          <div class="filter-footer">
            <button class="reset-filter-btn" @click="resetFilters">重置筛选</button>
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
          <ProductGrid 
            v-else
            :products="paginatedProducts"
            @add-to-cart="addToCart"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  // 导入可复用组件
  import SearchBar from '../components/Search/SearchBar.vue';
import FilterSection from '../components/Filter/FilterSection.vue';
import PriceFilter from '../components/Filter/PriceFilter.vue';
import FilterTags from '../components/Filter/FilterTags.vue';
import ProductGrid from '../components/ProductDisplay/ProductGrid.vue';
import Pagination from '../components/UI/Pagination.vue';
import CartService from '../services/CartService';
import { ArrowLeft } from '@element-plus/icons-vue';
  export default {
  name: 'CyclingSportsView',
  
  // 添加mounted生命周期钩子，确保组件挂载时加载数据
  mounted() {
    this.loadData();
  },
  
  data() {
    return {
      searchQuery: '',
      selectedCategories: [], // 选中的二级分类ID
      selectedCycleTypes: [], // 选中的骑行车型ID (三级分类，对应二级ID 17)
      selectedCyclingGear: [], // 选中的骑行装备ID (三级分类，对应二级ID 18)
      minPrice: '',
      maxPrice: '',
      priceFilter: { min: '', max: '' },
      currentPage: 1,       // 当前页码
      itemsPerPage: 12,     // 每页显示的商品数量
      loading: false,       // 加载状态
      loadingStatus: '',    // 数据加载状态文本
      // 数据从API获取
      mainCategory: {
        id: 4,
        name: '骑行运动',
        description: '各类骑行运动相关装备'
      },
      // 从API获取的分类数据
      subCategories: [],
      thirdCategories: [],
      products: [],
      // 筛选选项数据
      cycleTypeOptions: [
        { value: '45', label: '公路车' },
        { value: '46', label: '山地车' },
        { value: '47', label: '折叠车' }
      ],
      cyclingGearOptions: [
        { value: '48', label: '骑行头盔' },
        { value: '49', label: '骑行服' },
        { value: '50', label: '锁鞋' },
        { value: '51', label: '水壶架' }
      ]
    };
  },
  
  computed: {
    // 激活的筛选条件
    activeFilters() {
      const filters = {};
      if (this.searchQuery) {
        filters.search = this.searchQuery;
      }
      if (this.selectedCategories.length > 0) {
        filters.categories = this.selectedCategories;
      }
      if (this.selectedCycleTypes.length > 0) {
        filters.cycleTypes = this.selectedCycleTypes;
      }
      if (this.selectedCyclingGear.length > 0) {
        filters.cyclingGear = this.selectedCyclingGear;
      }
      if (this.minPrice || this.maxPrice) {
        filters.price = { min: this.minPrice, max: this.maxPrice };
      }
      return filters;
    },
    
    // 筛选标签显示文本
    filterLabels() {
      const labels = {};
      
      // 骑行车型标签
      labels.cycleTypes = {};
      this.cycleTypeOptions.forEach(option => {
        labels.cycleTypes[option.value] = `骑行车型: ${option.label}`;
      });
      
      // 骑行装备标签
      labels.cyclingGear = {};
      this.cyclingGearOptions.forEach(option => {
        labels.cyclingGear[option.value] = `骑行装备: ${option.label}`;
      });
      
      // 二级分类标签
      labels.categories = {};
      this.subCategories.forEach(cat => {
        labels.categories[cat.id] = cat.name;
      });
      
      return labels;
    },
    
    // 是否有激活的筛选条件
    hasActiveFilters() {
      return Object.keys(this.activeFilters).length > 0;
    },
    
    // 筛选商品 - 获取所有符合条件的商品
    allFilteredProducts() {
      return this.products.filter(product => {
        // 只显示主类ID为4的商品（骑行运动）
        if (product.mainCategoryId !== 4) return false;
        
        // 搜索筛选
        if (this.searchQuery && !product.name.toLowerCase().includes(this.searchQuery.toLowerCase())) {
          return false;
        }
        
        // 二级分类筛选（基于subCategoryId）
        if (this.selectedCategories.length > 0) {
          // 确保商品有二级分类ID
          if (!product.subCategoryId) return false;
          // 检查商品的二级分类ID是否在用户选择的分类中
          if (!this.selectedCategories.includes(product.subCategoryId?.toString())) {
            return false;
          }
        }
        
        // 骑行车型筛选（基于三级分类ID）
        if (this.selectedCycleTypes.length > 0) {
          if (!product.thirdCategoryId) return false;
          if (!this.selectedCycleTypes.includes(product.thirdCategoryId?.toString())) {
            return false;
          }
        }
        
        // 骑行装备筛选（基于三级分类ID）
        if (this.selectedCyclingGear.length > 0) {
          if (!product.thirdCategoryId) return false;
          if (!this.selectedCyclingGear.includes(product.thirdCategoryId?.toString())) {
            return false;
          }
        }
        
        // 价格筛选
        const productPrice = parseFloat(product.price) || 0;
        const minPriceValue = parseFloat(this.minPrice) || 0;
        const maxPriceValue = parseFloat(this.maxPrice) || Infinity;
        
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
    // 处理搜索
    handleSearch() {
      this.currentPage = 1;
    },
    
    // 处理页码变化
    handlePageChange(page) {
      this.currentPage = page;
    },
    
    // 处理价格筛选
    handlePriceFilter(filterValue) {
      this.minPrice = filterValue.min;
      this.maxPrice = filterValue.max;
      this.currentPage = 1;
    },
    
    // 移除整个筛选类型
    removeFilter(type) {
      switch (type) {
        case 'search':
          this.searchQuery = '';
          break;
        case 'categories':
          this.selectedCategories = [];
          break;
        case 'cycleTypes':
          this.selectedCycleTypes = [];
          break;
        case 'cyclingGear':
          this.selectedCyclingGear = [];
          break;
        case 'price':
          this.minPrice = '';
          this.maxPrice = '';
          this.priceFilter = { min: '', max: '' };
          break;
      }
      this.currentPage = 1;
    },
    
    // 移除筛选类型中的特定值
    removeFilterValue(type, value) {
      switch (type) {
        case 'categories':
          this.removeCategoryFilter(value);
          break;
        case 'cycleTypes':
          this.removeCycleTypeFilter(value);
          break;
        case 'cyclingGear':
          this.removeCyclingGearFilter(value);
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
        
        // 过滤出骑行运动商品（主分类ID为4）
      this.products = Array.isArray(allProducts) 
        ? allProducts.filter(product => product.mainCategoryId === 4)
        : [];
          
        this.loadingStatus = `成功加载 ${this.products.length} 个骑行运动商品`;
        
        // 处理分类数据 - 直接设置三级分类数据，确保筛选选项显示
        const missingCategories = [
          { id: 45, name: '公路车', sub_category_id: 17 },
          { id: 46, name: '山地车', sub_category_id: 17 },
          { id: 47, name: '折叠车', sub_category_id: 17 },
          { id: 48, name: '骑行头盔', sub_category_id: 18 },
          { id: 49, name: '骑行服', sub_category_id: 18 },
          { id: 50, name: '锁鞋', sub_category_id: 18 },
          { id: 51, name: '水壶架', sub_category_id: 18 }
        ];
        
        this.thirdCategories = missingCategories;
        console.log('直接设置的三级分类数据:', this.thirdCategories);
        
        // 同时处理从产品中提取的分类数据
        this.processCategoryData(this.products);
      } catch (error) {
        this.loadingStatus = '数据加载失败';
        console.error('加载商品数据失败:', error);
        
        // 即使加载失败也设置默认分类，确保UI正常
        this.thirdCategories = [
          { id: 45, name: '公路车', sub_category_id: 17 },
          { id: 46, name: '山地车', sub_category_id: 17 },
          { id: 47, name: '折叠车', sub_category_id: 17 },
          { id: 48, name: '骑行头盔', sub_category_id: 18 },
          { id: 49, name: '骑行服', sub_category_id: 18 },
          { id: 50, name: '锁鞋', sub_category_id: 18 },
          { id: 51, name: '水壶架', sub_category_id: 18 }
        ];
      } finally {
        this.loading = false;
      }
    },
    
    // 从产品数据中提取分类信息
    extractCategoriesFromProducts() {
      // 提取二级分类（骑行子分类）
      const uniqueSubCategories = {};
      this.products.forEach(product => {
        if (product.subCategoryId && product.subCategoryName) {
          uniqueSubCategories[product.subCategoryId] = {
            id: product.subCategoryId,
            name: product.subCategoryName,
            main_category_id: product.mainCategoryId
          };
        }
      });
      this.subCategories = Object.values(uniqueSubCategories);
      
      // 提取三级分类（骑行车型和装备）
      const uniqueThirdCategories = {};
      
      // 首先从产品中提取现有的三级分类
      this.products.forEach(product => {
        if (product.thirdCategoryId && product.thirdCategoryName && product.subCategoryId) {
          uniqueThirdCategories[product.thirdCategoryId] = {
            id: product.thirdCategoryId,
            name: product.thirdCategoryName,
            sub_category_id: product.subCategoryId
          };
        }
      });
      
      // 手动添加缺失的三级分类，确保符合数据库结构
      const missingCategories = [
        { id: 45, name: '公路车', sub_category_id: 17 },
        { id: 46, name: '山地车', sub_category_id: 17 },
        { id: 47, name: '折叠车', sub_category_id: 17 },
        { id: 48, name: '骑行头盔', sub_category_id: 18 },
        { id: 49, name: '骑行服', sub_category_id: 18 },
        { id: 50, name: '锁鞋', sub_category_id: 18 },
        { id: 51, name: '水壶架', sub_category_id: 18 }
      ];
      
      // 添加缺失的分类
      missingCategories.forEach(category => {
        uniqueThirdCategories[category.id] = category;
      });
      
      this.thirdCategories = Object.values(uniqueThirdCategories);
      console.log('三级分类数据:', this.thirdCategories);
    },
    
    // 处理分类数据
    processCategoryData(products) {
      if (!Array.isArray(products)) return;
      
      const uniqueSubCategories = {};
      const uniqueThirdCategories = {};
      const uniqueBrands = new Set();
      
      products.forEach(product => {
        // 处理子分类 - 使用驼峰式命名匹配后端DTO
        if (product.subCategoryId && product.subCategoryName) {
          uniqueSubCategories[product.subCategoryId] = {
            id: product.subCategoryId,
            name: product.subCategoryName,
            main_category_id: product.mainCategoryId || 4 // 添加主分类ID，默认为4（骑行运动）
          };
        }
        
        // 处理三级分类 - 使用驼峰式命名匹配后端DTO
        if (product.thirdCategoryId && product.thirdCategoryName) {
          uniqueThirdCategories[product.thirdCategoryId] = {
            id: product.thirdCategoryId,
            name: product.thirdCategoryName,
            sub_category_id: product.subCategoryId // 添加子分类ID关联
          };
        }
        
        // 处理品牌
        if (product.brand) {
          uniqueBrands.add(product.brand);
        }
      });
      
      // 手动添加缺失的三级分类，确保符合数据库结构
      const missingCategories = [
        { id: 45, name: '公路车', sub_category_id: 17 },
        { id: 46, name: '山地车', sub_category_id: 17 },
        { id: 47, name: '折叠车', sub_category_id: 17 },
        { id: 48, name: '骑行头盔', sub_category_id: 18 },
        { id: 49, name: '骑行服', sub_category_id: 18 },
        { id: 50, name: '锁鞋', sub_category_id: 18 },
        { id: 51, name: '水壶架', sub_category_id: 18 }
      ];
      
      // 添加缺失的分类
      missingCategories.forEach(category => {
        uniqueThirdCategories[category.id] = category;
      });
      
      this.subCategories = Object.values(uniqueSubCategories);
      this.thirdCategories = Object.values(uniqueThirdCategories);
      this.brands = Array.from(uniqueBrands);
      console.log('处理后的三级分类数据:', this.thirdCategories);
    },
    
    goBack() {
      this.$router.push('/');
    },
    
    resetFilters() {
      this.searchQuery = '';
      this.selectedCategories = [];
      this.selectedCycleTypes = [];
      this.selectedCyclingGear = [];
      this.minPrice = '';
      this.maxPrice = '';
      this.currentPage = 1;
    },
    
    // 移除骑行车型筛选
    removeCycleTypeFilter(cycleTypeId) {
      const index = this.selectedCycleTypes.indexOf(cycleTypeId);
      if (index > -1) {
        this.selectedCycleTypes.splice(index, 1);
      }
    },
    
    // 移除骑行装备筛选
    removeCyclingGearFilter(gearId) {
      const index = this.selectedCyclingGear.indexOf(gearId);
      if (index > -1) {
        this.selectedCyclingGear.splice(index, 1);
      }
    },
    
    // 移除特定的分类筛选
    removeCategoryFilter(categoryId) {
      const index = this.selectedCategories.indexOf(categoryId);
      if (index > -1) {
        this.selectedCategories.splice(index, 1);
      }
    },
    
    // 清除价格筛选
    clearPriceFilter() {
      this.minPrice = '';
      this.maxPrice = '';
      this.priceFilter = { min: '', max: '' };
    },
    
    // 根据ID获取分类名称
    getCategoryNameById(categoryId) {
      // 首先在三级分类中查找
      const thirdCategory = this.thirdCategories.find(cat => cat.id.toString() === categoryId);
      if (thirdCategory) {
        return thirdCategory.name;
      }
      // 如果找不到，再在二级分类中查找
      const category = this.subCategories.find(cat => cat.id.toString() === categoryId);
      return category ? category.name : '未知分类';
    },
    
    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    },
    
    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },
    
    // 跳转到指定页码
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page;
      }
    },
    
    // 添加商品到购物车 - 修复版（使用原生JavaScript提示）
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
        this.$message.success('添加到购物车成功');
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
        
        // 显示友好的错误消息 - 使用Element Plus消息提示
        this.$message.error(errorMessage);
        
        // 特别处理登录错误 - 使用Element Plus确认对话框
        if (errorMessage.includes('登录')) {
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
  
  components: {
    SearchBar,
    FilterSection,
    PriceFilter,
    FilterTags,
    ProductGrid,
    Pagination,
    ArrowLeft
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
      this.selectedCycleTypes = [];
      this.selectedCyclingGear = [];
    },
    
    // 当筛选条件改变时，回到第一页
    allFilteredProducts() {
      this.currentPage = 1;
    }
  },
  
  // 组件挂载时加载数据
  mounted() {
    // 组件挂载后自动加载数据
    this.loadData();
  }
};
</script>

<style scoped lang="scss">
// 导入全局变量
@import '../assets/css/variables.scss';

.cycling-sports-page {
  font-family: var(--font-family, 'Arial', sans-serif);
  background-color: var(--bg-color, #f5f5f5);
  min-height: 100vh;
}

.page-header {
  background-color: var(--white, #ffffff);
  padding: var(--spacing-lg, 20px) 0;
  box-shadow: var(--shadow-sm, 0 2px 4px rgba(0, 0, 0, 0.1));
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  margin: 0 auto;
  padding: 0 var(--spacing-lg, 20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--spacing-md, 15px);
}

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

.main-content {
  padding: var(--spacing-xl, 30px) 0;
  height: calc(100vh - 140px);
  overflow: hidden;
}

.container {
  margin: 0 auto;
  padding: 0 var(--spacing-lg, 20px);
  display: flex;
  gap: var(--spacing-xl, 30px);
  height: 100%;
}

.filter-sidebar {
  width: 280px;
  background-color: var(--white, #ffffff);
  border-radius: var(--border-radius-lg, 8px);
  overflow: hidden;
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.1));
  display: flex;
  flex-direction: column;
  height: 100%;
}

.filter-content {
  flex: 1;
  padding: var(--spacing-lg, 20px);
  overflow-y: auto;
}

.filter-footer {
  padding: var(--spacing-md, 15px) var(--spacing-lg, 20px);
  border-top: 1px solid var(--border-light, #f0f0f0);
  background-color: var(--bg-light, #fafafa);
}

.reset-filter-btn {
  width: 100%;
  padding: var(--spacing-md, 10px);
  background-color: var(--bg-light, #f0f0f0);
  color: var(--text-secondary, #666);
  border: 1px solid var(--border-color, #ddd);
  border-radius: var(--border-radius, 4px);
  cursor: pointer;
  font-size: var(--font-size-md, 14px);
  margin-bottom: var(--spacing-lg, 20px);
  transition: all var(--transition-speed, 0.3s);
  
  &:hover {
    background-color: #e0e0e0;
    color: var(--text-primary, #333);
  }
}

.products-container {
  flex: 1;
  background-color: var(--white, #ffffff);
  border-radius: var(--border-radius-lg, 8px);
  padding: var(--spacing-lg, 20px);
  box-shadow: var(--shadow-md, 0 2px 8px rgba(0, 0, 0, 0.1));
  overflow-y: auto;
  height: 100%;
}

.filter-info {
  margin-bottom: var(--spacing-lg, 20px);
  font-size: var(--font-size-md, 14px);
  color: var(--text-secondary, #666);
}

.loading-state {
  text-align: center;
  padding: var(--spacing-2xl, 50px) 0;
  color: var(--text-secondary, #666);
  font-size: var(--font-size-lg, 16px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    flex-direction: column;
  }
  
  .filter-sidebar {
    width: 100%;
  }
  
  .header-container {
    flex-direction: column;
    align-items: stretch;
  }  
}  
</style>