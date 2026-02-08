# 项目目录结构

## 项目根目录

```
campus-fenbushi/
├── backend/                        # Spring Boot 后端项目
├── frontend-user/                  # Vue 3 用户前端项目
├── frontend-admin/                 # Vue 3 管理前端项目
├── docs/                           # 项目文档目录
├── .opencode/                      # OpenCode 配置
├── .env                            # 环境变量配置
├── .env.example                    # 环境变量示例
├── .env.development                # 开发环境配置
├── .env.production                 # 生产环境配置
├── AGENTS.md                       # AI 助手编码规范
├── README.md                       # 项目说明文档
└── pom.xml                        # Maven 项目配置
```

## backend 目录结构

```
backend/
├── src/main/java/com/campus/
│   ├── CampusApplication.java      # Spring Boot 启动类
│   ├── config/                     # 配置类
│   │   ├── SecurityConfig.java           # Spring Security 配置
│   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   ├── JwtConfig.java                # JWT 配置
│   │   ├── DynamicDataSourceConfig.java  # 动态数据源配置
│   │   ├── RedisConfig.java              # Redis 配置
│   │   ├── WebSocketConfig.java          # WebSocket 配置
│   │   ├── SwaggerConfig.java            # Swagger 配置
│   │   ├── MybatisPlusConfig.java        # MyBatis-Plus 配置
│   │   └── WebConfig.java                # Web MVC 配置
│   │
│   ├── common/                     # 公共组件
│   │   ├── Result.java             # 统一响应包装类
│   │   ├── ResultCode.java         # 响应状态码枚举
│   │   └── GlobalExceptionHandler.java   # 全局异常处理
│   │
│   ├── entity/                     # 实体类
│   │   └── BaseEntity.java         # 基础实体类（公共字段）
│   │
│   ├── mapper/                     # 基础 Mapper
│   │   └── BaseMapper.java         # 通用 Mapper 接口
│   │
│   └── modules/                    # 业务模块
│       ├── auth/                   # 认证模块
│       │   ├── controller/
│       │   │   └── AuthController.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   └── impl/AuthServiceImpl.java
│       │   └── dto/
│       │       ├── LoginRequest.java
│       │       └── RegisterRequest.java
│       │
│       ├── forum/                  # 论坛模块
│       │   ├── controller/
│       │   │   ├── PostController.java
│       │   │   ├── CommentController.java
│       │   │   └── BoardController.java
│       │   ├── service/
│       │   │   ├── PostService.java
│       │   │   ├── CommentService.java
│       │   │   └── impl/
│       │   ├── mapper/
│       │   │   ├── PostMapper.java
│       │   │   ├── CommentMapper.java
│       │   │   └── BoardMapper.java
│       │   ├── entity/
│       │   │   ├── Post.java
│       │   │   ├── Comment.java
│       │   │   ├── Board.java
│       │   │   ├── Like.java
│       │   │   └── Collect.java
│       │   ├── dto/
│       │   └── publisher/
│       │       └── NotificationPublisher.java
│       │
│       ├── trade/                  # 交易模块
│       │   ├── controller/
│       │   │   ├── ItemController.java
│       │   │   └── ItemCollectController.java
│       │   ├── service/
│       │   │   ├── ItemService.java
│       │   │   └── impl/
│       │   ├── mapper/
│       │   │   ├── ItemMapper.java
│       │   │   └── ItemCollectMapper.java
│       │   ├── entity/
│       │   │   ├── Item.java
│       │   │   └── ItemCollect.java
│       │   └── dto/
│       │
│       ├── chat/                   # 聊天模块
│       │   ├── controller/
│       │   │   └── ChatController.java
│       │   ├── service/
│       │   │   ├── ChatService.java
│       │   │   └── impl/ChatServiceImpl.java
│       │   ├── mapper/
│       │   │   ├── MessageMapper.java
│       │   │   └── ConversationMapper.java
│       │   ├── entity/
│       │   │   ├── Message.java
│       │   │   └── Conversation.java
│       │   ├── websocket/
│       │   │   ├── WebSocketAuthInterceptor.java
│       │   │   └── WebSocketSecurityConfig.java
│       │   ├── subscriber/
│       │   │   └── ChatMessageSubscriber.java
│       │   └── publisher/
│       │       └── MessagePublisher.java
│       │
│       ├── user/                   # 用户模块
│       │   ├── controller/
│       │   │   ├── UserController.java
│       │   │   └── UserPublicController.java
│       │   ├── service/
│       │   │   ├── UserService.java
│       │   │   └── impl/UserServiceImpl.java
│       │   ├── mapper/
│       │   │   └── UserMapper.java
│       │   └── entity/
│       │       └── User.java
│       │
│       └── admin/                  # 后台管理模块
│           ├── controller/
│           │   ├── DashboardController.java
│           │   ├── UserManagementController.java
│           │   ├── PostManagementController.java
│           │   ├── ItemManagementController.java
│           │   ├── BoardManagementController.java
│           │   └── AdminAuthController.java
│           ├── service/
│           │   ├── DashboardService.java
│           │   └── AdminService.java
│           └── entity/
│               └── Admin.java
│
├── src/main/resources/
│   ├── application.yml             # 主配置文件
│   ├── application-dev.yml         # 开发环境配置
│   ├── mapper/                     # MyBatis XML 映射文件
│   └── logback-spring.xml          # 日志配置
│
├── src/test/java/com/campus/       # 测试代码
│   ├── ApplicationTest.java
│   └── modules/
│       ├── auth/AuthServiceTest.java
│       ├── forum/
│       │   ├── PostServiceTest.java
│       │   └── CommentServiceTest.java
│       └── trade/item/ItemServiceTest.java
│
├── src/test/resources/
│   ├── application-test.yml
│   ├── application-mysql-test.yml
│   ├── schema-h2.sql
│   └── test-data.sql
│
├── sql/
│   └── init.sql                    # 数据库初始化脚本
│
└── pom.xml                         # Maven 构建配置
```

