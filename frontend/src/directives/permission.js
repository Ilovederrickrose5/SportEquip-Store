/**
 * 按钮级权限控制指令 v-permission
 * 路由守卫控制"页面级"访问，本指令控制"元素级"访问，二者互补
 *
 * 用法：
 *   v-permission="'ADMIN'"                    // 仅 ADMIN 可见
 *   v-permission="['ADMIN', 'MANAGER']"       // 多角色任一匹配即可见
 *
 * 行为：当前用户角色不在允许列表中时，元素从 DOM 中移除（比 v-show/display:none
 * 更彻底，避免通过 DevTools 手动改样式绕过）
 *
 * 角色来源：localStorage 中的 user.role（登录时由 AuthService 写入，
 * 401 登出时统一清除），与 authPlugin / 路由守卫取值口径一致
 */
import { hasRole } from '../plugins/auth'

function checkAndRemove(el, binding) {
  const value = binding.value
  // 未传值的用法视为无效指令，不做处理（开发期错误，交给开发者自查）
  if (!value || (typeof value !== 'string' && !Array.isArray(value))) {
    console.warn('[v-permission] 需要传入角色名或角色数组，如 v-permission="\'ADMIN\'"')
    return
  }
  const allowedRoles = Array.isArray(value) ? value : [value]
  const allowed = allowedRoles.some((role) => hasRole(role))
  if (!allowed && el.parentNode) {
    el.parentNode.removeChild(el)
  }
}

const permission = {
  mounted: checkAndRemove,
  // updated 阶段重新校验：若角色由后端异步刷新，列表中的按钮随之显隐。
  // 注意：元素已被 remove 后 updated 无法"复活"，本项目的角色只在登录时确定，
  // 因此仅需 mounted 移除 + updated 兜底即可
  updated: checkAndRemove
}

export default permission
