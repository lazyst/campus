# start-dev

启动校园互助平台的开发环境，包括后端服务和前端服务。

## 用途

在本地开发时，使用此命令启动所有服务：
- 后端 API 服务（端口 8080）
- 用户前端（端口 3000）
- 管理前端（端口 3001）

## 前置条件

1. 确保 MySQL 和 Redis 服务已在虚拟机上运行（192.168.100.100）
2. 确保数据库已初始化（有 12 张表）
3. 确保本地安装了 Java 17+ 和 Node.js 18+

## 执行步骤

```bash
# 1. 启动后端服务（自动加载 .env.development 配置）
cd backend
SPRING_CONFIG_ADDITIONAL_LOCATION=file:../.env.development mvn spring-boot:run

# 2. 启动用户前端（新终端）
cd frontend-user
npm run dev

# 3. 启动管理前端（新终端）
cd frontend-admin
npm run dev
```

**或者使用项目提供的启动脚本（推荐）：**

```bash
# Windows
start-dev.bat

# Linux/Mac
chmod +x start-dev.sh
./start-dev.sh
```

## 服务访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 后端 API | http://localhost:8080/api | RESTful API 接口 |
| Swagger 文档 | http://localhost:8080/swagger-ui.html | API 文档 |
| 用户前端 | http://localhost:3000 | 移动端校园互助平台 |
| 管理前端 | http://localhost:3001 | 后台管理系统 |

## 配置文件说明

开发环境使用以下配置：
- **后端**：`backend/src/main/resources/application.yml` + `.env.development`
- **前端**：`frontend-user/.env.development` + `frontend-user/.env`

### .env.development 关键配置

```bash
DB_HOST=192.168.100.100    # 虚拟机 MySQL
DB_PORT=3306
DB_PASSWORD=123            # MySQL 密码
REDIS_HOST=192.168.100.100 # 虚拟机 Redis
REDIS_PORT=6379
REDIS_PASSWORD=123         # Redis 密码
```

## 验证

执行完成后，检查以下内容：

1. 后端日志显示 "Started CampusApplication"
2. 前端页面可以正常访问
3. API 接口可以正常返回数据

```bash
# 测试 API
curl http://localhost:8080/api/boards
# 期望返回：板块列表数据

curl http://localhost:8080/api/posts?page=1&size=10
# 期望返回：帖子列表
```

## 常见问题

### 后端启动失败，Redis 连接错误

确保 Redis 容器正在运行：

```bash
# SSH 连接并检查
ssh root@192.168.100.100
docker ps | grep redis
docker logs campus-redis
```

### 后端启动失败，数据库连接错误

确保 MySQL 容器正在运行且数据库已初始化：

```bash
# SSH 连接并检查
ssh root@192.168.100.100
docker ps | grep mysql
docker exec -it campus-mysql mysql -uroot -p123 -e "SHOW DATABASES;"
```

### 前端页面显示空白

检查浏览器控制台是否有 API 请求错误，可能是后端服务未启动。

## 相关信息

- 项目文档：[docs/DEVELOPMENT.md](../docs/DEVELOPMENT.md)
- 部署文档：[docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md)
- VM 服务配置：[vm-services.md](../rules/vm-services.md)
