import axios from 'axios'
import { getApiBaseUrl } from '../config/api'
import router from '../router'

// 调试输出baseURL配置
console.log('API配置:', {
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  environment: import.meta.env.MODE || 'development'
})

// 创建Axios实例
const axiosInstance = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  // 允许跨域携带凭证
  withCredentials: true,
  // 请求头配置
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 添加全局调试标记
const DEBUG_MODE = true

// 请求拦截器 - 确保正确处理token
axiosInstance.interceptors.request.use(
  config => {
    if (DEBUG_MODE) {
      console.log('\n===== 请求拦截器 =====')
      console.log('API基础URL:', config.baseURL)
      console.log('完整请求URL:', config.baseURL + config.url)
      console.log('请求方法:', config.method?.toUpperCase())
    }
    
    // 检查是否是登录请求，如果是登录请求，则不添加Authorization头
    const isLoginRequest = config.url?.includes('/auth/login') || config.url?.includes('/login')
    
    if (isLoginRequest && DEBUG_MODE) {
      console.log('登录请求，不添加Authorization头')
    }
    
    // 非登录请求才从localStorage获取token
    if (!isLoginRequest) {
      // 从localStorage获取token
      const token = localStorage.getItem('token')
      
      if (DEBUG_MODE) {
        console.log('token存在:', !!token)
      }
      
      // 清除可能存在的旧token格式问题
      if (token && token.startsWith('Bearer ')) {
        if (DEBUG_MODE) {
          console.warn('Token已包含Bearer前缀，移除重复前缀')
        }
        const cleanToken = token.substring(7)
        localStorage.setItem('token', cleanToken)
        config.headers.Authorization = `Bearer ${cleanToken}`
      } else if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    
    if (DEBUG_MODE) {
      // 打印请求头（移除token以保护隐私）
      const safeHeaders = { ...config.headers }
      if (safeHeaders['Authorization']) {
        safeHeaders['Authorization'] = safeHeaders['Authorization'].substring(0, 10) + '...'
      }
      console.log('请求头:', safeHeaders)
      
      // 打印请求数据
      if (config.data) {
        console.log('请求数据:', config.data)
      }
      console.log('======================\n')
    }
    
    return config
  },
  error => {
    console.error('\n===== 请求配置错误 =====')
    console.error('错误详情:', error)
    console.error('============================\n')
    return Promise.reject(error)
  }
)

// 响应拦截器 - 添加统一错误处理
axiosInstance.interceptors.response.use(
  response => {
    if (DEBUG_MODE) {
      console.log('\n===== 响应拦截器 =====')
      console.log('请求URL:', response.config.baseURL + response.config.url)
      console.log('响应状态:', response.status, response.statusText)
      console.log('响应数据:', response.data)
      console.log('======================\n')
    }
    
    return response
  },
  error => {
    if (DEBUG_MODE) {
      console.error('\n===== 响应拦截器错误 =====')
      console.error('请求URL:', error.config?.baseURL + error.config?.url)
      
      if (error.response) {
        console.error('响应状态:', error.response.status, error.response.statusText)
        console.error('响应头:', error.response.headers)
        console.error('响应数据:', error.response.data)
        
        // 特别处理500错误
        if (error.response.status === 500) {
          console.error('⚠️ 服务器内部错误(500)，请检查后端日志 ⚠️')
          // 尝试提取具体错误信息
          if (error.response.data && typeof error.response.data === 'object') {
            console.error('错误详情:', error.response.data.message || error.response.data.error || '未知错误')
          }
        }
      } else if (error.request) {
        console.error('请求已发送，但未收到响应')
        console.error('请求详情:', error.request)
      } else {
        console.error('请求配置错误:', error.message)
      }
      console.error('============================\n')
    }
    
    // 处理401未授权错误 - 自动登出并重定向到登录页
    if (error.response && error.response.status === 401) {
      console.error('未授权访问，清除登录状态并跳转到登录页')
      // 清除localStorage中的登录信息
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('loginTimestamp')
      
      // 重定向到登录页，保留当前路径以便登录后返回
      router.push({
        name: 'login',
        query: { redirect: window.location.pathname }
      })
      
      return Promise.reject(new Error('未授权访问，请重新登录'))
    }
    
    // 处理网络错误 (无响应)
    if (!error.response) {
      console.error('网络连接错误，请检查后端服务是否运行正常')
      return Promise.reject(new Error('网络连接失败，请检查后端服务是否正常运行或网络连接是否畅通'))
    }
    
    // 处理其他错误
    const errorMessage = error.response?.data?.message || error.response?.data || error.message || '请求失败'
    // 保留原始错误对象，而不是创建新的Error对象，这样可以保留response等原始信息
    console.error('完整错误对象:', error)
    return Promise.reject(error)
  }
)

// 导出axios实例
export default axiosInstance