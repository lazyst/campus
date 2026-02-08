# VM 服务配置规则

## 概述

本项目使用虚拟机 `192.168.100.100` 上的 Docker 容器运行 MySQL 和 Redis 服务。

## 服务器信息

| 服务 | 地址 | 端口 | 用户名 | 密码 |
|------|------|------|--------|------|
| MySQL | 192.168.100.100 | 3306 | root | 123 |
| Redis | 192.168.100.100 | 6379 | - | 123 |

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

### 使用 SSH 连接

```bash
# SSH 连接
ssh root@192.168.100.100

# 密码: 123
```

## 环境变量

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
