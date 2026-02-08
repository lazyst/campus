# 环境配置指南

## 依赖服务运行方式

- [x] Docker Compose（推荐）
- [ ] 本地安装
- [x] 远程服务器

本项目使用虚拟机 `192.168.100.100` 上的 Docker 容器运行 MySQL 和 Redis 服务。开发环境直接连接虚拟机上的服务，无需本地安装数据库。

## 数据库配置

### MySQL

- **运行方式**：Docker 容器（远程服务器）
- **连接信息**：
  - Host: `192.168.100.100`
  - Port: `3306`
  - Database: `campus_fenbushi`
  - User: `root`
  - Password: `123`

### Redis

- **运行方式**：Docker 容器（远程服务器）
- **连接信息**：
  - Host: `192.168.100.100`
  - Port: `6379`
  - Password: `123`

## Docker 容器信息

### MySQL 容器

```bash
# 容器名称
campus-mysql

# 启动命令（如果需要手动启动）
docker run -d \
  --name campus-mysql \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=123 \
  -e MYSQL_DATABASE=campus_fenbushi \
  mysql:8.0

# 连接 MySQL
mysql -h 192.168.100.100 -uroot -p123 campus_fenbushi
```

### Redis 容器

```bash
# 容器名称
campus-redis

# 启动命令（如果需要手动启动）
docker run -d \
  --name campus-redis \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:alpine \
  redis-server --requirepass 123

# 连接 Redis
redis-cli -h 192.168.100.100 -p 6379 -a 123
```

## 开发环境连接配置

### 后端 application.yml

```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://192.168.100.100:3306/campus_fenbushi?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: 123
    slave:
      url: jdbc:mysql://192.168.100.100:3306/campus_fenbushi?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: 123

  data:
    redis:
      host: 192.168.100.100
      port: 6379
      password: 123
```

### 前端环境变量

开发环境使用 `.env.development` 文件配置：

```bash
# 数据库配置
DB_HOST=192.168.100.100
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=123

# Redis 配置
REDIS_HOST=192.168.100.100
REDIS_PORT=6379
REDIS_PASSWORD=123
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库地址 | 192.168.100.100 |
| DB_PORT | 数据库端口 | 3306 |
| DB_USERNAME | 数据库用户名 | root |
| DB_PASSWORD | 数据库密码 | 123 |
| REDIS_HOST | Redis 地址 | 192.168.100.100 |
| REDIS_PORT | Redis 端口 | 6379 |
| REDIS_PASSWORD | Redis 密码 | 123 |

## 初始化

### 数据库初始化脚本

- **脚本路径**：`backend/sql/init.sql`
- **包含内容**：11 张数据表结构和示例数据

### 首次启动步骤

#### 后端启动

```bash
cd backend
mvn clean compile              # 编译
mvn spring-boot:run            # 启动 (端口 8080)
```

#### 前端启动 (user 和 admin)

```bash
cd frontend-user  # 或 frontend-admin
npm run dev       # 开发服务器
```

## 服务状态检查

### 检查 MySQL

```bash
# SSH 连接后检查
ssh root@192.168.100.100
docker ps | grep mysql
docker exec -it campus-mysql mysql -uroot -p123 -e "SHOW DATABASES;"
```

### 检查 Redis

```bash
# SSH 连接后检查
ssh root@192.168.100.100
docker ps | grep redis
docker exec -it campus-redis redis-cli -a 123 ping
```

## 故障排查

### MySQL 连接失败

1. 检查容器是否运行

   ```bash
   ssh root@192.168.100.100
   docker ps | grep mysql
   ```

2. 检查端口是否开放

   ```bash
   telnet 192.168.100.100 3306
   ```

3. 检查防火墙

   ```bash
   ssh root@192.168.100.100
   firewall-cmd --list-ports
   ```

### Redis 连接失败

1. 检查容器是否运行

   ```bash
   ssh root@192.168.100.100
   docker ps | grep redis
   ```

2. 验证密码

   ```bash
   redis-cli -h 192.168.100.100 -p 6379 -a 123 ping
   ```

## 注意事项

1. **开发环境**：直接连接虚拟机上的服务
2. **生产环境**：使用 Docker Compose，服务间通过服务名通信
3. **密码安全**：生产环境请使用强密码并通过环境变量设置
4. **备份**：定期备份数据库和 Redis 数据
