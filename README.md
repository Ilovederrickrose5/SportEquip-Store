# 运动装备电商项目网站

## 项目概述

这是一个完整的运动装备电商系统，包含功能完善的前后端实现。系统提供商品管理、用户认证、订单处理、购物车、收藏夹、评价系统等核心电商功能，采用现代化技术栈开发，支持响应式设计。

## 技术栈

### 后端技术
- **框架**：Spring Boot 3.5.6
- **ORM**：Spring Data JPA
- **数据库**：MySQL 8.0（兼容MySQL 5.7+）
- **安全认证**：Spring Security + JWT
- **构建工具**：Maven
- **Java版本**：JDK 17

### 前端技术
- **框架**：Vue 3 + Vite
- **路由**：Vue Router
- **状态管理**：Pinia
- **UI组件库**：Element Plus
- **HTTP客户端**：Axios

## 功能特性

### 用户系统
- 用户注册、登录与JWT认证
- 用户信息管理（个人资料、地址管理）
- 头像上传与管理
- 角色权限控制（普通用户、管理员）

### 商品系统
- 多级商品分类管理（一级分类、二级分类、三级分类）
- 商品列表、详情展示
- 商品搜索与筛选
- 商品评价与评分系统
- 商品收藏功能

### 购物车与订单
- 购物车商品管理（添加、删除、修改数量）
- 订单创建、支付状态管理
- 订单历史查询与管理
- 订单详情查看

### 其他功能
- 文件上传（支持头像、商品图片）
- 响应式设计，支持移动端访问
- RESTful API设计
- 数据校验与错误处理

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/             # Java源代码
│   │   └── resources/        # 配置文件
│   └── uploads/              # 上传文件存储目录
├── frontend/                 # 前端代码目录
│   ├── public/               # 静态资源
│   ├── src/                  # Vue源代码
│   └── ...
├── uploads/                  # 文件上传实际存储目录
│   ├── avatar/               # 用户头像
│   └── product/              # 商品图片
├── DEPLOY.md                 # 部署文档
└── README.md                 # 项目说明文档
```

## 环境要求

### 后端环境
- JDK 17 或更高版本
- MySQL 5.7+ 或 MySQL 8.0
- Maven 3.6+
- 足够的磁盘空间用于文件上传

### 前端环境
- Node.js 14+（如需重新构建前端）
- npm 或 yarn（如需重新构建前端）
- 浏览器：Chrome 90+、Firefox 88+、Safari 14+、Edge 90+

## 数据库初始化

由于项目代码中不包含数据库文件，您需要手动初始化数据库：

### 方法一：使用JPA自动建表（推荐用于开发环境）

1. 确保MySQL服务已启动
2. 创建数据库：
   ```sql
   CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 创建数据库用户并授权（如果需要）：
   ```sql
   CREATE USER 'root'@'localhost' IDENTIFIED BY '123456';
   GRANT ALL PRIVILEGES ON sport_equipment.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```
4. 修改`application.properties`中的JPA配置：
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

### 方法二：手动创建表结构

按照项目实体类定义创建对应的数据库表。实体类包括：User、Product、Order、OrderItem、Cart、CartItem、Address、Review、Favorite、MainCategory、SubCategory、ThirdCategory等。

## 如何运行

### 后端运行

1. 确保数据库已初始化
2. 确保`uploads`目录存在且有读写权限
3. 进入项目根目录
4. 执行以下命令启动后端服务：
   ```bash
   # Windows
   mvnw.cmd spring-boot:run
   
   # Linux/Mac
   ./mvnw spring-boot:run
   ```
   或使用IDE直接运行Spring Boot应用

5. 后端服务默认运行在 `http://localhost:8080`

### 前端运行

1. 进入`frontend`目录
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

主要配置文件位于`src/main/resources/application.properties`，关键配置项包括：

- **数据库配置**：数据库连接URL、用户名、密码
- **JWT配置**：密钥和过期时间
- **文件上传配置**：上传目录和最大文件大小
- **日志配置**：日志级别

## 注意事项

1. **数据库配置**：确保数据库连接信息正确，数据库已创建
2. **文件上传目录**：确保`uploads`目录存在且有正确的读写权限
3. **端口占用**：默认后端使用8080端口，前端使用5173端口，请确保这些端口未被占用
4. **JPA配置**：生产环境建议将`spring.jpa.hibernate.ddl-auto`设置为`none`，避免意外修改数据库结构
5. **安全配置**：生产环境请修改默认的JWT密钥和数据库密码

## 常见问题

### 数据库连接失败
- 检查MySQL服务是否运行
- 验证数据库名称、用户名和密码是否正确
- 确认用户有足够的权限访问数据库

### 文件上传失败
- 检查上传目录是否存在且有读写权限
- 确认文件大小未超过配置的限制（默认10MB）

### 前后端通信问题
- 确保后端服务正在运行
- 检查Vite代理配置是否正确
- 验证API请求路径是否正确

## 部署说明

详细的部署指南请参考项目中的`DEPLOY.md`文件。

## 许可证

保留所有权利。
