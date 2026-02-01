# 校园互助平台 API 接口文档

## 一、API 设计规范

### 1.1 RESTful 风格

本项目 API 设计遵循 RESTful 架构风格，通过 URL 标识资源，使用 HTTP 方法表示对资源的操作。RESTful 风格使得 API 接口语义清晰、易于理解，符合业界通用标准，便于前端开发者调用和第三方系统集成。

资源命名采用名词复数形式表示资源集合，使用单数形式表示单个资源。资源 URL 使用小写字母和连字符（可选）组成，避免使用下划线和大写字母。例如，用户集合表示为 `/users`，单个用户表示为 `/users/{id}`。相关资源之间使用嵌套关系表示层级关联，如用户的帖子表示为 `/users/{userId}/posts`。

HTTP 方法的使用遵循语义化原则，不同方法代表不同的操作类型。GET 方法用于获取资源，是幂等操作，不应该产生副作用。POST 方法用于创建新资源或执行操作，服务器在处理请求后返回新创建的资源。PUT 方法用于完整更新资源，客户端提供资源的完整表示。PATCH 方法用于部分更新资源，客户端只提供需要修改的字段。DELETE 方法用于删除资源，是幂等操作。

路径参数与查询参数有明确的区分使用场景。路径参数用于标识特定的资源，如 `/users/{id}` 中的 `{id}` 是路径参数。查询参数用于过滤、排序、分页等场景，如 `/posts?boardId=1&pageNum=1&pageSize=10` 中的 `boardId`、`pageNum`、`pageSize` 是查询参数。路径参数是必需的，查询参数是可选的。

### 1.2 版本控制

API 采用 URL 版本控制策略，在路径中包含版本号。版本号使用 `v` 加数字的形式，放置在 `/api` 之后，如 `/api/v1/users`。这种策略简单直观，便于客户端进行版本适配，是业界广泛采用的版本控制方式。

向后兼容是 API 版本演进的基本原则。已发布的 API 接口不应该修改路径、请求参数和响应格式，不应该删除已存在的字段。如果需要进行破坏性变更，应该发布新版本的 API，并提供合理的迁移周期。旧版本 API 在废弃前应该至少保留 6 个月的使用周期，给予客户端充分的适配时间。

版本废弃流程包括三个阶段：公告阶段（至少 3 个月）、废弃阶段（至少 3 个月）、下线阶段。公告阶段在文档和响应头中标注接口即将废弃的信息。废弃阶段接口仍可访问，但会返回警告响应头。废弃阶段结束后正式下线，不再提供服务。

### 1.3 接口分类

API 接口按访问权限分为公开接口、认证接口和管理接口三类。不同类型的接口有不同的访问控制策略和安全要求。

公开接口无需身份认证即可访问，用于获取公开信息。公开接口包括用户注册、用户登录、板块列表、帖子列表（公开部分）、帖子详情（公开帖子）、闲置列表（公开部分）等。公开接口应该避免返回敏感信息，对返回的数据量进行限制，防止恶意爬取。

认证接口需要有效的 JWT Token 才能访问，用于用户私有数据的操作。认证接口在请求头中携带 Token 信息，服务端验证 Token 的有效性后处理请求。认证接口包括用户个人资料管理、帖子管理、闲置管理、收藏管理、消息管理等。Token 过期或无效时返回 401 状态码，客户端收到响应后引导用户重新登录。

管理接口需要管理员权限才能访问，用于后台管理功能的操作。管理接口使用独立的认证方式，与普通用户认证分开。管理接口包括用户管理、内容管理、数据统计、系统配置等。普通用户访问管理接口返回 403 状态码，提示权限不足。

## 二、认证机制

### 2.1 Token 获取

用户通过登录接口获取 JWT Token，Token 是后续 API 调用的身份凭证。登录接口支持用户名密码方式认证，认证成功后返回访问 Token 和刷新 Token。

