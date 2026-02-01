# 校园互助平台 Docker 开发环境指南

## 一、Docker 环境准备

### 1.1 Docker 安装

在开始使用 Docker 之前，需要确保开发环境中已正确安装 Docker 和 Docker Compose。

**Windows 系统安装**：

1. 下载 Docker Desktop for Windows 安装包
2. 运行安装程序，按照向导完成安装
3. 安装完成后重启计算机
4. 打开 PowerShell 或命令提示符，执行以下命令验证安装：

```powershell
# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker-compose --version

# 运行 hello-world 镜像验证 Docker 正常运行
docker run hello-world
```

**macOS 系统安装**：

1. 下载 Docker Desktop for Mac 安装包
2. 将 Docker.app 拖入应用程序文件夹
3. 启动 Docker Desktop
4. 打开终端，验证安装：

```bash
# 检查 Docker 版本
docker --version

# 检查 Docker Compose 版本
docker-compose --version
```

**Linux 系统安装（以 Ubuntu 为例）**：

```bash
# 更新软件包索引
sudo apt update

# 安装依赖包
sudo apt install apt-transport-https ca-certificates curl software-properties-common

# 添加 Docker 官方 GPG 密钥
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

# 添加 Docker 仓库
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

# 安装 Docker
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io

# 启动 Docker 服务
sudo systemctl start docker

# 设置 Docker 服务开机启动
sudo systemctl enable docker

# 添加当前用户到 docker 组（免 sudo 执行 docker 命令）
sudo usermod -aG docker $USER
```

### 1.2 Docker 配置优化

为了提升 Docker 使用体验，可以进行以下配置优化：

**配置 Docker 镜像加速**：

编辑 Docker 配置文件（Windows 和 macOS 在 Docker Desktop 设置中配置，Linux 编辑 `/etc/docker/daemon.json`）：

```json
{
  "registry-mirrors": [
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

配置修改后，重启 Docker 服务使配置生效：

```bash
# Linux 重启 Docker
sudo systemctl restart docker

# 验证镜像加速配置
docker info | grep "Registry Mirrors"
```

### 1.3 Docker 资源分配

如果使用 Docker Desktop，可以在设置中调整分配给 Docker 的资源：

- **CPU**：建议分配至少 4 个核心
- **内存**：建议分配至少 4GB 内存
- **磁盘**：确保有足够的磁盘空间存储镜像和容器数据

## 二、Docker Compose 配置说明

### 2.1 配置文件结构

项目使用 Docker Compose 管理多个 Docker 容器。配置文件位于 `deployment/docker-compose.yml`，定义了所有服务的配置信息。

```
deployment/
├── docker-compose.yml          # Docker Compose 主配置
├── Dockerfile.backend          # 后端服务 Dockerfile
├── Dockerfile.user             # 用户前端 Dockerfile
├── Dockerfile.admin            # 管理前端 Dockerfile
└── nginx.conf                  # Nginx 配置
```

### 2.2 docker-compose.yml 配置说明

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: campus-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: campus
      MYSQL_CHARSET: utf8mb4
      MYSQL_COLLATION: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./backend/src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ../docker
      dockerfile: Dockerfile.backend
    container_name: campus-backend
    restart: always
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: campus
      DB_USERNAME: root
      DB_PASSWORD: root123456
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend-user:
    build:
      context: ../docker
      dockerfile: Dockerfile.user
    container_name: campus-frontend-user
    restart: always
    depends_on:
      - backend
    ports:
      - "3000:80"
    networks:
      - campus-network

  frontend-admin:
    build:
      context: ../docker
      dockerfile: Dockerfile.admin
    container_name: campus-frontend-admin
    restart: always
    depends_on:
      - backend
    ports:
      - "3001:80"
    networks:
      - campus-network

  nginx:
    image: nginx:alpine
    container_name: campus-nginx
    restart: always
    depends_on:
      - backend
      - frontend-user
      - frontend-admin
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ../frontend-user/dist:/usr/share/nginx/html/user:ro
      - ../frontend-admin/dist:/usr/share/nginx/html/admin:ro
      - uploads_data:/usr/share/nginx/uploads
    networks:
      - campus-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  campus-network:
    driver: bridge

volumes:
  mysql_data:
    driver: local
  uploads_data:
    driver: local
```

### 2.3 服务说明

| 服务名 | 镜像 | 端口 | 说明 |
|--------|------|------|------|
| mysql | mysql:8.0 | 3306 | MySQL 数据库服务 |
| backend | 自定义 | 8080 | 后端 API 服务 |
| frontend-user | 自定义 | 3000 | 用户前端静态资源 |
| frontend-admin | 自定义 | 3001 | 管理前端静态资源 |
| nginx | nginx:alpine | 80/443 | 反向代理服务 |

