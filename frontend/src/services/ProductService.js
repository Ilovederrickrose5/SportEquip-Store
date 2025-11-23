import { API_ENDPOINTS } from '../config/api'
import axios from 'axios'

class ProductService {
  constructor() {
    // 直接创建axios实例以避免循环引用
    this.api = axios.create({
      baseURL: process.env.NODE_ENV === 'production' ? '/api' : 'http://localhost:8080/api',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    })
    
    // 添加请求拦截器获取token
    this.api.interceptors.request.use(config => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    })
  }
  
  // 获取所有产品
  async getAllProducts() {
    try {
      console.log('ProductService.getAllProducts() - 准备调用API:', API_ENDPOINTS.products.getAll);
      console.log('请求URL:', this.api.defaults.baseURL + API_ENDPOINTS.products.getAll);
      
      const startTime = Date.now();
      const response = await this.api.get(API_ENDPOINTS.products.getAll)
      const endTime = Date.now();
      
      console.log('ProductService.getAllProducts() - API调用成功');
      console.log('响应状态:', response.status);
      console.log('响应数据类型:', typeof response.data);
      console.log('响应数据长度:', Array.isArray(response.data) ? response.data.length : '对象');
      console.log('请求耗时:', endTime - startTime, 'ms');
      
      return response.data
    } catch (error) {
      console.error('ProductService.getAllProducts() - 获取产品列表失败:', error);
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      } else if (error.request) {
        console.error('请求已发送但没有收到响应:', error.request);
      } else {
        console.error('请求配置错误:', error.message);
      }
      throw error
    }
  }
  
  // 根据ID获取产品详情
  async getProductById(id) {
    try {
      const response = await this.api.get(API_ENDPOINTS.products.getById(id))
      return response.data
    } catch (error) {
      console.error(`获取产品详情失败 (ID: ${id}):`, error)
      throw error
    }
  }
  
  // 分页获取产品
  async getProductsByPage(page = 0, size = 10) {
    try {
      const response = await this.api.get(API_ENDPOINTS.products.getByPage(page, size))
      return response.data
    } catch (error) {
      console.error(`分页获取产品失败 (页: ${page}, 每页: ${size}):`, error)
      throw error
    }
  }
  
  // 创建新商品（需要管理员权限）
  async createProduct(productData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.products.getAll, productData)
      return response.data
    } catch (error) {
      console.error('ProductService.createProduct() - 创建商品失败:', error)
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      }
      throw error
    }
  }
  
  // 更新商品（需要管理员权限）
  async updateProduct(id, productData) {
    try {
      const response = await this.api.put(API_ENDPOINTS.products.getById(id), productData)
      return response.data
    } catch (error) {
      console.error(`ProductService.updateProduct() - 更新商品失败 (ID: ${id}):`, error)
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      }
      throw error
    }
  }
  
  // 删除商品（需要管理员权限）
  async deleteProduct(id) {
    try {
      await this.api.delete(API_ENDPOINTS.products.getById(id))
      return true
    } catch (error) {
      console.error(`ProductService.deleteProduct() - 删除商品失败 (ID: ${id}):`, error)
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      }
      throw error
    }
  }
  
  // 上传商品图片（需要管理员权限）
  async uploadProductImage(file, customName = null) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      // 如果提供了自定义文件名，则添加到formData中
      if (customName) {
        formData.append('customName', customName)
      }
      
      const response = await this.api.post('/files/upload/product', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
      
      return response.data
    } catch (error) {
      console.error('ProductService.uploadProductImage() - 上传商品图片失败:', error)
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      }
      throw error
    }
  }
  
  // 获取所有分类
  async getAllCategories() {
    try {
      console.log('ProductService.getAllCategories() - 准备调用API: /categories');
      console.log('请求URL:', this.api.defaults.baseURL + '/categories');
      
      const response = await this.api.get('/categories')
      
      console.log('ProductService.getAllCategories() - API调用成功');
      console.log('响应状态:', response.status);
      console.log('响应数据类型:', typeof response.data);
      console.log('响应数据长度:', Array.isArray(response.data) ? response.data.length : '对象');
      
      return response.data
    } catch (error) {
      console.error('ProductService.getAllCategories() - 获取分类列表失败:', error);
      if (error.response) {
        console.error('错误响应状态:', error.response.status);
        console.error('错误响应数据:', error.response.data);
      } else if (error.request) {
        console.error('请求已发送但没有收到响应:', error.request);
      } else {
        console.error('请求配置错误:', error.message);
      }
      throw error
    }
  }
}

export default new ProductService()