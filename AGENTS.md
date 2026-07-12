# 运动装备电商平台 - 技术文档

## 一、项目概述

本项目是一个基于 Spring Boot + Vue3 构建的全栈电商网站，专注于运动装备销售。系统提供完整的用户认证、商品管理、购物车、订单处理、评价、收藏、地址管理等功能。

## 二、技术栈

### 后端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| Spring Boot | 3.5.14 | 后端框架 |
| MyBatis | 3.0.3 (mybatis-spring-boot-starter) | ORM 框架 |
| Spring Security | 6.x | 安全框架 |
| JWT | 0.11.5 (jjwt) | 身份认证 |
| Redis | 7.x+ | 缓存服务 |
| Redisson | 3.26.0 | Redis 客户端 / 分布式锁 |
| MySQL | 8.0+ | 数据库 |
| Java | 17 | JDK 版本 |
| Maven | 3.6+ | 构建工具 |

### 前端技术
| 技术 | 版本 | 描述 |
|------|------|------|
| Vue | 3.5.22 | 前端框架 |
| Vite | 7.1.7 | 构建工具 |
| Pinia | 3.0.4 | 状态管理 |
| Vuex | 4.0.2 | 状态管理（历史引入，当前以 Pinia 为主） |
| Element Plus | 2.11.5 | UI 组件库 |
| Vue Router | 4.6.3 | 路由管理 |
| Axios | 1.12.2 | HTTP 客户端 |

### 开发工具
- **CodeGraph**：代码分析工具
- **Maven**：依赖管理
- **Git**：版本控制

## 三、核心模块

### 1. 用户认证模块
- 用户注册、登录
- JWT token 生成与校验
- 权限拦截与角色管理（USER / ADMIN）
- 用户信息管理（个人中心、头像上传）

### 2. 商品模块
- 商品 CRUD 操作
- 商品分类（三级分类体系：main_category / sub_category / third_category）
- 库存管理
- Redis 缓存优化
- 商品搜索与筛选（支持模糊查询）
- 随机商品推荐

### 3. 购物车模块
- 添加/修改/删除购物车项
- 购物车缓存（Redis，20~28 小时随机过期）
- 库存检查
- 细粒度分布式锁（用户 + 商品 ID）

### 4. 订单模块
- 下单流程
- 订单状态流转（PENDING / PAID / SHIPPED / DELIVERED / COMPLETED / CANCELLED）
- 库存扣减（分布式锁防止超卖）
- 订单列表分页查询与状态筛选
- 订单详情查询
- 订单项管理

### 5. 地址模块
- 收货地址管理
- 默认地址设置
- 地址权限验证

### 6. 评论模块
- 商品评价
- 评分系统（1~5 分）
- 评价权限验证（仅购买用户可评价）

### 7. 收藏模块
- 商品收藏
- 收藏列表管理

### 8. 文件上传模块
- 头像上传
- 商品图片上传
- 上传文件大小限制（默认 10MB）

## 四、项目结构

### 后端结构
```
backend/
├── src/main/java/com/sportsequipment/
│   ├── controller/     # REST API 控制层
│   ├── service/        # 业务逻辑接口层
│   ├── service/impl/   # 业务逻辑实现层
│   ├── mapper/         # MyBatis Mapper 接口层
│   ├── entity/         # 数据库实体
│   ├── dto/            # 数据传输对象
│   ├── config/         # 配置类
│   ├── security/       # 安全相关（JWT、UserDetails）
│   ├── exception/      # 异常处理
│   └── util/           # 工具类（RedisUtil、RedisLockUtil 等）
├── src/main/resources/
│   ├── mapper/         # MyBatis XML 映射文件
│   └── application.properties  # 应用配置
├── frontend/           # 前端 Vue3 项目
├── uploads/            # 上传文件存储目录
└── pom.xml             # Maven 配置
```

### 前端结构
```
frontend/
├── src/
│   ├── views/          # 页面视图
│   ├── components/     # 可复用组件
│   ├── store/          # Pinia 状态管理
│   ├── services/       # API 服务
│   ├── router/         # 路由配置
│   ├── utils/          # 工具函数
│   ├── assets/         # 静态资源（CSS/图片）
│   └── plugins/        # 插件（auth 等）
├── index.html
├── package.json
├── vite.config.js
└── .env.development / .env.production
```