**接口信息**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/login` |
| 请求方法 | `POST` |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例**

```json
{
  "username": "testuser",
  "password": "test123456"
}
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码 |
| message | String | 提示信息 |
| data.token | String | 访问 JWT Token |
| data.refreshToken | String | 刷新 Token |
| data.user | Object | 用户信息对象 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOmZhbHNlLCJpYXQiOjE3MzQxMDAwMDAsImV4cCI6MTczNDE4NjQwMH0.xGhm5wVJKiT6G2H5hX4f5Q",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4",
    "user": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "https://example.com/avatar/1.jpg"
    }
  }
}
```

注册接口用于创建新用户账号，注册成功后返回用户基本信息。

**接口信息**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/register` |
| 请求方法 | `POST` |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名，4-20位字母数字 |
| password | String | 是 | 密码，6-20位 |
| nickname | String | 是 | 昵称，2-20位 |
| phone | String | 是 | 手机号 |

**请求示例**

```json
{
  "username": "newuser",
  "password": "newpassword123",
  "nickname": "新用户",
  "phone": "13800138000"
}
```

### 2.2 Token 使用

客户端获取 Token 后，在后续请求中通过 HTTP 请求头传递 Token。Token 格式为 `Bearer <token>`，其中 `Bearer` 是 Token 类型，`<token>` 是实际的 JWT 字符串。

**请求头格式**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**请求示例**

```http
GET /api/user/profile HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

Token 验证失败时返回 401 状态码，响应体包含错误信息。客户端收到 401 响应后应该跳转到登录页面，引导用户重新登录。

**401 响应示例**

```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null
}
```

### 2.3 Token 刷新

访问 Token 的有效期较短（7 天），过期后需要使用刷新 Token 获取新的访问 Token。刷新操作不需要用户重新输入密码，保持用户会话的连续性。

**接口信息**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/refresh` |
| 请求方法 | `POST` |

**请求头格式**

```
Authorization: Bearer <refreshToken>
```

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| data.token | String | 新的访问 JWT Token |
| data.refreshToken | String | 新的刷新 Token |

刷新 Token 也过期后，用户需要重新登录获取新的 Token 对。刷新 Token 的有效期为 30 天。

### 2.4 登出

登出接口用于清除用户的登录状态，使当前 Token 失效。客户端应该在登出后删除本地存储的 Token 信息。

**接口信息**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/logout` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

## 三、统一响应格式

### 3.1 成功响应

所有 API 接口返回统一格式的响应数据，成功响应包含状态码、提示信息和数据三部分。

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码，200 表示成功 |
| message | String | 提示信息，描述操作结果 |
| data | Object/Array/null | 响应数据，根据接口返回不同类型 |

**对象类型响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户"
  }
}
```

**数组类型响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {"id": 1, "title": "帖子一"},
    {"id": 2, "title": "帖子二"}
  ]
}
```

**无数据响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 3.2 错误响应

接口调用失败时返回错误响应，错误响应包含错误码和错误信息。

**响应参数**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 错误码，非 200 表示错误 |
| message | String | 错误信息，描述错误原因 |
| data | null | 错误响应无数据 |

**错误响应示例**

```json
{
  "code": 500,
  "message": "服务器错误，请稍后重试",
  "data": null
}
```

### 3.3 状态码说明

HTTP 状态码和业务状态码共同表示请求的处理结果。

**HTTP 状态码**

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，Token 无效或过期 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**业务状态码**

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 操作失败 |
| 1001 | 用户名或密码错误 |
| 1002 | 用户名已存在 |
| 1003 | 用户不存在 |
| 1004 | 用户已被禁用 |
| 2001 | 帖子不存在 |
| 2002 | 无权操作此帖子 |
| 3001 | 闲置不存在 |
| 3002 | 无权操作此闲置 |

## 四、模块接口说明

### 4.1 用户认证模块（Auth）

用户认证模块提供登录、注册、登出等认证相关接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 登录 | `/api/auth/login` | POST | 否 | 用户名密码登录 |
| 注册 | `/api/auth/register` | POST | 否 | 新用户注册 |
| 登出 | `/api/auth/logout` | POST | 是 | 退出登录 |
| 获取当前用户 | `/api/auth/me` | GET | 是 | 获取登录用户信息 |
| 修改密码 | `/api/auth/password` | PUT | 是 | 修改用户密码 |

**获取当前用户接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/me` |
| 请求方法 | `GET` |
| 认证要求 | 需要认证 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar/1.jpg",
    "phone": "13800138000",
    "createdAt": "2026-01-01T00:00:00"
  }
}
```

**修改密码接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/auth/password` |
| 请求方法 | `PUT` |
| 认证要求 | 需要认证 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码，6-20位 |

