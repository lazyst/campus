# 校园互助平台 - API接口文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 产品名称 | 校园互助平台 |
| 版本号 | 1.0.0 |
| 最后更新 | 2026-02-28 |

---

## 1. 接口基础信息

### 1.1 基础URL

| 环境 | URL |
|------|-----|
| 开发环境 | http://localhost:8080 |

### 1.2 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

### 1.3 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效） |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 1.4 认证方式

- Header: `Authorization: Bearer <token>`
- Token有效期: 7天

---

## 2. 认证模块 (Auth)

### 2.1 用户注册

| 项目 | 内容 |
|------|------|
| URL | POST /api/auth/register |
| 认证 | 否 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号，11位数字 |
| password | String | 是 | 密码，6-20位 |
| nickname | String | 是 | 昵称，2-20位 |

**请求示例：**
```json
{
  "phone": "13800000001",
  "password": "123456",
  "nickname": "测试用户"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "phone": "13800000001",
    "nickname": "测试用户",
    "avatar": null,
    "gender": 0,
    "bio": null,
    "createdAt": "2026-02-28T10:00:00Z"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "timestamp": 1700000000000
}
```

---

### 2.2 用户登录

| 项目 | 内容 |
|------|------|
| URL | POST /api/auth/login |
| 认证 | 否 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "eyJhbGciOiJIUzI1NiJ9...",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "timestamp": 1700000000000
}
```

---

### 2.3 用户登出

| 项目 | 内容 |
|------|------|
| URL | POST /api/auth/logout |
| 认证 | 是 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "timestamp": 1700000000000
}
```

---

### 2.4 刷新Token

| 项目 | 内容 |
|------|------|
| URL | POST /api/auth/refresh |
| 认证 | 是 |

**请求头：**

| 参数名 | 说明 |
|--------|------|
| Authorization | Bearer {token} |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "新的token",
  "token": "新的token",
  "timestamp": 1700000000000
}
```

---

### 2.5 检查手机号

| 项目 | 内容 |
|------|------|
| URL | GET /api/auth/check-phone |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "registered": false,
    "wasDeleted": false,
    "previousNickname": null
  },
  "timestamp": 1700000000000
}
```

---

## 3. 用户模块 (User)

### 3.1 获取个人资料

| 项目 | 内容 |
|------|------|
| URL | GET /api/user/profile |
| 认证 | 是 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "phone": "13800000001",
    "nickname": "测试用户",
    "avatar": "https://...",
    "gender": 1,
    "bio": "个人简介",
    "grade": "大一",
    "major": "计算机科学与技术",
    "status": 1,
    "createdAt": "2026-02-28T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

### 3.2 更新个人资料

| 项目 | 内容 |
|------|------|
| URL | PUT /api/user/profile |
| 认证 | 是 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称 |
| gender | Integer | 否 | 性别: 0未知, 1男, 2女 |
| bio | String | 否 | 个人简介 |
| grade | String | 否 | 年级 |
| major | String | 否 | 专业 |

---

### 3.3 上传头像

| 项目 | 内容 |
|------|------|
| URL | POST /api/user/avatar |
| 认证 | 是 |
| Content-Type | multipart/form-data |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件，≤2MB |

---

### 3.4 获取用户公开信息

| 项目 | 内容 |
|------|------|
| URL | GET /api/users/{userId} |
| 认证 | 否 |

---

### 3.5 获取用户帖子列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/users/{userId}/posts |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 3.6 获取用户物品列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/users/{userId}/items |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

### 3.7 获取他人公开信息（兼容旧路径）

| 项目 | 内容 |
|------|------|
| URL | GET /api/user/public/{userId} |
| 认证 | 否 |

---

### 3.8 获取用户详细信息（包含个人简介）（兼容旧路径）

| 项目 | 内容 |
|------|------|
| URL | GET /api/user/profile/{userId} |
| 认证 | 否 |

---

### 3.9 注销账号

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/user/account |
| 认证 | 是 |

---

## 4. 论坛模块 (Forum)

### 4.1 板块管理

#### 4.1.1 获取板块列表（启用状态）

| 项目 | 内容 |
|------|------|
| URL | GET /api/boards |
| 认证 | 否 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "失物招领",
      "description": "发布失物或拾物信息",
      "sortOrder": 1,
      "postCount": 10,
      "createdAt": "2026-02-28T10:00:00Z"
    }
  ],
  "timestamp": 1700000000000
}
```

---

#### 4.1.2 分页获取板块列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/boards/page |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.1.3 获取板块详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/boards/{id} |
| 认证 | 否 |

---

### 4.2 帖子管理

#### 4.2.1 获取帖子列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| boardId | Long | 否 | 板块ID |
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.2.2 获取帖子详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/{id} |
| 认证 | 否 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 1,
    "boardId": 1,
    "title": "帖子标题",
    "content": "帖子内容",
    "images": ["https://..."],
    "viewCount": 100,
    "likeCount": 10,
    "commentCount": 5,
    "collectCount": 3,
    "isLiked": false,
    "isCollected": false,
    "user": {
      "id": 1,
      "nickname": "用户昵称",
      "avatar": "https://..."
    },
    "createdAt": "2026-02-28T10:00:00Z"
  },
  "timestamp": 1700000000000
}
```

