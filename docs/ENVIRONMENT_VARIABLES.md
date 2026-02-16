# 环境变量说明

## 概述

本项目使用环境变量管理不同环境的配置信息。主要配置文件位于项目根目录和各个子项目中。

## 配置文件位置

| 环境 | 后端配置文件 | 前端配置文件 |
|------|--------------|--------------|
| 开发环境 | `backend/src/main/resources/application-dev.yml` | `frontend-user/.env.development` |
| 生产环境 | `backend/src/main/resources/application.yml` | `frontend-user/.env.production` |
| 根目录 | `.env`, `.env.development`, `.env.production` | - |

## 后端环境变量

### 数据库配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `DB_HOST` | 是 | localhost | MySQL 数据库地址 (WSL Docker) |
| `DB_PORT` | 否 | 3306 | MySQL 数据库端口 |
| `DB_NAME` | 是 | campus_fenbushi | 数据库名称 |
| `DB_USERNAME` | 否 | root | 数据库用户名 |
| `DB_PASSWORD` | 是 | 123 | 数据库密码 |
| `DB_SLAVE_HOST` | 否 | localhost | 从库地址（读写分离） |
| `DB_SLAVE_PORT` | 否 | 3307 | 从库端口 |
| `DB_SLAVE_PASSWORD` | 否 | 同 DB_PASSWORD | 从库密码 |

### Redis 配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `REDIS_HOST` | 是 | localhost | Redis 服务器地址 (WSL Docker) |
| `REDIS_PORT` | 否 | 6379 | Redis 服务器端口 |
| `REDIS_PASSWORD` | 否 | 123 | Redis 访问密码 |

### 应用配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `SERVER_PORT` | 否 | 8080 | 应用服务端口 |
| `SPRING_PROFILES_ACTIVE` | 否 | dev | Spring 激活的配置文件 |

### JWT 配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `JWT_SECRET` | 是 | - | JWT 签名密钥（至少 256 位） |
| `JWT_EXPIRATION` | 否 | 604800000 | Token 过期时间（毫秒，默认 7 天） |
| `JWT_HEADER` | 否 | Authorization | Token 请求头名称 |
| `JWT_PREFIX` | 否 | Bearer | Token 前缀 |

### WebSocket 配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `WEBSOCKET_ENDPOINT` | 否 | /ws | WebSocket 端点路径 |
| `WEBSOCKET_ALLOWED_ORIGINS` | 否 | * | 允许的跨域来源 |

### 读写分离配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `READ_WRITE_SPLIT_ENABLED` | 否 | false | 是否启用读写分离 |

## 前端环境变量 (frontend-user)

### API 配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `VITE_API_BASE_URL` | 是 | http://localhost:8080 | 后端 API 基础地址 |
| `VITE_WS_URL` | 否 | ws://localhost:8080 | WebSocket 服务地址 |
| `VITE_UPLOAD_URL` | 否 | /api/upload | 文件上传地址 |

### 应用配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `VITE_APP_TITLE` | 否 | 校园互助平台 | 应用标题 |
| `VITE_APP_VERSION` | 否 | 1.0.0 | 应用版本号 |

### 开发环境配置 (.env.development)

```bash
# 后端 API 地址
VITE_API_BASE_URL=http://localhost:8080

# WebSocket 地址
VITE_WS_URL=ws://localhost:8080

# 文件上传地址
VITE_UPLOAD_URL=http://localhost:8080/api/upload

# 应用标题
VITE_APP_TITLE=校园互助平台（开发环境）
```

### 生产环境配置 (.env.production)

```bash
# 后端 API 地址（替换为实际域名或 IP）
VITE_API_BASE_URL=https://api.campus.example.com

# WebSocket 地址
VITE_WS_URL=wss://api.campus.example.com/ws

# 文件上传地址
VITE_UPLOAD_URL=https://api.campus.example.com/api/upload

# 应用标题
VITE_APP_TITLE=校园互助平台
```

## 前端环境变量 (frontend-admin)

### API 配置

