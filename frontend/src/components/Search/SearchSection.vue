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
            placeholder="搜索运动装备..." 
            class="search-input"
            v-model="searchQuery"
            @keyup.enter="handleSearch"
          >
          <button class="search-btn" @click="handleSearch">🔍</button>
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
  padding: 12px 20px;
  border: 2px solid var(--primary-color);
  border-radius: 20px 0 0 20px;
  font-size: 16px;
  outline: none;
  height: 44px;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: var(--primary-dark);
  box-shadow: 0 0 0 3px rgba(30, 144, 255, 0.2);
}

.search-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0 30px;
  border-radius: 0 20px 20px 0;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s;
  height: 44px;
  width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-btn:hover {
  background-color: var(--primary-dark);
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