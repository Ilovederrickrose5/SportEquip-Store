<template>
  <div class="sports-store">
    <!-- 顶部导航栏组件 -->
    <NavigationBar @logout="showLogoutModal = true" />
    
    <!-- 退出登录确认模态框组件 -->
    <LogoutModal 
      :visible="showLogoutModal"
      @confirm="confirmLogout"
      @cancel="cancelLogout"
    />

    <!-- 搜索区域组件 -->
    <SearchSection />

    <!-- 轮播图组件 -->
    <HeroCarousel 
      :carouselItems="carouselItems"
      :speed="carouselSpeed"
    />

    <!-- 个性化商品推荐区域 -->
    <section class="recommended-products">
      <div class="container">
        <h2 class="section-title">为您推荐</h2>
        <div class="loading-indicator" v-if="loadingRecommendedProducts">
          <p>正在加载推荐商品...</p>
        </div>
        <ProductGrid 
          v-else 
          :products="recommendedProducts"
          @add-to-cart="handleAddToCart"
        />
      </div>
    </section>

    <!-- 页脚组件 -->
    <MainFooter />
  </div>
</template>

<script>
// 导入组件
import NavigationBar from '../components/Layout/NavigationBar.vue';
import SearchSection from '../components/Search/SearchSection.vue';
import HeroCarousel from '../components/Layout/HeroCarousel.vue';
import MainFooter from '../components/Layout/MainFooter.vue';
import LogoutModal from '../components/UI/LogoutModal.vue';
import ProductGrid from '../components/ProductDisplay/ProductGrid.vue';
import ProductService from '../services/ProductService';
import CartService from '../services/CartService';

