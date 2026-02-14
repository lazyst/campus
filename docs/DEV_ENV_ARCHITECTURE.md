# 开发环境启动架构

## 架构图

```mermaid
flowchart TB
    subgraph WSL["WSL Docker 环境"]
        subgraph DockerNet["Docker 网络"]
            MySQL_Master["MySQL Master<br/>:3306 写"]
            MySQL_Slave["MySQL Slave<br/>:3307 读"]
            Redis["Redis<br/>:6379"]
        end
    end

    subgraph Local["本地开发环境"]
        Backend["Spring Boot 后端<br/>:8080"]

        subgraph Frontend["前端应用"]
            UserFE["用户前端<br/>:3000"]
            AdminFE["管理后台<br/>:3001"]
        end
    end

    subgraph DevTools["开发工具"]
        IDE["IDE<br/>IntelliJ / VSCode"]
    end

    User["用户浏览器"] --> UserFE
    Admin["管理员浏览器"] --> AdminFE

    UserFE -->|"HTTP / WebSocket"| Backend
    AdminFE -->|"HTTP"| Backend

    Backend -->|"写操作 INSERT/UPDATE/DELETE"| MySQL_Master
    Backend -->|"读操作 SELECT"| MySQL_Slave
    Backend -->|"缓存 / Session"| Redis

    IDE -.->|"代码编辑"| Backend
    IDE -.->|"代码编辑"| UserFE
    IDE -.->|"代码编辑"| AdminFE

    style MySQL_Master fill:#00758F,color:#fff
    style MySQL_Slave fill:#4A90D9,color:#fff
    style Redis fill:#DC382D,color:#fff
    style Backend fill:#6DB33F,color:#fff
    style UserFE fill:#42B883,color:#fff
    style AdminFE fill:#409EFF,color:#fff
```

## 数据流向说明

| 操作类型 | 目标数据库 |
|----------|------------|
| 写操作 (INSERT/UPDATE/DELETE) | Master (localhost:3306) |
| 读操作 (SELECT) | Slave (localhost:3307) |
| 缓存/Session | Redis (localhost:6379) |

## 启动命令

**后端** (Java 17 + Spring Boot):
```bash
cd backend
mvn spring-boot:run
```

**用户前端**:
```bash
cd frontend-user
npm run dev
```

**管理后台**:
```bash
cd frontend-admin
npm run dev
```

## 端口说明

| 服务 | 地址 |
|------|------|
| 用户前端 | http://localhost:3000 |
| 管理后台 | http://localhost:3001 |
| 后端 API | http://localhost:8080 |
| MySQL Master (WSL) | localhost:3306 |
| MySQL Slave (WSL) | localhost:3307 |
| Redis (WSL) | localhost:6379 |

## 技术栈

- **后端**: Spring Boot 3.2 + Java 17 + MyBatis-Plus (读写分离)
- **用户前端**: Vue 3 + Vant UI + Tailwind CSS v4
- **管理后台**: Vue 3 + Element Plus
- **数据库**: MySQL 8.0 (主从复制)
- **缓存**: Redis 7
