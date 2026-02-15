# 校园互助平台 - 生产环境部署指南

## 部署架构

```
                              ┌─────────────────┐
                              │   Nginx (80/443) │
                              │  campus-nginx   │
                              └────────┬────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
              ┌─────▼─────┐    ┌──────▼──────┐    ┌──────▼──────┐
              │ 前端用户   │    │   后端API    │    │  管理后台    │
              │ :3000     │    │  :8080 x2   │    │   :3001     │
              └───────────┘    └──────┬──────┘    └─────────────┘
                                      │
                           ┌──────────┼──────────┐
                           │          │          │
                      ┌────▼───┐ ┌───▼────┐ ┌───▼───┐
                      │ MySQL   │ │ Redis  │ │ 存储  │
                      │ :3306   │ │ :6379  │ │ uploads│
                      └─────────┘ └────────┘ └────────┘
```

## 服务列表

| 容器名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| campus-nginx | campus-nginx:latest | 80, 443 | Nginx反向代理 |
| campus-frontend-user | campus-frontend-user:latest | 3000 | 用户前端 |
| campus-frontend-admin | campus-frontend-admin:latest | 3001 | 管理后台 |
| campus-backend-1 | campus-backend:latest | 8080 | 后端API (主) |
| campus-backend-2 | campus-backend:latest | - | 后端API (备) |
| campus-mysql | mysql:8.0 | 3306 | MySQL数据库 |
| campus-redis | redis:7-alpine | 6379 | Redis缓存 |

## 关键配置

### 1. Nginx配置 (nginx-swarm.conf)

```nginx
location /api/ {
    # 重要：使用Docker服务名而非固定IP
    proxy_pass http://campus-backend-1:8080;
    # ...
}
```

**注意**：禁止使用硬编码IP地址如 `172.18.0.x`，容器重启后IP会变化！

### 2. MySQL字符集配置 (mysql-charset.cnf)

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
init-connect='SET NAMES utf8mb4'

[client]
default-character-set=utf8mb4
```

### 3. JDBC连接配置 (application.yml)

```yaml
url: jdbc:mysql://...campus_fenbushi?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci
```

## 部署步骤

### 首次部署

1. **构建镜像**
   ```bash
   # 后端
   cd backend && mvn clean package -DskipTests
   docker build -t campus-backend:latest .

   # 前端用户
   cd frontend-user && npm run build
   docker build -t campus-frontend-user:latest .

   # 前端管理
   cd frontend-admin && npm run build
   docker build -t campus-frontend-admin:latest .

   # Nginx
   docker build -t campus-nginx:latest -f nginx/Dockerfile nginx/
   ```

2. **配置MySQL字符集**
   ```bash
   # 上传字符集配置文件
   docker cp mysql-charset.cnf campus-mysql:/etc/mysql/conf.d/charset.cnf
   docker restart campus-mysql
   ```

3. **启动服务**
   ```bash
   docker network create -d overlay app-network 2>/dev/null || true

   # 启动MySQL和Redis
   docker run -d --name campus-mysql \
     --network app-network \
     -e MYSQL_ROOT_PASSWORD=123 \
     -e MYSQL_DATABASE=campus_fenbushi \
     -v mysql-data:/var/lib/mysql \
     mysql:8.0

   docker run -d --name campus-redis \
     --network app-network \
     redis:7-alpine redis-server --requirepass 123

   # 启动后端
   docker run -d --name campus-backend-1 \
     --network app-network \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DB_HOST=campus-mysql \
     -e DB_PASSWORD=123 \
     -e REDIS_HOST=campus-redis \
     -e REDIS_PASSWORD=123 \
     -p 8080:8080 \
     campus-backend:latest

   # 启动前端
   docker run -d --name campus-frontend-user \
     --network app-network \
     -p 3000:80 \
     campus-frontend-user:latest

   # 启动Nginx
   docker run -d --name campus-nginx \
     --network app-network \
     -p 80:80 \
     -v nginx-html:/usr/share/nginx/html \
     campus-nginx:latest
   ```

### 更新部署

1. **更新后端**
   ```bash
   # 重新构建
   cd backend && mvn clean package -DskipTests
   docker build -t campus-backend:latest .

   # 部署
   docker cp target/backend-1.0.0.jar campus-backend-1:/app.jar
   docker restart campus-backend-1
   ```

2. **更新Nginx配置**
   ```bash
   # 修改本地nginx配置后上传
   docker cp nginx-swarm.conf campus-nginx:/etc/nginx/nginx.conf
   docker restart campus-nginx
   ```

## 常见问题

### 1. 页面数据加载失败 (502/500错误)

**原因**：Nginx配置的backend IP错误

**解决**：
```bash
# 检查当前backend容器IP
docker inspect campus-backend-1 | grep IPAddress

# 更新nginx配置，使用Docker服务名
# 编辑 nginx-swarm.conf
proxy_pass http://campus-backend-1:8080;  # 使用服务名

# 重新加载
docker restart campus-nginx
```

### 2. 中文乱码

**原因**：MySQL字符集配置错误

**解决**：
```bash
# 1. 添加字符集配置
docker cp mysql-charset.cnf campus-mysql:/etc/mysql/conf.d/charset.cnf

# 2. 重启MySQL
docker restart campus-mysql

# 3. 检查字符集
docker exec campus-mysql mysql -uroot -p123 -e "SHOW VARIABLES LIKE 'character_set%';"

# 4. 修复数据库中的乱码数据
docker exec campus-mysql mysql -uroot -p123 campus_fenbushi -e "
  UPDATE board SET name = '正确的中文' WHERE id = 1;
"
```

### 3. API返回404

**原因**：请求未正确代理到后端

**解决**：
```bash
# 检查nginx日志
docker logs campus-nginx

# 检查后端是否正常
curl http://localhost:8080/api/health

# 检查nginx配置
docker exec campus-nginx nginx -T | grep location
```

## 验证命令

```bash
# 检查服务状态
curl -s http://192.168.100.133/ | head -5        # 首页
curl -s http://192.168.100.133/admin/ | head -5    # 管理后台
curl -s http://192.168.100.133/api/boards          # 板块API
curl -s http://192.168.100.133/api/posts           # 帖子API
curl -s http://192.168.100.133/api/items           # 二手API

# 检查数据库字符集
docker exec campus-mysql mysql -uroot -p123 -e "SHOW VARIABLES LIKE 'character_set%';"
```

## 文件清单

- `docker-stack.yml` - Docker Swarm编排配置
- `nginx/nginx-swarm.conf` - Nginx配置
- `mysql-charset.cnf` - MySQL字符集配置
- `backend/src/main/resources/application.yml` - 后端配置
- `backend/Dockerfile.prod` - 后端Dockerfile
- `frontend-user/Dockerfile` - 前端用户Dockerfile
- `frontend-admin/Dockerfile` - 管理后台Dockerfile
