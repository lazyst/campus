# API 测试指南

## 📋 快速测试方法

### 方式1：手动测试（推荐，最快）

启动前端和后端：
```bash
# 终端1：启动后端
cd backend
mvn spring-boot:run

# 终端2：启动前端
cd frontend-user
npm run dev
```

打开浏览器访问：http://localhost:3000

### 方式2：使用后端Swagger UI

访问：http://localhost:8080/swagger-ui/index.html

直接测试所有后端API端点。

---

## 🧪 API功能测试清单

### ✅ Auth模块 (3个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `login(data)` | 访问 `/login`，输入手机号密码 | 登录成功，跳转首页，Toast显示"登录成功" |
| `register(data)` | 访问 `/register`，填写注册信息 | 注册成功，自动登录，Toast显示"注册成功" |
| `logout()` | 访问个人中心，点击"退出登录" | 退出成功，跳转登录页 |

**验证点：**
- ✓ Toast自动显示
- ✓ Loading自动显示
- ✓ Token自动保存到localStorage
- ✓ 错误时Toast显示错误信息

---

### ✅ User模块 (4个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getUserInfo()` | 登录后访问个人中心 | 显示用户信息 |
| `updateProfile(data)` | 访问"编辑资料"，修改昵称/简介 | 修改成功，Toast显示"修改成功" |
| `getUserPublicInfo(id)` | 访问其他用户个人页面 | 显示该用户公开信息 |
| `uploadAvatar(file)` | 在"编辑资料"页上传头像 | 头像上传成功，显示新头像 |

---

### ✅ Board模块 (2个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getBoards()` | 访问论坛首页 | 显示所有板块标签 |
| `getBoardById(id)` | 点击板块进入 | 显示该板块的帖子列表 |

---

### ✅ Post模块 (10个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getPosts(params)` | 访问论坛首页 | 显示帖子列表 |
| `getPostById(id)` | 点击帖子标题 | 显示帖子详情，view_count+1 |
| `getMyPosts(params)` | 访问"我的帖子" | 显示当前用户的帖子 |
| `createPost(data)` | 点击"发布帖子" | 发布成功，Toast显示"发布成功" |
| `updatePost(id, data)` | 编辑自己的帖子 | 修改成功 |
| `deletePost(id)` | 删除自己的帖子 | 删除成功，Toast显示"删除成功" |
| `toggleLikePost(id)` | 点赞/取消点赞帖子 | 点赞数变化，生成通知 |
| `checkPostLiked(id)` | 查看帖子详情 | 点赞按钮状态正确 |
| `toggleCollectPost(id)` | 收藏/取消收藏帖子 | 收藏数变化，生成通知 |
| `checkPostCollected(id)` | 查看帖子详情 | 收藏按钮状态正确 |

---

### ✅ Comment模块 (3个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getCommentsByPost(id, params)` | 查看帖子详情 | 显示评论列表 |
| `createComment(data)` | 在帖子详情页发表评论 | 评论成功，comment_count+1 |
| `deleteComment(id)` | 删除自己的评论 | 删除成功 |

---

### ✅ Notification模块 (3个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getNotifications(params)` | 访问"消息通知" | 显示通知列表 |
| `markAsRead(id)` | 点击单条通知 | 标记为已读 |
| `markAllAsRead()` | 点击"全部已读" | 所有通知标记为已读 |

---

### ✅ Item模块 (12个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getItems(params)` | 访问"闲置"页面 | 显示物品列表 |
| `getItemById(id)` | 点击物品查看详情 | 显示物品详情，view_count+1 |
| `getMyItems(params)` | 访问"我的闲置" | 显示自己的物品 |
| `createItem(data)` | 点击"发布闲置" | 发布成功，Toast显示"发布成功" |
| `updateItem(id, data)` | 编辑自己的物品 | 修改成功 |
| `deleteItem(id)` | 删除自己的物品 | 删除成功 |
| `contactSeller(id)` | 联系卖家 | contact_count+1，创建会话 |
| `offlineItem(id)` | 下架自己的物品 | status变为3(已下架) |
| `onlineItem(id)` | 上架自己的物品 | status变为1(在售) |
| `completeItem(id)` | 标记交易完成 | status变为2(已完成) |