| 变量名 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|
| `VITE_API_BASE_URL` | 是 | http://localhost:8080 | 后端 API 基础地址 |
| `VITE_ADMIN_WS_URL` | 否 | ws://localhost:8080 | WebSocket 服务地址 |

### 开发环境配置 (.env.development)

```bash
# 后端 API 地址
VITE_API_BASE_URL=http://localhost:8080

# WebSocket 地址
VITE_ADMIN_WS_URL=ws://localhost:8080
```

### 生产环境配置 (.env.production)

```bash
# 后端 API 地址（替换为实际域名或 IP）
VITE_API_BASE_URL=https://api.campus.example.com

# WebSocket 地址
VITE_ADMIN_WS_URL=wss://api.campus.example.com/ws
```

## Docker 环境变量

### Docker Swarm 配置

Docker Swarm 部署使用独立的 `.env.swarm` 文件，与开发环境端口隔离：

```bash
# ==================== 基础配置 ====================
COMPOSE_PROJECT_NAME=campus-swarm

# ==================== 端口配置（已隔离，不占用开发环境端口） ====================
HTTP_PORT=80                      # HTTP 端口
MYSQL_PORT=13306                  # MySQL 端口（隔离：开发环境用 3306）
REDIS_PORT=16379                  # Redis 端口（隔离：开发环境用 6379）
BACKEND_PORT=18080                # 后端端口（隔离：开发环境用 8080）

# ==================== 数据库配置 ====================
DB_PASSWORD=SwarmMySQL2024!       # 生产级别强密码
DB_NAME=campus_fenbushi

# ==================== Redis 配置 ====================
REDIS_PASSWORD=SwarmRedis2024!

# ==================== JWT 配置 ====================
JWT_SECRET=campus-swarm-jwt-secret-key-2024-very-long-and-secure-and-unique-123456789

# ==================== 节点配置 ====================
MANAGER_IP=192.168.100.10        # Manager 节点 IP
WORKER1_IP=192.168.100.11         # Worker1 节点 IP
WORKER2_IP=192.168.100.12         # Worker2 节点 IP
```

**注意**：Docker Swarm 环境变量文件位于 `docker-swarm/.env.swarm`，与 `docker-compose` 部署的 `.env` 文件分开。

### docker-compose.yml 配置

```yaml
services:
  backend:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_PORT=3306
      - DB_NAME=campus_fenbushi
      - DB_USERNAME=root
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_SLAVE_HOST=mysql-slave
      - DB_SLAVE_PORT=3307
      - DB_SLAVE_PASSWORD=${DB_SLAVE_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION=604800000
      - READ_WRITE_SPLIT_ENABLED=true

  frontend-user:
    environment:
      - VITE_API_BASE_URL=http://localhost/api
      - VITE_WS_URL=ws://localhost/ws

  frontend-admin:
    environment:
      - VITE_API_BASE_URL=http://localhost/api
```

## 敏感信息管理

### 安全建议

1. **禁止硬编码**：所有敏感信息必须使用环境变量
2. **使用强密码**：数据库和 Redis 密码至少 16 位
3. **JWT Secret**：使用随机生成的密钥，至少 256 位
4. **环境变量文件**：`.env` 文件加入 `.gitignore`，不提交到版本控制

### .gitignore 配置

```
# 环境变量文件
.env
.env.local
.env.*.local

# 敏感文件
*.pem
*.key
*.crt
```

## 多环境切换

### 后端切换

```bash
# 开发环境
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# 生产环境
SPRING_PROFILES_ACTIVE=prod java -jar backend.jar
```

### 前端切换

```bash
# 开发环境
cd frontend-user
npm run dev

# 生产环境构建
cd frontend-user
npm run build

# 使用生产环境变量构建
npm run build -- --mode production
```

## 验证配置

### 后端配置验证

```bash
# 健康检查
curl http://localhost:8080/api/health

# 响应示例
{
  "code": 200,
  "data": {
    "status": "UP",
    "service": "campus-helping-platform"
  }
}
```

### 前端配置验证

```javascript
// 在代码中获取环境变量
console.log(import.meta.env.VITE_API_BASE_URL)
console.log(import.meta.env.VITE_WS_URL)
```
