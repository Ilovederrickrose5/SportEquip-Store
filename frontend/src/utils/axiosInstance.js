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

// ============================================================
// 双 Token 无感刷新（access_token 过期自动续期）
// 流程：业务请求 401 → 用 refreshToken 调 /auth/refresh 换新 token
//      → 成功：重放原请求与队列中所有挂起请求
//      → 失败：清登录态 + 跳登录页
// ============================================================

// 免刷新白名单：这些接口 401 不触发刷新
// - /auth/login 401 = 用户名或密码错误，直接抛给业务层提示
// - /auth/refresh 401 = refreshToken 也失效，绝不能再刷新（防死循环）
const AUTH_WHITELIST = ['/auth/login', '/auth/register', '/auth/refresh']

// 刷新进行中标志：保证并发 401 只触发一次刷新请求
let isRefreshing = false
// 刷新期间挂起的请求队列，元素为 { resolve, reject }，刷新完成后统一重放/拒绝
let pendingRequests = []

function isAuthWhitelist(url) {
  return AUTH_WHITELIST.some((path) => url?.includes(path))
}

// 清理本地登录态（覆盖所有登录相关 key，避免残留）
function clearAuthState() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  localStorage.removeItem('loginTimestamp')
}

// 跳转登录页并携带回跳地址，登录成功后由 LoginView 读取 redirect 返回原页面
function redirectToLogin() {
  router.push({
    name: 'login',
    query: { redirect: window.location.pathname }
  })
}

// 刷新成功：放行队列中所有挂起请求（携带新 token）
function replayPendingRequests(newToken) {
  pendingRequests.forEach(({ resolve }) => resolve(newToken))
  pendingRequests = []
}

// 刷新失败：拒绝队列中所有挂起请求，避免调用方永久 pending
function rejectPendingRequests(error) {
  pendingRequests.forEach(({ reject }) => reject(error))
  pendingRequests = []
}

/**
 * 401 统一处理：尝试无感刷新，失败才登出
 * @returns Promise，重放成功时 resolve 原请求的新响应
 */
async function handleUnauthorized(error) {
  const originalConfig = error.config
  const refreshToken = localStorage.getItem('refreshToken')

  // 场景1：登录/注册/刷新接口本身 401 —— 不刷新，直接抛给业务层
  // （登录 401 由 LoginView 提示"用户名或密码错误"，刷新 401 触发登出流程）
  if (isAuthWhitelist(originalConfig?.url)) {
    return Promise.reject(error)
  }

  // 场景2：本地没有 refreshToken（旧版本登录的用户）——只能登出
  if (!refreshToken) {
    if (DEBUG_MODE) console.warn('无 refreshToken 可用，直接登出')
    clearAuthState()
    redirectToLogin()
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }

  // 场景3：刷新已在进行中 —— 当前请求入队等待，避免并发重复刷新
  if (isRefreshing) {
    if (DEBUG_MODE) console.warn('刷新进行中，请求进入挂起队列:', originalConfig?.url)
    return new Promise((resolve, reject) => {
      pendingRequests.push({ resolve, reject })
    }).then((newToken) => {
      originalConfig.headers.Authorization = `Bearer ${newToken}`
      return axiosInstance(originalConfig)
    })
  }

  // 场景4：首个 401 —— 发起刷新。
  // 用裸 axios 而非 axiosInstance：裸实例没有挂载本拦截器，刷新请求 401 不会递归进入这里造成死循环
  isRefreshing = true
  try {
    const refreshResponse = await axios.post(
      `${getApiBaseUrl()}/auth/refresh`,
      { refreshToken },
      { headers: { 'Content-Type': 'application/json' } }
    )

    const data = refreshResponse.data || {}
    const newAccessToken = data.accessToken || data.token
    // 后端采用 rotation 轮换策略：每次刷新下发全新 refreshToken，旧 refresh 立即作废，
    // 本地必须同步覆盖保存，否则下一次刷新会因旧 jti 与服务端绑定不一致被判重放攻击
    const newRefreshToken = data.refreshToken || refreshToken

    if (!newAccessToken) {
      throw new Error('刷新响应缺少 accessToken')
    }

    // 持久化新 token
    localStorage.setItem('token', newAccessToken)
    localStorage.setItem('refreshToken', newRefreshToken)

    if (DEBUG_MODE) console.log('Token 刷新成功，重放挂起请求')

    isRefreshing = false
    replayPendingRequests(newAccessToken)

    // 重放当前触发刷新的请求（替换为新 token）
    originalConfig.headers.Authorization = `Bearer ${newAccessToken}`
    return axiosInstance(originalConfig)
  } catch (refreshError) {
    // 刷新失败：refreshToken 已过期/被轮换/被拉黑 —— 清态并拒绝所有挂起请求
    if (DEBUG_MODE) console.error('Token 刷新失败，强制登出:', refreshError?.response?.data || refreshError)
    isRefreshing = false
    rejectPendingRequests(refreshError)
    clearAuthState()
    redirectToLogin()
    return Promise.reject(new Error('登录已过期，请重新登录'))
  }
}

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

// 响应拦截器 - 统一错误处理 + 401 无感刷新
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

    // 处理401未授权：先尝试用 refreshToken 无感刷新并重放原请求，
    // 刷新失败（或无 refreshToken / 白名单接口）才清态跳登录页
    if (error.response && error.response.status === 401) {
      return handleUnauthorized(error)
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
