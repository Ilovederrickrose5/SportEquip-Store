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
| RabbitMQ | 3.x+ | 消息队列（异步解耦 + 延迟消息） |
| Spring AMQP | 3.x 同 Spring Boot | RabbitMQ 客户端（RabbitTemplate / @RabbitListener） |
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
- 用户注册、登录、主动退出登录
- **双 token 机制**：access_token（15 分钟，业务接口）+ refresh_token（7 天，无感续期）
- **JWT 吊销**：基于 Redis 黑名单（jti 索引，TTL = 剩余有效期），解决无状态 token 无法主动作废的问题
- **Refresh Token Rotation 轮换策略**：每次刷新下发新 refresh，旧 refresh 立刻拉黑并更新用户绑定，防重放
- 权限拦截与角色管理（USER / ADMIN）
- 用户信息管理（个人中心、头像上传）、密码重置后全端强制重登

### 2. 商品模块
- 商品 CRUD 操作
- 商品分类（**标准三级分类体系**：`main_category` 一级分类 → `sub_category` 二级分类 → `third_category` 三级分类，三张物理表通过外键逐级关联）
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
- **与 RabbitMQ 联动**：
  - 下单成功 → 发布 `order.created` 事件，异步清理用户/管理员订单列表缓存、清用户购物车缓存、清商品推荐缓存
  - 未支付 PENDING 订单 → 发布延迟消息（TTL 30 分钟）到 `order.delay.queue`，超时自动进 `order.cancel.queue` 执行订单取消 + 库存归还
  - 手动改状态到 CANCELLED / 删除 PENDING 订单，统一走 `OrderCancelService`，保证自动/手动两条路径逻辑一致、幂等

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

### 9. 消息队列（RabbitMQ）模块
- **生产者**：`MqEventPublisher` 统一封装，发送 `OrderCreatedEvent`（订单创建）和 `OrderPendingTimeoutEvent`（待支付超时）
- **延迟消息实现**：DLX（死信交换机）+ TTL 方案。`order.delay.queue` 带 `x-message-ttl`，过期后经 `order.dlx.exchange` 路由到 `order.cancel.queue`
- **队列拓扑**：
  - `order.exchange`（direct）
    - `order.created.queue`（routingKey=order.created）：异步缓存清理 / 通知
    - `order.delay.queue`（routingKey=order.delay，TTL=30min，DLX=order.dlx.exchange）：延迟未支付
  - `order.dlx.exchange`（direct）
    - `order.cancel.queue`（routingKey=order.cancel）：超时自动取消
- **幂等消费**：
  - 订单创建事件消费：`mq:idempotent:order-created:{orderId}`（SETNX + TTL 24h）
  - 订单取消事件/手动取消/删除统一：`mq:idempotent:order-cancel:{orderId}`
- **确认机制**：
  - 消费端 MANUAL ACK：业务成功 ack，首次异常 nack + requeue 重投 1 次；仍失败 nack + drop（死信/人工补偿），避免死循环
  - 生产端 Publisher Confirm + Returns 回调，写日志留痕
- **消息序列化**：`Jackson2JsonMessageConverter`，生产者与消费者工厂共用同一个 Converter
- **公共取消服务**：`OrderCancelService.cancelOrderAndRestoreStock` 承担「权限校验→仅 PENDING→分布式锁归还库存→状态→清缓存→幂等 SET」全链路，用户手动取消、管理员改 CANCELLED、管理员删除 PENDING、MQ 超时取消四条入口共用同一实现

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
│   │   └── mq/         # MQ 事件 DTO（OrderCreatedEvent、OrderPendingTimeoutEvent）
│   ├── config/         # 配置类（含 RabbitMQConfig）
│   ├── security/       # 安全相关（JWT、UserDetails）
│   ├── exception/      # 异常处理
│   ├── util/           # 工具类（RedisUtil、RedisLockUtil 等）
│   └── mq/             # 消息队列组件：MqEventPublisher、OrderCancelService、消费者
├── src/main/resources/
│   ├── mapper/         # MyBatis XML 映射文件
│   └── application.properties  # 应用配置（含 RabbitMQ、订单超时 TTL、幂等 TTL）
├── frontend/           # 前端 Vue3 项目
├── uploads/            # 上传文件存储目录
└── pom.xml             # Maven 配置（含 spring-boot-starter-amqp）
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
| `product` | 商品信息表 | 包含名称、价格、库存、分类、图片等，通过 `third_category_id` 外键关联三级分类 |
| `main_category` | 商品一级分类表 | 如「运动鞋」「服装」「器材」 |
| `sub_category` | 商品二级分类表 | 外键关联 `main_category.id`；如「跑步鞋」隶属于「运动鞋」 |
| `third_category` | 商品三级分类表 | 外键关联 `sub_category.id`；如「缓震款跑步鞋」隶属于「跑步鞋」 |
| `sub_categories` | 二级分类备用表 | 历史遗留空表 |
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
- `sub_category.main_category_id`：外键指向一级分类 `main_category.id`。
- `third_category.sub_category_id`：外键指向二级分类 `sub_category.id`。
- `product.third_category_id`：外键指向三级分类 `third_category.id`，商品以三级分类作为最细粒度归属。

