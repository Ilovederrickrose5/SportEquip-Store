<template>
  <footer class="main-footer">
    <div class="container">
      <div class="footer-content">
        <div class="footer-column">
          <h3 class="footer-title">关于我们</h3>
          <p>专业运动装备零售商，提供高品质运动产品和服务。</p>
        </div>
        
        <div class="footer-column">
          <h3 class="footer-title">联系我们</h3>
          <ul class="footer-contact">
            <li><span>电话：</span>1008611</li>
            <li><span>邮箱：</span>3077677548@qq.com</li>
          </ul>
        </div>
      </div>
      
      <div class="footer-bottom">
        <div class="copyright">
          <p>© 2025 专业运动装备商城. 保留所有权利.</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'MainFooter'
}
</script>

<style scoped>
@import '../../assets/css/variables.scss';

.main-footer {
  background-color: var(--primary-color);
  color: white;
  padding: 40px 0 20px;
  margin-top: 50px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  margin-bottom: 30px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.footer-column {
  display: flex;
  flex-direction: column;
}

.footer-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary-light);
}

.footer-column p {
  line-height: 1.6;
  margin-bottom: 0;
  color: rgba(255, 255, 255, 0.85);
}

.footer-contact {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-contact li {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
  line-height: 1.6;
}

.footer-contact li span:first-child {
  font-weight: 600;
  margin-right: 10px;
  color: var(--primary-light);
  min-width: 50px;
}

.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  padding-top: 20px;
  text-align: center;
}

.copyright p {
  margin: 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-footer {
    padding: 30px 0 15px;
  }
  
  .footer-content {
    grid-template-columns: 1fr;
    gap: 25px;
  }
  
  .footer-title {
    font-size: 16px;
    margin-bottom: 15px;
  }
}
</style>