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
        const query = this.searchQuery.trim().toLowerCase();
        
        // 创建分类关键词映射表，方便维护和扩展
        const categoryKeywords = {
          ballSports: {
            path: '/ball-sports',
            keywords: ['球', 'basketball', '篮球', 'football', '足球', 'tennis', '网球', 'volleyball', '排球', 'pingpong', '乒乓球', '羽毛球', 'badminton', '球拍', '球网', '球服', '球鞋', '训练', '比赛', '运动', '篮球架', '足球门', '网球拍', '羽毛球拍', '乒乓球拍']
          },
          outdoorAdventure: {
            path: '/outdoor-adventure',
            keywords: ['户外', 'outdoor', '露营', '登山', 'jeep', '徒步', '探险', '野营', '背包', '帐篷', '睡袋', '登山鞋', '户外服', '防水', '马丁靴', '登山靴', '冲锋衣', '抓绒衣', '户外鞋', '徒步鞋', '野营', '烧烤', '炉头', '水壶', '登山杖', '户外包', '探险', '越野']
          },
          fitnessTraining: {
            path: '/fitness-training',
            keywords: ['健身', 'fitness', '训练', '器材', '哑铃', '杠铃', '跑步机', '健身车', '卧推', '深蹲', '拉力器', '健腹轮', '瑜伽', 'yoga', '运动服', '健身裤', '训练服', '运动鞋', '健身鞋', '瑜伽垫', '弹力带', '腹肌轮', '引体向上', '俯卧撑', '臂力器', '握力器', '健身手套']
          },
          cyclingSports: {
            path: '/cycling-sports',
            keywords: ['骑行', '自行车', 'bike', 'cycling', '公路车', '山地车', '山地', '单车', '骑行服', '头盔', '自行车配件', '轮组', '车把', '车座', '链条', '刹车', '轮胎', '骑行裤', '骑行鞋', '骑行手套', '水壶架', '码表', '车灯', '自行车锁', '骑行背包', '护膝', '护肘']
          }
        };
        
        // 实现更灵活的搜索逻辑
        // 1. 统计每个分类的匹配关键词数量，选择匹配度最高的
        let matchedCategory = null;
        let maxMatchCount = 0;
        
        Object.values(categoryKeywords).forEach(category => {
          const matchCount = category.keywords.filter(keyword => query.includes(keyword)).length;
          if (matchCount > maxMatchCount) {
            maxMatchCount = matchCount;
            matchedCategory = category;
          }
        });
        
        // 2. 优先跳转到匹配度最高的分类页面
        if (matchedCategory && maxMatchCount > 0) {
          this.$router.push({
            path: matchedCategory.path,
            query: { search: query }
          });
        } else {
          // 3. 如果没有匹配的分类，默认跳转到首页，但仍然传递搜索参数
          // 这样首页可以实现对所有商品的模糊搜索
          this.$router.push({
            path: '/',
            query: { search: query }
          });
        }
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