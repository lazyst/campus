# 校园互助平台

## 一、项目概述

校园互助平台是一个面向高校学生的综合性校园服务应用，旨在为学生提供便捷的信息交流、闲置交易和即时通讯服务。平台采用现代化的前后端分离架构，基于 Vue 3 和 Spring Boot 3.2 技术栈构建，为用户提供流畅的使用体验。

本项目致力于解决高校学生在日常生活中面临的各种需求，包括校园信息获取、二手物品交易、即时沟通交流等方面。通过构建一个安全、便捷、功能丰富的校园生态系统，平台帮助学生更好地利用校园资源，促进学生之间的互助与合作。

平台具备完善的模块化设计，涵盖用户认证与个人管理、论坛发帖与互动、闲置物品交易、即时消息通讯以及后台管理系统等核心功能模块。各模块之间相互独立又有机协作，共同构成一个完整的校园服务生态。平台支持多端访问，包括移动端用户应用和管理端网页应用，满足不同场景下的使用需求。

## 二、功能特性

### 2.1 用户功能

用户功能模块为平台的基础模块，提供完整的用户生命周期管理服务。新用户可通过手机号注册账号并完成登录，已注册用户可随时修改个人资料、上传头像、更新联系方式等。系统采用 JWT 进行身份认证，确保用户数据的安全性和隐私性。用户可以在个人中心查看和管理自己的帖子、闲置物品、收藏内容以及消息记录，全面掌控自己在平台上的数据和活动。

### 2.2 论坛功能

论坛功能模块是平台的核心功能之一，为用户提供信息发布和交流的空间。平台预设多个讨论板块，涵盖学习交流、生活分享、校园活动、二手交易等主题，用户可根据内容类型选择相应的板块进行发帖。帖子支持富文本编辑，可包含文字、图片等多种内容形式，其他用户可对帖子进行点赞、收藏和评论操作，形成良好的互动氛围。系统自动记录帖子的发布时间、浏览量、点赞数等数据，帮助用户了解自己内容的传播效果。

### 2.3 闲置交易

闲置交易模块为学生提供便捷的二手物品交易服务。用户可以发布闲置物品信息，包括物品名称、描述、价格、新旧程度、实物图片等详细内容。物品支持多种状态管理，包括上架中、已下架、已交易完成等状态，便于用户追踪和管理自己的交易物品。其他用户可以浏览闲置列表，按分类筛选或通过搜索关键词查找感兴趣的商品，可以收藏喜欢的物品以便后续查看。平台提供联系卖家功能，买卖双方可以通过站内私信进行沟通，达成交易意向。

### 2.4 即时聊天

即时聊天模块为平台用户提供实时通讯服务。用户可以与平台上的其他用户进行一对一私信交流，支持文字消息的实时发送和接收。系统维护完整的会话历史记录，用户可以随时查看与各联系人的聊天记录。新消息到达时系统会推送通知提醒，确保用户及时收到重要信息。聊天功能与交易模块深度集成，便于买卖双方就交易细节进行沟通协商。

### 2.5 后台管理

后台管理模块为平台运营者提供全面的系统管理功能。管理员可以通过仪表盘查看平台的关键数据指标，包括用户数量、帖子数量、闲置数量、活跃度等统计信息。系统提供完整的用户管理功能，管理员可以查看用户详情、禁用或启用用户账号。内容管理功能允许管理员对违规帖子和闲置信息进行删除或下架处理，维护平台的健康环境。板块管理功能支持讨论板块的创建、编辑和删除，满足平台运营过程中的板块调整需求。

## 三、技术栈

### 3.1 前端技术栈

| 领域 | 技术选型 | 版本要求 | 说明 |
|------|----------|----------|------|
| 框架 | Vue 3 | 3.4+ | 采用组合式 API 和 Composition 语法 |
| 用户端组件库 | Vant UI | 4.x | 轻量级移动端 Vue 3 组件库 |
| 管理端组件库 | Element Plus | 2.x+ | 企业级 Vue 3 组件库 |
| 状态管理 | Pinia | 2.x+ | Vue 3 官方推荐状态管理方案 |
| 路由管理 | Vue Router | 4.x+ | Vue 3 官方路由解决方案 |
| 样式框架 | Tailwind CSS | 4.x | 原子化 CSS 框架 |
| 样式预处理器 | SCSS | - | 管理端使用 |

