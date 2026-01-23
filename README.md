# 校园互助平台 (Campus Helping Platform)

[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?style=flat-square&logo=vue.js)](https://vuejs.org/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.5-FF6C37?style=flat-square)](https://baomidou.com/)
[![Element Plus](https://true.svg)](#)

一个功能完整的校园互助平台，支持用户社交（论坛）、闲置交易（收购/出售）、一对一聊天，同时提供完整的后台管理系统。

## 📁 项目结构

```
campus/
├── backend/                    # 后端服务 (Spring Boot 3 + MyBatis-Plus)
│   ├── src/main/java/com/campus/
│   │   ├── config/            # 配置类
│   │   ├── common/            # 公共组件
│   │   ├── entity/            # 实体类
│   │   └── modules/           # 业务模块
│   │       ├── admin/         # 后台管理
│   │       ├── auth/          # 认证授权
│   │       ├── chat/          # 聊天功能
│   │       ├── forum/         # 论坛功能
│   │       ├── trade/         # 闲置交易
│   │       └── user/          # 用户管理
│   └── sql/
│       └── init.sql           # 数据库初始化脚本
├── frontend-user/             # 用户端前端 (Vue3 + Vant UI)
├── frontend-admin/            # 管理端前端 (Vue3 + Element Plus)
├── docker/
│   ├── docker-compose.yml     # Docker Compose 配置
│   └── nginx/
│       └── nginx.conf         # Nginx 配置
└── README.md
```

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p < backend/sql/init.sql

# 或使用 Docker 启动 MySQL
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123 mysql:8
mysql -u root -p < backend/sql/init.sql
```

### 2. 启动后端

```bash
cd backend

# 编译项目
mvn clean compile

# 启动服务 (默认端口 8080)
mvn spring-boot:run

# 或打包运行
mvn package -DskipTests
java -jar target/backend-1.0.0.jar
```

### 3. 启动前端 (开发模式)

**用户端:**
```bash
cd frontend-user
npm install
npm run dev
```

**管理端:**
```bash
cd frontend-admin
npm install
npm run dev
```

### 4. 访问应用

- **用户端**: http://localhost:3000
- **管理端**: http://localhost:4000
- **API文档**: http://localhost:8080/swagger-ui.html
- **管理后台**: http://localhost:4000/login (admin/admin123)

## 📱 功能特性

### 用户功能
- ✅ 手机号注册/登录
- ✅ 个人信息管理
- ✅ JWT 认证 (Token 有效期 7 天)
- ✅ 用户封禁/解封

### 论坛功能
- ✅ 板块管理 (交流、学习交流、兴趣搭子等)
- ✅ 帖子发布/编辑/删除
- ✅ 评论功能 (支持回复)
- ✅ 点赞/收藏
- ✅ 通知系统

### 闲置功能
- ✅ 闲置物品发布 (收购/出售)
- ✅ 物品状态管理 (正常/已完成/已下架)
- ✅ 物品收藏
- ✅ 搜索和筛选

### 聊天功能
- ✅ 一对一实时聊天
- ✅ 离线消息存储
- ✅ WebSocket + STOMP 协议

### 后台管理
- ✅ 用户管理 (封禁/解封/删除)
- ✅ 板块管理 (CRUD)
- ✅ 帖子管理 (查看/删除)
- ✅ 闲置管理 (下架/删除)
- ✅ 统计仪表盘

## 🛠 技术栈

### 后端
- **Spring Boot 3.2** - 应用框架
- **MyBatis-Plus 3.5.5** - ORM 框架
- **JWT (jjwt 0.12.3)** - Token 认证
- **WebSocket (STOMP)** - 实时通信
- **Spring Security** - 安全框架
- **SpringDoc OpenAPI 2.3.0** - API 文档

### 前端
- **Vue 3.4** - UI 框架
- **Vant UI 4** - 用户端组件库
- **Element Plus** - 管理端组件库
- **Pinia** - 状态管理
- **Vue Router 4** - 路由管理
- **Axios** - HTTP 客户端
- **ECharts** - 统计图表

### 部署
- **Docker** - 容器化
- **Nginx** - 反向代理
- **MySQL 8** - 数据库

## 🔧 配置说明

### 后端配置 (backend/src/main/resources/application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus?useUnicode=true&characterEncoding=utf8
    username: root
    password: 123
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: your-jwt-secret-key-here
  expiration: 604800000  # 7天

websocket:
  endpoint: /ws
  allowed-origins: "*"

springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
```

### Docker 部署

```bash
# 启动所有服务
cd docker
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## 📊 API 文档

访问 http://localhost:8080/swagger-ui.html 查看完整 API 文档。

### 认证方式

所有需要认证的接口需要在 Header 中添加:
```
Authorization: Bearer <token>
```

## 🧪 测试

```bash
# 运行后端测试
cd backend
mvn test

# 运行前端测试
cd frontend-admin
npm run test
```

## 📝 数据库表结构

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `admin` | 管理员表 |
| `board` | 板块表 |
| `post` | 帖子表 |
| `comment` | 评论表 |
| `like` | 点赞表 |
| `collect` | 收藏表 |
| `notification` | 通知表 |
| `item` | 闲置物品表 |
| `item_collect` | 闲置收藏表 |
| `conversation` | 会话表 |
| `message` | 消息表 |

## 🔐 默认账户

| 类型 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 测试用户 | 13800000000 | password123 |

## 📄 许可证

本项目基于 Apache License 2.0 许可证。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

**开发日期**: 2026年1月  
**版本**: 1.0.0