## 五、数据库设计

### 核心表
数据库名：`sport_equipment`，字符集：`utf8mb4`。

| 表名 | 描述 | 备注 |
|------|------|------|
| `user` | 用户信息表 | 包含用户名、密码、邮箱、角色、头像等 |
| `product` | 商品信息表 | 包含名称、价格、库存、分类、图片等 |
| `main_category` | 商品一级分类 | |
| `sub_category` | 商品二级分类 | 外键关联 main_category |
| `third_category` | 商品三级分类 | 外键关联 sub_category |
| `sub_categories` | 二级分类备用表 | 历史遗留表，当前业务逻辑使用 sub_category |
| `cart` | 购物车表 | 一个用户对应一个购物车 |
| `cart_item` | 购物车项表 | 记录购物车中的商品及数量 |
| `order` | 订单表 | 包含用户、总金额、状态、收货地址等 |
| `order_item` | 订单项表 | 记录订单中的商品及数量 |
| `address` | 收货地址表 | 用户收货地址管理 |
| `review` | 商品评价表 | 用户对商品的评价与评分 |
| `favorite` | 商品收藏表 | 用户收藏的商品 |

### 关键字段说明
- `user.role`：角色字段，取值为 `USER` 或 `ADMIN`。
- `product.status`：商品状态在代码中以字符串形式维护（当前未使用独立状态枚举）。
- `order.status`：订单状态字符串，取值为 `PENDING`、`PAID`、`SHIPPED`、`DELIVERED`、`COMPLETED`、`CANCELLED`。

## 六、关键 API 接口

### 用户认证
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/register-admin` | 管理员注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/reset-password` | 密码重置（仅管理员） |
| GET | `/api/users/me` | 获取当前用户信息 |

### 用户管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/users` | 获取所有用户（管理员） |
| GET | `/api/users/{id}` | 获取指定用户 |
| PUT | `/api/users/{id}` | 更新用户信息 |
| DELETE | `/api/users/{id}` | 删除用户 |
| PUT | `/api/users/{id}/role` | 修改用户角色 |
| POST | `/api/users/change-password` | 修改当前用户密码 |

### 商品管理
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/products` | 获取商品列表 |
| GET | `/api/products/{id}` | 获取商品详情 |
| GET | `/api/products/search` | 商品搜索（模糊查询） |
| GET | `/api/products/random` | 随机推荐商品 |
| POST | `/api/products` | 创建商品（管理员） |
| PUT | `/api/products/{id}` | 更新商品（管理员） |
| DELETE | `/api/products/{id}` | 删除商品（管理员） |

### 商品分类
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/categories` | 获取全部分类树 |
| GET | `/api/categories/main` | 获取一级分类 |
| GET | `/api/categories/sub/{mainCategoryId}` | 获取二级分类 |
| GET | `/api/categories/third/{subCategoryId}` | 获取三级分类 |

### 购物车
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/cart` | 获取当前用户购物车 |
| POST | `/api/cart/items` | 添加商品到购物车 |
| PUT | `/api/cart/items/{id}` | 更新购物车项数量 |
| DELETE | `/api/cart/items/{id}` | 删除购物车项 |
| DELETE | `/api/cart` | 清空购物车 |

### 订单
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/orders` | 获取当前用户订单（分页，支持状态筛选） |
| GET | `/api/orders/list` | 获取当前用户所有订单（不分页） |
| GET | `/api/orders/{id}` | 获取订单详情 |
| POST | `/api/orders` | 创建订单 |
| PUT | `/api/orders/{id}/status` | 更新订单状态 |
| DELETE | `/api/orders/{id}` | 删除订单（仅 PENDING 状态） |
| GET | `/api/orders/all` | 管理员获取所有订单（分页） |
| GET | `/api/orders/all/list` | 管理员获取所有订单（不分页） |

