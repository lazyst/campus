# 系统架构设计

## 技术架构

### 整体架构分层

本项目采用经典的前后端分离架构，整体分为五个层次：

```
┌─────────────────────────────────────────────────────────────┐
│                      前端展示层                             │
│  ┌─────────────────┐        ┌─────────────────┐           │
│  │  frontend-user  │        │ frontend-admin  │           │
│  │  (Vue 3 + Vant) │        │  (Vue 3 +      │           │
│  │  端口: 3000)    │        │   Element Plus)│           │
│  │                 │        │   端口: 3001)  │           │
│  └────────┬────────┘        └────────┬────────┘           │
├───────────┼──────────────────────────┼───────────────────┤
│           │        API 网关层        │                   │
│           │    ┌─────────────────┐   │                   │
│           └───►│  Nginx/Vite    │◄──┘                   │
│                │  代理/静态服务    │                       │
│                └────────┬────────┘                       │
├─────────────────────────┼────────────────────────────────┤
│                        │   业务应用层                    │
│                        ▼                                  │
│           ┌─────────────────────────────┐                │
│           │     Spring Boot 3.2         │                │
│           │     (Java 17 + MyBatis-Plus)│                │
│           │     端口: 8080              │                │
│           └─────────────┬───────────────┘                │
│                         │                                │
├─────────────────────────┼────────────────────────────────┤
│                         │   数据访问层                    │
│           ┌─────────────┴─────────────┐                 │
│           │                           │                  │
│           ▼                           ▼                  │
│    ┌─────────────┐          ┌─────────────┐            │
│    │   MySQL 8.0  │          │   Redis     │            │
│    │  (主从复制)  │          │   缓存/会话  │            │
│    └─────────────┘          └─────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### 前端架构 (frontend-user)

- **框架**：Vue 3.4+ 组合式 API
- **构建工具**：Vite 5.0+
- **UI 组件**：Vant UI 4.x (移动端组件库)
- **状态管理**：Pinia
- **路由**：Vue Router 4.x
- **样式**：Tailwind CSS 4.x (原子化 CSS)
- **HTTP 客户端**：Axios
- **WebSocket**：sockjs-client + stompjs
- **测试框架**：Vitest

### 前端架构 (frontend-admin)

- **框架**：Vue 3.4+ 组合式 API
- **构建工具**：Vite 5.0+
- **UI 组件**：Element Plus 2.x (PC 端组件库)
- **状态管理**：Pinia
- **路由**：Vue Router 4.x
- **样式**：SCSS
- **HTTP 客户端**：Axios
- **图表**：ECharts 5.x (仪表盘统计)

### 后端架构 (Spring Boot)

- **核心框架**：Spring Boot 3.2.0
- **开发语言**：Java 17
- **ORM 框架**：MyBatis-Plus 3.5.5
- **数据源**：Dynamic-Datasource 4.5.0 (读写分离)
- **连接池**：Druid 1.2.21
- **认证**：JWT (jjwt 0.12.3)
- **API 文档**：SpringDoc OpenAPI 2.3.0
- **WebSocket**：Spring WebSocket
- **缓存**：Spring Data Redis
- **日志**：SLF4J + Logback

### 数据存储架构

- **主数据库**：MySQL 8.0 (写操作)
- **从数据库**：MySQL 8.0 (读操作)
- **缓存系统**：Redis 7.x (会话、热点数据、WebSocket 状态)
- **读写分离**：通过 AOP 自动切换数据源

## 核心流程

### 用户认证流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  用户    │────►│  登录    │────►│ JWT 验证 │────►│  生成    │
│          │     │  接口    │     │  过滤器   │     │  Token   │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                                      │
                    ┌─────────────────────────────────┘
                    ▼
              ┌──────────┐     ┌──────────┐
              │  Token   │────►│  后续请求 │
              │  存储    │     │  携带 Token│
              └──────────┘     └──────────┘
```

1. 用户提交手机号和密码
2. 后端验证用户凭证
3. 生成 JWT Token (包含用户 ID、角色等信息)
4. Token 返回给前端存储
5. 后续请求携带 Token (Authorization: Bearer xxx)
6. JWT 过滤器验证 Token 有效性

### 帖子发布与互动流程

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  发帖    │────►│  数据    │────►│  写入    │────►│  触发    │
│  请求    │     │  校验    │     │  数据库   │     │  通知    │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                                      │
                    ┌─────────────────────────────────┘
                    ▼
              ┌──────────┐     ┌──────────┐
              │  其他   │────►│  更新    │
              │  用户   │     │  计数    │
              │  点赞   │     │  (Redis) │
              └──────────┘     └──────────┘
