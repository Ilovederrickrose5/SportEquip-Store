import axiosInstance from '../utils/axiosInstance';
import { useStore } from 'vuex';

/**
 * 购物车服务
 */
class CartService {
  constructor() {
    this.axiosInstance = axiosInstance;
    this.basePath = '/cart';
    // Vuex store将在方法内部获取，因为在服务初始化时可能尚未创建
  }
  
  // 获取Vuex store实例
  getStore() {
    // 在Vue 3中，需要从应用实例获取store
    // 这里直接导入store实例以避免依赖注入问题
    const store = useStore();
    return store;
  }

  /**
   * 获取当前用户的购物车
   */
  async getCart() {
    try {
      const response = await this.axiosInstance.get(`${this.basePath}`);
      
      // 更新Vuex store
      try {
        const store = this.getStore();
        store.commit('SET_CART_ITEMS', response.data.cartItems || []);
      } catch (storeError) {
        console.warn('无法更新Vuex store:', storeError);
      }
      
      return response.data;
    } catch (error) {
      console.error('获取购物车失败:', error);
      
      // 更新store错误状态
      try {
        const store = this.getStore();
        store.commit('SET_ERROR', '获取购物车失败');
      } catch (storeError) {
        console.warn('无法更新Vuex store错误状态:', storeError);
      }
      
      throw error;
    }
  }

  /**
   * 添加商品到购物车
   * @param {number} productId - 商品ID
   * @param {number} quantity - 数量
   */
  async addToCart(productId, quantity) {
    try {
      const response = await this.axiosInstance.post(`${this.basePath}/items`, null, {
        params: {
          productId,
          quantity
        }
      });
      
      // 更新Vuex store
      try {
        const store = this.getStore();
        // 假设响应中包含了完整的商品信息
        const product = {
          id: productId,
          // 其他商品信息可能需要从响应中获取或额外请求
          // 这里做简化处理
        };
        store.dispatch('addToCart', { product, quantity });
      } catch (storeError) {
        console.warn('无法更新Vuex store:', storeError);
      }
      
      return response.data;
    } catch (error) {
      console.error('添加商品到购物车失败:', error);
      
      // 更新store错误状态
      try {
        const store = this.getStore();
        store.commit('SET_ERROR', '添加商品失败');
      } catch (storeError) {
        console.warn('无法更新Vuex store错误状态:', storeError);
      }
      
      // 统一错误处理
      if (!error.response) {
        error.response = { data: { message: '网络错误或服务器未响应' } };
      } else if (!error.response.data) {
        error.response.data = { message: '服务器返回格式异常' };
      } else if (!error.response.data.message) {
        error.response.data.message = '添加商品失败';
      }
      throw error;
    }
  }

  /**
   * 更新购物车商品数量
   * @param {number} cartItemId - 购物车项ID
   * @param {number} quantity - 新数量
   */
  async updateCartItem(cartItemId, quantity) {
    try {
      const response = await this.axiosInstance.put(`${this.basePath}/items/${cartItemId}`, null, {
        params: { quantity }
      });
      
      // 更新Vuex store
      try {
        const store = this.getStore();
        store.dispatch('updateQuantity', { productId: cartItemId, quantity });
      } catch (storeError) {
        console.warn('无法更新Vuex store:', storeError);
      }
      
      return response.data;
    } catch (error) {
      console.error('更新购物车商品数量失败:', error);
      
      // 更新store错误状态
      try {
        const store = this.getStore();
        store.commit('SET_ERROR', '更新数量失败');
      } catch (storeError) {
        console.warn('无法更新Vuex store错误状态:', storeError);
      }
      
      throw error;
    }
  }

  /**
   * 从购物车移除商品
   * @param {number} cartItemId - 购物车项ID
   */
  async removeFromCart(cartItemId) {
    try {
      const response = await this.axiosInstance.delete(`${this.basePath}/items/${cartItemId}`);
      
      // 更新Vuex store
      try {
        const store = this.getStore();
        store.dispatch('removeFromCart', cartItemId);
      } catch (storeError) {
        console.warn('无法更新Vuex store:', storeError);
      }
      
      return response.data;
    } catch (error) {
      console.error('从购物车移除商品失败:', error);
      
      // 更新store错误状态
      try {
        const store = this.getStore();
        store.commit('SET_ERROR', '移除商品失败');
      } catch (storeError) {
        console.warn('无法更新Vuex store错误状态:', storeError);
      }
      
      throw error;
    }
  }

  /**
   * 清空购物车
   */
  async clearCart() {
    try {
      await this.axiosInstance.delete(`${this.basePath}`);
      
      // 更新Vuex store
      try {
        const store = this.getStore();
        store.dispatch('clearCart');
      } catch (storeError) {
        console.warn('无法更新Vuex store:', storeError);
      }
      
      return { cartItems: [] };
    } catch (error) {
      console.error('清空购物车失败:', error);
      
      // 更新store错误状态
      try {
        const store = this.getStore();
        store.commit('SET_ERROR', '清空购物车失败');
      } catch (storeError) {
        console.warn('无法更新Vuex store错误状态:', storeError);
      }
      
      throw error;
    }
  }
  
  /**
   * 获取购物车商品总数
   */
  async getCartItemCount() {
    try {
      const response = await this.axiosInstance.get(`${this.basePath}/count`);
      return response.data;
    } catch (error) {
      console.error('获取购物车商品数量失败:', error);
      return 0;
    }
  }
}

export default new CartService();