**请求示例**

```json
{
  "oldPassword": "test123456",
  "newPassword": "newpassword123"
}
```

### 4.2 用户模块（User）

用户模块提供用户信息获取和修改相关接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取用户信息 | `/api/user/profile` | GET | 是 | 获取当前用户信息 |
| 更新用户信息 | `/api/user/profile` | PUT | 是 | 更新当前用户信息 |
| 上传头像 | `/api/user/avatar` | POST | 是 | 上传用户头像 |
| 获取公开信息 | `/api/user/{id}` | GET | 否 | 获取指定用户公开信息 |
| 获取我的帖子 | `/api/user/posts` | GET | 是 | 获取当前用户的帖子 |
| 获取我的闲置 | `/api/user/items` | GET | 是 | 获取当前用户的闲置 |
| 获取我的收藏 | `/api/user/collects` | GET | 是 | 获取当前用户的收藏 |

**获取用户信息接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/user/profile` |
| 请求方法 | `GET` |
| 认证要求 | 需要认证 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar/1.jpg",
    "bio": "这是一个简介",
    "gender": 1,
    "createdAt": "2026-01-01T00:00:00"
  }
}
```

**更新用户信息接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/user/profile` |
| 请求方法 | `PUT` |
| 认证要求 | 需要认证 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称，2-20位 |
| bio | String | 否 | 个人简介，最大200字 |
| gender | Integer | 否 | 性别：0-未知，1-男，2-女 |

**请求示例**

```json
{
  "nickname": "新昵称",
  "bio": "这是新的个人简介",
  "gender": 1
}
```

**上传头像接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/user/avatar` |
| 请求方法 | `POST` |
| Content-Type | `multipart/form-data` |
| 认证要求 | 需要认证 |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| avatar | File | 是 | 头像图片文件，JPG/PNG，<=2MB |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "url": "https://example.com/avatar/1_new.jpg"
  }
}
```

**获取公开信息接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/user/{id}` |
| 请求方法 | `GET` |
| 认证要求 | 无 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 用户ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar/1.jpg",
    "postCount": 10,
    "itemCount": 5
  }
}
```

### 4.3 板块模块（Board）

板块模块提供讨论板块的查询接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取板块列表 | `/api/board` | GET | 否 | 获取所有板块 |
| 获取板块详情 | `/api/board/{id}` | GET | 否 | 获取板块详情及帖子 |

**获取板块列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/board` |
| 请求方法 | `GET` |
| 认证要求 | 无 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "学习交流",
      "description": "学习经验分享、考试资料交流",
      "icon": "https://example.com/icons/study.png",
      "postCount": 150,
      "order": 1
    },
    {
      "id": 2,
      "name": "生活分享",
      "description": "校园生活、趣事分享",
      "icon": "https://example.com/icons/life.png",
      "postCount": 80,
      "order": 2
    }
  ]
}
```

### 4.4 帖子模块（Post）

帖子模块提供帖子的 CRUD、互动等功能接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取帖子列表 | `/api/post` | GET | 否 | 分页获取帖子列表 |
| 获取帖子详情 | `/api/post/{id}` | GET | 否 | 获取帖子详情 |
| 发布帖子 | `/api/post` | POST | 是 | 创建新帖子 |
| 更新帖子 | `/api/post/{id}` | PUT | 是 | 更新帖子（限作者） |
| 删除帖子 | `/api/post/{id}` | DELETE | 是 | 删除帖子（限作者） |
| 点赞帖子 | `/api/post/{id}/like` | POST | 是 | 点赞帖子 |
| 取消点赞 | `/api/post/{id}/like` | DELETE | 是 | 取消点赞 |
| 收藏帖子 | `/api/post/{id}/collect` | POST | 是 | 收藏帖子 |
| 取消收藏 | `/api/post/{id}/collect` | DELETE | 是 | 取消收藏 |
| 获取我的帖子 | `/api/post/my` | GET | 是 | 获取当前用户的帖子 |

**获取帖子列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/post` |
| 请求方法 | `GET` |
| 认证要求 | 无 |

