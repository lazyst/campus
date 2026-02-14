# 校园帮平台 - 业务流程图

## 整体架构流程

```mermaid
flowchart TB
    subgraph Client ["客户端"]
        direction TB
        User["用户端<br/>localhost:3000"]
        Admin["管理端<br/>localhost:3001"]
    end

    subgraph Gateway ["nginx 反向代理"]
        NGINX[nginx<br/>80/443端口]
    end

    subgraph Backend ["后端服务 Spring Boot"]
        direction TB

        Auth["认证模块<br/>/api/auth/**"]
        UserModule["用户模块<br/>/api/user/**, /api/users/**"]
        Forum["论坛模块<br/>/api/posts,/api/comments,/api/likes,/api/collects,/api/boards"]
        Trade["闲置市场<br/>/api/items,/api/item-collects"]
        Chat["即时通讯<br/>/api/chat, /api/messages, /ws"]
        AdminModule["管理模块<br/>/api/admin/**"]
        Upload["文件上传<br/>/api/upload"]
        Notify["通知模块<br/>/api/notifications"]
    end

    subgraph Data ["数据层"]
        Redis[(Redis<br/>缓存/消息队列/Pub-Sub)]
        MySQL[(MySQL<br/>主库/从库)]
    end

    User --> NGINX
    Admin --> NGINX
    NGINX --> Backend

    Backend --> Auth
    Backend --> UserModule
    Backend --> Forum
    Backend --> Trade
    Backend --> Chat
    Backend --> AdminModule
    Backend --> Upload
    Backend --> Notify

    Auth --> Redis
    Auth --> MySQL
    UserModule --> MySQL
    Forum --> MySQL
    Trade --> MySQL
    Chat --> Redis
    Chat --> MySQL
    AdminModule --> MySQL
    Upload --> MySQL
    Notify --> MySQL
```

## 核心业务模块

### 1. 用户模块

```mermaid
flowchart LR
    subgraph 用户模块
        direction TB
        Register["用户注册<br/>手机号+密码+昵称"]
        Login["用户登录<br/>手机号+密码"]
        Profile["个人信息管理<br/>查看/编辑资料"]
        Token["JWT令牌<br/>7天有效期"]
        Avatar["头像上传<br/>最大2MB"]
    end

    Register --> Login
    Login --> Token
    Token --> Profile
    Profile --> Avatar
```

**用户端功能:**
- 注册: 手机号 + 密码 + 昵称
- 登录: 返回JWT Token
- 个人信息: 昵称、性别、简介、头像、年级、专业
- 查看他人主页: 获取用户公开信息和发布内容

### 2. 论坛模块

```mermaid
flowchart LR
    subgraph 论坛模块
        direction TB
        Boards["浏览板块"]
        PostList["查看帖子列表<br/>分页+搜索+板块筛选"]
        PostDetail["查看帖子详情<br/>自动增加浏览量"]
        CreatePost["发布帖子<br/>标题+内容+图片"]
        Comment["发表评论<br/>层级回复<br/>（楼中楼）"]
        Like["点赞/取消点赞"]
        Collect["收藏/取消收藏"]
        Notify["消息通知<br/>评论/点赞/收藏"]
    end

    Boards --> PostList
    PostList --> PostDetail
    PostDetail --> CreatePost
    PostDetail --> Comment
    PostDetail --> Like
    PostDetail --> Collect
    Comment --> Notify
    Like --> Notify
    Collect --> Notify
```

**数据结构:**
- `board`: 板块表 (id, name, description, icon, sort, status)
- `post`: 帖子表 (id, userId, boardId, title, content, images, viewCount, likeCount, commentCount, collectCount)
- `comment`: 评论表 (id, userId, postId, parentId, content) - 支持楼中楼回复
- `like`: 点赞表 (id, userId, postId) - 物理删除
- `collect`: 收藏表 (id, userId, postId) - 物理删除

### 3. 闲置市场模块

```mermaid
flowchart LR
    subgraph 闲置市场
        direction TB
        ItemList["浏览商品<br/>分类+价格排序+搜索"]
        ItemDetail["查看商品详情<br/>自动增加浏览量"]
        Publish["发布商品<br/>类型(收购/出售)+图片+价格+描述"]
        Favorite["收藏商品/取消收藏"]
        Contact["联系卖家<br/>创建会话"]
        Manage["商品管理<br/>上下架/编辑/删除/完成"]
    end

    ItemList --> ItemDetail
    ItemDetail --> Publish
    ItemDetail --> Favorite
    ItemDetail --> Contact
    Publish --> Manage
```

