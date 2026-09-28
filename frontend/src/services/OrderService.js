import axiosInstance from '../utils/axiosInstance';

class OrderService {
  constructor() {
    // 使用全局共享 axios 实例（含双 Token 无感刷新拦截器），
    // 不再自建 axios 实例：自建实例的 401 处理会绕过 Token 刷新逻辑直接跳登录页
    this.axiosInstance = axiosInstance;
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
      // 共享实例 baseURL=/api，此处补全 /orders 前缀 → POST /api/orders
      const response = await this.axiosInstance.post('/orders', orderData, {
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
      // 对应后端 OrderController 中的 GET /api/orders/list 接口：返回 List<OrderDTO> 纯数组
      // 注意：GET /api/orders（不带/list）返回的是 PageResponse 分页对象，不能直接赋给 this.orders 后调用 .sort
      const response = await this.axiosInstance.get('/orders/list');
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
      const response = await this.axiosInstance.get(`/orders/${orderId}`);
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
      // 管理员不分页接口返回纯数组，分页接口 /all 返回 PageResponse 对象
      const response = await this.axiosInstance.get('/orders/all/list');
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
      const response = await this.axiosInstance.put(`/orders/${orderId}/status`, null, { params: { status } });
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