### 3.2 后端技术栈

| 领域 | 技术选型 | 版本要求 | 说明 |
|------|----------|----------|------|
| 框架 | Spring Boot | 3.2+ | 自动配置、约定优于配置 |
| 语言 | Java | 17+ | 使用密封类、模式匹配等新特性 |
| ORM 框架 | MyBatis-Plus | 3.5+ | 增强 SQL 编写，减少模板代码 |
| 认证授权 | JWT | 0.9+ | JSON Web Token 实现无状态认证 |
| API 文档 | Swagger | 3.0 | OpenAPI 3.0 规范，自动生成文档 |
| 数据库 | MySQL | 8.0+ | 关系型数据库存储核心数据 |
| 缓存 | Redis | 7.0+ | 缓存热点数据、会话存储 |
| 读写分离 | MySQL-Proxy | - | 主从复制，读写分离 |

### 3.3 部署技术栈

| 领域 | 技术选型 | 说明 |
|------|----------|------|
| 容器化 | Docker | 应用容器化部署 |
| 容器编排 | Docker Swarm | 生产环境集群部署 |
| 反向代理 | Nginx | 请求转发、负载均衡 |
| 操作系统 | Ubuntu 24.04 | 推荐部署环境 |

## 四、项目结构

```
campus/
├── backend/                        # 后端 Spring Boot 项目
│   ├── src/main/java/com/campus/
│   │   ├── config/                 # 配置类
│   │   ├── common/                 # 公共组件（Result、异常处理等）
│   │   ├── modules/                # 业务模块
│   │   │   ├── auth/               # 认证模块
│   │   │   ├── forum/              # 论坛模块
│   │   │   ├── trade/              # 交易模块
│   │   │   ├── chat/               # 聊天模块
│   │   │   ├── user/               # 用户模块
│   │   │   └── admin/              # 后台管理模块
│   │   └── CampusApplication.java  # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml         # 配置文件
│   │   ├── application-dev.yml     # 开发环境配置
│   │   └── mapper/                 # MyBatis XML 映射文件
│   └── pom.xml                     # Maven 构建配置
│
├── frontend-user/                  # 用户前端 Vue 3 应用
│   ├── src/
│   │   ├── api/                    # API 接口定义
│   │   ├── components/             # 通用组件
│   │   ├── views/                  # 页面组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── styles/                 # 样式文件
│   │   └── main.js                 # 入口文件
│   ├── public/                     # 静态资源
│   ├── package.json                # 依赖配置
│   └── vite.config.js              # Vite 构建配置
│
├── frontend-admin/                 # 管理前端 Vue 3 应用
│   ├── src/
│   │   ├── api/                    # API 接口定义
│   │   ├── components/             # 通用组件
│   │   ├── views/                  # 页面组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── styles/                 # SCSS 样式文件
│   │   └── main.js                 # 入口文件
│   ├── public/                     # 静态资源
│   ├── package.json                # 依赖配置
│   └── vite.config.js              # Vite 构建配置
│
├── deploy/                          # 部署配置目录
│   ├── docker-compose.dev.yml       # Docker Compose 开发环境配置
│   ├── docker-stack.yml             # Docker Stack 生产环境配置
│   ├── Dockerfile.backend           # 后端 Dockerfile
│   ├── Dockerfile.frontend          # 前端 Dockerfile
│   ├── Dockerfile.nginx            # Nginx Dockerfile
│   └── nginx.conf                   # Nginx 配置
│
├── docs/                           # 项目文档
│   ├── ARCHITECTURE.md             # 架构设计文档
│   ├── API.md                      # API 接口文档
│   ├── BUSINESS_FLOW.md            # 业务流程文档
│   ├── DEPLOYMENT_PROD.md          # 生产环境部署文档
│   ├── E2E_TEST_PLAN.md            # 端到端测试计划
│   ├── ENVIRONMENT_VARIABLES.md    # 环境变量说明
│   ├── ENV_SETUP.md                # 环境搭建指南
│   ├── PRD.md                      # 产品需求文档
│   ├── QUICK_START.md              # 快速开始指南
│   └── TROUBLESHOOTING.md          # 常见问题排查
│
└── README.md                       # 项目说明文档
```

