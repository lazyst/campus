# 校园互助平台 Docker 生产部署教程

本教程详细介绍如何将校园互助平台部署到生产环境服务器。

## 目录

- [1. 架构概览](#1-架构概览)
- [2. 环境准备](#2-环境准备)
- [3. 项目结构](#3-项目结构)
- [4. 部署步骤](#4-部署步骤)
- [4.5 部署前检查清单](#45-部署前检查清单重要)
- [4.6 服务器端验证步骤](#46-服务器端验证步骤)
- [4.7 部署常见问题汇总](#47-部署常见问题汇总)
- [5. 常用命令](#5-常用命令)
- [6. 故障排查](#6-故障排查)
- [7. 监控与维护](#7-监控与维护)

---

## 1. 架构概览

```
                    ┌─────────────────────────────────────┐
                    │            服务器 (Linux)            │
                    │                                     │
    用户访问 ───────►│  ┌─────────────┐                   │
    (浏览器)         │  │   Nginx     │  端口: 80/443     │
                    │  │  (负载均衡)  │                   │
                    │  └──────┬──────┘                   │
                    │         │                          │
                    │  ┌──────┴──────┐                   │
                    │  │             │                   │
                    │  ▼             ▼                   │
                    │ ┌──────┐    ┌──────┐              │
                    │ │Backend│    │Backend│  双实例部署 │
                    │ │  -1   │    │  -2   │  端口:8080  │
                    │ └───┬───┘    └───┬───┘              │
                    │     │            │                   │
                    │ ┌───┴───┐    ┌───┴───┐             │
                    │ ▼        ▼  ▼        ▼             │
                    │ ┌────┐  ┌────┐  ┌────┐            │
                    │ │MySQL│  │Redis│  │Frontend│       │
                    │ │主库 │  │     │  │ Vue  │           │
                    │ └──┬──┘  └────┘  │ :3000 │           │
                    │    │同步           └───────┘           │
                    │ ┌──┴──┐                                  │
                    │ ▼     ▼                                  │
                    │ ┌────┐  ┌────┐  ┌────┐                │
                    │ │MySQL│  │     │  │                      │
                    │ │从库 │  │     │  │                      │
                    │ └────┘  └────┘                          │
                    └─────────────────────────────────────┘
```

### 组件说明

| 组件 | 镜像 | 用途 | 端口 |
|------|------|------|------|
| Nginx | nginx:alpine | 反向代理、负载均衡、静态资源 | 80/443 |
| Backend-1/2 | 自定义 Spring Boot | 后端 API 服务 | 8080 |
| Frontend | node:18-alpine | Vue 前端静态资源 | 3000 |
| MySQL Master | mysql:8.0 | 主数据库（写操作） | 3306 |
| MySQL Slave | mysql:8.0 | 从数据库（读操作） | 3307 |
| Redis | redis:7-alpine | 缓存、消息队列 | 6379 |

### 核心特性

- **双后端实例**：实现负载均衡和高可用
- **数据库读写分离**：MySQL 主从复制，写操作走主库，读操作走从库
- **Redis Pub/Sub**：跨容器消息实时同步（聊天功能）
- **健康检查**：每个服务都有健康检查机制
- **数据持久化**：MySQL 和 Redis 数据独立存储

---

## 2. 环境准备

### 2.1 服务器要求

| 配置项 | 最低要求 | 推荐配置 |
|--------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 2 GB | 4 GB |
| 硬盘 | 20 GB | 50 GB SSD |
| 带宽 | 1 Mbps | 5 Mbps |

### 2.2 安装 Docker 和 Docker Compose

```bash
# Ubuntu/Debian 系统
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# 添加 Docker 仓库
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# 启动 Docker
sudo systemctl start docker
sudo systemctl enable docker

# 验证安装
docker --version
docker compose version
```

### 2.3 开放防火墙端口

```bash
# Ubuntu ufw
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw allow 3306/tcp
sudo ufw allow 3307/tcp
sudo ufw reload

# 或 CentOS firewalld
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=22/tcp
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --permanent --add-port=3307/tcp
sudo firewall-cmd --reload
```

---

## 2.4 数据库读写分离配置（可选）

本节介绍如何配置 MySQL 主从复制实现数据库读写分离。

### 2.4.1 主从复制架构说明

```
┌─────────────────────────────────────────┐
│            MySQL 主从复制                │
├─────────────────────────────────────────┤
│                                         │
│   主库 (Master)        从库 (Slave)     │
│   ┌─────────┐         ┌─────────┐      │
│   │  写操作  │ ──同步──►│  读操作  │      │
│   │ INSERT  │         │ SELECT  │      │
│   │ UPDATE  │         │         │      │
│   │ DELETE  │         │         │      │
│   └─────────┘         └─────────┘      │
│       │                   │            │
│       │                   │            │
│       ▼                   ▼            │
│   端口:3306           端口:3307        │
│                                         │
└─────────────────────────────────────────┘
```

### 2.4.2 主库配置

在主库 `my.cnf` 中添加：

```ini
[mysqld]
# 主库唯一标识
server-id=1

# 启用二进制日志（用于同步）
log_bin=mysql-bin
binlog_format=ROW
binlog_expire_logs_seconds=604800
max_binlog_size=100M

# 同步配置
sync_binlog=1
binlog_checksum=NONE

# 忽略同步的数据库
binlog_ignore_db=mysql
binlog_ignore_db=information_schema
binlog_ignore_db=performance_schema

# 字符集
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
```

### 2.4.3 从库配置

在从库 `my.cnf` 中添加：

```ini
[mysqld]
# 从库唯一标识（不能与主库相同）
server-id=2

# 启用中继日志
relay_log=relay-bin
relay_log_purge=ON
relay_log_recovery=ON

# 只读模式（生产环境建议开启）
read_only=ON
super_read_only=ON

# 字符集
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
```

### 2.4.4 主从复制配置步骤

#### 步骤 1：主库创建同步账号

```sql
-- 登录主库 MySQL
mysql -uroot -p

-- 创建同步账号
CREATE USER 'repl'@'%' IDENTIFIED BY 'repl_password';
GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'repl'@'%';
FLUSH PRIVILEGES;

-- 查看主库状态
SHOW MASTER STATUS;
-- 记录 File 和 Position 的值
```

#### 步骤 2：从库连接主库

```sql
-- 登录从库 MySQL
mysql -uroot -p

-- 配置主从连接
CHANGE MASTER TO
    MASTER_HOST='master_host_ip',
    MASTER_USER='repl',
    MASTER_PASSWORD='repl_password',
    MASTER_PORT=3306,
    MASTER_LOG_FILE='mysql-bin.000001',
    MASTER_LOG_POS=123,
    GET_MASTER_PUBLIC_KEY=1;

-- 启动复制
START SLAVE;

-- 查看复制状态
SHOW SLAVE STATUS\G
```

#### 步骤 3：验证复制状态

```sql
-- 在主库执行
CREATE DATABASE test_replication;
USE test_replication;
CREATE TABLE test (id INT PRIMARY KEY, name VARCHAR(100));
INSERT INTO test VALUES (1, 'master_data');

-- 在从库验证
USE test_replication;
SELECT * FROM test;
-- 应该能看到主库插入的数据
```

### 2.4.5 读写分离环境变量

在 `.env` 文件中添加读写分离相关配置：

```bash
# ==================== 数据库读写分离配置 ====================

# 主库配置（写操作）
DB_HOST=localhost           # 主库地址
DB_PORT=3306                # 主库端口
DB_PASSWORD=your_password   # 主库密码

# 从库配置（读操作）
DB_SLAVE_HOST=localhost     # 从库地址
DB_SLAVE_PORT=3307          # 从库端口
DB_SLAVE_PASSWORD=your_password  # 从库密码

# 是否启用读写分离（true/false）
READ_WRITE_SPLIT_ENABLED=true
```

### 2.4.6 读写分离原理

系统采用 **dynamic-datasource** 组件实现读写分离：

- **写操作**：INSERT、UPDATE、DELETE 自动路由到主库
- **读操作**：SELECT、COUNT、AVG 等自动路由到从库
- **自动识别**：根据方法名自动判断读写操作

**读操作方法名模式**（走从库）：
- `get*`, `find*`, `list*`, `query*`, `search*`, `select*`
- `count*`, `sum*`, `avg*`, `min*`, `max*`
- `check*`, `verify*`, `is*`, `has*`, `exist*`

**写操作方法名模式**（走主库）：
- `save*`, `add*`, `create*`, `update*`, `delete*`
- `remove*`, `modify*`, `insert*`, `change*`

**默认策略**：不匹配以上模式的方法默认走主库（安全策略）

---

## 3. 项目结构

部署前，确保本地项目目录结构如下：

```
campus-fenbushi/
├── docker-compose.yml          # Docker Compose 主配置
├── .env                        # 环境变量（敏感信息）
├── .env.example                # 环境变量模板
│
├── backend/
│   ├── Dockerfile              # 后端 Docker 构建文件
│   ├── pom.xml                 # Maven 配置
│   └── src/                    # 源代码
│
├── frontend-user/              # 用户前端
│   ├── dist/                   # 构建产物（部署时上传）
│   └── ...
│
├── frontend-admin/             # 管理后台
│   ├── dist/
│   └── ...
│
├── nginx/
│   ├── nginx.conf              # Nginx 配置
│   ├── html/                   # 前端静态资源
│   └── ssl/                    # SSL 证书（可选）
│
├── mysql/
│   ├── init.sql                # 数据库初始化脚本
│   └── my.cnf                  # MySQL 配置
│
└── redis/
    └── redis.conf              # Redis 配置
```

### 3.1 必需文件清单

| 文件 | 说明 | 来源 |
|------|------|------|
| `.env` | 环境变量配置 | 复制 `.env.example` 并填写 |
| `docker-compose.yml` | 容器编排配置 | 项目根目录 |
| `backend/Dockerfile` | 后端镜像构建 | `backend/` 目录 |
| `frontend/dist/` | 前端构建产物 | `npm run build` 生成 |
| `nginx/nginx.conf` | Nginx 配置 | `nginx/` 目录 |
| `mysql/init.sql` | 数据库初始化 | `mysql/` 目录 |
| `redis/redis.conf` | Redis 配置 | `redis/` 目录 |

---

## 4. 部署步骤

### 4.1 本地准备

#### 步骤 1：构建前端

```bash
# 用户前端
cd frontend-user
npm install
npm run build
# 构建产物在 dist/ 目录

# 管理后台（可选）
cd ../frontend-admin
npm install
npm run build
```

#### 步骤 2：构建后端 JAR

```bash
cd backend
mvn clean package -DskipTests
# 构建产物在 target/backend-1.0.0.jar
```

#### 步骤 3：配置环境变量

```bash
# 复制模板
cp .env.example .env

# 编辑配置
vim .env

# 填写以下内容：
DB_PASSWORD=your_mysql_password              # MySQL 主库密码
DB_SLAVE_PASSWORD=your_mysql_slave_password  # MySQL 从库密码（读写分离用）
JWT_SECRET=your-super-secret-key             # JWT 密钥（越长越好）
REDIS_PASSWORD=your_redis_password           # Redis 密码
READ_WRITE_SPLIT_ENABLED=true                # 启用读写分离
```

### 4.2 上传文件到服务器

#### 方式一：使用 SSH（推荐）

```python
# 使用 Python SSHManager（Windows 推荐）
from scripts.ssh_manager import SSHManager

with SSHManager(host="192.168.100.100", username="root", password="123") as mgr:
    # 上传目录
    mgr.upload_folder("./campus-fenbushi", "/app/campus")
    # 或逐个上传
    mgr.upload_file("./docker-compose.yml", "/app/campus/docker-compose.yml")
    mgr.upload_file("./.env", "/app/campus/.env")
    mgr.upload_file("./backend/Dockerfile", "/app/campus/backend/Dockerfile")
    mgr.upload_file("./backend/target/backend-1.0.0.jar", "/app/campus/backend/backend-1.0.0.jar")
    mgr.upload_folder("./frontend/dist", "/app/campus/frontend/dist")
    mgr.upload_file("./nginx/nginx.conf", "/app/campus/nginx/nginx.conf")
    mgr.upload_file("./mysql/init.sql", "/app/campus/mysql/init.sql")
    mgr.upload_file("./redis/redis.conf", "/app/campus/redis/redis.conf")
```

#### 方式二：使用 scp 命令

```bash
# Linux/Git Bash
scp -r campus-fenbushi/* root@192.168.100.100:/app/campus/

# 逐个上传
scp docker-compose.yml root@192.168.100.100:/app/campus/
scp .env root@192.168.100.100:/app/campus/
scp backend/target/backend-1.0.0.jar root@192.168.100.100:/app/campus/backend/
```

### 4.3 服务器端部署

#### 步骤 1：登录服务器

```bash
ssh root@192.168.100.100
cd /app/campus
```

#### 步骤 2：检查文件

```bash
ls -la
ls -la backend/
ls -la frontend/
ls -la nginx/
```

#### 步骤 3：停止旧容器（如果存在）

```bash
# 查看运行中的容器
docker compose ps

# 停止所有容器
docker compose down

# 删除旧镜像（可选，清理空间）
docker image prune -a
```

#### 步骤 4：构建并启动

```bash
# 首次部署或代码更新后重新构建
docker compose up -d --build

# 仅重启服务（无代码变更）
docker compose up -d

# 后台构建，实时查看日志
docker compose up -d --build && docker compose logs -f
```

#### 步骤 5：验证部署

```bash
# 检查容器状态
docker compose ps

# 检查健康检查
curl http://localhost/api/health

# 查看后端日志
docker logs campus-backend-1 --tail 50
docker logs campus-backend-2 --tail 50

# 检查 Nginx
curl http://localhost/
```

### 4.4 预期输出

**健康检查响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "UP",
    "timestamp": "2026-02-03T10:30:00",
    "service": "campus-helping-platform"
  }
}
```

**容器状态：**
```
NAME              STATUS    HEALTH
campus-nginx      Up        Up
campus-backend-1  Up        healthy
campus-backend-2  Up        healthy
campus-frontend   Up        Up
campus-mysql      Up        healthy
campus-redis      Up        healthy
```

---

## 4.5 部署前检查清单（重要！）

> **⚠️ 关键步骤**：部署前必须检查以下项目，否则可能导致数据库表缺失、API 500错误等问题。

### 4.5.1 检查 init.sql 文件位置

**问题说明**：docker-compose.yml 中 MySQL 配置挂载路径为 `./mysql/init.sql`，但 init.sql 实际位于 `backend/sql/init.sql`。

```bash
# 本地检查 - 文件必须存在
ls -la mysql/init.sql
# 如果不存在，执行：
cp backend/sql/init.sql mysql/init.sql
```

### 4.5.2 验证环境变量文件

```bash
# 检查 .env 文件必须存在且包含必要配置
cat .env
# 必须包含：
# DB_PASSWORD=xxx
# JWT_SECRET=xxx（至少256位）
# REDIS_PASSWORD=xxx
```

### 4.5.3 检查前端构建产物

```bash
# 前端 dist 目录必须存在且有内容
ls -la frontend/dist/
# 必须包含 index.html 和 assets/ 目录
```

### 4.5.4 检查后端 JAR 文件

```bash
# backend 目录下必须有 JAR 文件
ls -la backend/backend-1.0.0.jar
# 或：ls -la backend/target/*.jar
```

---

## 4.6 服务器端验证步骤

部署完成后，必须执行以下验证步骤：

### 步骤 1：验证数据库表已创建

```bash
# 连接 MySQL 并检查表
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "USE campus_fenbushi; SHOW TABLES;"

# 期望输出（12张表）：
# +---------------------------+
# | Tables_in_campus_fenbushi |
# +---------------------------+
# | admin                     |
# | user                      |
# | board                     |
# | post                      |
# | comment                   |
# | collect                   |
# | like                      |
# | item                      |
# | item_collect              |
# | message                   |
# | notification              |
# | conversation              |
# +---------------------------+
```

### 步骤 2：验证测试数据已插入

```bash
# 检查帖子数量
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "SELECT COUNT(*) FROM post;"

# 期望输出：10
# 如果为0，说明 init.sql 未执行

# 检查板块数量
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "SELECT COUNT(*) FROM board;"
# 期望输出：4
```

### 步骤 3：如果数据库为空，手动初始化

```bash
# 方法1：使用已上传的 init.sql
docker exec -i campus-mysql mysql -uroot -p${DB_PASSWORD} < /app/campus/mysql/init.sql

# 方法2：进入容器手动执行
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD}
# 在 MySQL 中执行：
# USE campus_fenbushi;
# SOURCE /docker-entrypoint-initdb.d/init.sql;
```

### 步骤 4：验证 API 接口

```bash
# 健康检查
curl http://localhost/api/health
# 期望：{"code":200,"data":{"status":"UP"}...}

# 板块列表
curl http://localhost/api/boards
# 期望：返回4个板块数据

# 帖子列表
curl http://localhost/api/posts?page=1\&size=10
# 期望：返回帖子列表（可能有数据）
```

### 步骤 5：验证读写分离（可选）

如果配置了读写分离，验证数据源是否正确切换：

```bash
# 1. 检查后端日志，查看数据源切换情况
docker logs campus-backend-1 --tail 100 | grep -i "数据源"

# 期望输出示例：
# 切换数据源到: master  # 写操作
# 切换数据源到: slave   # 读操作

# 2. 验证主从复制状态
docker exec -it campus-mysql-slave mysql -uroot -p${DB_SLAVE_PASSWORD} -e "SHOW SLAVE STATUS\G"
# 期望：Slave_IO_Running: Yes, Slave_SQL_Running: Yes

# 3. 测试读写分离
# 在主库插入数据
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "INSERT INTO campus_fenbushi.post (title) VALUES ('读写分离测试');"

# 在从库查询数据
docker exec -it campus-mysql-slave mysql -uroot -LAVE_PASSWORD} -e "SELECT * FROM campus_fenbushi.post WHERE title='读写分离测试';"
# 期望：能查询到主库插入的数据
```

### 步骤 6：验证前端页面

```p${DB_Sbash
# 用户前端首页
curl http://localhost/ | grep "<title>"
# 期望输出：<title>校园互助平台</title>

# 管理后台
curl -I http://localhost/admin/
# 期望输出：HTTP 301 或 200
```

---

## 4.7 部署常见问题汇总

### 问题 1：前端显示 nginx 默认欢迎页面

**现象**：访问 http://服务器IP 显示 "Welcome to nginx!" 默认页面

**原因**：前端 dist 目录为空或未正确上传

**排查**：
```bash
# 1. 检查前端 dist 目录
ls -la frontend-user/dist/

# 2. 检查服务器上文件是否存在
ssh root@服务器IP "ls -la /app/campus/frontend-user/dist/"
```

**解决方案**：
```bash
# 本地构建前端
cd frontend-user && npm run build
cd ../frontend-admin && npm run build

# 上传到服务器
scp -r frontend-user/dist root@服务器IP:/app/campus/
scp -r frontend-admin/dist root@服务器IP:/app/campus/

# 重启 nginx
ssh root@服务器IP "docker restart campus-nginx"
```

### 问题 2：Docker bind mount 挂载空目录

**现象**：容器启动后挂载目录为空，后续上传的文件不生效

**原因**：Docker bind mount 特性 - 容器启动时如果源目录为空，会创建一个持久空目录

**解决方案**：
```bash
# 删除容器并重新启动（确保先上传文件）
docker compose down
# 上传前端文件...
docker compose up -d --build
```

### 问题 3：init.sql 文件位置错误

**现象**：数据库表不存在，API 返回 500 错误

**原因**：docker-compose.yml 挂载 `./mysql/init.sql`，但文件实际在 `backend/sql/init.sql`

**解决方案**：
```bash
# 本地复制文件
cp backend/sql/init.sql mysql/init.sql
```

---

## 5. 常用命令

### 5.1 服务管理

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启所有服务
docker compose restart

# 重启单个服务
docker compose restart backend-1

# 查看日志
docker compose logs -f          # 实时日志
docker compose logs --tail 100  # 最近100行

# 查看资源使用
docker stats
```

### 5.2 容器调试

```bash
# 进入容器终端
docker exec -it campus-backend-1 /bin/sh
docker exec -it campus-mysql mysql -uroot -p

# 查看容器IP
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' campus-backend-1

# 检查容器网络
docker network ls
docker network inspect campus_app-network
```

### 5.3 数据库操作

```bash
# 连接 MySQL
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD}

# 常用命令
SHOW DATABASES;
USE campus_fenbushi;
SHOW TABLES;
SELECT * FROM sys_user LIMIT 10;

# 备份数据库
docker exec campus-mysql mysqldump -uroot -p${DB_PASSWORD} campus_fenbushi > backup.sql
```

### 5.4 Redis 操作

```bash
# 连接 Redis
docker exec -it campus-redis redis-cli -a ${REDIS_PASSWORD}

# 常用命令
PING
KEYS *
GET chat:session:1:2
DEL chat:session:1:2
```

### 5.5 日志查看

```bash
# 后端日志
docker logs campus-backend-1 -f --tail 200

# Nginx 日志
tail -f /app/campus/nginx/logs/access.log
tail -f /app/campus/nginx/logs/error.log

# 查看实时请求
docker logs --follow campus-nginx 2>&1 | grep -v "192.168" | head -50
```

---

## 6. 故障排查

### 6.1 常见问题

#### 问题 1：健康检查失败

**现象**：`curl http://localhost/api/health` 返回错误

**排查**：
```bash
# 1. 检查容器是否运行
docker ps | grep backend

# 2. 查看容器日志
docker logs campus-backend-1

# 3. 进入容器检查
docker exec -it campus-backend-1 /bin/sh
curl http://localhost:8080/api/health
```

**解决方案**：
- 检查数据库连接配置
- 确保 MySQL 和 Redis 先启动
- 检查 `.env` 中的密码是否正确

#### 问题 2：前端无法访问

**现象**：浏览器访问返回 502 或空白页

**排查**：
```bash
# 1. 检查前端容器
docker exec -it campus-frontend curl localhost:3000

# 2. 检查 Nginx 配置
docker exec -it campus-nginx cat /etc/nginx/nginx.conf

# 3. 检查静态文件
ls -la /app/campus/nginx/html/
```

**解决方案**：
- 确保前端 dist 目录已上传
- 检查 Nginx 代理配置是否正确
- 重启 Nginx 容器

#### 问题 3：数据库连接失败

**现象**：`Connection to mysql:3306 refused`

**排查**：
```bash
# 1. 检查 MySQL 容器状态
docker logs campus-mysql

# 2. 检查网络连接
docker exec -it campus-backend-1 nc -zv mysql 3306

# 3. 检查密码配置
docker exec campus-backend-1 env | grep DB
```

**解决方案**：
- 等待 MySQL 完全启动（可能需要 30 秒）
- 检查 `.env` 中 `DB_PASSWORD` 是否正确
- 检查 MySQL 是否在初始化中

#### 问题 4：Redis 连接失败

**现象**：`Redis connection refused`

**排查**：
```bash
# 1. 检查 Redis 状态
docker exec -it campus-redis redis-cli -a ${REDIS_PASSWORD} ping

# 2. 检查密码
docker logs campus-redis

# 3. 检查网络
docker exec -it campus-backend-1 nc -zv redis 6379
```

**解决方案**：
- 确保 `REDIS_PASSWORD` 与 Redis 配置一致
- 检查 Redis 配置文件的 requirepass 设置

#### 问题 5：端口被占用

**现象**：`Bind for 0.0.0.0:80 failed: port is already allocated`

**排查**：
```bash
# 1. 查看端口占用
netstat -tlnp | grep 80

# 2. 查看使用端口的进程
lsof -i:80
```

**解决方案**：
- 停止占用端口的进程
- 或修改 docker-compose.yml 中的端口映射

#### 问题 6：API 返回 500 错误，表不存在

**现象**：`Table 'campus_fenbushi.post' doesn't exist` 或类似错误

**排查**：
```bash
# 1. 检查数据库表是否存在
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "USE campus_fenbushi; SHOW TABLES;"

# 2. 如果表不存在或表数量少于12张
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "SELECT COUNT(*) FROM post;"
# 期望输出：10，如果为0则说明 init.sql 未执行
```

**问题原因**：
- docker-compose.yml 挂载的 `./mysql/init.sql` 文件不存在
- init.sql 实际位于 `backend/sql/init.sql`

**解决方案**：
```bash
# 1. 本地复制 init.sql 到正确位置
cp backend/sql/init.sql mysql/init.sql

# 2. 上传到服务器
scp mysql/init.sql root@192.168.100.100:/app/campus/mysql/

# 3. 手动执行初始化脚本
docker exec -i campus-mysql mysql -uroot -p${DB_PASSWORD} < /app/campus/mysql/init.sql

# 4. 验证
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "SELECT COUNT(*) FROM post;"
# 应该输出：10
```

### 6.2 快速排查流程

```bash
# 1. 检查所有容器状态
docker compose ps

# 2. 查看错误日志
docker compose logs --tail=50 | grep -i error

# 3. 检查资源使用
docker stats

# 4. 检查磁盘空间
df -h

# 5. 检查内存
free -m
```

---

## 7. 监控与维护

### 7.1 资源监控

```bash
# 实时资源使用
docker stats

# 检查磁盘使用
df -h
docker system df

# 清理资源
docker system prune -af
```

### 7.2 日志轮转

```bash
# 配置日志轮转（编辑 /etc/docker/daemon.json）
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}

# 重启 Docker
sudo systemctl restart docker
```

### 7.3 备份策略

```bash
# 1. 数据库备份脚本（保存为 backup.sh）
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/app/campus/backups

mkdir -p $BACKUP_DIR
docker exec campus-mysql mysqldump -uroot -p${DB_PASSWORD} campus_fenbushi > $BACKUP_DIR/backup_$DATE.sql

# 保留最近7天备份
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete

# 2. 添加定时任务
crontab -e
# 每天凌晨2点执行备份
0 2 * * * /app/campus/backup.sh
```

### 7.4 更新部署

```bash
# 1. 本地构建新版本
cd backend
mvn clean package -DskipTests

# 2. 上传新 JAR 文件
scp target/backend-1.0.0.jar root@192.168.100.100:/app/campus/backend/

# 3. 服务器端更新
ssh root@192.168.100.100
cd /app/campus

# 滚动更新（零停机）
docker compose up -d --no-deps --build backend-1
docker compose up -d --no-deps --build backend-2

# 验证
curl http://localhost/api/health
```

### 7.5 SSL/HTTPS 配置（可选）

```bash
# 1. 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 2. 获取证书
sudo certbot --nginx -d yourdomain.com

# 3. 自动续期
sudo certbot renew --dry-run

# 4. 添加定时任务
crontab -e
# 每天凌晨自动续期
0 0 * * * /usr/bin/certbot renew --quiet
```

---

## 附录

### A. 文件权限设置

```bash
# 设置目录权限
chmod -R 755 /app/campus

# 设置日志目录可写
chmod -R 777 /app/campus/nginx/logs
```

### B. 常用端口

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | HTTP | 默认 Web 访问 |
| 443 | HTTPS | 安全 Web 访问 |
| 22 | SSH | 远程连接 |
| 3306 | MySQL | 数据库（不对外开放） |
| 6379 | Redis | 缓存（不对外开放） |

### C. 环境变量说明

| 变量 | 说明 | 示例 |
|------|------|------|
| DB_PASSWORD | MySQL 主库 root 密码 | `MySecurePass123` |
| DB_SLAVE_PASSWORD | MySQL 从库 root 密码 | `MySecurePass123` |
| JWT_SECRET | JWT 签名密钥 | 长度至少 256 位 |
| REDIS_PASSWORD | Redis 访问密码 | `RedisPass456` |
| READ_WRITE_SPLIT_ENABLED | 是否启用读写分离 | `true` / `false` |
| DB_SLAVE_HOST | 从库地址 | `localhost` 或 IP 地址 |
| DB_SLAVE_PORT | 从库端口 | `3307` |

### D. 读写分离配置参考

#### D.1 读写分离原理

系统采用 AOP 切面自动识别读写操作：

```
请求 → Controller → Service → AOP切面识别方法名 → 自动路由到对应数据源
                                                              │
                    ┌─────────────────────────┬───────────────┘
                    ▼                         ▼
              ┌─────────┐              ┌─────────┐
              │  主库    │              │  从库    │
              │ (写操作) │              │ (读操作) │
              └─────────┘              └─────────┘
```

#### D.2 方法名路由规则

| 方法名模式 | 数据源 | 示例 |
|-----------|--------|------|
| `get*`, `find*`, `list*`, `query*` | 从库 | `getUserById()`, `listPosts()` |
| `save*`, `add*`, `create*` | 主库 | `saveUser()`, `createPost()` |
| `update*`, `delete*`, `remove*` | 主库 | `updateUser()`, `deleteById()` |
| `count*`, `sum*`, `avg*` | 从库 | `countUsers()`, `sumScores()` |
| 其他方法 | 主库 | 未匹配的方法默认走主库 |

#### D.3 手动指定数据源

在特殊情况下，可以手动指定数据源：

```java
// 强制读主库（数据一致性要求高的场景）
@DS("master")
public User getUserById(Long id) {
    return userMapper.selectById(id);
}

// 强制读从库
@DS("slave")
public List<User> listUsers() {
    return userMapper.selectList(null);
}
```

### E. 参考资料

- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [Nginx 反向代理指南](https://docs.nginx.com/nginx/admin-guide/)
- [MySQL Docker 部署](https://dev.mysql.com/doc/refman/8.0/en/docker-mysql.html)
- [Redis Docker 部署](https://redis.io/docs/management/publishing/#docker)
- [Dynamic Datasource 官方文档](https://dynamic-datasource.com/)
- [MySQL 主从复制配置](https://dev.mysql.com/doc/refman/8.0/en/replication.html)

---

**文档版本**：1.1

**最后更新**：2026年2月

**更新内容**：
- 添加数据库读写分离架构概览（第1节）
- 添加 MySQL 主从复制配置说明（2.4节）
- 添加读写分离环境变量配置（4.2.3节）
- 添加读写分离验证步骤（4.6节）
- 添加读写分离故障排查（问题7、问题8）
- 添加读写分离配置参考（附录D、附录E）

**维护团队**：校园互助平台开发团队