**查询参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| boardId | Long | 否 | 板块ID |
| keyword | String | 否 | 搜索关键词 |
| sort | String | 否 | 排序方式：time（最新）、hot（最热） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "title": "求购高等数学教材",
        "summary": "求购高等数学第七版教材，有意者联系",
        "userId": 1,
        "nickname": "测试用户",
        "avatar": "https://example.com/avatar/1.jpg",
        "boardId": 1,
        "boardName": "学习交流",
        "viewCount": 120,
        "likeCount": 5,
        "commentCount": 3,
        "isLike": false,
        "isCollect": false,
        "createdAt": "2026-01-15T10:30:00"
      }
    ]
  }
}
```

**发布帖子接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/post` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| boardId | Long | 是 | 板块ID |
| title | String | 是 | 标题，5-50字 |
| content | String | 是 | 正文内容，10-5000字 |
| images | String[] | 否 | 图片URL数组，最多9张 |

**请求示例**

```json
{
  "boardId": 1,
  "title": "出售二手自行车",
  "content": "本人有一辆八成新自行车，骑了一年多，现在毕业低价转让。自行车质量良好，无任何问题。有意者请私信联系。",
  "images": [
    "https://example.com/images/1.jpg",
    "https://example.com/images/2.jpg"
  ]
}
```

**点赞帖子接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/post/{id}/like` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 帖子ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "isLike": true,
    "likeCount": 6
  }
}
```

### 4.5 评论模块（Comment）

评论模块提供帖子评论功能接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取评论列表 | `/api/comment/post/{postId}` | GET | 否 | 获取帖子的评论列表 |
| 发表评论 | `/api/comment` | POST | 是 | 发表评论 |
| 删除评论 | `/api/comment/{id}` | DELETE | 是 | 删除评论（限作者） |

**获取评论列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/comment/post/{postId}` |
| 请求方法 | `GET` |
| 认证要求 | 无 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | Long | 是 | 帖子ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "content": "这个自行车还在吗？",
      "userId": 2,
      "nickname": "张三",
      "avatar": "https://example.com/avatar/2.jpg",
      "replyCount": 1,
      "createdAt": "2026-01-15T11:00:00",
      "replies": [
        {
          "id": 2,
          "content": "在的，私信联系",
          "userId": 1,
          "nickname": "测试用户",
          "createdAt": "2026-01-15T11:05:00"
        }
      ]
    }
  ]
}
```

