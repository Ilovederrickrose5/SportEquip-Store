import { createStore } from 'vuex'

export default createStore({
  state() {
    return {
      // 状态
      cartItems: [],
      loading: false,
      error: null
    }
  },
  
  getters: {
    // 计算属性
    totalItems(state) {
      return state.cartItems.reduce((total, item) => total + item.quantity, 0)
    },
    
    totalPrice(state) {
      return state.cartItems.reduce((total, item) => total + (item.price * item.quantity), 0)
    }
  },
  
  mutations: {
    // 设置购物车项目
    SET_CART_ITEMS(state, items) {
      state.cartItems = items
    },
    
    // 添加商品到购物车
    ADD_TO_CART(state, { product, quantity = 1 }) {
      const existingItem = state.cartItems.find(item => item.productId === product.id)
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        state.cartItems.push({
          productId: product.id,
          name: product.name,
          price: product.price,
          image: product.image,
          quantity
        })
      }
    },
    
    // 更新商品数量
    UPDATE_QUANTITY(state, { productId, quantity }) {
      const item = state.cartItems.find(item => item.productId === productId)
      if (item) {
        item.quantity = quantity
      }
    },
    
    // 移除商品
    REMOVE_FROM_CART(state, productId) {
      state.cartItems = state.cartItems.filter(item => item.productId !== productId)
    },
    
    // 清空购物车
    CLEAR_CART(state) {
      state.cartItems = []
    },
    
    // 设置加载状态
    SET_LOADING(state, status) {
      state.loading = status
    },
    
    // 设置错误信息
    SET_ERROR(state, error) {
      state.error = error
    }
  },
  
  actions: {
    // 获取购物车项目
    async fetchCart({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      try {
        // 实际项目中这里会调用API
        // const response = await CartService.getCart()
        // commit('SET_CART_ITEMS', response.data)
        console.log('获取购物车数据')
      } catch (err) {
        commit('SET_ERROR', '获取购物车失败')
        console.error('获取购物车失败:', err)
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 添加商品到购物车
    async addToCart({ commit }, { product, quantity = 1 }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      try {
        // 实际项目中这里会调用API
        commit('ADD_TO_CART', { product, quantity })
        console.log('添加商品到购物车')
      } catch (err) {
        commit('SET_ERROR', '添加商品失败')
        console.error('添加商品失败:', err)
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 更新购物车商品数量
    async updateQuantity({ commit }, { productId, quantity }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      try {
        // 实际项目中这里会调用API
        commit('UPDATE_QUANTITY', { productId, quantity })
        console.log('更新商品数量')
      } catch (err) {
        commit('SET_ERROR', '更新数量失败')
        console.error('更新数量失败:', err)
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 移除购物车商品
    async removeFromCart({ commit }, productId) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      try {
        // 实际项目中这里会调用API
        commit('REMOVE_FROM_CART', productId)
        console.log('移除购物车商品')
      } catch (err) {
        commit('SET_ERROR', '移除商品失败')
        console.error('移除商品失败:', err)
      } finally {
        commit('SET_LOADING', false)
      }
    },
    
    // 清空购物车
    async clearCart({ commit }) {
      commit('SET_LOADING', true)
      commit('SET_ERROR', null)
      try {
        // 实际项目中这里会调用API
        commit('CLEAR_CART')
        console.log('清空购物车')
      } catch (err) {
        commit('SET_ERROR', '清空购物车失败')
        console.error('清空购物车失败:', err)
      } finally {
        commit('SET_LOADING', false)
      }
    }
  }
})