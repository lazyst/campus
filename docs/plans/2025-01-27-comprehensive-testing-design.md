# 校园互助平台全面测试体系设计方案

**设计日期**: 2025-01-27
**版本**: v1.0
**设计者**: Claude Code

---

## 1. 概述

### 1.1 设计目标

为校园互助平台建立完整的测试体系，确保系统质量和稳定性：
- 建立三层测试架构（后端、前端、E2E）
- 达到代码覆盖率 ≥ 70%
- 覆盖所有核心业务流程
- 支持持续集成和快速回归

### 1.2 测试范围

**后端测试（175个用例）**
- 认证模块：注册、登录、Token验证
- 用户模块：个人信息管理
- 论坛模块：发帖、评论、点赞、收藏
- 交易模块：商品发布、浏览、管理
- 聊天模块：会话、消息
- 通知系统：通知生成、已读标记

**前端测试（55个用例）**
- 登录注册组件
- 论坛组件（列表、详情、发帖）
- 交易组件（列表、详情、发布）
- 个人中心组件
- 状态管理和API拦截器

**E2E测试（25个步骤）**
- 新用户注册到发帖
- 登录到评论点赞收藏
- 二手交易完整流程
- 通知系统测试
- 权限控制测试

---

## 2. 测试架构

### 2.1 三层测试金字塔

```
        E2E测试 (25个)
       /             \
      /               \
     /                 \
  前端测试 (55个)    后端测试 (175个)
```

**底层：后端单元测试和集成测试**
- 技术栈：JUnit 5 + Mockito + Spring Boot Test
- 测试目标：Service层和Controller层
- 数据库：真实MySQL测试库
- 覆盖率目标：≥70%

**中层：前端组件测试**
- 技术栈：Vitest + Vue Test Utils
- 测试目标：组件和状态管理
- Mock策略：Mock API响应
- 覆盖率目标：核心组件100%

**顶层：端到端测试**
- 技术栈：Playwright
- 测试目标：完整用户场景
- 环境：真实浏览器 + 真实数据库
- 覆盖率目标：核心流程100%

### 2.2 测试数据库

**配置**
- 数据库名：campus_test
- 配置文件：application-test.yml
- 初始化脚本：test-data.sql

**测试数据**
- 测试用户：3个（普通用户2个、管理员1个）
- 测试板块：2个
- 测试帖子：5篇
- 测试商品：3个

**数据管理**
- 每次测试前清空并重新初始化
- 使用@Transactional回滚

---

## 3. 后端测试设计

### 3.1 P0 核心业务测试（必须）

#### 认证模块（32个用例）
```java
AuthServiceTest:
- register(): 成功注册、手机号重复、密码加密、必填字段验证
- login(): 成功登录、密码错误、用户不存在、用户被禁用
- generateToken(): Token生成、Token解析、过期验证
- validateToken(): 有效Token、无效Token、过期Token、格式错误
- getUserIdFromToken(): 正确提取、Token无效
```

#### 用户模块（15个用例）
```java
UserServiceTest:
- getByPhone(): 查询成功、查询失败
- existsByPhone(): 存在、不存在
- register(): 注册成功、手机号重复
- updateProfile(): 更新成功、更新不存在用户、字段验证
- getUserInfo(): 获取成功、用户不存在
```

#### 论坛模块（60个用例）
```java
PostServiceTest:
- createPost(): 成功、板块不存在、用户不存在
- updatePost(): 成功、非作者、帖子不存在
- deletePost(): 成功、非作者、帖子不存在
- incrementViewCount(): 浏览数+1
- incrementLikeCount(): 点赞数+1
- decrementLikeCount(): 点赞数-1
- incrementCommentCount(): 评论数+1
- incrementCollectCount(): 收藏数+1
- decrementCollectCount(): 收藏数-1
- isAuthor(): 验证成功、验证失败

CommentServiceTest:
- createComment(): 成功、帖子不存在
- getByPostId(): 获取成功、分页查询
- isAuthor(): 验证成功、验证失败

LikeServiceTest:
- toggleLike(): 点赞成功、取消点赞、重复点赞

CollectServiceTest:
- toggleCollect(): 收藏成功、取消收藏、重复收藏
```

### 3.2 P1 重要功能测试（应该）

