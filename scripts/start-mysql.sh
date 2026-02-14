#!/bin/bash
# MySQL 容器启动脚本 - 确保字符集配置正确

set -e

echo "=== 启动 MySQL 容器 ==="

# 停止并删除旧容器
docker stop campus-mysql 2>/dev/null || true
docker rm campus-mysql 2>/dev/null || true
docker stop campus-mysql-slave 2>/dev/null || true
docker rm campus-mysql-slave 2>/dev/null || true

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# 启动 MySQL Master - 强制使用 UTF-8MB4 字符集
echo "启动 MySQL Master..."
docker run -d --name campus-mysql \
  -e MYSQL_ROOT_PASSWORD=123 \
  -e MYSQL_DATABASE=campus_fenbushi \
  -p 3306:3306 \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --init-connect='SET NAMES utf8mb4' \
  --skip-character-set-client-handshake

# 等待 MySQL 启动
echo "等待 MySQL Master 启动..."
sleep 20

# 导入初始化数据
echo "导入数据到 Master..."
docker exec -i campus-mysql mysql -uroot -p123 campus_fenbushi < "$PROJECT_DIR/mysql/init.sql"

# 启动 MySQL Slave
echo "启动 MySQL Slave..."
docker run -d --name campus-mysql-slave \
  -e MYSQL_ROOT_PASSWORD=123 \
  -e MYSQL_DATABASE=campus_fenbushi \
  -p 3307:3306 \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --init-connect='SET NAMES utf8mb4' \
  --skip-character-set-client-handshake

sleep 15

# 导入数据到 Slave
echo "导入数据到 Slave..."
docker exec -i campus-mysql-slave mysql -uroot -p123 campus_fenbushi < "$PROJECT_DIR/mysql/init.sql"

echo ""
echo "=== MySQL 启动完成 ==="
echo "Master: localhost:3306"
echo "Slave:  localhost:3307"
echo "数据库: campus_fenbushi"
echo "字符集: utf8mb4"
