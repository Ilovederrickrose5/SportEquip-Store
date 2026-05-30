<template>
  <section class="search-section">
    <div class="container">
      <div class="search-container">
        <div class="logo">
          <h1 class="logo-title">🏆 专业运动装备，助力运动梦想</h1>
        </div>
        <div class="search-bar">
          <input 
            type="text" 
            class="search-input"
            v-model="searchQuery"
            @keyup.enter="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  name: 'SearchSection',
  data() {
    return {
      searchQuery: ''
    }
  },
  methods: {
    // 处理搜索按钮点击事件
    handleSearch() {
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.trim();
        
        // 直接跳转到搜索结果页面
        this.$router.push({
          path: '/search',
          query: { keyword: query }
        });
      }
    }
  }
}
</script>

<style scoped lang="scss">
@use '../../assets/css/variables.scss' as *;

.search-section {
  background-color: var(--primary-lighter);
  padding: 30px 0;
  border-bottom: 1px solid var(--primary-light);
}

.search-container {
  display: flex;
  align-items: center;
  flex-direction: column;
  gap: 20px;
}

.logo-title {
  margin: 0;
  color: var(--primary-color);
  font-size: 32px;
  text-align: center;
  font-weight: bold;
}

.search-bar {
  display: flex;
  width: 100%;
  max-width: 800px;
}

.search-input {
  flex: 1;
  padding: 12px 15px;
  border: 1px solid #333;
  border-right: none;
  font-size: 16px;
  outline: none;
  height: 42px;
  box-sizing: border-box;
  background-color: #fff;
  color: #333;
}

.search-input:focus {
  border-color: #666;
}

.search-btn {
  background-color: #333;
  color: #fff;
  border: 1px solid #333;
  border-left: none;
  padding: 0 18px;
  cursor: pointer;
  transition: all 0.3s;
  height: 42px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
}

.search-btn:hover {
  background-color: #555;
  border-color: #555;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    gap: 15px;
  }
  
  .logo-title {
    font-size: 24px;
  }
  
  .search-bar {
    max-width: 100%;
  }
}

@media (max-width: 576px) {
  .logo-title {
    font-size: 20px;
  }
  
  .search-input {
    padding: 10px 15px;
    font-size: 14px;
  }
}
</style>