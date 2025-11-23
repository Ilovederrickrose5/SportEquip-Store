import { reactive, provide, inject } from 'vue'
import AuthService from '../services/AuthService'

// 创建响应式的认证状态
const authState = reactive({
  user: null,
  isAuthenticated: false,
  isLoading: true
})

// 初始化认证状态
const initializeAuth = () => {
  try {
    authState.user = AuthService.getCurrentUser()
    authState.isAuthenticated = AuthService.isAuthenticated()
  } catch (error) {
    console.error('初始化认证状态失败:', error)
    authState.user = null
    authState.isAuthenticated = false
  } finally {
    authState.isLoading = false
  }
}

// 登录方法
const login = async (credentials) => {
  try {
    const data = await AuthService.login(credentials)
    
    // 更新响应式状态
    authState.user = AuthService.getCurrentUser()
    authState.isAuthenticated = true
    
    return data
  } catch (error) {
    throw error
  }
}

// 注册方法
const register = async (userData) => {
  try {
    return await AuthService.register(userData)
  } catch (error) {
    throw error
  }
}

// 登出方法
const logout = () => {
  AuthService.logout()
  
  // 更新响应式状态
  authState.user = null
  authState.isAuthenticated = false
}

// 检查用户角色
const hasRole = (role) => {
  return AuthService.hasRole(role)
}

// 创建provide的对象
const auth = {
  ...authState,
  initializeAuth,
  login,
  register,
  logout,
  hasRole
}

// 导出插件
export const authPlugin = {
  install(app) {
    // 提供全局认证状态
    app.provide('auth', auth)
    
    // 添加全局属性
    app.config.globalProperties.$auth = auth
    
    // 初始化认证状态
    initializeAuth()
  }
}

// 导出inject函数，用于在组件中注入
export const useAuth = () => {
  const auth = inject('auth')
  if (!auth) {
    throw new Error('useAuth must be used within an auth plugin context')
  }
  return auth
}

// 导出认证状态，可在非组件中使用
export { authState, initializeAuth, login, register, logout, hasRole }