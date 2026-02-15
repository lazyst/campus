#!/bin/bash

# Campus 停止脚本
# 功能：停止后端和前端服务

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 端口配置
BACKEND_PORT=8080
FRONTEND_USER_PORT=3000
FRONTEND_ADMIN_PORT=3001

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Campus 停止脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 停止占用指定端口的进程
kill_port() {
    local port=$1
    local name=$2

    if netstat -ano 2>/dev/null | grep -q ":$port "; then
        echo -e "${YELLOW}正在停止端口 $port ($name)...${NC}"

        # 获取占用端口的进程 PID
        PID=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $5}' | head -1)

        if [ -n "$PID" ]; then
            taskkill //PID $PID //F 2>/dev/null || kill -9 $PID 2>/dev/null || true
            echo -e "${GREEN}端口 $port ($name) 已停止${NC}"
        fi
    else
        echo -e "${GREEN}端口 $port ($name) 未被占用${NC}"
    fi
}

# 停止后端和前端
kill_port $BACKEND_PORT "后端"
kill_port $FRONTEND_USER_PORT "用户前端"
kill_port $FRONTEND_ADMIN_PORT "管理后台"

# 也尝试停止常见的开发服务进程
echo -e "\n${YELLOW}尝试停止常见开发服务进程...${NC}"

# 停止 Java 进程 (Spring Boot)
jps 2>/dev/null | grep "spring-boot" | awk '{print $1}' | while read pid; do
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}停止 Java 进程: $pid${NC}"
        taskkill //PID $pid //F 2>/dev/null || kill -9 $pid 2>/dev/null || true
    fi
done

# 停止 Node 进程 (Vite)
jps 2>/dev/null | grep "vite" | awk '{print $1}' | while read pid; do
    if [ -n "$pid" ]; then
        echo -e "${YELLOW}停止 Node 进程: $pid${NC}"
        taskkill //PID $pid //F 2>/dev/null || kill -9 $pid 2>/dev/null || true
    fi
done

echo -e "\n${GREEN}所有服务已停止${NC}"