**发表评论接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/comment` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | Long | 是 | 帖子ID |
| content | String | 是 | 评论内容，1-500字 |
| parentId | Long | 否 | 父评论ID，回复评论时必填 |

**请求示例**

```json
{
  "postId": 1,
  "content": "价格可以再商量吗？",
  "parentId": null
}
```

### 4.6 闲置模块（Item）

闲置模块提供闲置物品的发布、浏览、交易等功能接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取闲置列表 | `/api/item` | GET | 否 | 分页获取闲置列表 |
| 获取闲置详情 | `/api/item/{id}` | GET | 否 | 获取闲置详情 |
| 发布闲置 | `/api/item` | POST | 是 | 发布新闲置 |
| 更新闲置 | `/api/item/{id}` | PUT | 是 | 更新闲置（限作者） |
| 删除闲置 | `/api/item/{id}` | DELETE | 是 | 删除闲置（限作者） |
| 下架闲置 | `/api/item/{id}/offline` | PUT | 是 | 下架闲置（限作者） |
| 上架闲置 | `/api/item/{id}/online` | PUT | 是 | 重新上架（限作者） |
| 标记完成 | `/api/item/{id}/complete` | PUT | 是 | 标记交易完成（限作者） |
| 联系卖家 | `/api/item/{id}/contact` | POST | 是 | 发起与卖家的会话 |
| 收藏闲置 | `/api/item/{id}/collect` | POST | 是 | 收藏闲置 |
| 取消收藏 | `/api/item/{id}/collect` | DELETE | 是 | 取消收藏 |
| 获取我的闲置 | `/api/item/my` | GET | 是 | 获取当前用户的闲置 |
| 获取收藏的闲置 | `/api/item/collects` | GET | 是 | 获取收藏的闲置列表 |

**获取闲置列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/item` |
| 请求方法 | `GET` |
| 认证要求 | 无 |

**查询参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| category | String | 否 | 分类：electronics（数码）、books（书籍）、clothing（服装）等 |
| minPrice | Double | 否 | 最低价格 |
| maxPrice | Double | 否 | 最高价格 |
| status | String | 否 | 状态：online（上架）、offline（下架）、completed（已交易） |
| sort | String | 否 | 排序：time（最新）、price_asc（价格升）、price_desc（价格降） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "list": [
      {
        "id": 1,
        "title": "iPad Air 4 64GB",
        "description": "闲置自用 iPad Air 4，95新，无划痕",
        "price": 2800.00,
        "originalPrice": 4399.00,
        "category": "electronics",
        "images": ["https://example.com/item/1_1.jpg"],
        "status": "online",
        "viewCount": 150,
        "contactCount": 20,
        "userId": 1,
        "nickname": "卖家张三",
        "createdAt": "2026-01-10T00:00:00"
      }
    ]
  }
}
```

**发布闲置接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/item` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 标题，5-30字 |
| description | String | 是 | 描述，10-500字 |
| price | Double | 是 | 售价，大于0 |
| originalPrice | Double | 否 | 原价 |
| category | String | 是 | 分类 |
| images | String[] | 否 | 图片URL数组，最多9张 |

**请求示例**

```json
{
  "title": "出售高等数学教材",
  "description": "高等数学第七版，出版社：高等教育出版社，书中有少量笔记，不影响使用。",
  "price": 25.00,
  "originalPrice": 45.00,
  "category": "books",
  "images": ["https://example.com/item/2_1.jpg"]
}
```

### 4.7 通知模块（Notification）

通知模块提供系统通知的查询和操作接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取通知列表 | `/api/notification` | GET | 是 | 获取当前用户的通知 |
| 标记已读 | `/api/notification/{id}/read` | PUT | 是 | 标记单条通知已读 |
| 全部已读 | `/api/notification/read/all` | PUT | 是 | 标记全部通知已读 |

**获取通知列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/notification` |
| 请求方法 | `GET` |
| 认证要求 | 需要认证 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 10,
    "unreadCount": 3,
    "list": [
      {
        "id": 1,
        "type": "comment",
        "title": "评论通知",
        "content": "张三评论了你的帖子「出售自行车」",
        "isRead": false,
        "relatedId": 1,
        "createdAt": "2026-01-15T12:00:00"
      }
    ]
  }
}
```

### 4.8 会话模块（Conversation）