## frontend-user 目录结构

```
frontend-user/
├── src/
│   ├── api/                        # API 接口层
│   │   ├── __tests__/              # API 测试套件
│   │   │   ├── README.md           # 测试文档
│   │   │   ├── setup.js            # 测试配置
│   │   │   ├── testHelpers.js      # 测试辅助函数
│   │   │   ├── auth.test.js        # 认证测试
│   │   │   ├── user.test.js        # 用户 API 测试
│   │   │   ├── board.test.js       # 板块测试
│   │   │   ├── post.test.js        # 帖子测试
│   │   │   ├── comment.test.js     # 评论测试
│   │   │   ├── notification.test.js # 通知测试
│   │   │   ├── item.test.js        # 物品测试
│   │   │   ├── itemCollect.test.js # 物品收藏测试
│   │   │   ├── conversation.test.js # 会话测试
│   │   │   ├── message.test.js     # 消息测试
│   │   │   └── hooks.test.js       # Hooks 测试
│   │   │
│   │   ├── hooks/                  # 组合式函数
│   │   │   ├── useRequest.js      # 请求封装
│   │   │   ├── usePagination.js    # 分页逻辑
│   │   │   └── useInfiniteList.js  # 无限滚动
│   │   │
│   │   ├── modules/                # 按模块组织的 API
│   │   │   ├── auth.js             # 认证模块
│   │   │   ├── user.js             # 用户模块
│   │   │   ├── board.js            # 板块模块
│   │   │   ├── post.js             # 帖子模块
│   │   │   ├── comment.js          # 评论模块
│   │   │   ├── notification.js     # 通知模块
│   │   │   ├── item.js             # 物品模块
│   │   │   ├── itemCollect.js      # 物品收藏
│   │   │   ├── conversation.js     # 会话模块
│   │   │   ├── message.js          # 消息模块
│   │   │   ├── upload.js           # 文件上传
│   │   │   └── index.js            # 统一导出
│   │   │
│   │   ├── request.js             # Axios 实例配置
│   │   └── toast.js               # Toast 提示封装
│   │
│   ├── components/                 # 通用组件库
│   │   ├── base/                   # 基础组件
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Cell.vue
│   │   │   └── Card.vue
│   │   ├── feedback/               # 反馈组件
│   │   │   ├── Skeleton.vue
│   │   │   └── SkeletonItem.vue
│   │   ├── interactive/            # 交互组件
│   │   │   ├── Dialog.vue
│   │   │   ├── Toast.vue
│   │   │   └── types/
│   │   │       ├── dialog.js
│   │   │       └── toast.js
│   │   ├── navigation/             # 导航组件
│   │   │   ├── NavBar.vue
│   │   │   └── TabBar.vue
│   │   ├── BottomNav.vue           # 底部导航
│   │   ├── SearchBar.vue           # 搜索栏
│   │   ├── ImagePreview.vue        # 图片预览
│   │   ├── ProductCard.vue         # 商品卡片
│   │   ├── PostCard.vue            # 帖子卡片
│   │   └── CategoryCard.vue         # 分类卡片
│   │
│   ├── views/                      # 页面组件
│   │   ├── home/                   # 首页
│   │   │   ├── index.vue
│   │   │   ├── Forum.vue           # 论坛首页
│   │   │   ├── Trade.vue           # 交易首页
│   │   │   ├── Messages.vue       # 消息页
│   │   │   ├── Profile.vue        # 个人页
│   │   │   └── components/
│   │   │       └── MyProfile.vue
│   │   ├── login/                  # 登录注册
│   │   │   └── index.vue
│   │   ├── register/
│   │   │   └── index.vue
│   │   ├── forum/                  # 论坛模块
│   │   │   ├── index.vue          # 帖子列表
│   │   │   ├── create/            # 发帖
│   │   │   │   └── index.vue
│   │   │   ├── detail/            # 帖子详情
│   │   │   │   └── index.vue
│   │   │   └── components/
│   │   │       └── ForumList.vue
│   │   ├── trade/                  # 闲置交易
│   │   │   ├── index.vue          # 物品列表
│   │   │   ├── create/            # 发布物品
│   │   │   │   └── index.vue
│   │   │   ├── detail/            # 物品详情
│   │   │   │   └── index.vue
│   │   │   └── components/
│   │   │       └── TradeList.vue
│   │   ├── messages/               # 消息模块
│   │   │   ├── index.vue          # 会话列表
│   │   │   ├── chat/              # 聊天页面
│   │   │   │   └── index.vue
│   │   │   └── components/
│   │   │       └── MessageList.vue
│   │   └── profile/                # 个人中心
│   │       ├── index.vue
│   │       ├── user/              # 个人信息
│   │       │   └── index.vue
│   │       ├── edit/              # 编辑资料
│   │       │   └── index.vue
│   │       ├── posts/             # 我的帖子
│   │       │   └── index.vue
│   │       ├── items/             # 我的闲置
│   │       │   └── index.vue
│   │       ├── collections/       # 我的收藏
│   │       │   └── index.vue
│   │       └── messages/          # 我的消息
│   │           └── index.vue
│   │
│   ├── stores/                    # Pinia 状态管理
│   │   ├── user.js               # 用户状态
│   │   └── loginConfirm.js       # 登录确认弹窗
│   │
│   ├── router/                    # 路由配置
│   │   └── index.js              # 路由定义
│   │
│   ├── utils/                     # 工具函数
│   ├── styles/                    # 样式文件
│   │   ├── design-tokens.css     # 设计令牌
│   │   └── variables.scss        # SCSS 变量
│   │
│   ├── layouts/                    # 布局组件
│   ├── services/                  # 服务层
│   ├── examples/                  # 示例代码
│   ├── __tests__/                 # 根级测试
│   ├── main.js                    # 应用入口
│   ├── App.vue                    # 根组件
│   └── auto-imports.d.ts         # 自动导入声明
│
├── public/                        # 公共静态资源
├── tests/                         # 测试配置
├── .env                            # 环境变量
├── package.json                   # 项目配置
├── vite.config.js                 # Vite 配置
├── tailwind.config.js             # Tailwind CSS 配置
└── jsconfig.json                  # JavaScript 配置
```

