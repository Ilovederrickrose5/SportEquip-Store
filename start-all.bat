@echo off
rem 强制切换到 UTF-8 编码，解决乱码问题
chcp 65001 > nul
title 一键启动前后端

echo  Trae终端启动命令：
echo  chcp 65001
echo  .\start-all.bat

echo ========== 1/3 启动 Redis ==========
start "Redis服务" cmd /k "cd /d ""D:\Program Files\Redis"" && redis-server.exe redis.windows.conf"

echo ========== 2/3 启动后端 ==========
start "后端服务" cmd /k "cd /d ""D:\Users\30776\IdeaProjects\backend"" && mvn spring-boot:run"

echo ========== 3/3 启动前端 ==========
start "前端服务" cmd /k "cd /d ""D:\Users\30776\IdeaProjects\backend\frontend"" && npm run dev"


echo.
echo 前后端服务启动中，请等待窗口弹出...
echo 关闭对应窗口即可停止服务
pause