会话模块提供私信会话的查询和创建接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取会话列表 | `/api/conversation` | GET | 是 | 获取会话列表 |
| 创建会话 | `/api/conversation` | POST | 是 | 创建或获取会话 |

**获取会话列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/conversation` |
| 请求方法 | `GET` |
| 认证要求 | 需要认证 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "targetUserId": 2,
      "targetNickname": "张三",
      "targetAvatar": "https://example.com/avatar/2.jpg",
      "lastMessage": "这个自行车怎么卖？",
      "lastMessageTime": "2026-01-15T14:00:00",
      "unreadCount": 1
    }
  ]
}
```

### 4.9 消息模块（Message）

消息模块提供私信消息的查询和发送接口。

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 获取消息列表 | `/api/message/{conversationId}` | GET | 是 | 获取会话的消息 |
| 发送消息 | `/api/message` | POST | 是 | 发送消息 |

**获取消息列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/message/{conversationId}` |
| 请求方法 | `GET` |
| 认证要求 | 需要认证 |

**路径参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | Long | 是 | 会话ID |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "content": "你好，请问自行车还在吗？",
      "isSelf": false,
      "createdAt": "2026-01-15T10:00:00"
    },
    {
      "id": 2,
      "content": "在的，2800元",
      "isSelf": true,
      "createdAt": "2026-01-15T10:05:00"
    }
  ]
}
```

**发送消息接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/message` |
| 请求方法 | `POST` |
| 认证要求 | 需要认证 |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | Long | 是 | 会话ID |
| content | String | 是 | 消息内容，1-500字 |

**请求示例**

```json
{
  "conversationId": 1,
  "content": "可以便宜点吗？2500卖不卖？"
}
```

## 五、后台管理接口

### 5.1 认证

管理员登录使用独立的认证接口。

**管理员登录接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/admin/auth/login` |
| 请求方法 | `POST` |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 管理员用户名 |
| password | String | 是 | 管理员密码 |

### 5.2 用户管理

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 用户列表 | `/api/admin/user` | GET | 是 | 分页获取用户列表 |
| 用户详情 | `/api/admin/user/{id}` | GET | 是 | 获取用户详情 |
| 禁用用户 | `/api/admin/user/{id}/disable` | PUT | 是 | 禁用用户 |
| 启用用户 | `/api/admin/user/{id}/enable` | PUT | 是 | 启用用户 |

**用户列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/admin/user` |
| 请求方法 | `GET` |
| 认证要求 | 需要管理员 |

**查询参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词（用户名/昵称/手机号） |
| status | String | 否 | 状态：normal（正常）、disabled（禁用） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "list": [
      {
        "id": 1,
        "username": "testuser",
        "nickname": "测试用户",
        "phone": "13800138000",
        "status": "normal",
        "postCount": 10,
        "itemCount": 5,
        "createdAt": "2026-01-01T00:00:00",
        "lastLoginAt": "2026-01-15T10:00:00"
      }
    ]
  }
}
```

### 5.3 帖子管理

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 帖子列表 | `/api/admin/post` | GET | 是 | 分页获取帖子列表 |
| 删除帖子 | `/api/admin/post/{id}` | DELETE | 是 | 删除帖子 |

**帖子列表接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/admin/post` |
| 请求方法 | `GET` |
| 认证要求 | 需要管理员 |

**查询参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| boardId | Long | 否 | 板块ID |
| status | String | 否 | 状态：normal（正常）、deleted（已删除） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

### 5.4 闲置管理

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 闲置列表 | `/api/admin/item` | GET | 是 | 分页获取闲置列表 |
| 下架闲置 | `/api/admin/item/{id}/offline` | PUT | 是 | 下架闲置 |
| 删除闲置 | `/api/admin/item/{id}` | DELETE | 是 | 删除闲置 |

### 5.5 板块管理

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 板块列表 | `/api/admin/board` | GET | 是 | 获取板块列表 |
| 创建板块 | `/api/admin/board` | POST | 是 | 创建板块 |
| 更新板块 | `/api/admin/board/{id}` | PUT | 是 | 更新板块 |
| 删除板块 | `/api/admin/board/{id}` | DELETE | 是 | 删除板块 |

**创建板块接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/admin/board` |
| 请求方法 | `POST` |
| 认证要求 | 需要管理员 |
| Content-Type | `application/json` |

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 板块名称 |
| description | String | 是 | 板块描述 |
| icon | String | 否 | 板块图标URL |
| order | Integer | 否 | 排序序号 |