```

### 即时消息流程 (WebSocket)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  发送方  │────►│ WebSocket│────►│  消息    │────►│  接收方  │
│  发送消息│     │  连接    │     │  路由    │     │  接收消息│
└──────────┘     └──────────┘     └──────────┘     └──────────┘
       │                               │
       │                               ▼
       │                         ┌──────────┐
       │                         │  持久化   │
       │                         │  数据库   │
       │                         └──────────┘
       ▼
 ┌──────────┐
 │  推送    │
 │  通知    │
 └──────────┘
```

## 关键组件

### 后端关键组件

| 组件 | 职责 | 位置 |
|------|------|------|
| **AuthController** | 处理用户登录、注册、Token 刷新 | `modules/auth/controller/` |
| **PostController** | 帖子 CRUD、点赞、收藏 | `modules/forum/controller/` |
| **CommentController** | 评论管理 | `modules/forum/controller/` |
| **ItemController** | 闲置物品管理 | `modules/trade/controller/` |
| **ChatController** | 消息管理 | `modules/chat/controller/` |
| **UserController** | 用户资料管理 | `modules/user/controller/` |
| **AdminController** | 后台管理功能 | `modules/admin/controller/` |
| **JwtAuthenticationFilter** | JWT 请求鉴权 | `config/` |
| **DynamicDataSourceConfig** | 动态数据源配置 | `config/` |
| **WebSocketConfig** | WebSocket 配置 | `config/` |
| **GlobalExceptionHandler** | 全局异常处理 | `common/` |
| **Result** | 统一响应包装 | `common/` |

### 前端关键组件 (frontend-user)

| 组件 | 职责 | 位置 |
|------|------|------|
| **Login** | 用户登录注册 | `views/login/` |
| **Forum** | 论坛首页、帖子列表 | `views/forum/` |
| **PostDetail** | 帖子详情、评论 | `views/forum/detail/` |
| **Trade** | 闲置市场 | `views/trade/` |
| **ItemDetail** | 物品详情 | `views/trade/detail/` |
| **Messages** | 消息列表 | `views/messages/` |
| **Chat** | 聊天页面 | `views/messages/chat/` |
| **Profile** | 个人中心 | `views/profile/` |
| **useRequest** | 请求封装 Hook | `api/hooks/` |
| **usePagination** | 分页逻辑 Hook | `api/hooks/` |
| **userStore** | 用户状态管理 | `stores/` |

### 前端关键组件 (frontend-admin)

| 组件 | 职责 | 位置 |
|------|------|------|
| **Dashboard** | 仪表盘统计 | `views/dashboard/` |
| **UserManagement** | 用户管理 | `views/users/` |
| **PostManagement** | 帖子管理 | `views/posts/` |
| **ItemManagement** | 闲置管理 | `views/items/` |
| **BoardManagement** | 板块管理 | `views/boards/` |

### 数据库表结构

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `admin` | 管理员表 | id, username, password, role |
| `user` | 用户表 | id, phone, nickname, grade, major |
| `board` | 论坛板块表 | id, name, description, sort |
| `post` | 帖子表 | id, user_id, board_id, title, content |
| `comment` | 评论表 | id, user_id, post_id, content |
| `collect` | 帖子收藏表 | id, user_id, post_id |
| `like` | 帖子点赞表 | id, user_id, post_id |
| `item` | 闲置物品表 | id, user_id, title, price, status |
| `item_collect` | 物品收藏表 | id, user_id, item_id |
| `message` | 消息表 | id, sender_id, receiver_id, content |
| `notification` | 通知表 | id, user_id, type, is_read |
| `conversation` | 会话表 | id, user_id1, user_id2, last_message_id |

## 安全设计

### 认证与授权

- **JWT 无状态认证**：Token 包含用户身份信息，服务端不存储会话
- **Token 过期时间**：7 天 (604800000 ms)
- **敏感操作验证**：关键操作验证用户权限

### 数据安全

- **密码加密**：BCrypt 算法加密存储
- **SQL 注入防护**：MyBatis-Plus 参数化查询
- **XSS 防护**：输入过滤和转义
- **CORS 配置**：限制跨域请求来源

## 性能优化

### 后端优化

- **读写分离**：主库写、从库读，减轻主库压力
- **Redis 缓存**：热点数据缓存，减少数据库查询
- **连接池**：Druid 连接池管理
- **懒加载**：MyBatis-Plus 关联对象懒加载

### 前端优化

- **代码分割**：路由级别代码分割
- **资源压缩**：Vite 生产构建压缩
- **依赖优化**：按需加载组件
- **图片优化**：懒加载图片资源