## 五、快速开始

### 5.1 环境检查

在开始项目搭建之前，请确保开发环境中已安装以下必需软件：

```bash
# 检查 Node.js 版本（要求 >= 18.0）
node --version

# 检查 Java 版本（要求 >= 17）
java -version

# 检查 Maven 版本（要求 >= 3.8）
mvn --version

# 检查 MySQL 版本（要求 >= 8.0）
mysql --version

# 检查 Git 版本
git --version
```

如果上述软件未安装或版本不满足要求，请根据后续章节的指引进行安装和配置。

### 5.2 数据库启动

项目使用 MySQL（主从复制）和 Redis（缓存），推荐通过 WSL Docker 容器运行：

```bash
# 启动 MySQL 容器（需要先配置字符集）
wsl -d Ubuntu-24.04 -- bash /mnt/d/develop/campus-fenbushi/scripts/start-mysql.sh

# 或者手动启动
wsl -d Ubuntu-24.04 -- docker start campus-mysql campus-redis-master

# 验证容器运行状态
wsl -d Ubuntu-24.04 -- docker ps
```

本地端口映射：

| 服务 | 本地地址 | 用途 |
|------|----------|------|
| MySQL Master | localhost:3306 | 写操作 |
| MySQL Slave | localhost:3307 | 读操作 |
| Redis | localhost:6379 | 缓存 |

如果使用本地 MySQL 服务，请手动创建数据库：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE campus_fenbushi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 退出 MySQL
exit

# 导入初始化脚本
mysql -u root -p campus_fenbushi < mysql/init.sql
```

初始化脚本会自动创建所有数据表并插入初始数据，包括讨论板块和管理员账号。

### 5.3 后端启动

后端服务运行在 8080 端口，提供 RESTful API 接口：

```bash
# 进入后端项目目录
cd backend

# 安装 Maven 依赖
mvn clean install

# 启动后端服务（开发模式，指定 dev 配置）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或者打包后运行
mvn package -DskipTests
java -jar target/campus-backend.jar
```

后端服务启动成功后，可通过以下地址访问：

- API 根地址：http://localhost:8080/api
- Swagger 文档：http://localhost:8080/swagger-ui.html

### 5.4 前端启动

项目包含两个前端应用，分别服务不同用户群体：

#### 5.4.1 用户前端启动

用户前端应用运行在 3000 端口，提供移动端访问体验：

```bash
# 进入用户前端项目目录
cd frontend-user

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

用户前端启动成功后，可通过 http://localhost:3000 访问。

#### 5.4.2 管理前端启动

管理前端应用运行在 3001 端口，提供桌面端管理界面：

```bash
# 进入管理前端项目目录
cd frontend-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

管理前端启动成功后，可通过 http://localhost:3001 访问。

### 5.5 访问验证

完成以上步骤后，可通过以下地址验证各服务是否正常运行：

| 服务 | 访问地址 | 说明 |
|------|----------|------|
| 用户前端 | http://localhost:3000 | 移动端校园互助平台 |
| 管理前端 | http://localhost:3001 | 后台管理系统 |
| 后端 API | http://localhost:8080/api | RESTful API 接口 |
| API 文档 | http://localhost:8080/swagger-ui.html | Swagger 在线文档 |

首次访问时，可使用以下测试账号进行登录：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 普通用户 | 13800000001 | 123456 |
| 管理员 | admin | admin123 |

## 六、Docker 快速启动

对于希望快速体验或部署项目的用户，可以使用 Docker Compose 进行一键启动：

```bash
# 进入部署目录
cd deploy

# 启动所有服务
docker-compose -f docker-compose.dev.yml up -d

# 查看服务状态
docker-compose -f docker-compose.dev.yml ps

# 查看服务日志
docker-compose -f docker-compose.dev.yml logs -f