export default {
  name: 'HomeView',
  components: {
    NavigationBar,
    SearchSection,
    HeroCarousel,
    MainFooter,
    LogoutModal,
    ProductGrid
  },
  data() {
    return {
      showLogoutModal: false,
      // 轮播图数据
      carouselItems: [
        {
          id: 1,
          image: '/src/assets/images/lunbotu/banner1.avif',
          title: 'LEBRON XXIII',
          subtitle: 'CHOSEN ONE',
          buttonText: '生来配王者',
          link: '#'
        },
        {
          id: 2,
          image: '/src/assets/images/lunbotu/banner2.webp',
          title: '专业跑步装备上新',
          subtitle: '轻盈舒适，助力突破个人记录',
          buttonText: '查看详情',
          link: '#'
        },
        {
          id: 3,
          image: '/src/assets/images/lunbotu/banner3.avif',
          title: '健身达人必备装备',
          subtitle: '提升训练效果，打造完美身材',
          buttonText: '立即选购',
          link: '#'
        }
      ],
      carouselSpeed: 2000, // 轮播速度，2秒切换一次
      // 推荐商品数据
      recommendedProducts: [],
      loadingRecommendedProducts: false
    }
  },
  
  computed: {
    // 计算属性：获取特色商品（第一个或标记为特色的商品）
    featuredProduct() {
      // 如果有标记为特色的商品，优先使用
      const featured = this.recommendedProducts.find(product => product.isFeatured);
      return featured || this.recommendedProducts[0] || null;
    },
    
    // 计算属性：获取普通商品（除特色商品外的其他商品）
    regularProducts() {
      if (!this.featuredProduct) return this.recommendedProducts;
      return this.recommendedProducts.filter(product => product.id !== this.featuredProduct.id);
    }
  },
  
  async mounted() {
    // 页面加载时获取推荐商品
    await this.loadRecommendedProducts();
  },
  methods: {
    // 加载推荐商品
    async loadRecommendedProducts() {
      this.loadingRecommendedProducts = true;
      try {
        // 获取所有商品，然后筛选出一部分作为推荐
        const allProducts = await ProductService.getAllProducts();
        
        // 简单的推荐逻辑：取前8个商品作为推荐
        // 实际应用中可以根据用户历史行为、热门商品等进行更复杂的推荐算法
        this.recommendedProducts = allProducts.slice(0, 8);
      } catch (error) {
        console.error('加载推荐商品失败:', error);
        // 显示一些默认推荐商品作为备选
        this.recommendedProducts = this.getDefaultRecommendedProducts();
      } finally {
        this.loadingRecommendedProducts = false;
      }
    },
    
    // 获取默认推荐商品（当API调用失败时使用）
    getDefaultRecommendedProducts() {
      return [
        {
          id: 1,
          name: '专业跑步鞋',
          description: '轻盈缓震，提供出色的跑步体验',
          price: 899,
          imageUrl: '/product/product_004718b1-8956-4ac2-b5a6-d056386a83c2.jpg'
        },
        {
          id: 2,
          name: '健身哑铃套装',
          description: '可调节重量，满足不同训练需求',
          price: 599,
          imageUrl: '/product/product_0c2b4f33-6a8c-498d-8b06-1f9c3be00f48.jpg'
        },
        {
          id: 3,
          name: '运动T恤',
          description: '透气速干，舒适贴身',
          price: 199,
          imageUrl: '/product/product_4cdb3c67-be31-4080-9bb0-a5255e9f9f2f.jpg'
        },
        {
          id: 4,
          name: '篮球',
          description: '专业比赛用球，手感出色',
          price: 299,
          imageUrl: '/product/product_4e82cb08-04ae-4423-a5da-67c73ef38ac7.jpg'
        }
      ];
    },
    
    // 添加商品到购物车
    async handleAddToCart(productId, quantity) {
      try {
        await CartService.addToCart(productId, quantity);
        alert('商品已成功添加到购物车！');
      } catch (error) {
        console.error('添加商品到购物车失败:', error);
        alert('添加商品失败，请稍后重试。');
      }
    },
    
    // 取消退出登录
    cancelLogout() {
      console.log('用户取消退出登录');
      this.showLogoutModal = false;
    },
    
    // 确认退出登录
    confirmLogout() {
      console.log('用户确认退出登录');
      this.showLogoutModal = false;
      this.performLogout();
    },
    
    // 执行退出登录操作
    performLogout() {
      try {
        console.log('开始执行退出登录操作');
        
        // 首先更新全局auth状态
        if (this.$auth && this.$auth.user !== null) {
          this.$auth.user = null;
          this.$auth.isAuthenticated = false;
        }
        
        // 使用try-catch包装localStorage操作，确保即使出错也能继续执行
        try {
          // 清除localStorage中的所有登录相关数据
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('loginTimestamp');
          localStorage.removeItem('userAvatar');
          localStorage.removeItem('username');
          localStorage.removeItem('userToken');
          console.log('localStorage清理完成');
        } catch (storageError) {
          console.error('清除localStorage时出错:', storageError);
          // 即使localStorage清理失败，也要继续退出流程
        }
        
        // 使用延迟跳转确保所有状态更新完成
        setTimeout(() => {
          alert('已成功退出登录');
          // 使用window.location.href强制刷新，确保在所有环境中正确跳转
          window.location.href = '/';
        }, 100);
      } catch (error) {
        console.error('退出登录过程中出现严重错误:', error);
        alert('退出登录过程中出现错误，但您的会话已终止，请刷新页面');
        // 即使出错，也尝试跳转到首页
        setTimeout(() => {
          window.location.href = '/';
        }, 500);
      }
    }
  }
}
</script>

<style>
/* 全局导入变量 */
@import '../assets/css/variables.scss';

.sports-store {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 容器和响应式 */
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 推荐商品区域样式 */
.recommended-products {
  padding: 40px 0;
  background-color: #f9f9f9;
}

.section-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 30px;
  color: var(--text-primary);
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 3px;
  background-color: var(--primary-color);
  margin: 10px auto 0;
  border-radius: 2px;
}

.loading-indicator {
  text-align: center;
  padding: 50px 0;
  color: var(--text-secondary);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .recommended-products {
    padding: 20px 0;
  }
  
  .section-title {
    font-size: 20px;
    margin-bottom: 20px;
  }
}
</style>