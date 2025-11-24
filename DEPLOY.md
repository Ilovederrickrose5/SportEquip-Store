# 体育用品商城系统部署说明

## 文档说明

本文档提供了体育用品商城项目的详细部署指南，包括从环境准备到系统上线的完整流程。按照本文档的步骤操作，即使没有数据库文件，您也能顺利部署并运行整个系统。

## 目录

1. [项目架构概述](#项目架构概述)
2. [环境要求](#环境要求)
3. [数据库配置](#数据库配置)
4. [后端部署](#后端部署)
5. [前端部署](#前端部署)
6. [环境变量和配置参数](#环境变量和配置参数)
7. [服务验证](#服务验证)
8. [安全注意事项](#安全注意事项)
9. [常见问题排查](#常见问题排查)
10. [服务管理](#服务管理)
11. [版本信息](#版本信息)

## 项目架构概述

- **前端**：Vue 3 + Vite + Vue Router + Pinia/Vuex + Element Plus
- **后端**：Spring Boot 3.5.6 + Spring Data JPA + MySQL + Spring Security + JWT
- **数据库**：MySQL 8.0

## 环境要求

### 后端环境
- JDK 17 或更高版本（项目使用Java 17编译）
- MySQL 5.7 或更高版本（推荐MySQL 8.0）
- 足够的磁盘空间用于文件上传（默认上传目录需要读写权限）

### 前端环境
- **Nginx**（推荐的生产环境Web服务器）
- Node.js 14+（可选，用于重新构建前端）
- npm 或 yarn（可选，用于安装前端依赖）

## 数据库配置

> **重要说明**：项目代码中不包含数据库文件，您需要按照以下步骤初始化数据库。

### 第1步：创建数据库

```sql
-- 创建数据库，使用utf8mb4字符集以支持emoji表情等特殊字符
CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换到创建的数据库
USE sport_equipment;
```

### 第2步：创建数据库用户并授权

```sql
-- 创建数据库用户（如果需要）
CREATE USER 'root'@'localhost' IDENTIFIED BY '123456';

-- 授予该用户访问sport_equipment数据库的所有权限
GRANT ALL PRIVILEGES ON sport_equipment.* TO 'root'@'localhost';

-- 刷新权限使其生效
FLUSH PRIVILEGES;
```

### 第3步：初始化数据库表结构

由于项目使用JPA，您有三种选择来创建数据库表：

#### 选项A：使用JPA自动建表（推荐开发环境）

1. 修改`application.properties`文件中的JPA配置：
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

2. 启动Spring Boot应用，Hibernate将根据实体类自动创建表结构

#### 选项B：手动创建表结构（推荐生产环境）

项目包含以下实体类，您可以根据这些类手动创建表结构：
- User（用户表）
- Product（商品表）
- Order（订单表）
- OrderItem（订单项表）
- Cart（购物车表）
- CartItem（购物车项表）
- Address（地址表）
- Review（评价表）
- Favorite（收藏表）
- MainCategory（一级分类表）
- SubCategory（二级分类表）
- ThirdCategory（三级分类表）

#### 选项C：导入SQL文件（推荐新手用户）

项目仓库中已提供完整的数据库初始化SQL文件，您可以直接导入：

1. 确保您已下载项目仓库，其中包含`sport_equipment.sql`文件
2. 使用MySQL客户端或命令行导入SQL文件：

```bash
# Windows命令行
mysql -u root -p sport_equipment < sport_equipment.sql

# Linux命令行
mysql -u root -p sport_equipment < sport_equipment.sql
```

此SQL文件包含所有表结构、外键关系以及初始数据，包括管理员账号和商品分类数据。

### 第4步：添加初始数据（如果不使用SQL文件导入）

> **注意**：如果您使用了选项C（SQL文件导入），这一步可以跳过，因为SQL文件中已经包含了所有必要的初始数据。

为了让系统正常运行，您需要添加一些基本的初始数据：

```sql
-- 添加管理员账号（密码为123456的加密值）
INSERT INTO user (id, username, password, email, phone, role, created_at, updated_at, is_enabled) 
VALUES (1, 'admin', '$2a$10$L1JQl1z4fE8p2sXQz5T0/OzK8LJm5O0h1z5O8LJm5O0h1z5O8LJm5O', 'admin@example.com', '13800138000', 'ADMIN', NOW(), NOW(), true);

-- 添加一些商品分类数据
INSERT INTO main_category (id, name, description) VALUES (1, '运动服装', '各类运动服装');
INSERT INTO main_category (id, name, description) VALUES (2, '运动装备', '各类运动装备');

-- 后续可以根据系统需求添加更多初始数据
```

## 后端部署

### 配置准备

后端应用的主要配置项如下（在application.properties中）：

```properties
# 数据库连接信息
spring.datasource.url=jdbc:mysql://localhost:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA配置
spring.jpa.hibernate.ddl-auto=none  # 生产环境建议设置为none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# 文件上传配置
file.upload-dir=d:/Users/30776/IdeaProjects/backend/uploads  # 修改为您的实际路径
file.max-size=10485760  # 最大上传文件大小，单位字节（10MB）

# 服务器配置
server.port=8080
server.servlet.context-path=/
server.url=http://localhost:8080

# JWT配置
sportsequipment.app.jwtSecret=ThisIsASecureJWTSecretKeyThatIsLongEnoughForHS512AlgorithmAndProvidesAdequateSecurityForTheApplication
sportsequipment.app.jwtExpirationMs=86400000  # 24小时

# 日志配置
logging.level.root=INFO
logging.level.com.sportsequipment=DEBUG
```

### 部署步骤

#### 第1步：准备文件

- 确保已获得 `sport-equip-store-backend-v1.0.jar` 文件
- 创建文件上传目录（确保有读写权限）：
  ```bash
  # Windows
  mkdir -p D:\uploads\avatar
  mkdir -p D:\uploads\product
  
  # Linux
  mkdir -p /path/to/uploads/avatar
  mkdir -p /path/to/uploads/product
  ```

#### 第2步：配置修改

创建外部配置文件 `application.properties`，覆盖默认配置的关键参数：

```properties
# 数据库连接信息
spring.datasource.url=jdbc:mysql://数据库IP:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=数据库用户名
spring.datasource.password=数据库密码

# 文件上传目录
file.upload-dir=/path/to/uploads  # 修改为您的实际路径

# 服务器URL
server.url=http://服务器IP:8080

# 生产环境安全配置
sportsequipment.app.jwtSecret=您的新JWT密钥（请确保足够复杂）
```

#### 第3步：启动服务

根据您的操作系统选择不同的启动方式：

```bash
# Windows：基本启动方式
java -jar sport-equip-store-backend-v1.0.jar

# Windows：指定外部配置文件启动
java -jar -Dspring.config.location=D:\config\application.properties sport-equip-store-backend-v1.0.jar

# Linux：基本启动方式
java -jar sport-equip-store-backend-v1.0.jar

# Linux：指定外部配置文件启动
java -jar -Dspring.config.location=/path/to/application.properties sport-equip-store-backend-v1.0.jar

# Linux：指定端口启动
java -jar -Dserver.port=8080 sport-equip-store-backend-v1.0.jar

# Linux：后台运行
nohup java -jar sport-equip-store-backend-v1.0.jar > backend.log 2>&1 &
```

## 前端部署

### 准备前端文件

1. 解压前端压缩包：
   ```bash
   # Windows
   unzip sport-equip-store-frontend-v1.0.zip -d D:\frontend
   
   # Linux
   unzip sport-equip-store-frontend-v1.0.zip -d /path/to/frontend
   ```

### 方法一：使用Nginx部署（推荐生产环境）

#### 第1步：安装Nginx

- **Ubuntu/Debian**: 
  ```bash
  sudo apt-get update
  sudo apt-get install nginx
  ```
- **CentOS/RHEL**: 
  ```bash
  sudo yum install epel-release
  sudo yum install nginx
  ```
- **Windows**: 从Nginx官网下载并安装

#### 第2步：配置Nginx

创建或编辑Nginx配置文件：

**Linux**: `/etc/nginx/conf.d/sport-equip.conf`
**Windows**: `nginx/conf/conf.d/sport-equip.conf`（如果不存在请创建conf.d目录）

```nginx
server {
    listen 80;
    server_name your-domain.com; # 或使用服务器IP地址
    
    # 前端静态资源 - Vue应用的dist目录
    location / {
        root /path/to/frontend/dist; # 指向解压后的dist目录
        index index.html;
        # 重要：支持Vue Router的HTML5 History模式
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理 - 解决跨域问题
    location /api {
        proxy_pass http://localhost:8080; # 指向后端服务
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 增加超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 静态文件缓存配置
    location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
        root /path/to/frontend/dist;
        expires 30d;
        add_header Cache-Control "public, max-age=2592000";
    }
}
```

#### 第3步：检查配置并启动Nginx

```bash
# 检查配置文件语法
nginx -t

# 启动或重新加载Nginx
# Linux
nginx -s reload

# Windows
# 在Nginx安装目录下运行：
nginx.exe -s reload
```

### 方法二：使用Node.js工具部署（开发/测试环境）

#### 第1步：安装依赖

```bash
npm install -g serve
```

#### 第2步：启动服务

```bash
cd /path/to/frontend/dist
serve -s -l 80
```

#### 第3步：配置API连接

编辑前端的 `.env.production` 文件，修改API基础URL：

```javascript
# API基础URL - 修改为您的后端服务地址
VITE_API_BASE_URL=http://localhost:8080
```

## 环境变量和配置参数

### 后端关键配置参数

| 配置项 | 描述 | 默认值 | 是否必须修改 |
|-------|------|--------|------------|
| spring.datasource.url | 数据库连接URL | jdbc:mysql://localhost:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true | 是（如果数据库不在本地） |
| spring.datasource.username | 数据库用户名 | root | 是（如果使用不同的用户） |
| spring.datasource.password | 数据库密码 | 123456 | 是（出于安全考虑） |
| spring.jpa.hibernate.ddl-auto | JPA表创建策略 | none | 否（开发环境可设为update） |
| file.upload-dir | 文件上传目录 | d:/Users/30776/IdeaProjects/backend/uploads | 是（部署环境需要修改） |
| file.max-size | 最大上传文件大小 | 10485760 (10MB) | 否（如需调整则修改） |
| server.url | 服务器URL | http://localhost:8080 | 是（部署环境需要修改） |
| server.port | 服务器端口 | 8080 | 否（如端口冲突则修改） |
| sportsequipment.app.jwtSecret | JWT密钥 | [长字符串] | 是（出于安全考虑） |
| sportsequipment.app.jwtExpirationMs | JWT过期时间 | 86400000 (24小时) | 否（如需调整则修改） |

### 前端环境变量

前端的环境变量在 `.env.development`（开发环境）和 `.env.production`（生产环境）文件中定义：

- `VITE_API_BASE_URL`：API基础URL，生产环境默认为 `/api`
- `VITE_APP_TITLE`：应用标题

## 服务验证

部署完成后，请按以下步骤验证系统是否正常运行：

### 第1步：验证后端服务

```bash
# 使用curl命令测试后端API（需要后端服务已启动）
curl -i http://服务器IP:8080/api/products

# 或直接在浏览器中访问
# http://服务器IP:8080
```

### 第2步：验证前端服务

在浏览器中访问：
```
http://服务器IP/  # 如果使用Nginx部署
# 或
http://服务器IP:80  # 如果使用serve工具部署
```

### 第3步：验证功能

1. 尝试注册新用户
2. 尝试登录系统（可用初始管理员账号：admin/123456）
3. 尝试浏览商品列表
4. 尝试上传头像或商品图片（测试文件上传功能）

## 安全注意事项

### 1. 数据库安全

- 修改默认密码为强密码（包含字母、数字和特殊字符）
- 限制数据库用户权限，仅授予必要的权限
- 考虑使用连接池和防火墙保护数据库
- 定期备份数据库

### 2. JWT安全

- 部署时务必修改默认的JWT密钥，使用至少32位的随机字符串
- 考虑调整JWT过期时间以符合安全策略
- 生产环境中使用HTTPS传输JWT令牌

### 3. 文件上传安全

- 确保上传目录有适当的访问权限（Linux下建议755）
- 考虑限制上传文件类型和大小
- 不要将上传目录放在Web根目录下
- 实施文件重命名策略，避免文件名冲突和路径遍历攻击

### 4. 生产环境安全配置

- 关闭开发环境特有的功能（如调试模式、热重载等）
- 设置适当的错误页面，避免敏感信息泄露
- 考虑使用HTTPS加密传输

## 常见问题排查

### 1. 数据库连接失败

- **检查MySQL服务状态**：
  ```bash
  # Linux
  systemctl status mysql
  
  # Windows
  services.msc  # 查看MySQL服务状态
  ```
- **验证连接参数**：确保数据库URL、用户名和密码正确
- **检查防火墙**：确保数据库端口（默认为3306）未被防火墙阻止
- **检查权限**：确保数据库用户有足够的权限访问指定的数据库

### 2. 后端服务启动失败

- **检查端口占用**：
  ```bash
  # Linux
  netstat -tuln | grep 8080
  
  # Windows
  netstat -ano | findstr :8080
  ```
- **查看错误日志**：Spring Boot默认会在控制台输出错误信息
- **检查文件上传目录**：确保目录存在且有读写权限

### 3. 前端无法访问后端API

- **检查后端服务状态**：确保后端服务正在运行
- **验证Nginx代理配置**：检查`proxy_pass`是否正确指向后端服务
- **检查CORS配置**：如果直接访问API，确保后端已配置CORS
- **检查API路径**：确保前端请求的API路径正确

### 4. 文件上传失败

- **检查上传目录权限**：
  ```bash
  # Linux
  ls -la /path/to/uploads
  
  # Windows
  icacls D:\uploads  # 查看权限
  ```
- **检查文件大小限制**：确保上传的文件大小未超过`file.max-size`设置
- **查看错误日志**：分析服务器日志获取详细错误信息

### 5. 用户认证失败

- **检查JWT配置**：确保JWT密钥在前后端一致
- **检查用户权限**：确保用户具有访问特定资源的权限
- **检查数据库连接**：确保用户信息能正确从数据库读取

## 服务管理

### 启动服务

#### 后端服务

```bash
# Windows
java -jar sport-equip-store-backend-v1.0.jar

# Linux
java -jar sport-equip-store-backend-v1.0.jar

# Linux后台运行
nohup java -jar sport-equip-store-backend-v1.0.jar > backend.log 2>&1 &
```

#### Nginx服务

```bash
# Linux
# 方式一：使用systemctl
systemctl start nginx

# 方式二：直接启动
nginx

# Windows
# 使用服务管理器或直接运行nginx.exe
```

### 停止服务

#### 后端服务

```bash
# Linux
# 查找进程
ps -ef | grep java
# 终止进程
kill -9 [进程ID]

# Windows
# 查找进程
netstat -ano | findstr :8080
# 终止进程
taskkill /PID [进程ID] /F
```

#### Nginx服务

```bash
# Linux
# 方式一：使用systemctl
systemctl stop nginx

# 方式二：使用Nginx命令
nginx -s stop

# Windows
# 在Nginx安装目录下运行：
nginx.exe -s stop
```

### 重启服务

#### 后端服务

```bash
# 停止当前服务，然后重新启动
```

#### Nginx服务

```bash
# Linux
# 方式一：使用systemctl
systemctl restart nginx

# 方式二：使用Nginx命令
nginx -s reload

# Windows
# 在Nginx安装目录下运行：
nginx.exe -s reload
```

### 查看服务状态

#### 后端服务

```bash
# 检查端口是否在监听
# Linux
netstat -tuln | grep 8080

# Windows
netstat -ano | findstr :8080
```

#### Nginx服务

```bash
# Linux
# 方式一：使用systemctl
systemctl status nginx

# 方式二：检查端口
netstat -tuln | grep 80

# Windows
# 检查端口
netstat -ano | findstr :80
```

## 版本信息

- 后端版本：0.0.1-SNAPSHOT（Spring Boot 3.5.6）
- 前端版本：基于Vue 3.5.22
- 数据库要求：MySQL 5.7+
- JDK要求：Java 17+

## 附录：项目技术栈

### 后端
- Spring Boot 3.5.6
- Spring Data JPA
- Spring Security
- JWT (JSON Web Token)
- MySQL Connector/J 8.0.33
- Lombok

### 前端
- Vue 3.5.22
- Vue Router 4.6.3
- Pinia 3.0.4 / Vuex 4.0.2
- Element Plus 2.11.5
- Axios 1.12.2
- Vite 7.1.7