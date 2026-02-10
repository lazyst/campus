# 校园互助平台 - 快速部署指南

## 服务器信息

- **服务器IP**: `192.168.100.133`
- **SSH**: `root/123`
- **部署目录**: `/app/campus`

## 访问地址

| 应用 | 地址 |
|------|------|
| 用户前端 | http://192.168.100.133/ |
| 管理后台 | http://192.168.100.133/admin/ |
| API地址 | http://192.168.100.133/api |

## 快速部署步骤

### 1. 本地构建（开发机）

```bash
# 构建后端JAR
cd backend
mvn clean package -DskipTests
# 产物: backend/target/backend-1.0.0.jar (62MB)

# 构建用户前端
cd ../frontend-user
npm install
npm run build
# 产物: frontend-user/dist/

# 构建管理前端
cd ../frontend-admin
npm install
npm run build
# 产物: frontend-admin/dist/
```

### 2. 复制初始化脚本

初始化脚本已存在于 `mysql/init.sql`（数据库初始化），无需额外复制。

### 3. 上传文件到服务器

**使用 scp**：

```bash
# 上传所有配置文件
scp -r docker-compose.yml .env nginx/ mysql/ redis/ root@192.168.100.133:/app/campus/

# 上传后端JAR
scp backend/target/backend-1.0.0.jar root@192.168.100.133:/app/campus/backend/

# 上传前端构建产物（直接复制到 nginx/html 目录）
scp -r frontend-user/dist/* root@192.168.100.133:/app/campus/nginx/html/user/
scp -r frontend-admin/dist/* root@192.168.100.133:/app/campus/nginx/html/admin/
```

### 4. 服务器端部署

```bash
ssh root@192.168.100.133

cd /app/campus

# 停止旧容器（如果有）
docker compose down

# 启动服务
docker compose up -d

# 等待启动（约60秒）
sleep 60

# 验证
docker compose ps
```

### 5. 验证部署

```bash
# 测试前端
curl http://192.168.100.133/ | grep "<title>"

# 测试管理后台
curl http://192.168.100.133/admin/ | grep "<title>"

# 测试API
curl http://192.168.100.133/api/health
```

## 常用命令

```bash
# 启动服务
docker compose up -d

# 停止服务
docker compose down

# 重启单个服务
docker restart campus-nginx
docker restart campus-backend-1
docker restart campus-backend-2

# 查看资源使用
docker stats

# 进入容器
docker exec -it campus-backend-1 /bin/sh
docker exec -it campus-mysql mysql -uroot -p123
docker exec -it campus-redis redis-cli -a 123
```

## 故障排查

### 1. Admin页面空白或显示用户前端

**症状**: 访问 `/admin` 显示用户前端页面

**解决**: 确保前端文件已上传到正确目录
```bash
ls -la /app/campus/nginx/html/admin/
```

### 2. 500 Internal Server Error

检查Nginx日志：
```bash
docker logs campus-nginx --tail 20
docker logs campus-backend-1 --tail 50
```

### 3. 400 Bad Request

通常是Host header问题，修复Nginx配置：
```bash
# nginx.conf 中使用 $http_host 而不是 $host
docker restart campus-nginx
```

### 4. API无响应

检查后端是否启动：
```bash
docker exec campus-backend-1 wget -qO- http://localhost:8080/api/health
```

### 5. WebSocket 连接失败

**症状**: 聊天功能无法使用，控制台显示 `WebSocket connection to 'ws://192.168.100.133/ws' failed`

**解决**:
```bash
# 检查 nginx WebSocket 代理配置
docker exec campus-nginx cat /etc/nginx/nginx.conf | grep -A10 "location /ws"

# 重启 nginx
docker restart campus-nginx
```

### 6. 图片无法加载

检查上传目录是否正确挂载：
```bash
docker exec campus-backend-1 ls /app/uploads/
```

## 环境变量

`.env` 文件配置（用于 Docker 环境变量）：

```env
# MySQL/Redis 配置（容器内使用服务名，外部访问使用 localhost）
DB_HOST=mysql
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=123
JWT_SECRET=campus-helping-platform-jwt-secret-key-2024-very-long-and-secure
JWT_EXPIRATION=604800000
```

## 目录结构

```
/app/campus/
├── docker-compose.yml
├── .env
├── backend/
│   └── backend-1.0.0.jar
├── nginx/
│   ├── nginx.conf       # 包含 /uploads/ 代理配置
│   ├── ssl/
│   ├── logs/
│   └── html/
│       ├── user/        # 用户前端构建产物
│       │   ├── index.html
│       │   └── assets/
│       └── admin/       # 管理后台构建产物
│           ├── index.html
│           └── assets/
├── mysql/
│   ├── init.sql
│   └── my.cnf
└── redis/
    └── redis.conf
```

## 架构说明

| 组件 | 配置 | 说明 |
|------|------|------|
| 用户前端 | nginx/html/user | 通过 Nginx 直接服务 |
| 管理后台 | nginx/html/admin | 通过 Nginx 直接服务 |
| API | /api/ | 代理到后端集群 |
| WebSocket | /ws, /ws/ | 代理到后端集群，支持 Upgrade |
| 图片上传 | /uploads/ | 代理到后端静态资源服务 |
| 数据存储 | Docker 卷 | mysql-data, redis-data, uploads-data |

## WebSocket 端点配置

```java
// 后端 WebSocketConfig.java - 支持 /ws 和 /ws/ 两种路径
registry.addEndpoint("/ws").setAllowedOrigins("*");
registry.addEndpoint("/ws").withSockJS();
registry.addEndpoint("/ws/").setAllowedOrigins("*");
registry.addEndpoint("/ws/").withSockJS();
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | 13800000000 | 123456 |
| 管理员 | admin | admin123 |

## 快速参考（完整部署流程）

```bash
# ========== 1. 本地构建 ==========
cd backend && mvn clean package -DskipTests
cd ../frontend-user && npm install && npm run build
cd ../frontend-admin && npm install && npm run build

# ========== 2. 上传文件 ==========
scp docker-compose.yml .env nginx/ mysql/ redis/ root@192.168.100.133:/app/campus/
scp backend/target/backend-1.0.0.jar root@192.168.100.133:/app/campus/backend/
scp -r frontend-user/dist/* root@192.168.100.133:/app/campus/nginx/html/user/
scp -r frontend-admin/dist/* root@192.168.100.133:/app/campus/nginx/html/admin/

# ========== 3. 服务器部署 ==========
ssh root@192.168.100.133 << 'EOF'
cd /app/campus
docker compose down
docker compose up -d
sleep 60
docker compose ps
EOF

# ========== 4. 验证 ==========
curl http://192.168.100.133/api/health
```

---

**部署完成！** 访问 http://192.168.100.133/ 查看前端页面。