**物品类型:**
- 类型: 1=收购, 2=出售
- 状态: 0=已删除, 1=正常, 2=已完成, 3=已下架

### 4. 即时通讯模块

```mermaid
sequenceDiagram
    participant U as 用户A
    participant W as WebSocket /ws
    participant R as Redis Pub/Sub
    participant M as MySQL

    U->>W: 建立连接(携带Token)
    W->>W: JWT认证解析用户ID
    W->>R: 订阅消息队列

    Note over U,R: 用户A发送消息给用户B
    U->>W: STOMP SEND /app/chat.send/{receiverId}
    W->>M: 1.保存消息到message表
    W->>M: 2.创建/更新会话conversation
    W->>R: 3.发布到chat:message频道
    R->>W: 推送消息给用户B
    W->>U: 实时送达 /user/{userId}/queue/messages

    Note over U,R: 集群环境支持
    Server1->>R: 发布消息
    R->>Server2: 跨服务器推送
```

**WebSocket配置:**
- 端点: `/ws`, `/ws/` (支持SockJS)
- STOMP代理: 简单内存代理
- 广播主题: `/topic/*`
- 点对点队列: `/queue/*`
- 应用前缀: `/app`
- 用户前缀: `/user`

**Redis Pub/Sub:**
- 频道: `chat:message` - 用于集群环境消息同步
- 订阅者: `ChatMessageSubscriber` - 接收消息并推送给用户

**数据结构:**
- `conversation`: 会话表 (id, userId1, userId2, lastMessageId, unreadCount1, unreadCount2)
- `message`: 消息表 (id, conversationId, senderId, receiverId, content, type)

### 5. 通知模块

```mermaid
flowchart LR
    subgraph 通知模块
        direction TB
        NotifyList["通知列表"]
        NotifyType["通知类型<br/>1=评论 2=点赞 3=收藏"]
        MarkRead["标记已读<br/>单条/全部"]
        UnreadCount["未读数量"]
    end

    NotifyType --> NotifyList
    NotifyList --> MarkRead
    MarkRead --> UnreadCount
```

**触发场景:**
- 评论: 他人评论你的帖子
- 点赞: 他人点赞你的帖子
- 收藏: 他人收藏你的帖子

### 6. 管理后台模块

```mermaid
flowchart TB
    subgraph 管理后台
        direction TB
        AdminLogin["管理员登录<br/>用户名+密码"]
        Dashboard["仪表盘<br/>数据统计+趋势+最近活跃"]
        UserMgmt["用户管理<br/>查看/禁用/启用/封禁/删除"]
        PostMgmt["帖子管理<br/>查看/删除/查看评论"]
        BoardMgmt["板块管理<br/>增删改"]
        ItemMgmt["物品管理<br/>查看/下架/删除"]
        StorageMgmt["存储管理<br/>图片上传/清理未使用"]
    end

    AdminLogin --> Dashboard
    Dashboard --> UserMgmt
    Dashboard --> PostMgmt
    Dashboard --> BoardMgmt
    Dashboard --> ItemMgmt
    Dashboard --> StorageMgmt
```

**管理员角色:**
- 超级管理员 (role=1): 全部权限
- 普通管理员 (role=2): 有限权限

## 前端页面结构

### 用户端 (localhost:3000)

```mermaid
flowchart TB
    subgraph 用户端
        direction TB
        Main["四大主Tab"]
        Detail["详情页"]
        Profile["个人中心"]
    end

    subgraph Main
        Forum["论坛 /"]
        Trade["闲置市场 /trade"]
        Messages["消息中心 /messages"]
        My["个人中心 /profile"]
    end

    subgraph Detail
        PostDetail["帖子详情 /forum/detail/:id"]
        ItemDetail["商品详情 /trade/:id"]
        ChatDetail["聊天详情 /messages/:id"]
    end

    subgraph Profile
        Edit["编辑资料 /profile/edit"]
        MyPosts["我的帖子 /profile/posts"]
        MyItems["我的闲置 /profile/items"]
        Collections["我的收藏 /profile/collections"]
        Notifications["系统通知 /profile/messages"]
        UserProfile["他人主页 /profile/user/:id"]
    end

    Forum --> PostDetail
    Trade --> ItemDetail
    Messages --> ChatDetail
    My --> Edit
    My --> MyPosts
    My --> MyItems
    My --> Collections
    My --> Notifications
    My --> UserProfile
```