### JWT Claims 说明
- `sub`：用户名（subject）
- `jti`：每个 JWT 的唯一编号（UUID），用于 Redis 黑名单索引
- `typ`：token 类型，取值 `access` / `refresh`，过滤器强制 refresh_token 不得访问业务接口
- `iat` / `exp`：签发时间 / 过期时间

## 六、关键 API 接口

### 用户认证
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/register-admin` | 管理员注册 |
| POST | `/api/auth/login` | 用户登录，返回 `{accessToken, refreshToken, expiresIn...}` 双 token |
| POST | `/api/auth/logout` | **主动退出登录**：将当前 access_token 的 jti 加入 Redis 黑名单，并拉黑该用户绑定的 refresh_token，实现双端立刻失效 |
| POST | `/api/auth/refresh` | **刷新令牌**：使用 refresh_token 换取新的 access_token + 新 refresh_token（Rotation 轮换） |
| POST | `/api/auth/reset-password` | 密码重置（仅管理员）；重置后自动清理该用户 refresh 绑定，全端强制重登 |
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
| GET | `/api/categories` | 获取全部分类树（一级→二级→三级嵌套结构） |
| GET | `/api/categories/main` | 获取全部一级分类列表 |
| GET | `/api/categories/sub/{mainCategoryId}` | 获取指定一级分类下的全部二级分类 |
| GET | `/api/categories/third/{subCategoryId}` | 获取指定二级分类下的全部三级分类 |
| GET | `/api/categories/main/{id}/sub` | 获取一级分类下的二级分类（别名兼容） |
| GET | `/api/categories/sub/{id}/third` | 获取二级分类下的三级分类（别名兼容） |

### 商品分类管理（管理员）
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/categories/main` | 新建一级分类 |
| PUT | `/api/categories/main/{id}` | 更新一级分类 |
| DELETE | `/api/categories/main/{id}` | 删除一级分类（若其下有二级/三级分类会连带清理） |
| POST | `/api/categories/sub` | 新建二级分类（入参含所属 mainCategoryId） |
| PUT | `/api/categories/sub/{id}` | 更新二级分类 |
| DELETE | `/api/categories/sub/{id}` | 删除二级分类（若其下有三级分类会连带清理） |
| POST | `/api/categories/third` | 新建三级分类（入参含所属 subCategoryId） |
| PUT | `/api/categories/third/{id}` | 更新三级分类 |
| DELETE | `/api/categories/third/{id}` | 删除三级分类 |

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
| 商品列表 | `product:list::all` | 60 分钟 TTL，30 分钟 maxIdle（@Cacheable + RedissonSpringCacheManager） | 缓存全部商品 |
| 商品详情 | `product:detail::{id}` | 60 分钟 TTL，30 分钟 maxIdle | 缓存单个商品 |
| 商品搜索 | `product:search:{keyword}` | 有结果 25-35 分钟随机 / 空值 4-6 分钟 | 防止缓存穿透 |
| 热门商品 | `product:hot:{limit}` | 30-60 分钟随机 | 按销量聚合的热门推荐 |
| 随机推荐 | `product:random:{limit}` | 30-60 分钟随机 | 随机商品推荐 |
| 购物车 | `cart:user:{userId}` | 20~28 小时随机 | 防止缓存雪崩 |
| 订单列表 | `order:list:user:{userId}:...` | 8-12 分钟随机 | 加速订单列表查询 |
| 分类列表/详情 | `category:list`、`category:main`、`category:sub`、`category:third` | 120 分钟 TTL，60 分钟 maxIdle（@Cacheable + RedissonSpringCacheManager） | 分类树、一级、二级、三级结果缓存 |
| **access_token 黑名单** | `auth:blacklist:{accessJti}` | TTL = access_token 剩余有效期（ms） | /logout 吊销 access_token；TTL 到期 Redis 自动清理，防止黑名单无限膨胀 |
| **refresh_token 用户绑定** | `auth:refresh:user:{userId}` | TTL = refresh_token 有效期（7 天） | 存储该用户当前唯一有效 refreshJti；用于防重放、单点登录绑定、改密一键踢全端 |
| **refresh_token 黑名单** | `auth:refresh:blacklist:{refreshJti}` | TTL = 旧 refresh 剩余有效期（ms），兜底 7 天 | Rotation 轮换时作废旧 refresh，防止 refresh 被盗反复重放 |
| **MQ 幂等（订单创建）** | `mq:idempotent:order-created:{orderId}` | 24h TTL（可配置） | 订单创建事件消费者 SETNX，防止消息重投造成重复缓存清理 |
| **MQ 幂等（订单取消）** | `mq:idempotent:order-cancel:{orderId}` | 24h TTL（可配置） | 覆盖 MQ 自动取消 / 用户手动取消 / 删除 PENDING 三条入口，防止重复归还库存 |