#### 交易模块（28个用例）
```java
ItemServiceTest:
- createItem(): 发布成功、用户不存在
- updateItem(): 更新成功、非发布者、商品不存在
- updateStatus(): 状态更新（正常→完成、正常→下架）
- incrementViewCount(): 浏览数+1
- incrementContactCount(): 联系数+1
- onlineItem(): 上架成功、已上架状态
- offlineItem(): 下架成功、已下架状态
```

#### 聊天模块（16个用例）
```java
ChatServiceTest:
- getOrCreateConversation(): 创建成功、获取已有、双向查找
- saveMessage(): 保存成功、会话不存在
- getMessages(): 获取成功、分页查询、会话不存在
- getMessagesWithUser(): 获取成功、分页查询
```

#### 通知系统（16个用例）
```java
NotificationServiceTest:
- createNotification(): 创建成功、用户不存在
- getUserNotifications(): 获取成功、分页查询
- markAsRead(): 标记成功、通知不存在
- markAllAsRead(): 全部标记成功
```

### 3.3 测试用例统计

| 模块 | 测试类 | 方法数 | 用例数 |
|------|--------|--------|--------|
| 认证模块 | AuthServiceTest | 8 | 32 |
| 用户模块 | UserServiceTest | 5 | 15 |
| 论坛-帖子 | PostServiceTest | 10 | 40 |
| 论坛-评论 | CommentServiceTest | 3 | 12 |
| 论坛-点赞 | LikeServiceTest | 2 | 8 |
| 论坛-收藏 | CollectServiceTest | 2 | 8 |
| 交易模块 | ItemServiceTest | 7 | 28 |
| 聊天模块 | ChatServiceTest | 4 | 16 |
| 通知系统 | NotificationServiceTest | 4 | 16 |
| **总计** | **9个** | **45个** | **175个** |

---

## 4. 前端测试设计

### 4.1 核心组件测试

#### 登录注册（10个用例）
```typescript
login.spec.ts:
- 表单验证：手机号格式、密码长度
- 登录成功：保存token、跳转首页
- 登录失败：显示错误提示
- 注册成功：自动登录、跳转首页
- 注册失败：手机号重复
```

#### 论坛组件（15个用例）
```typescript
PostList.spec.ts:
- 加载帖子列表
- 下拉刷新
- 上拉加载更多
- 点击帖子跳转

PostDetail.spec.ts:
- 显示帖子详情
- 点赞功能（登录/未登录）
- 评论功能（登录/未登录）
- 收藏功能（登录/未登录）
```

#### 交易组件（10个用例）
```typescript
ItemList.spec.ts:
- 商品列表展示
- 筛选排序
- 点击详情

ItemCreate.spec.ts:
- 表单验证
- 发布成功
- 图片上传
```

#### 个人中心（10个用例）
```typescript
Profile.spec.ts:
- 显示用户信息
- 编辑资料
- 我的帖子
- 我的商品
- 我的收藏
```

### 4.2 状态管理测试（10个用例）

```typescript
user.spec.ts (Pinia Store):
- login(): 保存token和用户信息
- logout(): 清除token和用户信息
- fetchUserInfo(): 获取用户信息
- isAuthenticated(): 判断登录状态
- updateProfile(): 更新用户信息
```

### 4.3 API拦截器测试（5个用例）

```typescript
request.spec.ts:
- 请求拦截：自动添加Authorization头
- 401响应：清除token、跳转登录
- 403响应：显示权限不足
- Token自动保存
- 错误提示显示
```

---

## 5. E2E测试设计

### 5.1 核心场景

**场景1：新用户注册到发帖（7个步骤）**
1. 访问首页
2. 点击注册，填写信息
3. 注册成功，自动登录
4. 进入论坛页面
5. 点击发帖按钮
6. 填写并发布帖子
7. 验证帖子显示在列表

**场景2：登录到评论点赞收藏（9个步骤）**
1. 访问登录页
2. 输入账号密码登录
3. 浏览帖子列表
4. 打开帖子详情
5. 点赞帖子，验证点赞数+1
6. 发表评论，验证评论数+1
7. 收藏帖子，验证收藏数+1
8. 查看个人中心-我的收藏
9. 验证收藏的帖子显示

