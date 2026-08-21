@echo off
rem 强制切换到 UTF-8 编码，解决乱码问题
chcp 65001 > nul
title 一键启动前后端

echo  Trae终端启动命令：
echo  chcp 65001
echo  .\start-all.bat

echo ========== 1/4 启动 Redis ==========
start "Redis服务" cmd /k "cd /d ""D:\Program Files\Redis"" && redis-server.exe redis.windows.conf"

echo ========== 2/4 启动 RabbitMQ ==========
docker info >nul 2>&1
if errorlevel 1 (
    echo Docker Desktop 未运行，尝试启动...
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    echo 等待 Docker 引擎就绪（30s）...
    timeout /t 30 /nobreak > nul
    docker info >nul 2>&1
    if errorlevel 1 (
        echo [警告] Docker Desktop 启动失败，请手动启动后重试
        pause
        exit /b 1
    )
)
docker start rabbitmq >nul 2>&1
if errorlevel 1 (
    echo 首次启动，正在拉取并创建 rabbitmq:3.12-management 容器...
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.12-management
)
echo 等待 RabbitMQ 就绪（15s）...
timeout /t 15 /nobreak > nul

echo ========== 3/4 启动后端 ==========
start "后端服务" cmd /k "cd /d ""D:\Users\30776\IdeaProjects\backend"" && mvn spring-boot:run"

echo ========== 4/4 启动前端 ==========
start "前端服务" cmd /k "cd /d ""D:\Users\30776\IdeaProjects\backend\frontend"" && npm run dev"


echo.
echo 前后端服务启动中，请等待窗口弹出...
echo RabbitMQ 管理面板：http://localhost:15672  （账号/密码 guest/guest）
echo 关闭对应窗口即可停止服务
pause
