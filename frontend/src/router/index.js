import { createRouter, createWebHistory } from 'vue-router'

// 懒加载组件
const HomeView = () => import('../views/HomeView.vue')
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('../views/RegisterView.vue')
const UserProfileView = () => import('../views/UserProfileView.vue')
const AdminUserManagementView = () => import('../views/AdminUserManagementView.vue')
const AdminProductManagementView = () => import('../views/AdminProductManagementView.vue')
const AdminOrderManagementView = () => import('../views/AdminOrderManagementView.vue')
const BallSportsView = () => import('../views/BallSportsView.vue')
const OutdoorAdventureView = () => import('../views/OutdoorAdventureView.vue')
const FitnessTrainingView = () => import('../views/FitnessTrainingView.vue')
const CyclingSportsView = () => import('../views/CyclingSportsView.vue')
const CartView = () => import('../views/CartView.vue')
const MyOrdersView = () => import('../views/MyOrdersView.vue')

// 路由守卫 - 检查是否已登录
const requireAuth = (to, from, next) => {
  const token = localStorage.getItem('token')
  if (token) {
    next() // 已登录，允许访问
  } else {
    next({ name: 'login' }) // 未登录，重定向到登录页
  }
}

// 路由守卫 - 检查是否为管理员
const requireAdmin = (to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token) {
    next({ name: 'login' }) // 未登录，重定向到登录页
    return
  }
  
  const user = JSON.parse(localStorage.getItem('user'))
  if (user && user.role === 'ADMIN') {
    next() // 是管理员，允许访问
  } else {
    next({ name: 'home' }) // 不是管理员，重定向到首页
  }
}

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/profile',
    name: 'profile',
    component: UserProfileView,
    beforeEnter: requireAuth // 需要认证才能访问
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUserManagementView,
    beforeEnter: requireAdmin // 只有管理员可以访问
  },
  {
    path: '/ball-sports',
    name: 'ball-sports',
    component: BallSportsView
  },
  {
    path: '/fitness-training',
    name: 'fitness-training',
    component: FitnessTrainingView
  },
  {
    path: '/outdoor-adventure',
    name: 'outdoor-adventure',
    component: OutdoorAdventureView
  },
  {
    path: '/cycling-sports',
    name: 'cycling-sports',
    component: CyclingSportsView
  },
  {
    path: '/admin/products',
    name: 'admin-products',
    component: AdminProductManagementView,
    beforeEnter: requireAdmin // 只有管理员可以访问
  },
  {
    path: '/cart',
    name: 'cart',
    component: CartView,
    beforeEnter: requireAuth
  },
  {
    path: '/orders',
    name: 'my-orders',
    component: MyOrdersView,
    beforeEnter: requireAuth
  },
  {
    path: '/admin/orders',
    name: 'admin-orders',
    component: AdminOrderManagementView,
    beforeEnter: requireAdmin
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router