### 分布式锁
| 场景 | 锁 Key 示例 | 作用 |
|------|------------|------|
| 商品创建 | `lock:product:create` | 防止高并发下缓存击穿 |
| 商品更新/删除 | `lock:product:{id}` | 防止同商品并发修改 |
| 分类更新/删除 | `lock:category:main:{id}`、`lock:category:sub:{id}`、`lock:category:third:{id}` | 防止同分类并发修改；删除一级/二级时联动清理其下子分类 |
| 购物车操作 | `cart:lock:{userId}:product:{productId}` | 细粒度锁，提高并发 |
| 订单扣库存 | `lock:product:{productId}` | leaseTime=30 秒（非 watchdog 自动续期），配合双重库存检查防超卖 |
| 订单取消归还库存 | `lock:product:{productId}` | MQ 超时取消 / 手动取消 / 删除订单 共用，防止并发取消下库存超加 |
| 缓存重建（热点商品/分类） | `lock:{cacheKey}`（如 `lock:product:hot:10`） | 单实例重建缓存，防止缓存击穿 |

### 缓存一致性策略
- 更新/删除数据时，**先更新数据库，再删除 Redis 缓存**。
- 使用 `@CacheEvict`（`allEntries=true`）清除 Spring Cache 管理的商品/分类命名空间缓存。
- 使用 `redisUtil.delete()` / `redisUtil.deletePattern()` 清除自定义缓存：
  - Product 创建/更新/删除：额外清除 `product:list`、`product:detail:*`、`product:hot:*`、`product:random:*`
  - Order 创建：
    - **同步链路不再清理缓存**，通过 `order.created` 事件由 `OrderCreatedConsumer` 异步清理，降低下单接口 P99
    - 异步内容包括：`order:list:user:{userId}:*`、`order:list:admin:*`、`cart:user:{userId}`、`product:hot:*`、`product:random:*`
  - Order 状态流转非 CANCELLED：同步清 `order:list:user:*` + `order:list:admin:*`
  - Order 取消/删除：由 `OrderCancelService` 同步清用户+管理员订单缓存 + 相关商品详情缓存
- Redis 操作（缓存删除、锁获取/释放）不在 Spring 事务之内；如果数据库事务回滚，需要考虑补偿策略或接受短暂不一致。

## 八、部署说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.x+（建议 Docker：`rabbitmq:3.12-management`，需映射 5672/15672）
- Node.js 18+

