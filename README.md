# 运动装备电商项目网站

## 项目概述

这是一个完整的运动装备电商系统，包含功能完善的前后端实现。系统提供商品管理、用户认证、订单处理、购物车、收藏夹、评价系统、地址管理等核心电商功能，采用现代化技术栈开发，支持响应式设计。

## 技术栈

### 后端技术
- **框架**：Spring Boot 3.5.14
- **ORM**：MyBatis 3.0.3（mybatis-spring-boot-starter）
- **数据库**：MySQL 8.0+
- **缓存**：Redis 7.0+ + Redisson 3.26.0
- **安全认证**：Spring Security + JWT（jjwt 0.11.5）
- **构建工具**：Maven
- **Java版本**：JDK 17

### 前端技术
- **框架**：Vue 3.5.22 + Vite 7.1.7
- **路由**：Vue Router 4.6.3
- **状态管理**：Pinia 3.0.4
- **UI组件库**：Element Plus 2.11.5
- **HTTP客户端**：Axios 1.12.2
- **样式**：SCSS / CSS

## 功能特性

### 用户系统
- 用户注册、登录与JWT认证
- 用户信息管理（个人资料、地址管理、头像上传）
- 角色权限控制（普通用户、管理员）

### 商品系统
- 多级商品分类管理（一级分类、二级分类、三级分类）
- 商品列表、详情展示
- 商品搜索与筛选（支持模糊查询）
- 随机商品推荐
- 商品评价与评分系统
- 商品收藏功能

### 购物车与订单
- 购物车商品管理（添加、删除、修改数量）
- 订单创建、状态管理
- 订单历史查询与管理（支持分页和状态筛选）
- 订单详情查看

### 其他功能
- 文件上传（支持头像、商品图片）
- 响应式设计，支持移动端访问
- RESTful API设计
- 统一响应格式与全局异常处理
- Redis缓存与分布式锁（防止缓存穿透、击穿、雪崩）

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/             # Java源代码
│   │   │   └── com/sportsequipment/
│   │   │       ├── controller/   # REST API控制器
│   │   │       ├── service/      # 业务逻辑接口
│   │   │       ├── service/impl/ # 业务逻辑实现
│   │   │       ├── mapper/       # MyBatis Mapper接口
│   │   │       ├── entity/       # 数据库实体
│   │   │       ├── dto/          # 数据传输对象
│   │   │       ├── config/       # 配置类
│   │   │       ├── security/     # JWT与安全相关
│   │   │       ├── exception/    # 异常处理
│   │   │       └── util/         # 工具类
│   │   └── resources/
│   │       ├── mapper/           # MyBatis XML映射文件
│   │       └── application.properties  # 应用配置
│   └── uploads/              # 上传文件存储目录
├── frontend/                 # 前端 Vue3 项目
│   ├── src/
│   │   ├── views/            # 页面视图
│   │   ├── components/       # 可复用组件
│   │   ├── store/            # Pinia状态管理
│   │   ├── services/         # API服务
│   │   ├── router/           # 路由配置
│   │   ├── utils/            # 工具函数
│   │   └── assets/           # 静态资源
│   ├── package.json
│   └── vite.config.js
├── DEPLOY.md                 # 部署文档
├── AGENTS.md                 # 技术文档
├── SECURITY.md               # 安全策略
└── README.md                 # 项目说明文档
```

## 环境要求

### 后端环境
- JDK 17 或更高版本
- MySQL 8.0+ 或 MySQL 5.7
- Redis 7.0+
- Maven 3.6+
- 足够的磁盘空间用于文件上传

### 前端环境
- Node.js 18+（推荐）
- npm 或 yarn
- 浏览器：Chrome 90+、Firefox 88+、Safari 14+、Edge 90+

## 数据库初始化

### 第1步：创建数据库

```sql
CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 第2步：创建数据库用户并授权（可选）

