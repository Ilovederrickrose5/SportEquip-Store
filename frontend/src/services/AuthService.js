import axiosInstance from '../utils/axiosInstance'
import { API_ENDPOINTS } from '../config/api'

class AuthService {
  constructor() {
    // 使用统一的axios实例
    this.api = axiosInstance
  }
  
  // 登录方法
  async login(credentials) {
    try {
      const response = await this.api.post(API_ENDPOINTS.auth.login, credentials)
      
      // 保存token和用户信息到localStorage
      // 适配后端返回的字段名（可能是token或accessToken）
      const token = response.data.accessToken || response.data.token
      if (token) {
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify({
          username: response.data.username,
          id: response.data.id,
          role: response.data.role || 'USER'
        }))
      }
      
      return response.data
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }
  
  // 注册方法
  async register(userData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.auth.register, userData)
      return response.data
    } catch (error) {
      throw error
    }
  }
  
  // 管理员注册方法（如果需要）
  async registerAdmin(userData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.auth.registerAdmin, userData)
      return response.data
    } catch (error) {
      throw error
    }
  }
  
  // 重置密码方法
  async resetPassword(email) {
    try {
      const response = await this.api.post(API_ENDPOINTS.auth.resetPassword, { email })
      return response.data
    } catch (error) {
      throw error
    }
  }
  
  // 登出方法
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }
  
  // 获取当前登录用户信息
  getCurrentUser() {
    try {
      const userStr = localStorage.getItem('user')
      if (!userStr) return null
      return JSON.parse(userStr)
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return null
    }
  }
  
  // 检查是否已认证
  isAuthenticated() {
    const token = localStorage.getItem('token')
    const user = this.getCurrentUser()
    
    // 简单检查token是否存在和用户信息是否有效
    if (!token || !user) {
      return false
    }
    
    // 可以在这里添加token过期检查逻辑
    try {
      // 解析token获取过期时间（简化版）
      const tokenParts = token.split('.')
      if (tokenParts.length !== 3) {
        return false
      }
      
      const payload = JSON.parse(atob(tokenParts[1]))
      const expirationTime = payload.exp * 1000 // 转换为毫秒
      
      // 检查是否过期
      if (Date.now() > expirationTime) {
        this.logout()
        return false
      }
      
      return true
    } catch (error) {
      console.error('Token验证失败:', error)
      this.logout()
      return false
    }
  }
  
  // 获取用户角色
  getUserRole() {
    const user = this.getCurrentUser()
    return user ? user.role : null
  }
  
  // 检查用户是否有特定角色
  hasRole(role) {
    const userRole = this.getUserRole()
    return userRole && userRole.toUpperCase() === role.toUpperCase()
  }
}

// 导出单例实例
export default new AuthService()