### 2.4 数据卷说明

| 数据卷 | 挂载路径 | 说明 |
|--------|----------|------|
| mysql_data | /var/lib/mysql | MySQL 数据存储 |
| uploads_data | /usr/share/nginx/uploads | 上传文件存储 |

## 三、开发环境启动

### 3.1 首次启动

首次使用 Docker Compose 启动项目，需要执行以下步骤：

```bash
# 进入部署目录
cd deployment

# 构建并启动所有服务（首次启动会下载镜像和构建容器）
docker-compose up -d --build

# 查看服务启动状态
docker-compose ps

# 查看服务日志
docker-compose logs -f
```

### 3.2 服务管理命令

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 停止所有服务并删除数据卷（谨慎使用，会删除数据库数据）
docker-compose down -v

# 重启单个服务
docker-compose restart backend

# 查看服务日志
docker-compose logs -f backend

# 查看服务资源使用情况
docker stats

# 进入服务容器内部
docker exec -it campus-backend /bin/bash
```

### 3.3 验证服务状态

所有服务启动后，通过以下方式验证服务是否正常运行：

```bash
# 检查所有容器状态
docker-compose ps

# 应该看到所有服务状态为 "Up"

# 验证 MySQL 连接
docker exec -it campus-mysql mysql -u root -proot123456 -e "SELECT 1"

# 验证后端 API
curl http://localhost:8080/api/health

# 验证前端页面
curl -I http://localhost:3000
curl -I http://localhost:3001
```

## 四、本地调试配置

### 4.1 后端远程调试

在 Docker 环境中进行后端代码调试，需要配置远程调试端口。

**修改 docker-compose.yml**：

```yaml
backend:
  build:
    context: ../docker
    dockerfile: Dockerfile.backend
  container_name: campus-backend
  ports:
    - "8080:8080"
    - "5005:5005"  # 添加 JDWP 调试端口
  environment:
    SPRING_PROFILES_ACTIVE: prod
    JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

**IDEA 远程调试配置**：

1. 打开 Run/Debug Configurations
2. 添加 Remote JVM Debug 配置
3. 配置连接信息：
   - Host: localhost
   - Port: 5005
4. 点击 Debug 开始调试

### 4.2 热重载配置

后端代码修改后需要重新构建镜像才能生效。如果需要热重载功能，可以使用以下方案：

**方案一：挂载源码目录**（仅限开发环境）：

```yaml
backend:
  volumes:
    - ../backend:/app
    - /root/.m2:/root/.m2  # Maven 缓存
```

**方案二：使用 Spring Boot DevTools**：

在 `application-prod.yml` 中添加 DevTools 配置：

```yaml
spring:
  devtools:
    livereload:
      enabled: true
```

### 4.3 前端调试

前端开发建议使用本地开发服务器（`npm run dev`），通过 Nginx 代理实现与 Docker 后端的通信。

**配置 Vite 代理**（`vite.config.js`）：

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
})
```

## 五、常见问题

### 5.1 端口冲突

如果启动时提示端口被占用，检查并修改配置中的端口映射：

```yaml
# 修改端口映射
ports:
  - "3307:3306"  # MySQL 改为 3307 端口
```

或者停止占用端口的进程：

```bash
# Windows
netstat -ano | findstr :3306
taskkill /PID <pid> /F

# Linux/macOS
lsof -i :3306
kill <pid>
```

### 5.2 容器启动失败

容器启动失败时，查看详细日志定位问题：

```bash
# 查看所有服务日志
docker-compose logs

# 查看指定服务日志
docker-compose logs backend

# 查看最近 100 行日志
docker-compose logs --tail 100 backend
```

常见问题及解决方案：

- **MySQL 连接失败**：检查数据库连接配置，确保 MySQL 服务完全启动后再启动后端
- **镜像下载失败**：配置 Docker 镜像加速器，或检查网络连接
- **内存不足**：增加 Docker 分配的内存资源

### 5.3 数据库数据丢失

数据卷中的数据在容器删除后仍然保留。如果需要完全清除数据：

```bash
# 停止所有服务
docker-compose down

# 删除数据卷
docker volume rm deployment_mysql_data

# 重新启动
docker-compose up -d
```

### 5.4 构建缓存

如果修改 Dockerfile 或依赖后构建失败，可以清除构建缓存后重新构建：

```bash
# 清除构建缓存
docker-compose build --no-cache

# 重新构建并启动
docker-compose up -d --build
```

### 5.5 网络问题

服务间无法通信时，检查网络配置：

```bash
# 查看网络列表
docker network ls

# 查看网络详情
docker network inspect deployment_campus-network

# 检查服务是否在同一网络
docker inspect <container_name> | grep NetworkMode
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
