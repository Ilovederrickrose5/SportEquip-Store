<template>
  <div class="search-results-page">
    <!-- 顶部导航栏 -->
    <NavigationBar />
    
    <!-- 搜索区域 -->
    <SearchSection />
    
    <!-- 搜索结果区域 -->
    <section class="search-results">
      <div class="container">
        <!-- 搜索结果头部 -->
        <div class="search-header">
          <h2 class="search-title">搜索结果</h2>
          <p class="search-count" v-if="searchQuery">
            为您找到 <span>{{ products.length }}</span> 个与 "{{ searchQuery }}" 相关的商品
          </p>
          <p class="search-count" v-else>
            请输入关键词进行搜索
          </p>
        </div>
        
        <!-- 搜索框（方便用户再次搜索） -->
        <div class="search-box-container">
          <input 
            type="text" 
            v-model="localSearchQuery"
            placeholder="输入关键词搜索..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">🔍</button>
        </div>
        
        <!-- 加载状态 -->
        <div class="loading-indicator" v-if="loading">
          <p>正在搜索中...</p>
        </div>
        
        <!-- 商品列表 -->
        <div v-else-if="products.length > 0">
          <ProductGrid 
            :products="products"
            @add-to-cart="handleAddToCart"
          />
        </div>
        
        <!-- 无结果状态 -->
        <div v-else class="no-results">
          <div class="no-results-icon">🔍</div>
          <h3>未找到相关商品</h3>
          <p>试试其他关键词，或者浏览我们的分类</p>
          <div class="category-suggestions">
            <button 
              v-for="category in quickCategories" 
              :key="category.path"
              class="category-btn"
              @click="goToCategory(category.path)"
            >
              {{ category.name }}
            </button>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 页脚 -->
    <MainFooter />
  </div>
</template>

<script>
import NavigationBar from '../components/Layout/NavigationBar.vue';
import SearchSection from '../components/Search/SearchSection.vue';
import ProductGrid from '../components/ProductDisplay/ProductGrid.vue';
import MainFooter from '../components/Layout/MainFooter.vue';
import ProductService from '../services/ProductService';

export default {
  name: 'SearchResultsView',
  components: {
    NavigationBar,
    SearchSection,
    ProductGrid,
    MainFooter
  },
  data() {
    return {
      searchQuery: '',
      localSearchQuery: '',
      products: [],
      loading: false,
      quickCategories: [
        { name: '球类运动', path: '/ball-sports' },
        { name: '户外探险', path: '/outdoor-adventure' },
        { name: '健身训练', path: '/fitness-training' },
        { name: '骑行运动', path: '/cycling-sports' }
      ]
    }
  },
  async mounted() {
    // 从URL获取搜索关键词
    this.searchQuery = this.$route.query.keyword || '';
    this.localSearchQuery = this.searchQuery;
    
    // 如果有搜索关键词，执行搜索
    if (this.searchQuery) {
      await this.performSearch(this.searchQuery);
    }
  },
  watch: {
    // 监听URL参数变化
    '$route.query.keyword': function(newKeyword) {
      this.searchQuery = newKeyword || '';
      this.localSearchQuery = this.searchQuery;
      if (this.searchQuery) {
        this.performSearch(this.searchQuery);
      }
    }
  },
  methods: {
    // 执行搜索
    async performSearch(keyword) {
      if (!keyword || keyword.trim() === '') {
        this.products = [];
        return;
      }
      
      this.loading = true;
      try {
        console.log('Performing search with keyword:', keyword);
        this.products = await ProductService.searchProducts(keyword.trim());
        console.log('Search results:', this.products.length);
      } catch (error) {
        console.error('搜索失败:', error);
        this.products = [];
      } finally {
        this.loading = false;
      }
    },
    
    // 处理搜索按钮点击
    handleSearch() {
      if (this.localSearchQuery.trim()) {
        this.$router.push({
          path: '/search',
          query: { keyword: this.localSearchQuery.trim() }
        });
      }
    },
    
    // 跳转到分类页面
    goToCategory(path) {
      this.$router.push(path);
    },
    
    // 添加到购物车
    handleAddToCart(productId, quantity) {
      console.log('Add to cart:', productId, quantity);
      // 可以在这里调用购物车服务
    }
  }
}
</script>

<style scoped lang="scss">
.search-results-page {
  min-height: 100vh;
}

.search-results {
  padding: 40px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-header {
  text-align: center;
  margin-bottom: 30px;
}

.search-title {
  font-size: 28px;
  color: #333;
  margin: 0 0 10px 0;
}

.search-count {
  font-size: 16px;
  color: #666;
  margin: 0;
  
  span {
    color: #1e90ff;
    font-weight: bold;
  }
}

.search-box-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.search-input {
  flex: 1;
  padding: 12px 20px;
  border: 2px solid #1e90ff;
  border-radius: 25px 0 0 25px;
  font-size: 16px;
  outline: none;
}

.search-input:focus {
  border-color: #1077cc;
  box-shadow: 0 0 0 3px rgba(30, 144, 255, 0.2);
}

.search-btn {
  background-color: #1e90ff;
  color: white;
  border: none;
  padding: 0 25px;
  border-radius: 0 25px 25px 0;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #1077cc;
}

.loading-indicator {
  text-align: center;
  padding: 50px 0;
  color: #666;
}

.no-results {
  text-align: center;
  padding: 50px 0;
}

.no-results-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.no-results h3 {
  font-size: 24px;
  color: #333;
  margin: 0 0 10px 0;
}

.no-results p {
  font-size: 16px;
  color: #666;
  margin: 0 0 30px 0;
}

.category-suggestions {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.category-btn {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.category-btn:hover {
  background-color: #1e90ff;
  color: white;
  border-color: #1e90ff;
}
</style>