### 收藏
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/favorites/{productId}` | 添加收藏 |
| DELETE | `/api/favorites/{productId}` | 取消收藏 |
| GET | `/api/favorites` | 获取当前用户收藏列表 |

### 评价
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/reviews` | 提交评价 |
| GET | `/api/reviews/product/{productId}` | 获取商品评价列表 |

### 地址
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/addresses` | 获取当前用户地址 |
| POST | `/api/addresses` | 添加地址 |
| PUT | `/api/addresses/{id}` | 更新地址 |
| DELETE | `/api/addresses/{id}` | 删除地址 |
| PUT | `/api/addresses/{id}/default` | 设置默认地址 |

### 文件上传
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/upload/avatar` | 上传头像 |
| POST | `/api/upload/product` | 上传商品图片 |
| GET | `/api/upload/{filename}` | 获取上传文件 |

## 七、缓存与分布式锁设计

### Redis 使用场景
| 模块 | 缓存 Key 示例 | 过期策略 | 作用 |
|------|--------------|---------|------|
| 商品列表 | `product:list::all` | 永不过期（@Cacheable） | 缓存全部商品 |
| 商品详情 | `product:detail::{id}` | 永不过期（@Cacheable） | 缓存单个商品 |
| 商品搜索 | `product:search:{keyword}` | 有结果 30 分钟 / 空值 5 分钟 | 防止缓存穿透 |
| 购物车 | `cart:user:{userId}` | 20~28 小时随机 | 防止缓存雪崩 |
| 订单列表 | `order:list:user:{userId}:...` | 10 分钟 | 加速订单列表查询 |
| 分类列表 | `category:list`、`category:main` 等 | 永不过期（@Cacheable） | 缓存分类数据 |

### 分布式锁
| 场景 | 锁 Key 示例 | 作用 |
|------|------------|------|
| 商品创建 | `lock:product:create` | 防止高并发下缓存击穿 |
| 商品更新/删除 | `lock:product:{id}` | 防止同商品并发修改 |
| 分类更新/删除 | `lock:category:main:{id}` 等 | 防止同分类并发修改 |
| 购物车操作 | `cart:lock:{userId}:product:{productId}` | 细粒度锁，提高并发 |
| 订单扣库存 | `lock:product:{productId}` | 防止超卖 |

### 缓存一致性策略
- 更新/删除数据时，先更新数据库，再删除 Redis 缓存。
- 使用 `@CacheEvict` 清除 Spring Cache 管理的缓存。
- 使用 `redisUtil.delete()` / `redisUtil.deletePattern()` 清除自定义缓存。

## 八、部署说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+

### 数据库初始化
项目使用 MyBatis 作为 ORM，**不再使用 JPA 自动建表**。数据库初始化方式：
1. 手动创建数据库：`CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. 根据 `entity` 包中的实体类或 `resources/mapper/*.xml` 中的表结构手动创建表。
3. 导入初始数据（管理员账号、商品分类等）。

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

## 九、安全说明

1. **密码加密**：当前开发环境使用自定义 `PasswordEncoder` 进行明文密码比较（方便测试）。生产环境必须替换为 `BCryptPasswordEncoder`。
2. **JWT 认证**：Token 有效期 24 小时。
3. **权限控制**：基于 Spring Security 的角色权限控制，支持 `hasRole('ADMIN')` 等细粒度控制。
4. **CORS 配置**：配置允许前端跨域访问，来源通过 `cors.allowed-origins` 配置。
5. **SQL 注入防护**：使用 MyBatis 参数化查询（`#{}`）。
6. **文件上传**：限制上传文件大小（默认 10MB），上传目录独立配置。

## 十、开发规范

1. 代码注释清晰，遵循 JavaDoc 规范。
2. 异常处理统一，使用自定义异常类 + `GlobalExceptionHandler` 全局处理。
3. 接口返回统一格式，使用 `ApiResponse<T>` 包装。
4. 事务管理合理，写操作使用 `@Transactional`。
5. 依赖注入推荐使用构造函数注入，避免字段 `@Autowired`。
6. 关键业务逻辑（库存扣减、缓存更新）使用分布式锁保证并发安全。

---

*文档版本：v1.1*  
*最后更新：2026-07-05*