### 数据库初始化
项目使用 MyBatis 作为 ORM，**不再使用 JPA 自动建表**。数据库初始化方式：
1. 手动创建数据库：`CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
2. 根据 `src/main/java/com/sportsequipment/entity/` 包下的实体类手动创建数据库表，或参考 `src/main/resources/mapper/*.xml` 中的字段定义。**主要实体类包括**：
   - `User`（用户表）
   - `Product`（商品表）
   - `MainCategory`（一级分类表）
   - `SubCategory`（二级分类表）
   - `ThirdCategory`（三级分类表）
   - `Cart`（购物车表）、`CartItem`（购物车项表）
   - `Order`（订单表）、`OrderItem`（订单项表）
   - `Address`（地址表）
   - `Review`（评价表）
   - `Favorite`（收藏表）
3. 导入初始数据：管理员账号、一/二/三级分类基础数据等。
4. JWT 相关配置（`application.properties`）：
   ```
   sportsequipment.app.jwtExpirationMs=86400000            # 兼容旧 token 默认 24h
   sportsequipment.app.accessTokenExpirationMs=900000       # 15分钟
   sportsequipment.app.refreshTokenExpirationMs=604800000   # 7天
   ```
5. RabbitMQ 相关配置（`application.properties`）：
   ```
   spring.rabbitmq.host=localhost
   spring.rabbitmq.port=5672
   spring.rabbitmq.username=guest
   spring.rabbitmq.password=guest
   spring.rabbitmq.virtual-host=/
   spring.rabbitmq.publisher-confirm-type=correlated          # 生产端 Confirm 回调
   spring.rabbitmq.publisher-returns=true                     # 生产端 Returns 回调（未路由到队列）
   spring.rabbitmq.listener.simple.acknowledge-mode=manual    # 消费端手动 ACK
   spring.rabbitmq.listener.simple.prefetch=10                # 预取 10，均衡消费
   spring.rabbitmq.listener.simple.concurrency=2              # 默认消费者线程数
   spring.rabbitmq.listener.simple.max-concurrency=8          # 最大消费者线程数
   sportsequipment.mq.order-pending-ttl-ms=1800000            # PENDING 订单超时 TTL=30min
   sportsequipment.mq.idempotent-ttl-seconds=86400            # MQ 幂等 Redis key TTL=24h
   ```

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
2. **JWT 双 token 认证**：
   - `access_token`：有效期 15 分钟（`accessTokenExpirationMs`），仅用于业务接口访问；过期后前端通过 `refresh_token` 无感续期。
   - `refresh_token`：有效期 7 天（`refreshTokenExpirationMs`），仅用于 `POST /api/auth/refresh` 换新令牌；Spring Security 过滤器强制 `typ=refresh` 的 token 不得访问业务接口，杜绝"refresh_token 当长口令"的风险。
   - 旧版单 token（24h，无 `typ` claim）向后兼容：过滤器会把缺失 `typ` 的 token 视为合法 `access_token`。
3. **JWT 吊销机制（主动退出）**：基于 **Redis + jti 黑名单**：
   - `/api/auth/logout` 将当前 access_token 的 jti 写入 `auth:blacklist:{jti}`，TTL = token 剩余有效期，过期自动清理。
   - 同时将该用户绑定的 refresh_token（`auth:refresh:user:{userId}` 里对应的 jti）也加入 `auth:refresh:blacklist:{jti}` 黑名单并删除绑定，实现 access + refresh 双端立刻失效。
   - `JwtAuthTokenFilter` 在每次校验签名通过后，先检查「是否 access 类型」→ 再检查「是否在黑名单中」，二者任一命中即清理上下文（401）。
4. **Refresh Token Rotation 轮换 + 防重放**：
   - 每次调 `/api/auth/refresh` 都返回**新的** `refresh_token`，旧 refresh 立刻拉黑（TTL = 旧 token 剩余有效期）。
   - Redis 维护 `auth:refresh:user:{userId} = 当前合法 refreshJti`，刷新时请求的 jti 必须与绑定值相等；若不一致（说明有人拿到旧 refresh 在重放），立即**删除用户全部 refresh 绑定**，强制全端重新登录（放血策略），避免令牌循环盗用。
   - 改密 / 管理员踢人等操作只需 `DEL auth:refresh:user:{userId}` 即可让该用户所有端 refresh 失效，无需遍历 token。
5. **权限控制**：基于 Spring Security 的角色权限控制，支持 `hasRole('ADMIN')` 等细粒度控制；`/api/auth/login`、`/register*`、`/refresh` 为 permitAll，其余 `anyRequest().authenticated()`。
6. **CORS 配置**：配置允许前端跨域访问，来源通过 `cors.allowed-origins`（多 origin 逗号分隔）。
7. **SQL 注入防护**：使用 MyBatis 参数化查询（`#{}`），关键检索（商品按 ngram 全文、订单按 user+status+time 复合索引、review 按 product+time 复合索引）走 MySQL 组合索引，避免 `SELECT *` 与全表扫描。
8. **文件上传**：限制上传文件大小（默认 10MB），上传目录独立配置。

## 十、开发规范

1. 代码注释清晰，遵循 JavaDoc 规范。
2. 异常处理统一，使用自定义异常类 + `GlobalExceptionHandler` 全局处理。
3. 接口返回统一格式，使用 `ApiResponse<T>` 包装。
4. 事务管理合理，写操作使用 `@Transactional`。
5. 依赖注入推荐使用构造函数注入，避免字段 `@Autowired`。
6. 关键业务逻辑（库存扣减、缓存更新）使用分布式锁保证并发安全。

---

*文档版本：v1.3*
*最后更新：2026-10-15*

### v1.2 → v1.3 变更摘要
1. **RabbitMQ 消息队列接入**：
   - 引入 `spring-boot-starter-amqp`，新增 `RabbitMQConfig`：2 台 direct 交换机（order.exchange / order.dlx.exchange）+ 3 条队列 + DLX 死信路由 + Jackson JSON 序列化 + Publisher Confirm/Returns 回调
   - 新增 `MqEventPublisher` 统一发布 `OrderCreatedEvent` / `OrderPendingTimeoutEvent`
   - 新增 `OrderCreatedConsumer`：订单创建后异步清理订单列表缓存、用户购物车缓存、热门/随机商品缓存（下单同步链路不再做清缓存，缩短响应）
   - 新增 `OrderTimeoutCancelConsumer`：通过 DLX + TTL（默认 30min）实现 PENDING 订单未支付自动取消
2. **统一订单取消领域服务 `OrderCancelService`**：
   - 聚合「权限判断 → 仅 PENDING 可取消 → 按商品维度分布式锁归还库存 → 更新订单状态 → 清订单/商品缓存 → Redis 幂等 SET」全流程
   - 被 4 条入口复用：用户手动改状态 CANCELLED、管理员改 CANCELLED、管理员删除 PENDING 订单、MQ 超时自动取消
   - 修复原 `deleteOrder(Long)` 删除 PENDING 订单未归还库存造成的库存泄漏
3. **幂等 & 可靠性兜底**：
   - 消费端手动 ACK：异常重投 1 次仍失败则 nack+drop 记 error 日志，避免死循环
   - 生产端 Confirm/Returns 回调日志，发现 broker 可达性或路由配置问题
   - RedisUtil 新增 `setIfAbsent` 原子 SETNX（Redisson `RBucket.trySet`），支撑幂等 Key
4. **配置规范补齐**：`application.properties` 新增 RabbitMQ 连接参数、consumer 并发/预取、`order-pending-ttl-ms`、`idempotent-ttl-seconds`
5. **AGENTS.md 更新**：技术栈加 RabbitMQ / Spring AMQP、新增「消息队列（RabbitMQ）」核心模块、Redis 场景表加 MQ 幂等 Key、分布式锁表加订单取消归还库存的锁、部署文档加环境要求和配置范例

### v1.1 → v1.2 变更摘要
1. **用户主动退出登录（JWT 吊销）**：新增 `POST /api/auth/logout`，实现基于 Redis + jti 的 access_token 黑名单，TTL = 剩余有效期自动清理，解决无状态 JWT 无法主动作废的问题。
2. **双 token 机制（access + refresh）**：登录同时下发 `access_token（15min）` 与 `refresh_token（7d）`，新增 `POST /api/auth/refresh` 使用 Rotation 轮换策略无感续期；过滤器强制 `typ=refresh` 不得访问业务接口；刷新时重放检测（jti 与用户绑定不一致即全端作废旧 refresh）。
3. **改密/踢人一键生效**：`reset-password` 改密后通过删除 `auth:refresh:user:{userId}` 绑定立刻让所有设备失效。
4. **Redis Key 与 TTL 规范补齐**：auth 模块 3 组黑名单/绑定 Key（`auth:blacklist:*`、`auth:refresh:user:*`、`auth:refresh:blacklist:*`）、product/category/order 等各模块 TTL/随机过期/空值 TTL 与项目约束规范统一。
5. **部署文档补齐**：初始化数据库结构补充实体类清单 + JWT 配置说明。
6. **遗留代码清理**：删除不再使用的 `MainCategoryMapper/SubCategoryMapper/ThirdCategoryMapper` 及对应 MyBatis XML，`Product.java` 清理冗余三级分类关联对象字段（分类代码层面对外仍保持标准三张分类实体 API 契约，数据库表保留）。
