<template>
  <section 
    class="hero-carousel"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
  >
    <div class="carousel-container">
      <!-- 使用v-for循环渲染轮播图项 -->
      <div 
        v-for="(item, index) in carouselItems" 
        :key="item.id"
        class="carousel-item"
        :class="{
          'active': index === currentSlide,
          'right-enter': index === currentSlide && direction === 'right',
          'left-enter': index === currentSlide && direction === 'left'
        }"
      >
        <img :src="item.image" :alt="item.title">
        <div class="carousel-caption">
          <h2>{{ item.title }}</h2>
          <p>{{ item.subtitle }}</p>
          <button 
            v-if="item.buttonText"
            class="btn btn-primary"
            @click="handleButtonClick(item)"
          >
            {{ item.buttonText }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 轮播图箭头 -->
    <button class="carousel-arrow carousel-arrow-left" @click="prevSlide">
      &#10094;
    </button>
    <button class="carousel-arrow carousel-arrow-right" @click="nextSlide">
      &#10095;
    </button>
    
    <!-- 轮播图指示器 -->
    <div class="carousel-indicators">
      <button 
        v-for="(item, index) in carouselItems" 
        :key="item.id"
        :class="{ active: index === currentSlide }"
        @click="goToSlide(index)"
        :aria-label="`切换到轮播图 ${index + 1}`"
      ></button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'HeroCarousel',
  props: {
    carouselItems: {
      type: Array,
      default: () => [
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
      ]
    },
    speed: {
      type: Number,
      default: 2000
    }
  },
  data() {
    return {
      currentSlide: 0,
      carouselInterval: null,
      direction: 'left' // 自动轮播方向：'left' 或 'right'
    }
  },
  mounted() {
    this.startCarousel();
  },
  beforeUnmount() {
    this.stopCarousel();
  },
  methods: {
    startCarousel() {
      this.stopCarousel();
      this.carouselInterval = setInterval(() => {
        this.prevSlide();
      }, this.speed);
    },
    
    stopCarousel() {
      if (this.carouselInterval) {
        clearInterval(this.carouselInterval);
        this.carouselInterval = null;
      }
    },
    
    nextSlide() {
      this.direction = 'left';
      this.currentSlide = (this.currentSlide + 1) % this.carouselItems.length;
    },
    
    prevSlide() {
      this.direction = 'right';
      this.currentSlide = (this.currentSlide - 1 + this.carouselItems.length) % this.carouselItems.length;
    },
    
    goToSlide(index) {
      this.currentSlide = index;
      this.startCarousel();
    },
    
    handleMouseEnter() {
      this.stopCarousel();
    },
    
    handleMouseLeave() {
      this.startCarousel();
    },
    
    handleButtonClick(item) {
      if (item.link) {
        console.log('轮播图按钮点击:', item.title);
        // 可以在这里添加按钮点击的逻辑
        // window.location.href = item.link;
      }
    }
  }
}
</script>

<style scoped>
@import '../../assets/css/variables.scss';

.hero-carousel {
  position: relative;
  width: 100%;
  height: 500px;
  overflow: hidden;
  margin-bottom: 2rem;
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transform: translateX(100%);
  transition: all 0.6s ease-in-out;
  z-index: 1;
}

.carousel-item.active {
  opacity: 1;
  transform: translateX(0);
  z-index: 2;
}

.carousel-item.active.right-enter {
  transform: translateX(100%);
  animation: slideFromRight 0.6s ease-in-out forwards;
}

.carousel-item.active.left-enter {
  transform: translateX(-100%);
  animation: slideFromLeft 0.6s ease-in-out forwards;
}

@keyframes slideFromRight {
  to {
    transform: translateX(0);
  }
}

@keyframes slideFromLeft {
  to {
    transform: translateX(0);
  }
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 2rem;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  color: white;
  text-align: center;
  transform: translateY(20px);
  opacity: 0;
  transition: transform 0.6s ease, opacity 0.6s ease;
}

.carousel-item.active .carousel-caption {
  transform: translateY(0);
  opacity: 1;
}

.carousel-caption h2 {
  margin-bottom: 1rem;
  font-size: 2.5rem;
  font-weight: bold;
  color: white;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.8);
  animation: fadeInUp 0.8s ease-out 0.3s both;
}

.carousel-caption p {
  margin-bottom: 1.5rem;
  font-size: 1.2rem;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
  color: #ffffff;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.8);
  animation: fadeInUp 0.8s ease-out 0.5s both;
}

.carousel-caption .btn-primary {
    animation: fadeInUp 0.8s ease-out 0.7s both;
    background-color: transparent;
    border: none;
    padding: 0.75rem 1.5rem;
    font-size: 1.1rem;
    font-weight: 600;
    border-radius: var(--border-radius);
    transition: var(--transition-default);
    color: white;
    cursor: pointer;
    text-shadow: 2px 2px 4px rgba(0,0,0,0.8);
  }

  .carousel-caption .btn-primary:hover {
    background-color: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
  }

.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  background-color: rgba(0,0,0,0.5);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 2rem;
  cursor: pointer;
  transition: var(--transition-default);
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
}

.hero-carousel:hover .carousel-arrow {
  opacity: 1;
  visibility: visible;
}

.carousel-arrow-left {
  left: 20px;
}

.carousel-arrow-right {
  right: 20px;
}

.carousel-arrow:hover {
  background-color: rgba(0,0,0,0.8);
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
}

.carousel-arrow:focus {
  outline: none;
}

.carousel-indicators {
  position: absolute;
  bottom: 1rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.5rem;
  z-index: 10;
}

.carousel-indicators button {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    border: none;
    background-color: rgba(255, 255, 255, 0.5);
    cursor: pointer;
    transition: all 0.3s ease;
    margin: 0 4px;
  }

  .carousel-indicators button:hover {
    background-color: rgba(255, 255, 255, 0.8);
    transform: scale(1.2);
  }

  .carousel-indicators button.active {
    background-color: white;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    transform: scale(1.3);
  }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .carousel-caption h2 {
    font-size: 40px;
  }
}

@media (max-width: 768px) {
  .carousel-caption {
    left: 20px;
    right: 20px;
    text-align: center;
  }
  
  .carousel-caption h2 {
    font-size: 28px;
  }
  
  .carousel-caption p {
    font-size: 16px;
  }
}

@media (max-width: 576px) {
  .carousel-caption h2 {
    font-size: 24px;
  }
}
</style>