## frontend-admin 目录结构

```
frontend-admin/
├── src/
│   ├── api/                        # API 接口
│   │   └── admin/                  # 管理员 API
│   │       ├── auth.js            # 认证
│   │       ├── user.js            # 用户管理
│   │       ├── post.js            # 帖子管理
│   │       ├── item.js            # 闲置管理
│   │       ├── board.js           # 板块管理
│   │       ├── dashboard.js       # 仪表盘数据
│   │       └── storage.js        # 存储管理
│   │
│   ├── views/                      # 页面组件
│   │   ├── login/                 # 登录页
│   │   │   └── index.vue
│   │   ├── dashboard/             # 仪表盘
│   │   │   ├── index.vue
│   │   │   └── components/
│   │   │       ├── StatCard.vue       # 统计卡片
│   │   │       ├── TrendChart.vue     # 趋势图
│   │   │       ├── RecentActivity.vue # 最近活动
│   │   │       ├── QuickActions.vue  # 快捷操作
│   │   │       └── SystemStatus.vue   # 系统状态
│   │   ├── users/                 # 用户管理
│   │   │   └── index.vue
│   │   ├── posts/                 # 帖子管理
│   │   │   └── index.vue
│   │   ├── items/                 # 闲置管理
│   │   │   └── index.vue
│   │   ├── boards/                # 板块管理
│   │   │   └── index.vue
│   │   └── storage/               # 存储管理
│   │       └── index.vue
│   │
│   ├── router/                     # 路由配置
│   │   └── index.js
│   ├── stores/                     # Pinia 状态管理
│   ├── styles/                     # 样式文件
│   ├── layouts/                    # 布局组件
│   ├── __tests__/                  # 测试目录
│   ├── main.js                     # 入口文件
│   └── App.vue                     # 根组件
│
├── public/                         # 公共静态资源
├── .env                            # 环境变量
├── package.json                    # 项目配置
└── vite.config.js                 # Vite 配置
```