---

#### 4.2.3 发布帖子

| 项目 | 内容 |
|------|------|
| URL | POST /api/posts |
| 认证 | 是 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| boardId | Long | 是 | 板块ID |
| title | String | 是 | 标题，1-100字符 |
| content | String | 是 | 内容，1-10000字符 |
| images | List<String> | 否 | 图片URL列表 |

---

#### 4.2.4 更新帖子

| 项目 | 内容 |
|------|------|
| URL | PUT /api/posts/{id} |
| 认证 | 是（仅作者） |

---

#### 4.2.5 删除帖子

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/posts/{id} |
| 认证 | 是（仅作者） |

---

#### 4.2.6 获取我的帖子

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/my |
| 认证 | 是 |

---

#### 4.2.7 搜索帖子

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/search |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |

---

### 4.3 评论管理

#### 4.3.1 获取帖子评论

| 项目 | 内容 |
|------|------|
| URL | GET /api/comments/post/{postId} |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认10 |

---

#### 4.3.2 发表评论

| 项目 | 内容 |
|------|------|
| URL | POST /api/comments |
| 认证 | 是 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | Long | 是 | 帖子ID |
| content | String | 是 | 评论内容 |
| parentId | Long | 否 | 父评论ID（楼中楼回复） |

---

#### 4.3.3 删除评论

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/comments/{id} |
| 认证 | 是（仅作者） |

---

### 4.4 点赞管理

#### 4.4.1 点赞帖子

| 项目 | 内容 |
|------|------|
| URL | POST /api/posts/{postId}/like |
| 认证 | 是 |

---

#### 4.4.2 检查是否点赞

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/{postId}/like/check |
| 认证 | 是 |

---

### 4.5 收藏管理

#### 4.5.1 收藏帖子

| 项目 | 内容 |
|------|------|
| URL | POST /api/posts/{postId}/collect |
| 认证 | 是 |

---

#### 4.5.2 检查是否收藏

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/{postId}/collect/check |
| 认证 | 是 |

---

#### 4.5.3 获取收藏列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/posts/collections |
| 认证 | 是 |

---

### 4.6 通知管理

#### 4.6.1 获取通知列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/notifications |
| 认证 | 是 |

---

#### 4.6.2 获取未读数

| 项目 | 内容 |
|------|------|
| URL | GET /api/notifications/unread/count |
| 认证 | 是 |

---

#### 4.6.3 标记已读

| 项目 | 内容 |
|------|------|
| URL | PUT /api/notifications/{notificationId}/read |
| 认证 | 是 |

---

#### 4.6.4 全部已读

| 项目 | 内容 |
|------|------|
| URL | PUT /api/notifications/read/all |
| 认证 | 是 |

---

## 5. 闲置交易模块 (Trade)

### 5.1 物品管理

#### 5.1.1 获取物品列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/items |
| 认证 | 否 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | Integer | 否 | 类型: 1收购, 2出售 |
| status | Integer | 否 | 状态: 1正常, 2已完成, 3下架 |
| page | Integer | 否 | 页码 |
| size | Integer | 否 | 每页数量 |
| sortBy | String | 否 | 排序: latest, price_asc, price_desc |

---

#### 5.1.2 获取物品详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/items/{itemId} |
| 认证 | 否 |

---

#### 5.1.3 发布物品

| 项目 | 内容 |
|------|------|
| URL | POST /api/items |
| 认证 | 是 |
| Content-Type | application/json |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | Integer | 是 | 类型: 1收购, 2出售 |
| category | String | 是 | 分类 |
| title | String | 是 | 标题 |
| description | String | 是 | 描述 |
| price | BigDecimal | 是 | 价格 |
| images | List<String> | 否 | 图片列表 |
| location | String | 否 | 位置 |

---

#### 5.1.4 更新物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/items/{itemId} |
| 认证 | 是（仅作者） |

---

#### 5.1.5 删除物品

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/items/{itemId} |
| 认证 | 是（仅作者） |

---

#### 5.1.6 上架物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/items/{itemId}/online |
| 认证 | 是（仅作者） |

---

#### 5.1.7 下架物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/items/{itemId}/offline |
| 认证 | 是（仅作者） |