# 停止所有服务
docker-compose -f docker-compose.dev.yml down
```

Docker Compose 会自动启动以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL Master | 3306 | 数据库主库（写操作） |
| MySQL Slave | 3307 | 数据库从库（读操作） |
| Redis | 6379 | 缓存服务 |
| Backend | 8080 | 后端 API 服务 |
| Frontend-User | 3000 | 用户前端服务 |
| Frontend-Admin | 3001 | 管理前端服务 |
| Nginx | 80 | 反向代理服务 |

首次启动 Docker 环境时，首次启动可能需要较长时间下载基础镜像。启动完成后，可通过 http://localhost 访问用户前端应用，管理前端可通过 http://localhost/admin 访问。

## 七、API 文档

项目采用 OpenAPI 3.0 规范进行 API 文档编写，并通过 Swagger UI 提供在线文档浏览和接口测试功能。

### 7.1 开发环境访问

在本地开发环境下，API 文档访问地址为：http://localhost:8080/swagger-ui.html

Swagger 文档提供了以下功能：

- 接口列表浏览：按模块分类展示所有 API 接口
- 接口详情查看：查看每个接口的请求参数、响应格式
- 在线接口测试：可以直接在页面中发送请求测试接口
- 代码生成支持：支持生成多种语言的客户端代码

### 7.2 离线 API 文档

详细的 API 接口说明请参考项目文档 docs/API.md，该文档提供了完整的接口描述、请求示例、响应示例和错误码说明。

### 7.3 认证说明

大部分 API 接口需要进行身份认证，认证方式为 JWT Token：

```bash
# 登录获取 Token
POST /api/auth/login
Content-Type: application/json

{
  "phone": "13800000001",
  "password": "123456"
}

# 响应
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {...}
  }
}

# 后续请求携带 Token
Authorization: Bearer <your-token>
```

## 八、文档导航

项目文档位于 docs/ 目录下，以下是各文档的用途说明：

| 文档 | 说明 |
|------|------|
| ARCHITECTURE.md | 系统架构设计指南，包含技术选型、架构模式、安全设计等内容 |
| API.md | API 接口完整参考，包含所有接口的详细说明和示例 |
| BUSINESS_FLOW.md | 业务流程文档，包含各功能模块的业务流程说明 |
| DEPLOYMENT_PROD.md | 生产环境部署文档，包含 Docker Swarm 部署配置 |
| E2E_TEST_PLAN.md | 端到端测试计划，包含测试用例和测试流程 |
| ENVIRONMENT_VARIABLES.md | 环境变量说明，包含所有配置项的详细说明 |
| ENV_SETUP.md | 环境搭建指南，包含开发环境配置说明 |
| PRD.md | 产品需求文档，包含功能需求和设计决策 |
| QUICK_START.md | 快速开始指南，帮助开发者快速启动项目 |
| TROUBLESHOOTING.md | 常见问题排查指南，包含常见问题及解决方案 |

## 九、常见问题

### 9.1 后端启动失败

如果后端启动失败，请检查以下几点：

- 数据库连接配置是否正确，确保 MySQL 服务已启动
- 端口 8080 是否被占用，可通过 `netstat -ano | findstr 8080` 查看
- Java 版本是否为 17 或更高版本
- Maven 依赖是否完整，可尝试执行 `mvn clean install` 重新下载依赖

### 9.2 前端启动失败

如果前端启动失败，请检查以下几点：

- Node.js 版本是否为 18 或更高版本
- 依赖是否安装完整，可尝试删除 node_modules 后重新执行 `npm install`
- 端口 3000 或 3001 是否被占用
- Vite 配置是否正确

### 9.3 数据库连接失败

如果出现数据库连接失败，请检查以下几点：

- MySQL 服务是否已启动
- 数据库用户名和密码是否正确
- 数据库是否已创建
- 用户是否具有足够的数据库权限

## 十、贡献指南

感谢您对校园互助项目的关注！如果您希望为项目贡献代码，请遵循以下步骤：

1. Fork 本项目到您的 GitHub 账户
2. 创建您的特性分支：`git checkout -b feature/amazing-feature`
3. 提交您的更改：`git commit -m 'feat: 添加新功能'`
4. 推送您的分支：`git push origin feature/amazing-feature`
5. 创建 Pull Request，等待代码审核

在提交代码前，请确保已通过代码检查和测试：

```bash
# 后端代码检查
cd backend
mvn checkstyle:check

# 前端代码检查
cd frontend-user
npm run lint

cd frontend-admin
npm run lint
```

## 十一、许可证

本项目基于 MIT 许可证开源，您可以自由使用、修改和分发本项目的代码，但需要保留原始的版权声明和许可证文本。

---

**更新日期**：2026年3月