**场景3：二手交易完整流程（10个步骤）**
1. 登录系统
2. 进入闲置市场
3. 点击发布商品
4. 填写商品信息
5. 上传商品图片
6. 发布成功
7. 浏览商品列表，验证新商品
8. 点击商品详情，验证浏览数+1
9. 点击联系卖家，跳转聊天
10. 下架商品，验证状态更新

**场景4：通知系统测试（7个步骤）**
1. 用户A登录
2. 用户B登录
3. 用户B评论用户A的帖子
4. 用户A查看通知列表
5. 验证收到评论通知
6. 标记为已读
7. 验证通知未读数减少

**场景5：权限控制测试（6个步骤）**
1. 未登录访问首页 → 允许
2. 未登录点击发帖 → 提示登录
3. 未登录点击点赞 → 提示登录
4. 未登录访问个人中心 → 提示登录
5. 登录后重复操作 → 全部成功
6. Token过期访问 → 提示重新登录

---

## 6. 实施计划

### 6.1 时间表

**Phase 1: 后端测试（Day 1-2）**
- Day 1 上午：环境搭建（测试数据库、JaCoCo、测试数据脚本）
- Day 1 下午：认证、用户模块测试
- Day 2 上午：论坛模块测试
- Day 2 下午：交易、聊天、通知测试 + 覆盖率验证

**Phase 2: 前端测试（Day 3-4）**
- Day 3 上午：前端测试环境搭建
- Day 3 下午：登录注册、论坛组件测试
- Day 4：交易、个人中心组件测试 + 验证

**Phase 3: E2E测试（Day 5）**
- Day 5 上午：E2E环境搭建
- Day 5 下午：5个核心场景测试 + 回归测试

### 6.2 工具和依赖

**后端**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>
<dependency>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
</dependency>
```

**前端**
```bash
npm install -D vitest @vitest/ui
npm install -D @vue/test-utils
npm install -D @playwright/test
npm install -D @testing-library/vue
npm install -D msw
```

### 6.3 运行命令

**后端**
```bash
mvn clean test
mvn test -Dtest=AuthServiceTest
mvn clean test jacoco:report
```

**前端**
```bash
npm run test
npm run test -- --ui
npm run test -- --coverage
```

**E2E**
```bash
npm run test:e2e
npm run test:e2e -- user-journey.spec.ts
npm run test:e2e -- --debug
```

---

## 7. 成功标准

### 7.1 后端测试
- ✅ 所有测试用例通过（175个）
- ✅ 行覆盖率 ≥ 70%
- ✅ 分支覆盖率 ≥ 60%
- ✅ 无 skipped 测试

### 7.2 前端测试
- ✅ 所有测试用例通过（55个）
- ✅ 关键组件100%覆盖
- ✅ 状态管理100%覆盖

### 7.3 E2E测试
- ✅ 5个核心场景全部通过
- ✅ 每个场景的步骤成功率100%

---

## 8. 附录

### 8.1 测试数据脚本示例

```sql
-- test-data.sql
DELETE FROM message;
DELETE FROM conversation;
DELETE FROM notification;
DELETE FROM item_collect;
DELETE FROM item;
DELETE FROM collect;
DELETE FROM like_record;
DELETE FROM comment;
DELETE FROM post;
DELETE FROM board;
DELETE FROM user WHERE phone LIKE 'test%';

INSERT INTO user (phone, password, nickname, avatar, gender, bio, status, created_at, deleted) VALUES
('testuser1', '$2a$10$encrypted', '测试用户1', 'https://example.com/avatar1.jpg', 1, '这是测试用户1', 1, NOW(), 0),
('testuser2', '$2a$10$encrypted', '测试用户2', 'https://example.com/avatar2.jpg', 2, '这是测试用户2', 1, NOW(), 0);

INSERT INTO board (name, description, sort_order, created_at, deleted) VALUES
('测试板块1', '这是测试板块1', 1, NOW(), 0),
('测试板块2', '这是测试板块2', 2, NOW(), 0);

INSERT INTO post (user_id, board_id, title, content, view_count, like_count, comment_count, collect_count, created_at, deleted) VALUES
(1, 1, '测试帖子1', '这是测试帖子1的内容', 0, 0, 0, 0, NOW(), 0),
(2, 2, '测试帖子2', '这是测试帖子2的内容', 0, 0, 0, 0, NOW(), 0);
```

### 8.2 JaCoCo配置

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

**文档状态**: ✅ 设计完成
**下一步**: 开始实施测试