### 管理端 (localhost:3001)

```mermaid
flowchart TB
    subgraph 管理端
        direction TB
        Login["登录 /admin/login"]
        Dashboard["数据看板 /admin/"]
        UserMgmt["用户管理 /admin/users"]
        BoardMgmt["板块管理 /admin/boards"]
        PostMgmt["帖子管理 /admin/posts"]
        ItemMgmt["物品管理 /admin/items"]
        StorageMgmt["存储管理 /admin/storage"]
    end

    Login --> Dashboard
    Dashboard --> UserMgmt
    Dashboard --> BoardMgmt
    Dashboard --> PostMgmt
    Dashboard --> ItemMgmt
    Dashboard --> StorageMgmt
```

## 数据模型关系

```mermaid
erDiagram
    USER ||--o{ POST : "发布"
    USER ||--o{ COMMENT : "发表"
    USER ||--o{ LIKE : "点赞"
    USER ||--o{ COLLECT : "收藏"
    USER ||--o{ MESSAGE : "发送"
    USER ||--o{ NOTIFICATION : "接收"
    USER ||--o{ ITEM : "发布"
    USER ||--o{ ITEM_COLLECT : "收藏"

    BOARD ||--o{ POST : "包含"

    POST ||--o{ COMMENT : "包含"
    POST ||--o{ LIKE : "包含"
    POST ||--o{ COLLECT : "包含"
    POST ||--o{ NOTIFICATION : "触发"

    COMMENT ||--o{ COMMENT : "回复"

    CONVERSATION ||--o{ MESSAGE : "包含"
    CONVERSATION ||--o{ USER : "参与"
```

## API 路由概览

### 认证模块 `/api/auth/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |

### 用户模块 `/api/user/**`, `/api/users/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/user/profile | 获取当前用户信息 |
| PUT | /api/user/profile | 更新用户信息 |
| GET | /api/user/public/{userId} | 获取用户公开信息 |
| POST | /api/user/avatar | 上传头像 |
| GET | /api/users/{id} | 获取用户公开档案 |
| GET | /api/users/{id}/posts | 获取用户帖子 |
| GET | /api/users/{id}/items | 获取用户物品 |

### 论坛模块
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/posts | 帖子列表 |
| GET | /api/posts/{id} | 帖子详情 |
| POST | /api/posts | 发布帖子 |
| PUT | /api/posts/{id} | 更新帖子 |
| DELETE | /api/posts/{id} | 删除帖子 |
| GET | /api/posts/search | 搜索帖子 |
| POST | /api/posts/{postId}/like | 点赞/取消 |
| POST | /api/posts/{postId}/collect | 收藏/取消 |
| GET | /api/posts/collections | 收藏列表 |
| GET | /api/comments/post/{postId} | 评论列表 |
| POST | /api/comments |发表评论 |
| DELETE | /api/comments/{id} | 删除评论 |
| GET | /api/boards | 板块列表 |

### 闲置市场模块 `/api/items/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/items | 物品列表 |
| GET | /api/items/{itemId} | 物品详情 |
| POST | /api/items | 发布物品 |
| PUT | /api/items/{itemId} | 更新物品 |
| DELETE | /api/items/{itemId} | 删除物品 |
| PUT | /api/items/{itemId}/online | 上架 |
| PUT | /api/items/{itemId}/offline | 下架 |
| PUT | /api/items/{itemId}/complete | 标记完成 |
| POST | /api/items/{itemId}/contact | 联系卖家 |
| POST | /api/items/{itemId}/collect | 收藏/取消 |
| GET | /api/items/collected | 收藏列表 |

### 即时通讯模块 `/api/chat/**`, `/api/messages/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/conversations | 会话列表 |
| GET | /api/conversations/{id}/messages | 消息历史 |
| GET | /api/messages/{userId} | 聊天记录 |
| POST | /api/messages/{userId} | 发送消息 |
| POST | /api/conversations/{id}/read | 标记已读 |
| GET | /api/conversations/unread/count | 未读数 |

