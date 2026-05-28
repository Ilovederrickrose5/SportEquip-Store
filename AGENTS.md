# 项目背景：SpringBoot + Vue3 全栈电商网站
## 技术栈
- 后端：Spring Boot 3.x + MyBatis + Redis + Spring Security + JWT
- 前端：Vue 3 + Vite + Pinia
- 工具：已配置 CodeGraph、MySQL、Filesystem MCP，所有代码分析基于 CodeGraph 索引结果

## 核心模块
1. 用户认证模块：注册、登录、JWT 校验、权限拦截
2. 商品模块：商品管理、分类、库存、缓存
3. 购物车模块：添加/修改/删除购物车、结算
4. 订单模块：下单流程、状态流转、扣减库存
5. 支付模块：支付请求、回调处理

## 对话规则
1. 所有代码分析优先使用 CodeGraph 工具，查询调用链、依赖关系、影响范围
2. 涉及数据库操作，使用 MySQL MCP 工具
3. 回答需结合项目实际代码，不要脱离当前项目场景进行假设
4. 所有代码分析结果需符合项目代码规范，避免使用个人经验或假设