---

### ✅ ItemCollect模块 (3个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `toggleItemCollect(id)` | 收藏/取消收藏物品 | 收藏状态toggle |
| `checkItemCollected(id)` | 查看物品详情 | 收藏按钮状态正确 |
| `getCollectedItems(params)` | 访问"我的收藏" | 显示收藏的物品 |

---

### ✅ Conversation模块 (2个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getConversations()` | 访问"消息"页面 | 显示会话列表 |
| `createConversation(data)` | 联系卖家/买家 | 创建新会话 |

---

### ✅ Message模块 (2个函数)

| API函数 | 测试方法 | 预期结果 |
|--------|---------|---------|
| `getMessages(id, params)` | 点击会话 | 显示消息列表 |
| `getMessagesWithUser(id, params)` | 与用户聊天 | 显示与该用户的聊天记录 |

---

## 🔍 核心功能验证

### 1. Toast自动提示
- ✓ 成功操作显示绿色Toast
- ✓ 失败操作显示红色Toast
- ✓ Toast内容清晰（如"登录成功"、"手机号已存在"）

### 2. Loading自动显示
- ✓ API请求期间显示Loading
- ✓ 请求完成后Loading消失
- ✓ 多个并发请求正确处理

### 3. Token自动管理
- ✓ 登录/注册后自动保存Token
- ✓ 请求自动附加Authorization头
- ✓ 401错误自动清除Token并跳转登录

### 4. 错误自动处理
- ✓ 401 → Toast提示 + 跳转登录
- ✓ 403 → Toast提示"权限不足"
- ✓ 500 → Toast提示"服务器错误"
- ✓ 网络错误 → Toast提示详细错误

---

## 📊 测试统计

- **API模块数**: 10个
- **API函数总数**: 40+个
- **预计测试时间**: 15-20分钟（完整手动测试）
- **覆盖率**: 100%（所有API函数都有测试路径）

---

## 🐛 常见问题

### Q1: Toast不显示
**A**: 检查`toast.js`是否正确配置，确保`window.ElmMessage`可用或fallback到console

### Q2: Token没有保存
**A**: 检查：
- `request.js`的响应拦截器是否正确提取token
- `auth.js`的register函数是否正确保存token

### Q3: 401错误没有跳转
**A**: 检查`request.js`的错误拦截器是否处理401情况

### Q4: CORS错误
**A**: 确保后端已配置CORS，前端正确代理`/api`到`http://localhost:8080`

---

## ✨ 测试技巧

1. **使用浏览器DevTools**
   - Network标签：查看API请求
   - Console标签：查看错误日志
   - Application标签：查看localStorage

2. **测试异常情况**
   - 未登录访问需要认证的API
   - 提交空表单
   - 提交重复数据
   - 断网情况下操作

3. **验证数据更新**
   - 点赞后数字是否+1
   - 删除后列表是否刷新
   - 编辑后内容是否更新

---

## 📝 测试检查表

使用此清单确保所有功能都已测试：

- [ ] 登录功能（成功/失败）
- [ ] 注册功能（成功/手机号重复）
- [ ] 帖子CRUD（创建/读取/更新/删除）
- [ ] 帖子互动（点赞/收藏/评论）
- [ ] 物品CRUD（创建/读取/更新/删除）
- [ ] 物品状态变更（上架/下架/完成）
- [ ] 通知查看和标记已读
- [ ] 个人资料编辑
- [ ] 消息和会话
- [ ] Toast和Loading显示
- [ ] 错误处理（401/403/500）

---

## 🎯 测试完成标准

- ✅ 所有核心功能都能正常使用
- ✅ Toast和Loading自动显示/隐藏
- ✅ Token自动保存和清除
- ✅ 错误正确处理和提示
- ✅ 数据正确更新和刷新

如果所有项都通过，说明API层工作正常！🎉
