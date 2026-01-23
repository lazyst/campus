# 校园互助平台 - 快速启动指南

## 📋 目录

1. [前置条件](#前置条件)
2. [数据库初始化](#数据库初始化)
3. [启动后端服务](#启动后端服务)
4. [启动管理前端](#启动管理前端)
5. [访问应用](#访问应用)
6. [验证部署](#验证部署)
7. [常见问题](#常见问题)

---

## 前置条件

确保已安装以下软件：

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 18+ | 前端运行时 |
| MySQL | 8.0+ | 数据库 |
| Git | 2.0+ | 版本控制（可选） |

---

## 数据库初始化

### 步骤1: 确保MySQL服务运行

```bash
# Windows - 检查MySQL服务状态
net start | findstr MySQL

# 如果MySQL未启动，启动它
net start MySQL80
```

### 步骤2: 创建数据库

```bash
cd backend

# 运行数据库初始化脚本
mysql -h localhost -P 3306 -u root -p123 < sql/init.sql

# 或在MySQL命令行中执行
# source D:/develop/campus/backend/sql/init.sql
```

### 步骤3: 验证数据库

```bash
mysql -h localhost -P 3306 -u root -p123 -e "SHOW DATABASES LIKE 'campus%';"
```

**预期输出**:
```
Database
campus
```

---

## 启动后端服务

### 方法1: 使用Maven直接启动（推荐）

```bash
cd backend

# 清理并编译
mvn clean compile -DskipTests

# 启动服务
mvn spring-boot:run
```

**预期输出**:
```
2026-01-22 01:40:52 [main] INFO  - Starting CampusApplication using Java 17.0.x
2026-01-22 01:40:55 [main] INFO  - Tomcat started on port 8080
2026-01-22 01:40:55 [main] INFO  - CampusApplication started in 3.5 seconds
```

### 方法2: 打包后运行

```bash
cd backend

# 打包
mvn clean package -DskipTests

# 运行JAR文件
java -jar target/backend-1.0.0.jar
```

---

## 启动管理前端

### 步骤1: 安装依赖

```bash
cd frontend-admin

# 使用npm安装
npm install

# 或使用pnpm（更快）
pnpm install
```

### 步骤2: 开发模式启动

```bash
npm run dev
```

**预期输出**:
```
  VITE v5.x.x  ready in 300 ms

  ➜  Local:   http://localhost:5173/
  ➜  Network: use --host to expose
```

### 步骤3: 生产构建

```bash
# 构建生产版本
npx vite build

# 预览构建结果
npm run preview
```

---

## 访问应用

| 应用 | 地址 | 说明 |
|------|------|------|
| **后端API** | http://localhost:8080 | REST API服务 |
| **Swagger文档** | http://localhost:8080/swagger-ui.html | API文档 |
| **管理前端** | http://localhost:5173 | 后台管理系统 |
| **健康检查** | http://localhost:8080/api/health | 服务健康状态 |

---

## 验证部署

### 1. 数据库连接测试

```bash
mysql -h localhost -P 3306 -u root -p123 -e "USE campus; SELECT COUNT(*) FROM user;"
```

**预期输出**:
```
COUNT(*)
1
```

### 2. API健康检查

```bash
curl http://localhost:8080/api/health
```

**预期输出**:
```json
{"status":"UP"}
```

### 3. Swagger文档

访问 http://localhost:8080/swagger-ui.html

### 4. 管理后台登录

1. 访问 http://localhost:5173
2. 使用管理员账户登录:
   - **用户名**: `admin`
   - **密码**: `admin123`

---

## 常见问题

### Q1: MySQL连接失败

**问题**: `Access denied for user 'root'@'localhost'`

**解决方案**:
```bash
# 检查MySQL服务状态
net start MySQL80

# 验证凭据
mysql -h localhost -P 3306 -u root -p123 -e "SELECT 1;"
```

### Q2: 端口被占用

**问题**: `Port 8080/5173 already in use`

**解决方案**:
```bash
# 查找占用端口的进程
netstat -ano | findstr :8080

# 终止进程
taskkill /PID <PID> /F
```

### Q3: 后端编译失败

**问题**: `cannot find symbol` 或 `package does not exist`

**解决方案**:
```bash
cd backend
mvn clean compile -U
```

### Q4: 前端构建失败

**问题**: `npm install` 失败

**解决方案**:
```bash
cd frontend-admin

# 清除缓存并重新安装
rm -rf node_modules package-lock.json
npm install

# 使用淘宝镜像（如果在国外源失败）
npm install --registry=https://registry.npmmirror.com
```

### Q5: 数据库表不存在

**问题**: `Table 'campus.user' doesn't exist`

**解决方案**:
```bash
# 重新初始化数据库
mysql -h localhost -P 3306 -u root -p123 -e "DROP DATABASE IF EXISTS campus;"
mysql -h localhost -P 3306 -u root -p123 < sql/init.sql
```

---

## 🚀 一键启动脚本

创建 `start.bat` 文件：

```batch
@echo off
echo ================================
echo 校园互助平台 - 一键启动
echo ================================

echo [1/4] 检查MySQL服务...
net start | findstr MySQL >nul
if %errorlevel% equ 0 (
    echo ✅ MySQL服务已启动
) else (
    echo ⚠️  正在启动MySQL服务...
    net start MySQL80
)

echo [2/4] 初始化数据库...
cd /d %~dp0backend
mysql -h localhost -P 3306 -u root -p123 < sql/init.sql
echo ✅ 数据库初始化完成

echo [3/4] 启动后端服务 (端口8080)...
start cmd /c "mvn spring-boot:run"
echo ✅ 后端服务启动中...

echo [4/4] 启动管理前端 (端口5173)...
cd /d %~dp0frontend-admin
start cmd /c "npm run dev"
echo ✅ 管理前端启动中...

echo ================================
echo 🎉 启动完成！
echo ================================
echo 后端API: http://localhost:8080
echo Swagger文档: http://localhost:8080/swagger-ui.html
echo 管理前端: http://localhost:5173
echo 管理员账户: admin / admin123
echo ================================
```

---

## 📞 获取帮助

如果遇到问题：

1. 查看 [常见问题](#常见问题)
2. 查看项目Issues
3. 联系项目维护者

---

**最后更新**: 2026年1月22日  
**版本**: 1.0.0