## 目录职责说明

### 根目录

| 目录/文件 | 职责 |
|-----------|------|
| `backend/` | Spring Boot 后端服务，包含所有业务逻辑和数据访问层 |
| `frontend-user/` | Vue 3 用户前端，移动端校园互助平台 |
| `frontend-admin/` | Vue 3 管理前端，后台管理系统 |
| `docs/` | 项目文档目录 |
| `AGENTS.md` | AI 助手编码规范 |
| `README.md` | 项目主说明文档 |

### backend 目录

| 目录 | 职责 |
|------|------|
| `config/` | Spring Security、JWT、Redis、WebSocket、Swagger 等配置类 |
| `common/` | 统一响应包装、异常处理、状态码枚举 |
| `entity/` | 数据实体类，继承基础实体 |
| `modules/` | 按业务划分的模块（auth、forum、trade、chat、user、admin） |

### frontend-user 目录

| 目录 | 职责 |
|------|------|
| `api/` | Axios 请求封装、模块化 API 接口、组合式 Hooks |
| `components/` | 基础组件、反馈组件、交互组件、导航组件 |
| `views/` | 按功能组织的页面组件 |
| `stores/` | Pinia 状态管理（用户状态、登录确认等） |
| `router/` | Vue Router 路由配置 |
| `styles/` | Tailwind CSS 设计令牌、SCSS 变量 |

### frontend-admin 目录

| 目录 | 职责 |
|------|------|
| `api/` | 管理端 API 接口 |
| `views/` | 管理端页面组件（仪表盘、用户管理、内容管理等） |
| `router/` | 管理端路由配置 |
| `stores/` | 管理端状态管理 |
| `styles/` | SCSS 样式文件 |