**WebSocket:**
- 端点: `/ws`
- 发送: `/app/chat.send/{receiverId}`
- 接收: `/user/{userId}/queue/messages`

### 通知模块 `/api/notifications/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notifications | 通知列表 |
| GET | /api/notifications/unread/count | 未读数 |
| PUT | /api/notifications/{id}/read | 标记已读 |
| PUT | /api/notifications/read/all | 全部已读 |

### 管理模块 `/api/admin/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/admin/auth/login | 管理员登录 |
| GET | /api/admin/dashboard/overview | 仪表盘数据 |
| GET | /api/admin/users | 用户列表 |
| PUT | /api/admin/users/{id}/status | 更新状态 |
| PUT | /api/admin/users/{id}/ban | 封禁用户 |
| GET | /api/admin/posts | 帖子列表 |
| DELETE | /api/admin/posts/{id} | 删除帖子 |
| GET | /api/admin/items | 物品列表 |
| GET | /api/admin/boards | 板块列表 |
| POST | /api/admin/boards | 创建板块 |

### 文件上传 `/api/upload/**`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/upload/image | 单图上传 |
| POST | /api/upload/images | 多图上传 |
| DELETE | /api/upload/image | 删除图片 |
| GET | /api/upload/unused | 未使用图片 |
| DELETE | /api/upload/unused/clean | 清理未使用 |

## 读写分离架构

```mermaid
flowchart LR
    subgraph WritePath ["写操作"]
        App["应用服务"] --> |write| Master["主库 MySQL"]
    end

    subgraph ReadPath ["读操作"]
        App --> |read| Slave["从库 MySQL"]
    end

    Master -.->|同步| Slave
```

**实现方式:**
- `@DS("master")` - 写操作 (默认)
- `@DS("slave")` - 读操作
- 通过 `DsAspectConfig` 和 `ReadWriteRouteAspect` 配置

## 部署架构

```mermaid
flowchart TB
    subgraph Production ["生产环境 Docker Swarm"]
        direction TB
        LB["负载均衡 nginx"]

        subgraph Services ["微服务"]
            FE1["用户前端 x2"]
            FE2["管理前端 x2"]
            BE["后端服务 x2"]
        end

        DB["数据库集群<br/>MySQL + Redis"]
    end

    User[用户设备] --> LB
    Admin[管理员设备] --> LB
    LB --> FE1
    LB --> FE2
    LB --> BE
    BE --> DB
```

## 技术栈总结

| 层级 | 技术 |
|------|------|
| 前端用户 | Vue 3 + Vant UI + Tailwind CSS |
| 前端管理 | Vue 3 + Element Plus |
| 后端 | Spring Boot 3.2 + Java 17 |
| ORM | MyBatis-Plus |
| 数据库 | MySQL (读写分离) |
| 缓存/消息 | Redis |
| WebSocket | STOMP over WebSocket + Redis Pub/Sub |
| 反向代理 | nginx |

## 核心业务流程细节

### 1. 用户注册登录流程
```
1. 用户POST /api/auth/register (phone, password, nickname)
2. 后端验证并创建用户，返回JWT Token
3. 后续请求Header携带 Authorization: Bearer <token>
4. Token有效期7天
```

### 2. 帖子发布流程
```
1. 用户登录后访问 /forum/create
2. 上传图片到 /api/upload/images
3. POST /api/posts (title, content, images, boardId)
4. 后端保存帖子，返回帖子ID
5. 跳转到帖子详情页
```

### 3. 互动通知流程
```
1. 用户A点赞用户B的帖子
2. 后端:
   - 保存点赞记录到 like 表
   - 更新帖子 likeCount
   - 创建通知记录到 notification 表
3. 用户B收到通知 (WebSocket推送 + 通知列表)
```

### 4. 物品交易流程
```
1. 用户A发布物品 (类型: 收购/出售)
2. 用户B查看物品详情
3. 点击"联系卖家"
4. POST /api/items/{id}/contact
5. 后端创建会话 conversation
6. 双方进入聊天页面 /messages/:id
7. 通过 WebSocket 实时聊天
```

### 5. 管理员审核流程
```
1. 管理员登录 /admin/login
2. 查看仪表盘统计数据
3. 对违规内容:
   - 帖子: DELETE /api/admin/posts/{id}
   - 物品: PUT /api/admin/items/{id}/offline
   - 用户: PUT /api/admin/users/{id}/ban
```
