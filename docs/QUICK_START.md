# 开发环境启动流程

## 前置条件

MySQL 和 Redis 运行在 WSL 的 Docker 容器中，端口已映射到本地 localhost：

| 服务 | 本地地址 | WSL端口 | 用途 |
|------|----------|---------|------|
| MySQL Master | localhost:3306 | 3306 | 写操作 |
| MySQL Slave | localhost:3307 | 3307 | 读操作 |
| Redis | localhost:6379 | 6379 | 缓存 |

### 启动 MySQL（首次或重新部署）

```bash
# 在 WSL 中执行脚本（自动配置正确字符集）
wsl -d Ubuntu-24.04 -- bash /mnt/d/develop/campus-fenbushi/scripts/start-mysql.sh

# 或者手动验证 MySQL 状态
wsl -d Ubuntu-24.04 -- docker ps
```

### 启动 Redis（如未运行）

```bash
wsl -d Ubuntu-24.04 -- docker start campus-redis-master
```

## 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动成功后访问：http://localhost:8080

## 启动用户前端

```bash
cd frontend-user
npm install   # 首次执行
npm run dev
```

访问：http://localhost:3000

## 启动管理后台

```bash
cd frontend-admin
npm install   # 首次执行
npm run dev
```

访问：http://localhost:3001

## 验证

| 检查项 | 地址 |
|--------|------|
| 后端 API | http://localhost:8080/api/health |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |
| 用户前端 | http://localhost:3000 |
| 管理后台 | http://localhost:3001 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 用户 | 13800000000 | 123456 |

## 生产环境

生产环境部署在 **192.168.100.133**：

| 服务 | 地址 |
|------|------|
| 前端首页 | http://192.168.100.133/ |
| 管理后台 | http://192.168.100.133/admin/ |
| 后端API | http://192.168.100.133/api/ |

## 常见问题

**端口被占用**：
```bash
# Windows 查看占用端口
netstat -ano | findstr "8080"
taskkill /PID <PID> /F
```