### 5.6 数据统计

**接口列表**

| 接口 | 路径 | 方法 | 认证 | 说明 |
|------|------|------|------|------|
| 统计概览 | `/api/admin/stats/overview` | GET | 是 | 获取统计概览 |
| 用户统计 | `/api/admin/stats/users` | GET | 是 | 获取用户统计 |
| 内容统计 | `/api/admin/stats/content` | GET | 是 | 获取内容统计 |

**统计概览接口详情**

| 属性 | 值 |
|------|-----|
| 请求路径 | `/api/admin/stats/overview` |
| 请求方法 | `GET` |
| 认证要求 | 需要管理员 |

**响应示例**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userCount": 1000,
    "postCount": 5000,
    "itemCount": 2000,
    "conversationCount": 500,
    "todayNewUsers": 10,
    "todayNewPosts": 50,
    "todayNewItems": 20
  }
}
```

## 六、错误码参考表

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 操作成功 | 无需处理 |
| 400 | 参数错误 | 检查请求参数是否正确 |
| 401 | 未授权 | Token 无效或过期，请重新登录 |
| 403 | 禁止访问 | 权限不足，请确认账号权限 |
| 404 | 资源不存在 | 检查请求的资源ID是否正确 |
| 500 | 服务器错误 | 请稍后重试或联系管理员 |
| 1001 | 用户名或密码错误 | 检查用户名和密码是否正确 |
| 1002 | 用户名已存在 | 请使用其他用户名 |
| 1003 | 用户不存在 | 检查用户ID是否正确 |
| 1004 | 用户已被禁用 | 联系管理员处理 |
| 1005 | Token 无效 | 请重新登录 |
| 1006 | Token 已过期 | 请刷新 Token 或重新登录 |
| 2001 | 帖子不存在 | 检查帖子ID是否正确 |
| 2002 | 无权操作此帖子 | 仅帖子作者可以操作 |
| 2003 | 帖子已删除 | 帖子已被删除 |
| 3001 | 闲置不存在 | 检查闲置ID是否正确 |
| 3002 | 无权操作此闲置 | 仅闲置发布者可以操作 |
| 3003 | 闲置状态不允许操作 | 检查闲置当前状态 |
| 4001 | 评论不存在 | 检查评论ID是否正确 |
| 4002 | 无权操作此评论 | 仅评论作者可以操作 |

## 七、Swagger 文档入口

项目集成了 Swagger 3.0（OpenAPI 3.0 规范），提供在线 API 文档浏览和接口测试功能。

**开发环境访问地址**

```
http://localhost:8080/swagger-ui.html
```

**生产环境访问地址**

```
/swagger-ui.html
```

**Swagger UI 功能说明**

- **接口列表浏览**：按标签（Tag）分类展示所有 API 接口
- **接口详情查看**：查看每个接口的路径、方法、参数、响应格式
- **在线接口测试**：填写参数后可直接在页面中发送请求测试接口
- **数据模型查看**：查看所有请求/响应数据模型的定义
- **代码生成**：支持生成多种语言的客户端 SDK 代码

**Swagger 注解示例**

```java
@Operation(summary = "获取帖子详情")
@GetMapping("/{id}")
public Result<PostVO> getPost(
    @Parameter(description = "帖子ID") @PathVariable Long id
) {
    PostVO post = postService.getPostById(id);
    return Result.success(post);
}
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
