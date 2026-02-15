#!/bin/bash

# Campus 一键启动脚本
# 功能：启动后端和前端，检测并处理端口占用

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 端口配置
BACKEND_PORT=8080
FRONTEND_USER_PORT=3000
FRONTEND_ADMIN_PORT=3001

# 项目路径 (脚本在 scripts 目录下，需要回到上一级)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_USER_DIR="$SCRIPT_DIR/frontend-user"
FRONTEND_ADMIN_DIR="$SCRIPT_DIR/frontend-admin"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Campus 一键启动脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 检测端口占用并kill
kill_port() {
    local port=$1
    local name=$2

    # Windows 兼容：使用 netstat
    if netstat -ano 2>/dev/null | grep -q ":$port "; then
        echo -e "${YELLOW}端口 $port ($name) 已被占用，正在查找进程...${NC}"

        # 获取占用端口的进程 PID
        if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
            # Windows Git Bash
            PID=$(netstat -ano 2>/dev/null | grep ":$port " | grep LISTENING | awk '{print $5}' | head -1)
        else
            # Linux
            PID=$(lsof -ti:$port 2>/dev/null || true)
        fi

        if [ -n "$PID" ]; then
            echo -e "${YELLOW}正在终止进程 PID: $PID${NC}"
            taskkill //PID $PID //F 2>/dev/null || kill -9 $PID 2>/dev/null || true
            sleep 1
            echo -e "${GREEN}端口 $port 已释放${NC}"
        fi
    else
        echo -e "${GREEN}端口 $port ($name) 可用${NC}"
    fi
}

# 检查端口是否可用
check_port() {
    local port=$1
    netstat -ano 2>/dev/null | grep -q ":$port " && return 1 || return 0
}

# 等待端口可用
wait_for_port() {
    local port=$1
    local name=$2
    local max_wait=30
    local count=0

    echo -e "${YELLOW}等待端口 $port ($name) 可用...${NC}"
    while [ $count -lt $max_wait ]; do
        if check_port $port; then
            echo -e "${GREEN}端口 $port 已就绪${NC}"
            return 0
        fi
        sleep 1
        count=$((count + 1))
    done

    echo -e "${RED}端口 $port 等待超时${NC}"
    return 1
}

# 启动后端
start_backend() {
    echo -e "\n${GREEN}[1/3] 启动后端 (端口 $BACKEND_PORT)...${NC}"

    cd "$BACKEND_DIR"

    # 检查是否有 Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}Maven 未安装${NC}"
        exit 1
    fi

    # 后台启动
    mvn spring-boot:run > backend.log 2>&1 &
    BACKEND_PID=$!

    echo -e "${GREEN}后端已启动 (PID: $BACKEND_PID)${NC}"
    echo -e "${YELLOW}查看日志: tail -f backend/backend.log${NC}"

    return 0
}

# 启动用户前端
start_frontend_user() {
    echo -e "\n${GREEN}[2/3] 启动用户前端 (端口 $FRONTEND_USER_PORT)...${NC}"

    cd "$FRONTEND_USER_DIR"

    # 检查是否有 npm
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}Node.js/npm 未安装${NC}"
        exit 1
    fi

    # 后台启动
    npm run dev > frontend-user.log 2>&1 &
    FRONTEND_USER_PID=$!

    echo -e "${GREEN}用户前端已启动 (PID: $FRONTEND_USER_PID)${NC}"
    echo -e "${YELLOW}查看日志: tail -f frontend-user/frontend-user.log${NC}"

    return 0
}

# 启动管理后台
start_frontend_admin() {
    echo -e "\n${GREEN}[3/3] 启动管理后台 (端口 $FRONTEND_ADMIN_PORT)...${NC}"

    cd "$FRONTEND_ADMIN_DIR"

    # 检查是否有 npm
    if ! command -v npm &> /dev/null; then
        echo -e "${RED}Node.js/npm 未安装${NC}"
        exit 1
    fi

    # 后台启动
    npm run dev > frontend-admin.log 2>&1 &
    FRONTEND_ADMIN_PID=$!

    echo -e "${GREEN}管理后台已启动 (PID: $FRONTEND_ADMIN_PID)${NC}"
    echo -e "${YELLOW}查看日志: tail -f frontend-admin/frontend-admin.log${NC}"

    return 0
}

# 主流程
main() {
    # 检测并处理端口占用
    echo -e "\n${GREEN}检查端口占用情况...${NC}"
    kill_port $BACKEND_PORT "后端"
    kill_port $FRONTEND_USER_PORT "用户前端"
    kill_port $FRONTEND_ADMIN_PORT "管理后台"

    # 启动后端
    start_backend

    # 等待后端启动
    echo -e "${YELLOW}等待后端服务就绪 (约30秒)...${NC}"
    sleep 15

    # 启动用户前端
    start_frontend_user

    # 启动管理后台
    start_frontend_admin

    # 完成
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}  启动完成!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo -e "后端 API: ${GREEN}http://localhost:$BACKEND_PORT${NC}"
    echo -e "Swagger: ${GREEN}http://localhost:$BACKEND_PORT/swagger-ui.html${NC}"
    echo -e "用户前端: ${GREEN}http://localhost:$FRONTEND_USER_PORT${NC}"
    echo -e "管理后台: ${GREEN}http://localhost:$FRONTEND_ADMIN_PORT${NC}"
    echo -e "\n${YELLOW}停止服务请运行: scripts/stop.sh${NC}"
    echo -e "${YELLOW}查看后端日志: tail -f backend/backend.log${NC}"
    echo -e "${YELLOW}查看用户前端日志: tail -f frontend-user/frontend-user.log${NC}"
    echo -e "${YELLOW}查看管理后台日志: tail -f frontend-admin/frontend-admin.log${NC}"
}

main "$@"
