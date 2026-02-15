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
server {
    listen 80;

    # 管理后台 - 使用 root + location 匹配
    # 重要：必须放在 /api 前面
    location /admin {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    location /admin/ {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # API代理 - 使用Docker服务名而非固定IP
    location /api/ {
        proxy_pass http://campus-backend-1:8080;
        # ...
    }
}
```

**注意**：
1. 禁止使用硬编码IP地址如 `172.18.0.x`，容器重启后IP会变化！
2. `/admin` location 必须放在 `/api` 前面，否则会被错误匹配
3. 使用 `root` 代替 `alias`，避免 try_files fallback 路径问题

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

2. **创建Docker网络**
   ```bash
   # 创建自定义网络（重要：nginx需要和backend通信）
   docker network create app-network 2>/dev/null || true

   # 将后端容器连接到网络
   docker network connect app-network campus-backend-1 2>/dev/null || true
   docker network connect app-network campus-backend-2 2>/dev/null || true
   ```

3. **配置MySQL字符集**
   ```bash
   # 上传字符集配置文件
   docker cp mysql-charset.cnf campus-mysql:/etc/mysql/conf.d/charset.cnf
   docker restart campus-mysql
   ```

4. **启动Nginx容器（推荐方式）**
   ```bash
   # 方式1：复制文件到容器内（推荐，避免权限问题）
   docker run -d --name campus-nginx -p 80:80 --network app-network nginx:alpine
   docker cp /root/nginx/nginx.conf campus-nginx:/etc/nginx/nginx.conf
   docker cp /path/to/dist/. campus-nginx:/usr/share/nginx/html/
   docker cp /path/to/dist-admin/. campus-nginx:/usr/share/nginx/html/admin/

   # 方式2：使用挂载（注意权限问题）
   # docker run -d --name campus-nginx -p 80:80 --network app-network \
   #   -v /root/nginx/nginx.conf:/etc/nginx/nginx.conf:ro \
   #   -v /path/to/html:/usr/share/nginx/html:ro \
   #   nginx:alpine
   ```

5. **启动服务**
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

### 4. Nginx容器无法启动/端口冲突

**原因**：宿主机上可能有 nginx 进程占用了 80 端口

**解决**：
```bash
# 检查端口占用
ss -tlnp | grep :80

# 检查宿主机上的 nginx 进程
ps aux | grep nginx

# 杀掉所有 nginx 进程（宿主机和Docker的）
pkill -9 nginx

# 重启 nginx 容器
docker restart campus-nginx
```

**注意**：使用 `pkill -9 nginx` 会同时杀掉宿主机和容器内的 nginx，需要重启容器

### 5. Nginx返回500/权限拒绝

**原因**：挂载的静态文件目录权限不足

**解决**：
```bash
# 方法1：复制文件到容器内（推荐）
docker cp /path/to/html/. campus-nginx:/usr/share/nginx/html/

# 方法2：设置权限
chmod -R 755 /path/to/html/
chown -R root:root /path/to/html/
```

### 6. 后台管理页面刷新404

**原因**：Vue Router SPA路由刷新时，nginx未正确配置fallback

**现象**：访问 `/admin/` 正常，但访问 `/admin/items` 后刷新页面返回404

**解决**：
```nginx
# 错误配置 - 使用 alias
location /admin/ {
    alias /usr/share/nginx/html/admin/;
    try_files $uri $uri/ /admin/index.html;  # alias下fallback路径问题
}

# 正确配置 - 使用 root
location /admin {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /admin/index.html;
}

location /admin/ {
    root /usr/share/nginx/html;
    try_files $uri $uri/ /admin/index.html;
}
```

**关键点**：
1. 使用 `root` 代替 `alias`，避免路径拼接问题
2. 添加不带斜杠的 `/admin` location，确保精确匹配
3. `try_files` fallback 必须使用绝对路径 `/admin/index.html`
4. `/admin` location 必须放在 `/api` 之前

## 验证命令

```bash
# 检查服务状态 - 前端
curl -s http://192.168.100.133/ | head -5        # 首页 (应该返回HTML)
curl -s -o /dev/null -w "%{http_code}" http://192.168.100.133/  # 返回200

# 检查服务状态 - 管理后台
curl -s http://192.168.100.133/admin/ | head -5    # 管理后台首页
curl -s -o /dev/null -w "%{http_code}" http://192.168.100.133/admin/  # 返回200

# 检查服务状态 - 后台子页面（关键：刷新不返回404）
curl -s -o /dev/null -w "%{http_code}" http://192.168.100.133/admin/items  # 返回200

# 检查服务状态 - API
curl -s http://192.168.100.133/api/boards          # 板块API
curl -s http://192.168.100.133/api/posts           # 帖子API
curl -s http://192.168.100.133/api/items           # 二手API
curl -s -o /dev/null -w "%{http_code}" http://192.168.100.133/api/auth/code  # 应返回后端响应

# 检查响应大小（判断是否返回正确页面）
curl -s -o /dev/null -w "%{size_download}" http://192.168.100.133/        # 前台 ~708字节
curl -s -o /dev/null -w "%{size_download}" http://192.168.100.133/admin/   # 后台 ~420字节
curl -s -o /dev/null -w "%{size_download}" http://192.168.100.133/admin/items  # 后台 ~420字节

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
