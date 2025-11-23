import axios from 'axios';

class OrderService {
  constructor() {
    // 创建axios实例
    this.axiosInstance = axios.create({
      baseURL: '/api/orders',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // 请求拦截器 - 添加认证token
    this.axiosInstance.interceptors.request.use(
      config => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      error => {
        return Promise.reject(error);
      }
    );

    // 响应拦截器 - 统一错误处理
    this.axiosInstance.interceptors.response.use(
      response => response,
      error => {
        if (error.response && error.response.status === 401) {
          // 未授权，清除token并跳转到登录页
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  /**
   * 创建订单
   * @param {Object} orderData - 订单数据
   * @returns {Promise} 创建的订单
   */
  async createOrder(orderData) {
    try {
      console.log('OrderService.createOrder 接收到的数据:', JSON.stringify(orderData, null, 2));
      
      // 添加详细的请求配置，以便更好地调试
      const response = await this.axiosInstance.post('', orderData, {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        transformRequest: [(data) => {
          console.log('转换后发送的数据:', JSON.stringify(data, null, 2));
          return JSON.stringify(data);
        }]
      });
      
      console.log('订单创建成功，响应:', response.data);
      return response.data;
    } catch (error) {
      console.error('创建订单失败:', error);
      console.error('错误详情:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        config: error.config?.url
      });
      
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data = { message: '创建订单失败' };
      }
      throw error;
    }
  }

  /**
   * 获取用户订单列表
   * @returns {Promise} 订单列表
   */
  async getUserOrders() {
    try {
      // 修改路径为根路径，与后端OrderController中的GET /api/orders端点匹配
      const response = await this.axiosInstance.get('');
      return response.data;
    } catch (error) {
      console.error('获取用户订单失败:', error);
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data = { message: '获取订单失败' };
      }
      throw error;
    }
  }

  /**
   * 获取订单详情
   * @param {number} orderId - 订单ID
   * @returns {Promise} 订单详情
   */
  async getOrderDetails(orderId) {
    try {
      const response = await this.axiosInstance.get(`/${orderId}`);
      return response.data;
    } catch (error) {
      console.error('获取订单详情失败:', error);
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data = { message: '获取订单详情失败' };
      }
      throw error;
    }
  }

  /**
   * 获取所有订单（管理员用）
   * @returns {Promise} 所有订单列表
   */
  async getAllOrders() {
    try {
      const response = await this.axiosInstance.get('/all');
      return response.data;
    } catch (error) {
      console.error('获取所有订单失败:', error);
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data = { message: '获取订单失败' };
      }
      throw error;
    }
  }

  /**
   * 更新订单状态
   * @param {number} orderId - 订单ID
   * @param {string} status - 新状态
   * @returns {Promise} 更新后的订单
   */
  async updateOrderStatus(orderId, status) {
    try {
      // 将status作为查询参数传递，而不是请求体
      const response = await this.axiosInstance.put(`/${orderId}/status`, null, { params: { status } });
      return response.data;
    } catch (error) {
      console.error('更新订单状态失败:', error);
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data = { message: '更新订单状态失败' };
      }
      throw error;
    }
  }
}

export default new OrderService();