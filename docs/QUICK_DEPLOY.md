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
# 创建生产环境配置文件
# 文件: frontend-admin/.env.production
echo 'VITE_API_BASE_URL=http://192.168.100.133' > frontend-admin/.env.production

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

```bash
cp backend/sql/init.sql mysql/init.sql
```

### 3. 上传文件到服务器

**使用Python脚本**（推荐）：

```bash
# 上传所有配置文件
scp -r docker-compose.yml .env nginx/ mysql/ redis/ root@192.168.100.133:/app/campus/

# 上传后端JAR
scp backend/target/backend-1.0.0.jar root@192.168.100.133:/app/campus/backend/

# 上传前端构建产物
scp -r frontend-user/dist/* root@192.168.100.133:/app/campus/frontend/
scp -r frontend-admin/dist/* root@192.168.100.133:/app/campus/frontend-admin/
```

### 4. 服务器端部署

```bash
ssh root@192.168.100.133

cd /app/campus

# 创建前端目录
mkdir -p nginx/html/user nginx/html/admin

# 复制前端文件到Nginx目录
cp -r frontend/* nginx/html/user/
cp -r frontend-admin/* nginx/html/admin/

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

### 5. 验证部署

```bash
# 测试前端
curl http://192.168.100.133/ | grep "<title>"

# 测试API
curl http://192.168.100.133/api/health
curl http://192.168.100.133/api/boards
```

## 关键配置说明

### 前端API地址配置

**重要**: 管理前端必须配置正确的API服务器地址

```bash
# 创建生产环境配置文件
echo 'VITE_API_BASE_URL=http://192.168.100.133' > frontend-admin/.env.production
```

**注意**: 如果不配置，生产环境会使用 `http://localhost:8080`，导致API请求失败。

### Nginx配置注意事项

**重要**: 使用 `$http_host` 而不是 `$host`

```nginx
location /api {
    proxy_pass http://backend_cluster;
    proxy_set_header Host $http_host;  # 正确
    proxy_set_header X-Real-IP $remote_addr;
}
```

**常见错误**: `400 Bad Request - Host header invalid`
- 原因: `$host` 变量转义问题
- 解决: 改为 `$http_host`

### Docker Compose配置

使用简化版本，直接运行JAR：

```yaml
backend-1:
  image: eclipse-temurin:17-jre-alpine
  command: java -Xms512m -Xmx1024m -jar /app/backend-1.0.0.jar
  volumes:
    - ./backend/backend-1.0.0.jar:/app/backend-1.0.0.jar:ro
```

**不要使用** `build: context: ./backend`，因为Docker构建时路径解析有问题。

## 服务状态检查

```bash
# 查看容器状态
docker compose ps

# 查看日志
docker logs campus-nginx --tail 50
docker logs campus-backend-1 --tail 50
docker logs campus-backend-2 --tail 50

# 测试后端健康
docker exec campus-backend-1 wget -qO- http://localhost:8080/api/health
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

**原因**: Nginx将 `/admin` 路径代理到了frontend容器

**解决**: 确保Nginx配置了正确的静态文件路径
```nginx
location /admin {
    alias /usr/share/nginx/html/admin;
    index index.html;
    try_files $uri $uri/ /admin/index.html;
}
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
# 修改 nginx/nginx.conf
proxy_set_header Host $http_host;  # 不是 $host

# 重启Nginx
docker restart campus-nginx
```

### 4. API无响应

检查后端是否启动：
```bash
docker exec campus-backend-1 netstat -tlnp | grep 8080
curl http://localhost:8080/api/health
```

### 4. 前端无法加载

检查静态文件：
```bash
curl http://localhost/ | grep "<title>"
ls -la /app/campus/nginx/html/user/
```

## 环境变量

`.env` 文件配置：

```env
DB_HOST=mysql
DB_PORT=3306
DB_PASSWORD=123
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=123
JWT_SECRET=your-super-secret-key
JWT_EXPIRATION=604800000
```

## 目录结构

```
/app/campus/
├── docker-compose.yml
├── .env
├── backend/
│   ├── backend-1.0.0.jar
│   └── Dockerfile
├── nginx/
│   ├── nginx.conf
│   └── html/
│       ├── user/
│       │   ├── index.html
│       │   └── assets/
│       └── admin/
│           ├── index.html
│           └── assets/
├── mysql/
│   ├── init.sql
│   └── my.cnf
└── redis/
    └── redis.conf
```

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | testuser | test123456 |
| 管理员 | admin | admin123456 |

---

## 快速参考（下次部署用）

```bash
# 1. 创建生产环境配置
echo 'VITE_API_BASE_URL=http://192.168.100.133' > frontend-admin/.env.production

# 2. 本地构建
cd backend && mvn clean package -DskipTests
cd ../frontend-user && npm install && npm run build
cd ../frontend-admin && npm install && npm run build
cp backend/sql/init.sql mysql/

# 3. 上传文件
scp -r docker-compose.yml .env nginx/ mysql/ redis/ root@192.168.100.133:/app/campus/
scp backend/target/backend-1.0.0.jar root@192.168.100.133:/app/campus/backend/
scp -r frontend-user/dist/* root@192.168.100.133:/app/campus/frontend/
scp -r frontend-admin/dist/* root@192.168.100.133:/app/campus/frontend-admin/

# 4. 服务器部署
ssh root@192.168.100.133 << 'EOF'
cd /app/campus
mkdir -p nginx/html/user nginx/html/admin
cp -r frontend/* nginx/html/user/
cp -r frontend-admin/* nginx/html/admin/
docker compose down
docker compose up -d
sleep 60
docker compose ps
EOF

# 5. 验证
curl http://192.168.100.133/api/health
```

---

**部署完成！** 访问 http://192.168.100.133/ 查看前端页面。