```sql
CREATE USER 'root'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON sport_equipment.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### 第3步：初始化数据库表结构

项目使用 MyBatis 作为 ORM 框架，**不再使用 JPA 自动建表**。请通过以下方式之一初始化表结构：

#### 方法一：参考实体类手动建表（推荐生产环境）

根据 `src/main/java/com/sportsequipment/entity/` 包下的实体类创建对应的数据库表。主要实体类包括：
- User（用户表）
- Product（商品表）
- MainCategory（一级分类表）
- SubCategory（二级分类表）
- ThirdCategory（三级分类表）
- Cart（购物车表）
- CartItem（购物车项表）
- Order（订单表）
- OrderItem（订单项表）
- Address（地址表）
- Review（评价表）
- Favorite（收藏表）

#### 方法二：参考 MyBatis XML 文件

`src/main/resources/mapper/` 目录下的 XML 文件包含了各表的 SQL 操作，可作为建表参考。

#### 方法三：导入 SQL 文件（推荐新手用户）

如果项目仓库中提供了 `sport_equipment.sql` 文件，可直接导入：

```bash
# Windows
mysql -u root -p sport_equipment < sport_equipment.sql

# Linux/Mac
mysql -u root -p sport_equipment < sport_equipment.sql
```

### 第4步：添加初始数据

如果未使用 SQL 文件导入，需要手动添加基础数据：

```sql
-- 添加管理员账号（当前开发环境使用明文密码）
INSERT INTO user (id, username, password, email, phone, role, created_at, updated_at) 
VALUES (1, 'admin', '123456', 'admin@example.com', '13800138000', 'ADMIN', NOW(), NOW());

-- 添加商品分类数据
INSERT INTO main_category (id, name, description) VALUES (1, '运动服装', '各类运动服装');
INSERT INTO main_category (id, name, description) VALUES (2, '运动装备', '各类运动装备');
```

> **注意**：生产环境请使用 BCrypt 加密密码，并修改默认管理员账号。

## 如何运行

### 后端运行

1. 确保数据库和 Redis 已启动
2. 确保 `uploads` 目录存在且有读写权限
3. 进入项目根目录
4. 执行以下命令启动后端服务：

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

或使用 IDE 直接运行 `SportEquipmentApplication`。

5. 后端服务默认运行在 `http://localhost:8080`

### 前端运行

1. 进入 `frontend` 目录
2. 安装依赖（首次运行需要）：

```bash
npm install
```

3. 启动前端开发服务器：

```bash
npm run dev
```

4. 前端应用默认运行在 `http://localhost:5173`

## 配置说明

主要配置文件位于 `src/main/resources/application.properties`，关键配置项包括：

- **数据库配置**：数据库连接 URL、用户名、密码
- **MyBatis 配置**：Mapper 扫描路径、驼峰命名转换
- **Redis 配置**：Redis 连接信息、连接池
- **JWT 配置**：密钥和过期时间
- **文件上传配置**：上传目录和最大文件大小
- **CORS 配置**：允许跨域的前端地址
- **日志配置**：日志级别

## 注意事项

1. **数据库配置**：确保数据库连接信息正确，数据库已创建
2. **Redis 配置**：确保 Redis 服务已启动，连接信息正确
3. **文件上传目录**：确保 `uploads` 目录存在且有正确的读写权限
4. **端口占用**：默认后端使用 8080 端口，前端使用 5173 端口，请确保这些端口未被占用
5. **密码安全**：生产环境必须将自定义明文 PasswordEncoder 替换为 BCryptPasswordEncoder
6. **JWT 安全**：生产环境请修改默认的 JWT 密钥和数据库密码

## 常见问题

### 数据库连接失败
- 检查 MySQL 服务是否运行
- 验证数据库名称、用户名和密码是否正确
- 确认用户有足够的权限访问数据库

### Redis 连接失败
- 检查 Redis 服务是否运行
- 验证 `application.properties` 中的 Redis 配置是否正确

### 文件上传失败
- 检查上传目录是否存在且有读写权限
- 确认文件大小未超过配置的限制（默认 10MB）

### 前后端通信问题
- 确保后端服务正在运行
- 检查 Vite 代理配置是否正确
- 验证 API 请求路径是否正确

## 部署说明

详细的部署指南请参考项目中的 [DEPLOY.md](DEPLOY.md) 文件。

## 许可证

保留所有权利。
