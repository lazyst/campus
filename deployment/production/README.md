# 校园互助平台生产环境部署指南

本指南介绍如何在生产环境中部署校园互助平台。

## 📋 目录

- [环境要求](#环境要求)
- [快速部署](#快速部署)
- [手动部署](#手动部署)
- [配置说明](#配置说明)
- [健康检查](#健康检查)
- [故障排查](#故障排查)
- [数据备份](#数据备份)

---

## 环境要求

| 组件 | 最低要求 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 磁盘 | 20 GB SSD | 50 GB SSD |
| Docker | 20.x | 24.x |
| Docker Compose | 2.x | 2.x |

## 快速部署

### 1. 准备环境

```bash
# 安装 Docker 和 Docker Compose
# Ubuntu:
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# 验证安装
docker --version
docker compose version
```

### 2. 下载项目

```bash
# 克隆项目（如果使用Git）
git clone <your-repo-url> campus
cd campus

# 或下载并解压
# wget https://github.com/your-repo/campus.tar.gz
# tar -xzf campus.tar.gz
```

### 3. 配置环境变量

```bash
# 复制环境变量模板
cp deployment/production/.env.example deployment/production/.env

# 编辑配置
vim deployment/production/.env
```

**重要配置项：**

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=your_secure_root_password
MYSQL_PASSWORD=your_secure_db_password

# JWT密钥（请使用强随机字符串）
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here

# 管理后台密码
ADMIN_PASSWORD=your_secure_admin_password

# 时区设置
TZ=Asia/Shanghai
```

### 4. 启动服务

```bash
cd deployment/production

# 构建并启动所有服务
docker compose up -d

# 查看启动日志
docker compose logs -f
```

### 5. 验证部署

```bash
# 检查服务状态
docker compose ps

# 健康检查
curl http://localhost/health
# 预期输出: OK

# 查看服务日志
docker compose logs backend
docker compose logs nginx
```

## 手动部署

### 1. 构建后端

```bash
cd backend

# Maven 构建
mvn clean package -DskipTests -Pproduction

# 或使用 Docker 构建
docker build -f Dockerfile.production -t campus-backend:latest .
```

### 2. 构建管理端

```bash
cd frontend-admin

# 安装依赖
npm install

# 构建生产版本
npx vite build

# 构建 Docker 镜像
docker build -t campus-admin:latest .
```

### 3. 准备数据目录

```bash
# 创建数据目录
mkdir -p /data/campus/{mysql,uploads,logs}

# 设置权限
chmod -R 755 /data/campus
```

### 4. 启动MySQL

```bash
docker run -d \
  --name campus-mysql \
  -p 3306:3306 \
  -v /data/campus/mysql:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=your_password \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 5. 初始化数据库

```bash
# 等待MySQL启动
sleep 30

# 导入初始化脚本
mysql -h localhost -u root -p < backend/sql/init.sql
```

### 6. 启动后端

```bash
docker run -d \
  --name campus-backend \
  -p 8080:8080 \
  -v /data/campus/uploads:/app/uploads \
  -v /data/campus/logs:/app/logs \
  --link campus-mysql:mysql \
  -e SPRING_PROFILES_ACTIVE=production \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/campus \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  campus-backend:latest
```

### 7. 启动管理端

```bash
docker run -d \
  --name campus-admin \
  -p 80:80 \
  campus-admin:latest
```

### 8. 配置Nginx

```bash
# 复制Nginx配置
cp deployment/production/nginx/nginx.conf /etc/nginx/nginx.conf
cp deployment/production/nginx/server.conf /etc/nginx/conf.d/server.conf

# 测试配置
nginx -t

# 重启Nginx
nginx -s reload
```

## 配置说明

### Docker Compose 配置

| 服务 | 端口 | 说明 |
|------|------|------|
| mysql | 3306 | MySQL 数据库 |
| backend | 8080 | 后端 API 服务 |
| admin | 80 | 管理端前端 |
| nginx | 80 | 反向代理 |

### 后端配置 (application-production.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/campus?useSSL=true&allowPublicKeyRetrieval=true
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000

  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

server:
  port: 8080

logging:
  level:
    com.campus: INFO
    org.springframework: WARN
```

### Nginx 配置

- **API代理**: `/api/` → 后端:8080
- **WebSocket**: `/ws/` → 后端:8080
- **静态资源**: `/uploads/` → 数据目录
- **管理端**: `/admin/` → admin:80

## 健康检查

### 服务健康检查

```bash
# 检查所有容器状态
docker compose ps

# 检查容器健康状态
docker inspect --format='{{.State.Health.Status}}' campus-backend
docker inspect --format='{{.State.Health.Status}}' campus-mysql
```

### API 健康检查

```bash
# 后端健康检查
curl http://localhost/api/health
# 或
curl http://localhost/health

# 预期响应:
# {"status":"UP"}
```

### 数据库连接检查

```bash
# 检查MySQL连接
docker exec -it campus-mysql mysql -u root -p -e "SELECT 1"

# 检查数据库是否存在
docker exec -it campus-mysql mysql -u root -p -e "SHOW DATABASES"
```

## 故障排查

### 查看日志

```bash
# 查看所有服务日志
docker compose logs

# 查看指定服务日志
docker compose logs backend
docker compose logs nginx
docker compose logs mysql

# 实时查看日志
docker compose logs -f backend
```

### 常见问题

#### 1. 后端无法启动

```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查数据库连接
docker exec -it campus-backend curl http://localhost:8080/api/health

# 查看详细错误
docker compose logs backend
```

#### 2. 数据库连接失败

```bash
# 检查MySQL容器状态
docker ps | grep mysql

# 检查MySQL日志
docker logs campus-mysql

# 手动测试连接
docker exec -it campus-mysql mysql -u root -p
```

#### 3. 管理端无法访问

```bash
# 检查admin容器状态
docker ps | grep admin

# 检查Nginx配置
nginx -t

# 查看Nginx错误日志
tail -f /var/log/nginx/error.log
```

#### 4. 文件上传失败

```bash
# 检查上传目录权限
ls -la /data/campus/uploads/

# 修复权限
chmod -R 755 /data/campus/uploads
chown -R 1000:1000 /data/campus/uploads
```

### 重启服务

```bash
# 重启单个服务
docker compose restart backend
docker compose restart nginx

# 重启所有服务
docker compose restart

# 完全重启（停止再启动）
docker compose down
docker compose up -d
```

## 数据备份

### 数据库备份

```bash
# 备份命令（每天凌晨2点执行）
docker exec campus-mysql mysqldump -u root -p campus > backup/campus_$(date +%Y%m%d).sql

# 自动备份脚本
#!/bin/bash
BACKUP_DIR=/data/campus/backup
DATE=$(date +%Y%m%d_%H%M%S)
docker exec campus-mysql mysqldump -u root -p${MYSQL_PASSWORD} campus > ${BACKUP_DIR}/campus_${DATE}.sql

# 保留最近7天备份
find ${BACKUP_DIR} -name "campus_*.sql" -mtime +7 -delete
```

### 文件备份

```bash
# 备份上传文件
tar -czf /data/campus/backup/uploads_$(date +%Y%m%d).tar.gz /data/campus/uploads/

# 备份Nginx配置
tar -czf /data/campus/backup/nginx_$(date +%Y%m%d).tar.gz /etc/nginx/
```

### 恢复数据

```bash
# 恢复数据库
docker exec -i campus-mysql mysql -u root -p campus < backup/campus_20240122.sql
```

## 监控建议

### 1. 日志监控

```bash
# 使用 journalctl（如果使用 systemd）
journalctl -u docker -f

# 或使用 ELK Stack
```

### 2. 资源监控

```bash
# Docker Stats
docker stats

# 或使用 Prometheus + Grafana
```

### 3. 安全建议

- 定期更新 Docker 镜像
- 使用强密码
- 启用 SSL/TLS
- 配置防火墙
- 定期备份数据

## 联系支持

如有问题，请：

1. 查看 [常见问题](#故障排查)
2. 查看项目 Issues
3. 联系项目维护者

---

**版本**: 1.0.0  
**最后更新**: 2026年1月  
**技术支持**: Campus Development Team
