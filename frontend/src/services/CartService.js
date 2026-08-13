import axiosInstance from '../utils/axiosInstance';
// ✅ 修复：Vuex 的 useStore() 内部调用了 inject()，只能在 setup() / 函数式组件内使用
// CartService 是全局单例类（非组件），直接 import 应用实例注册时的同一个 store 对象即可
import store from '../store/cartStore';

/**
 * 购物车服务
 * 所有 store.commit / store.dispatch 都加了 「store 存在判空 + try/catch 吞没」
 * 原因：即使 store 初始化失败 / 方法名变更 / 购物车逻辑异常，也不能阻塞订单页 / 结算页主流程
 */
class CartService {
  constructor() {
    this.axiosInstance = axiosInstance;
    this.basePath = '/cart';
  }

  /**
   * 安全调用 Vuex store.commit：任何报错只 warn，不向上抛
   * @param {string} mutation
   * @param {*} payload
   */
  safeCommit(mutation, payload) {
    try {
      if (store && typeof store.commit === 'function') {
        store.commit(mutation, payload);
      }
    } catch (err) {
      console.warn(`[CartService] store.commit(${mutation}) 失败:`, err);
    }
  }

  /**
   * 安全调用 Vuex store.dispatch：任何报错只 warn，不向上抛
   * @param {string} action
   * @param {*} payload
   */
  safeDispatch(action, payload) {
    try {
      if (store && typeof store.dispatch === 'function') {
        // 注意：这里不 await，因为 dispatch 结果失败不应阻塞主流程；若需严格同步可 await 再 try/catch
        store.dispatch(action, payload);
      }
    } catch (err) {
      console.warn(`[CartService] store.dispatch(${action}) 失败:`, err);
    }
  }

  /**
   * 获取当前用户的购物车
   */
  async getCart() {
    try {
      const response = await this.axiosInstance.get(`${this.basePath}`);

      // 更新Vuex store（失败不影响主流程）
      this.safeCommit('SET_CART_ITEMS', response.data?.cartItems || []);

      return response.data;
    } catch (error) {
      console.error('获取购物车失败:', error);

      // 更新store错误状态（失败了也不能再往外抛，否则引发连锁报错）
      this.safeCommit('SET_ERROR', '获取购物车失败');

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
      this.safeDispatch('addToCart', {
        product: { id: productId },
        quantity
      });

      return response.data;
    } catch (error) {
      console.error('添加商品到购物车失败:', error);

      this.safeCommit('SET_ERROR', '添加商品失败');

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

      this.safeDispatch('updateQuantity', { productId: cartItemId, quantity });

      return response.data;
    } catch (error) {
      console.error('更新购物车商品数量失败:', error);
      this.safeCommit('SET_ERROR', '更新数量失败');
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
      this.safeDispatch('removeFromCart', cartItemId);
      return response.data;
    } catch (error) {
      console.error('从购物车移除商品失败:', error);
      this.safeCommit('SET_ERROR', '移除商品失败');
      throw error;
    }
  }

  /**
   * 清空购物车（下单成功后调用）
   * ✅ 兜底：任何错误（后端/网络/store）都不会阻塞 "跳转我的订单页" 主流程
   */
  async clearCart() {
    try {
      await this.axiosInstance.delete(`${this.basePath}`);

      // 优先同步清空本地 Vuex store
      this.safeCommit('CLEAR_CART');
      // 兼容历史 actions 调用（若存在）
      this.safeDispatch('clearCart');

      return { cartItems: [] };
    } catch (error) {
      console.error('清空购物车失败:', error);

      this.safeCommit('SET_ERROR', '清空购物车失败');

      // 清空失败视为软失败：不再向上抛 → 订单成功后跳转我的订单页不受影响
      // 但返回空购物车结果，让调用方 UI 保持一致性
      return { cartItems: [] };
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
