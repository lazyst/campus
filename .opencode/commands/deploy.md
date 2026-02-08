# deploy

将校园互助平台部署到生产环境服务器。

## 用途

使用 Docker Compose 将项目完整部署到服务器，包括：
- MySQL 数据库
- Redis 缓存
- 后端 API 服务（双实例）
- Nginx 反向代理
- 前端静态资源

## 前置条件

1. 确保服务器已安装 Docker 和 Docker Compose
2. 确保防火墙已开放必要端口（80, 443, 22, 3306, 6379）
3. 准备好以下文件：
   - `.env` 环境变量文件（包含所有密码）
   - `docker-compose.yml` 编排文件
   - 前端构建产物
   - 后端 JAR 文件

## 执行步骤

### 步骤 1：本地准备

```bash
# 1. 构建前端
cd frontend-user
npm install
npm run build

cd ../frontend-admin
npm install
npm run build

# 2. 构建后端
cd ../backend
mvn clean package -DskipTests

# 3. 配置环境变量
cd ..
cp .env.example .env
# 编辑 .env 文件，填写所有密码
```

### 步骤 2：上传文件到服务器

```bash
# 使用 scp 上传
scp -r campus-fenbushi/* root@192.168.100.100:/app/campus/
```

### 步骤 3：服务器端部署

```bash
# SSH 连接
ssh root@192.168.100.100
cd /app/campus

# 检查文件
ls -la

# 停止旧容器（如有）
docker compose down

# 构建并启动
docker compose up -d --build

# 查看日志
docker compose logs -f
```

## 服务访问

部署完成后，通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| 用户前端 | http://服务器IP | 校园互助平台 |
| 管理后台 | http://服务器IP/admin | 后台管理系统 |
| API 健康检查 | http://服务器IP/api/health | 服务状态 |

## 验证部署

### 1. 检查容器状态

```bash
docker compose ps
```

期望输出：
```
NAME              STATUS    HEALTH
campus-nginx      Up        Up
campus-backend-1  Up        healthy
campus-backend-2  Up        healthy
campus-frontend   Up        Up
campus-mysql      Up        healthy
campus-redis      Up        healthy
```

### 2. 验证 API

```bash
# 健康检查
curl http://localhost/api/health

# 板块列表
curl http://localhost/api/boards

# 帖子列表
curl http://localhost/api/posts?page=1&size=10
```

### 3. 验证数据库

```bash
docker exec -it campus-mysql mysql -uroot -p${DB_PASSWORD} -e "USE campus_fenbushi; SHOW TABLES;"
```

期望输出 12 张表。

## 回滚

如果部署出现问题，执行回滚：

```bash
# 停止新部署的容器
docker compose down

# 重启旧版本（如有备份）
docker compose up -d
```

## 常见问题

### 1. 前端显示 nginx 默认页面

原因：前端 dist 目录为空或未正确上传。

解决：
```bash
# 本地重新构建
cd frontend-user && npm run build
scp -r frontend/dist root@192.168.100.100:/app/campus/

# 重启 nginx
ssh root@192.168.100.100
docker restart campus-nginx
```

### 2. API 返回 500，表不存在

原因：数据库未初始化。

解决：
```bash
# 手动执行初始化脚本
docker exec -i campus-mysql mysql -uroot -p${DB_PASSWORD} < mysql/init.sql
```

### 3. 健康检查失败

原因：后端服务启动失败。

解决：
```bash
# 查看后端日志
docker logs campus-backend-1
docker logs campus-backend-2
```

## 相关信息

- 详细部署文档：[docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md)
- Docker Compose 配置：[docker-compose.yml](../docker-compose.yml)
- 环境变量模板：[.env.example](../.env.example)
