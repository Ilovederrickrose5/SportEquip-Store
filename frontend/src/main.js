import { createApp } from 'vue'
import store from './store/cartStore'
import './assets/css/variables.scss'
import './style.css'
import './assets/css/theme.scss'
import './assets/css/components.scss'
import App from './App.vue'
import router from './router'
import { authPlugin } from './plugins/auth'
// 导入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const app = createApp(App)

// 注册路由
app.use(router)

// 注册Vuex状态管理
app.use(store)

// 注册认证插件
app.use(authPlugin)
// 使用Element Plus
app.use(ElementPlus)

// 挂载应用
app.mount('#app')
