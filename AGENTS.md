# 运动装备电商平台 - 技术文档

## 一、项目概述

本项目是一个基于 Spring Boot + Vue3 构建的全栈电商网站，专注于运动装备销售。系统提供完整的用户认证、商品管理、购物车、订单处理和支付功能。

## 二、技术栈

### 后端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| Spring Boot | 3.2.x | 后端框架 |
| MyBatis | 3.5.x | ORM 框架 |
| Spring Security | 6.2.x | 安全框架 |
| JWT | 0.12.x | 身份认证 |
| Redis | 7.2.x | 缓存服务 |
| Redisson | 3.26.x | Redis 客户端 |
| MySQL | 8.0.x | 数据库 |

### 前端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| Vite | 5.2.x | 构建工具 |
| Pinia | 2.1.x | 状态管理 |
| Element Plus | 2.6.x | UI 组件库 |
| Vue Router | 4.3.x | 路由管理 |

### 开发工具
- **CodeGraph**：代码分析工具
- **Maven**：依赖管理
- **Git**：版本控制

## 三、核心模块

### 1. 用户认证模块
- 用户注册、登录
- JWT token 生成与校验
- 权限拦截与角色管理
- 用户信息管理（个人中心）

### 2. 商品模块
- 商品 CRUD 操作
- 商品分类（三级分类体系）
- 库存管理
- Redis 缓存优化
- 商品搜索与筛选

### 3. 购物车模块
- 添加/修改/删除购物车项
- 购物车缓存（Redis）
- 库存检查
- 购物车结算

### 4. 订单模块
- 下单流程
- 订单状态流转（待支付、已支付、已发货、已完成、已取消）
- 库存扣减
- 订单详情查询
- 订单项管理

### 5. 支付模块
- 支付请求处理
- 微信支付集成
- 支付回调处理
- 订单状态更新

### 6. 地址模块
- 收货地址管理
- 默认地址设置
- 地址权限验证

### 7. 评论模块
- 商品评价
- 评分系统
- 评价权限验证（仅购买用户可评价）

### 8. 收藏模块
- 商品收藏
- 收藏列表管理

## 四、项目结构

### 后端结构
```
backend/
├── src/main/java/com/sportsequipment/
│   ├── controller/     # REST API 控制层
│   ├── service/        # 业务逻辑层
│   ├── service/impl/   # 业务实现层
│   ├── mapper/         # MyBatis 数据访问层
│   ├── entity/         # 数据库实体
│   ├── dto/            # 数据传输对象
│   ├── config/         # 配置类
│   ├── security/       # 安全相关
│   ├── exception/      # 异常处理
│   └── util/           # 工具类
├── src/main/resources/
│   ├── mapper/         # MyBatis XML 配置
│   ├── application.yml # 应用配置
│   └── schema.sql      # 数据库初始化脚本
└── pom.xml             # Maven 配置
```

### 前端结构
```
frontend/
├── src/
│   ├── views/          # 页面视图
│   ├── components/     # 可复用组件
│   ├── stores/         # Pinia 状态管理
│   ├── services/       # API 服务
│   ├── router/         # 路由配置
│   └── utils/          # 工具函数
├── index.html
├── package.json
├── vite.config.js
└── vue.config.js
```

## 五、数据库设计

### 核心表
| 表名 | 描述 |
|------|------|
| `user` | 用户信息表 |
| `product` | 商品信息表 |
| `main_category` | 商品一级分类 |
| `sub_category` | 商品二级分类 |
| `third_category` | 商品三级分类 |
| `cart` | 购物车表 |
| `cart_item` | 购物车项表 |
| `order` | 订单表 |
| `order_item` | 订单项表 |
| `address` | 收货地址表 |
| `review` | 商品评价表 |
| `favorite` | 商品收藏表 |

## 六、关键 API 接口

### 用户认证
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/users/me` | 获取当前用户信息 |

### 商品管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/products` | 获取商品列表 |
| GET | `/api/products/{id}` | 获取商品详情 |
| POST | `/api/products` | 创建商品 |
| PUT | `/api/products/{id}` | 更新商品 |
| DELETE | `/api/products/{id}` | 删除商品 |

### 购物车
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/cart` | 获取购物车 |
| POST | `/api/cart/items` | 添加商品到购物车 |
| PUT | `/api/cart/items/{id}` | 更新购物车项 |
| DELETE | `/api/cart/items/{id}` | 删除购物车项 |
| DELETE | `/api/cart` | 清空购物车 |

### 订单
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/orders` | 获取用户订单列表 |
| GET | `/api/orders/{id}` | 获取订单详情 |
| POST | `/api/orders` | 创建订单 |
| PUT | `/api/orders/{id}/status` | 更新订单状态 |
| DELETE | `/api/orders/{id}` | 删除订单 |

### 支付
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/payments` | 创建支付请求 |
| POST | `/api/payments/callback` | 支付回调 |

## 七、部署说明

### 环境要求
- JDK 21+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+

### 启动方式

**后端启动：**
```bash
cd backend
mvn spring-boot:run
```

**前端启动：**
```bash
cd frontend
npm install
npm run dev
```

### 访问地址
- 后端 API：http://localhost:8080
- 前端页面：http://localhost:5173

## 八、安全说明

1. **密码加密**：开发环境使用明文密码比较（NoOpPasswordEncoder），生产环境需使用 BCryptPasswordEncoder
2. **JWT 认证**：Token 有效期 24 小时
3. **权限控制**：基于 Spring Security 的角色权限控制
4. **CORS 配置**：配置允许前端跨域访问
5. **SQL 注入防护**：使用 MyBatis 参数化查询

## 九、开发规范

1. 代码注释清晰，遵循 JavaDoc 规范
2. 异常处理统一，使用自定义异常类
3. 日志记录完善，便于问题排查
4. 接口返回统一格式，包含状态码和消息
5. 事务管理合理，避免数据不一致

---

*文档版本：v1.0*  
*最后更新：2026-05-29*