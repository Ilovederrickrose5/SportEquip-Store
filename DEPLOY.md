# 体育用品商城系统部署说明

本文档提供了体育用品商城项目的详细部署指南，包括后端Spring Boot应用和前端Vue应用的完整部署流程。

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

1. **创建数据库**
   ```sql
   CREATE DATABASE sport_equipment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **创建数据库用户**（如果需要）
   ```sql
   CREATE USER 'root'@'localhost' IDENTIFIED BY '123456';
   GRANT ALL PRIVILEGES ON sport_equipment.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **导入初始数据**（如果有SQL脚本）
   ```bash
   mysql -u root -p sport_equipment < init_data.sql
   ```

## 后端部署

### 配置准备

后端应用的主要配置项如下（在application.properties中）：

```properties
# 数据库连接信息
spring.datasource.url=jdbc:mysql://localhost:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456

# 文件上传配置
file.upload-dir=d:/Users/30776/IdeaProjects/backend/uploads
file.max-size=10485760

# JWT配置
sportsequipment.app.jwtSecret=ThisIsASecureJWTSecretKeyThatIsLongEnoughForHS512AlgorithmAndProvidesAdequateSecurityForTheApplication
sportsequipment.app.jwtExpirationMs=86400000
```

### 部署步骤

1. **准备文件**
   - 确保已获得 `sport-equip-store-backend-v1.0.jar` 文件
   - 创建文件上传目录（确保有读写权限）：
     ```bash
     mkdir -p /path/to/uploads/avatar
     mkdir -p /path/to/uploads/product
     ```

2. **配置修改**（可选，但推荐）
   - 创建外部配置文件 `application.properties`：
     ```properties
     # 覆盖默认配置的关键参数
     spring.datasource.url=jdbc:mysql://数据库IP:3306/sport_equipment?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
     spring.datasource.username=数据库用户名
     spring.datasource.password=数据库密码
     file.upload-dir=/path/to/uploads
     server.url=http://服务器IP:8080
     ```

3. **启动服务**
   ```bash
   # 基本启动方式
   java -jar sport-equip-store-backend-v1.0.jar
   
   # 指定外部配置文件启动
   java -jar -Dspring.config.location=/path/to/application.properties sport-equip-store-backend-v1.0.jar
   
   # 指定端口启动
   java -jar -Dserver.port=8080 sport-equip-store-backend-v1.0.jar
   
   # 后台运行（Linux）
   nohup java -jar sport-equip-store-backend-v1.0.jar > backend.log 2>&1 &
   ```

4. **验证服务启动**
   - 访问 `http://服务器IP:8080`，应该能看到服务响应
   - 检查日志是否有错误信息

## 前端部署

### 准备前端文件

1. 解压前端压缩包：
   ```bash
   unzip sport-equip-store-frontend-v1.0.zip -d /path/to/frontend
   ```

### 方法一：使用Nginx部署（推荐生产环境）

1. **安装Nginx**（如果尚未安装）
   - Ubuntu/Debian: `apt-get install nginx`
   - CentOS/RHEL: `yum install nginx`
   - Windows: 从官网下载并安装

2. **配置Nginx**
   
   创建或编辑Nginx配置文件（例如 `/etc/nginx/conf.d/sport-equip.conf` 或 Windows下的 `nginx/conf/conf.d/sport-equip.conf`）：
   
   ```nginx
   server {
       listen 80;
       server_name your-domain.com; # 或服务器IP
       
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
           
           # 可选：增加超时设置
           proxy_connect_timeout 60s;
           proxy_send_timeout 60s;
           proxy_read_timeout 60s;
       }
   }
   ```

3. **检查配置并启动Nginx**
   ```bash
   # 检查配置文件语法
   nginx -t
   
   # 启动或重新加载Nginx
   nginx -s reload  # Linux
   # Windows下可以在安装目录运行nginx.exe或使用服务管理器
   ```

### 方法二：使用Node.js工具部署（开发/测试环境）

1. **安装依赖**
   ```bash
   npm install -g serve
   ```

2. **启动服务**
   ```bash
   cd /path/to/frontend/dist
   serve -s -l 80
   ```

3. **配置API代理**
   - 在这种情况下，您需要确保后端服务和前端服务在不同端口上运行
   - 或者在前端构建前修改 `.env.production` 文件中的 `VITE_API_BASE_URL` 为后端服务的实际地址

## 环境变量和配置参数

### 后端关键配置参数

| 配置项 | 描述 | 默认值 | 是否必须修改 |
|-------|------|--------|------------|
| spring.datasource.url | 数据库连接URL | jdbc:mysql://localhost:3306/sport_equipment | 是（如果数据库不在本地） |
| spring.datasource.username | 数据库用户名 | root | 是（如果使用不同的用户） |
| spring.datasource.password | 数据库密码 | 123456 | 是（出于安全考虑） |
| file.upload-dir | 文件上传目录 | d:/Users/30776/IdeaProjects/backend/uploads | 是（部署环境需要修改） |
| server.url | 服务器URL | http://localhost:8080 | 是（部署环境需要修改） |
| sportsequipment.app.jwtSecret | JWT密钥 | [长字符串] | 是（出于安全考虑） |
| server.port | 服务器端口 | 8080 | 否（如端口冲突则修改） |

### 前端环境变量

前端的环境变量在 `.env.development`（开发环境）和 `.env.production`（生产环境）文件中定义：

- `VITE_API_BASE_URL`：API基础URL，生产环境默认为 `/api`
- `VITE_APP_TITLE`：应用标题

## 安全注意事项

1. **数据库安全**
   - 修改默认密码为强密码
   - 限制数据库用户权限，仅授予必要的权限
   - 考虑使用连接池和防火墙保护数据库

2. **JWT安全**
   - 部署时务必修改默认的JWT密钥
   - 考虑调整JWT过期时间以符合安全策略

3. **文件上传安全**
   - 确保上传目录有适当的访问权限
   - 考虑限制上传文件类型和大小
   - 不要将上传目录放在Web根目录下

## 常见问题排查

1. **数据库连接失败**
   - 检查数据库服务是否运行
   - 验证数据库URL、用户名和密码是否正确
   - 确认数据库用户有足够的权限

2. **前端无法访问后端API**
   - 检查Nginx代理配置是否正确
   - 验证后端服务是否正常运行
   - 检查是否存在跨域问题

3. **文件上传失败**
   - 确认上传目录存在且有写权限
   - 检查文件大小是否超过限制
   - 查看服务器日志获取详细错误信息

## 服务管理

### 启动服务

1. **后端服务**
   ```bash
   java -jar sport-equip-store-backend-v1.0.jar
   ```

2. **Nginx服务**
   ```bash
   # Linux
   systemctl start nginx
   # 或
   nginx
   
   # Windows
   # 使用服务管理器或直接运行nginx.exe
   ```

### 停止服务

1. **后端服务**
   ```bash
   # 查找进程
   ps -ef | grep java
   # 终止进程
   kill -9 [进程ID]
   ```

2. **Nginx服务**
   ```bash
   # Linux
   systemctl stop nginx
   # 或
   nginx -s stop
   
   # Windows
   # 使用服务管理器或nginx -s stop
   ```

### 重启服务

1. **后端服务**
   ```bash
   # 停止并重新启动
   ```

2. **Nginx服务**
   ```bash
   # Linux
   systemctl restart nginx
   # 或
   nginx -s reload
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