---

#### 5.1.8 标记完成

| 项目 | 内容 |
|------|------|
| URL | PUT /api/items/{itemId}/complete |
| 认证 | 是（仅作者） |

---

#### 5.1.9 联系卖家

| 项目 | 内容 |
|------|------|
| URL | POST /api/items/{itemId}/contact |
| 认证 | 是 |

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "sellerId": 2,
    "sellerName": "用户2"
  },
  "timestamp": 1700000000000
}
```

---

#### 5.1.10 获取我的物品

| 项目 | 内容 |
|------|------|
| URL | GET /api/items/my |
| 认证 | 是 |

---

#### 5.1.11 搜索物品

| 项目 | 内容 |
|------|------|
| URL | GET /api/items/search |
| 认证 | 否 |

---

### 5.2 物品收藏

#### 5.2.1 收藏物品

| 项目 | 内容 |
|------|------|
| URL | POST /api/items/{itemId}/collect |
| 认证 | 是 |

---

#### 5.2.2 检查是否收藏

| 项目 | 内容 |
|------|------|
| URL | GET /api/items/{itemId}/collect/check |
| 认证 | 是 |

---

#### 5.2.3 获取收藏列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/items/collected |
| 认证 | 是 |

---

## 6. 消息模块 (Chat)

### 6.1 会话管理

#### 6.1.1 获取会话列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/conversations |
| 认证 | 是 |

---

#### 6.1.2 创建会话

| 项目 | 内容 |
|------|------|
| URL | POST /api/conversations |
| 认证 | 是 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| targetUserId | Long | 是 | 对方用户ID |

---

### 6.2 消息管理

#### 6.2.1 获取会话消息

| 项目 | 内容 |
|------|------|
| URL | GET /api/conversations/{conversationId}/messages |
| 认证 | 是 |

---

#### 6.2.2 获取与某用户的聊天记录

| 项目 | 内容 |
|------|------|
| URL | GET /api/messages/{userId} |
| 认证 | 是 |

---

#### 6.2.3 发送消息

| 项目 | 内容 |
|------|------|
| URL | POST /api/messages/{userId} |
| 认证 | 是 |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| content | String | 是 | 消息内容 |
| type | Integer | 否 | 类型: 1文本, 2图片 |

---

#### 6.2.4 标记已读

| 项目 | 内容 |
|------|------|
| URL | POST /api/conversations/{userId}/read |
| 认证 | 是 |

---

#### 6.2.5 获取未读数

| 项目 | 内容 |
|------|------|
| URL | GET /api/conversations/unread/count |
| 认证 | 是 |

---

## 7. 管理端接口 (Admin)

### 7.1 仪表盘

#### 7.1.1 统计数据

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/dashboard/stats |
| 认证 | 是（管理员） |

---

#### 7.1.2 趋势数据

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/dashboard/trend |
| 认证 | 是（管理员） |

---

#### 7.1.3 最近数据

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/dashboard/recent |
| 认证 | 是（管理员） |

---

#### 7.1.4 系统状态

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/dashboard/status |
| 认证 | 是（管理员） |

---

#### 7.1.5 数据概览

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/dashboard/overview |
| 认证 | 是（管理员） |

---

### 7.2 用户管理

#### 7.2.1 获取用户列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/users |
| 认证 | 是（管理员） |

---

#### 7.2.2 获取用户详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/users/{userId} |
| 认证 | 是（管理员） |

---

#### 7.2.3 更新用户

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/users/{userId} |
| 认证 | 是（管理员） |

---

#### 7.2.4 禁用/启用用户

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/users/{userId}/status |
| 认证 | 是（管理员） |

---

#### 7.2.5 封禁用户

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/users/{userId}/ban |
| 认证 | 是（管理员） |

---

#### 7.2.6 解封用户

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/users/{userId}/unban |
| 认证 | 是（管理员） |

---

#### 7.2.7 删除用户

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/admin/users/{userId}/delete |
| 认证 | 是（管理员） |

---

#### 7.2.8 用户统计

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/users/stats |
| 认证 | 是（管理员） |

---

### 7.3 管理员认证

#### 7.3.1 管理员登录

| 项目 | 内容 |
|------|------|
| URL | POST /api/admin/auth/login |
| 认证 | 否 |

---

#### 7.3.2 初始化管理员

| 项目 | 内容 |
|------|------|
| URL | POST /api/admin/auth/init-admin |
| 认证 | 否 |

---

#### 7.3.3 重置管理员密码

| 项目 | 内容 |
|------|------|
| URL | POST /api/admin/auth/reset-admin-password |
| 认证 | 否 |

---

### 7.4 内容管理

#### 7.4.1 帖子管理

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/posts |
| 认证 | 是（管理员） |

---

#### 7.4.2 获取帖子详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/posts/{postId} |
| 认证 | 是（管理员） |

---

#### 7.4.3 删除帖子

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/admin/posts/{postId} |
| 认证 | 是（管理员） |

---

#### 7.4.4 更新帖子

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/posts/{postId} |
| 认证 | 是（管理员） |

---

#### 7.4.5 帖子统计

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/posts/stats |
| 认证 | 是（管理员） |

---

#### 7.4.6 获取帖子评论

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/posts/{postId}/comments |
| 认证 | 是（管理员） |

---

#### 7.4.7 删除评论

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/admin/posts/comments/{commentId} |
| 认证 | 是（管理员） |

---

#### 7.4.8 物品管理

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/items |
| 认证 | 是（管理员） |

---

#### 7.4.9 获取物品详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/items/{itemId} |
| 认证 | 是（管理员） |

---

#### 7.4.10 删除物品

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/admin/items/{itemId} |
| 认证 | 是（管理员） |

---

#### 7.4.11 更新物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/items/{itemId} |
| 认证 | 是（管理员） |

---

#### 7.4.12 下架物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/items/{itemId}/offline |
| 认证 | 是（管理员） |

---

#### 7.4.13 上架物品

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/items/{itemId}/online |
| 认证 | 是（管理员） |

---

#### 7.4.14 物品统计

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/items/stats |
| 认证 | 是（管理员） |

---

### 7.5 板块管理

#### 7.5.1 获取板块列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/boards |
| 认证 | 是（管理员） |

---

#### 7.5.2 获取板块详情

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/boards/{boardId} |
| 认证 | 是（管理员） |

---

#### 7.5.3 创建板块

| 项目 | 内容 |
|------|------|
| URL | POST /api/admin/boards |
| 认证 | 是（管理员） |

---

#### 7.5.4 更新板块

| 项目 | 内容 |
|------|------|
| URL | PUT /api/admin/boards/{boardId} |
| 认证 | 是（管理员） |

---

#### 7.5.5 删除板块

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/admin/boards/{boardId} |
| 认证 | 是（管理员） |

---

#### 7.5.6 板块统计

| 项目 | 内容 |
|------|------|
| URL | GET /api/admin/boards/stats |
| 认证 | 是（管理员） |

---

### 7.6 存储管理

#### 7.6.1 图片列表

| 项目 | 内容 |
|------|------|
| URL | GET /api/upload/list |
| 认证 | 是（管理员） |

---

#### 7.6.2 按日期获取图片

| 项目 | 内容 |
|------|------|
| URL | GET /api/upload/dates |
| 认证 | 是（管理员） |

---

#### 7.6.3 获取未使用图片

| 项目 | 内容 |
|------|------|
| URL | GET /api/upload/unused |
| 认证 | 是（管理员） |

---

#### 7.6.4 删除单张图片

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/upload/image |
| 认证 | 是（管理员） |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 图片URL |

---

#### 7.6.5 批量删除图片

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/upload/images/batch |
| 认证 | 是（管理员） |

---

#### 7.6.6 清理未使用图片

| 项目 | 内容 |
|------|------|
| URL | DELETE /api/upload/unused/clean |
| 认证 | 是（管理员） |

---

#### 7.6.7 获取存储统计

| 项目 | 内容 |
|------|------|
| URL | GET /api/upload/stats |
| 认证 | 是（管理员） |

---

## 8. 通用接口

### 8.1 图片上传

| 项目 | 内容 |
|------|------|
| URL | POST /api/upload/image |
| 认证 | 是 |
| Content-Type | multipart/form-data |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件，≤2MB |

---

### 8.2 批量上传图片

| 项目 | 内容 |
|------|------|
| URL | POST /api/upload/images |
| 认证 | 是 |
| Content-Type | multipart/form-data |

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| files | List<File> | 是 | 图片文件列表，最多9张 |

---

### 8.3 健康检查

| 项目 | 内容 |
|------|------|
| URL | GET /api/health |
| 认证 | 否 |

---

## 9. WebSocket接口

### 9.1 连接地址

```
ws://localhost:8080/ws
```

### 9.2 连接认证

通过STOMP协议的头部传递Token：
- Authorization: Bearer {token}

### 9.3 订阅主题

| 主题 | 说明 |
|------|------|
| /user/queue/messages | 用户私有消息队列 |
| /topic/notifications | 全局通知 |

### 9.4 发送消息

| 端点 | 说明 |
|------|------|
| /app/chat | 发送聊天消息 |

---

## 附录

### 附录A：错误码详情

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 附录B：分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 10
  },
  "timestamp": 1700000000000
}
```
