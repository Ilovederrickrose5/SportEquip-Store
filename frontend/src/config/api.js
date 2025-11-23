// API配置文件

// API基础路径
const API_BASE_URL = {
  // 开发环境
  development: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  // 生产环境
  production: import.meta.env.VITE_API_BASE_URL || '/api'
}

// 获取当前环境的API基础路径
export const getApiBaseUrl = () => {
  // 优先使用环境变量中的API基础路径
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  const env = import.meta.env.MODE || 'development'
  return API_BASE_URL[env] || API_BASE_URL.development
}

// API端点配置
export const API_ENDPOINTS = {
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    registerAdmin: '/auth/register-admin',
    resetPassword: '/auth/reset-password'
  },
  user: {
    // 普通用户相关API
    getCurrent: '/users/me',
    update: (id) => `/users/${id}`,
    changePassword: '/users/change-password',
    // 用户管理相关API（管理员）
    getAll: '/users',
    getById: (id) => `/users/${id}`,
    updateUser: (id) => `/users/${id}`,
    deleteUser: (id) => `/users/${id}`,
    changeRole: (id) => `/users/${id}/role`
  },
  products: {
    getAll: '/products',
    getById: (id) => `/products/${id}`,
    getByPage: (page, size) => `/products?page=${page}&size=${size}`
  }
}

// 请求超时时间（毫秒）
export const REQUEST_TIMEOUT = 30000

// 最大重试次数
export const MAX_RETRY_COUNT = 3

// 导出默认配置
export default {
  baseURL: getApiBaseUrl(),
  timeout: REQUEST_TIMEOUT,
  maxRetries: MAX_RETRY_COUNT,
  endpoints: API_ENDPOINTS
}