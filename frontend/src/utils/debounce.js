/**
 * 通用防抖函数
 * 适用场景：搜索输入、窗口 resize、按钮防连点等高频触发事件
 *
 * @param {Function} fn        需要防抖的目标函数
 * @param {number}   wait      防抖等待时间（毫秒），默认 300ms
 * @param {boolean}  immediate true = 前沿触发（第一次立即执行，后续静默期内不执行）
 *                             false = 后沿触发（静默期结束后才执行，默认）
 * @returns {Function} 包装后的防抖函数，附带 .cancel() 方法用于组件销毁时取消待执行任务
 */
export function debounce(fn, wait = 300, immediate = false) {
  let timer = null

  const debounced = function (...args) {
    // 每次触发都重置计时器，实现"静默期重新计算"
    if (timer) clearTimeout(timer)

    // 前沿模式：静默期开始时立即执行一次
    if (immediate && !timer) {
      fn.apply(this, args)
    }

    timer = setTimeout(() => {
      timer = null
      // 后沿模式：静默期结束后以最后一次的参数执行
      if (!immediate) {
        fn.apply(this, args)
      }
    }, wait)
  }

  /**
   * 取消尚未执行的防抖任务
   * 典型用法：组件 beforeUnmount 时调用，避免销毁后回调访问已卸载的实例
   */
  debounced.cancel = function () {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debounced
}

export default debounce
