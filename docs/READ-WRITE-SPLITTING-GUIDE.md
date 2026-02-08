# 校园互助平台 - 数据库读写分离启用指南

## 概述

本指南说明如何为校园互助平台启用数据库读写分离功能。

## 当前状态

✅ **已完成**：
- 动态数据源配置 (`application-dev.yml`)
- AOP 切面配置 (`DsAspectConfig.java`)
- 主从数据库连接配置

❌ **需要手动修复**：
- 批量修改过程中出现了编译错误
- 需要重新正确修改 ServiceImpl 文件

## 读写分离规则

| 操作类型 | 数据源 | 注解 |
|---------|--------|------|
| 查询（SELECT） | 从库（slave） | `@DS("slave")` |
| 写入（INSERT/UPDATE/DELETE） | 主库（master） | 默认，无需注解 |

## 修改方法

### 方法一：手动修改（推荐）

为每个读操作方法添加 `@DS("slave")` 注解：

```java
// 修改前
@Override
public List<Board> list() {
    return super.list();
}

// 修改后
@Override
@DS("slave")
public List<Board> list() {
    return super.list();
}
```

### 方法二：使用 IDE 重构

1. 在 IDE 中打开 `BoardServiceImpl.java`
2. 找到 `existsByName` 方法
3. 添加注解：`@DS("slave")`

## 需要修改的文件清单

### 论坛模块

#### 1. BoardServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/BoardServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, Board> implements BoardService {

    @Override
    @DS("slave")
    public boolean existsByName(String name) {
        // 现有代码保持不变
    }
}
```

#### 2. PostServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/PostServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    @DS("slave")
    public boolean isAuthor(Long postId, Long userId) {
        // 现有代码保持不变
    }

    // 其他读操作方法...
}
```

#### 3. CommentServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/CommentServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    @DS("slave")
    public List<Comment> getByPostId(Long postId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public boolean isAuthor(Long commentId, Long userId) {
        // 现有代码保持不变
    }
}
```

#### 4. LikeServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/LikeServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Override
    @DS("slave")
    public boolean hasLiked(Long userId, Long postId) {
        // 现有代码保持不变
    }

    // toggleLike 是写操作，不需要注解
}
```

#### 5. CollectServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/CollectServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Override
    @DS("slave")
    public boolean hasCollected(Long userId, Long postId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<Post> getCollectedPosts(Long userId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<Long> getCollectedPostIds(Long userId) {
        // 现有代码保持不变
    }
}
```

#### 6. NotificationServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/forum/service/impl/NotificationServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    @DS("slave")
    public List<Notification> getByUserId(Long userId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public int getUnreadCount(Long userId) {
        // 现有代码保持不变
    }
}
```

### 交易模块

#### 7. ItemServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/trade/service/impl/ItemServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Override
    @DS("slave")
    public List<Item> getByUserId(Long userId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public Item getDetail(Long itemId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public boolean isAuthor(Long userId, Long itemId) {
        // 现有代码保持不变
    }
}
```

#### 8. ItemCollectServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/trade/service/impl/ItemCollectServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class ItemCollectServiceImpl extends ServiceImpl<ItemCollectMapper, ItemCollect> implements ItemCollectService {

    @Override
    @DS("slave")
    public boolean hasCollected(Long userId, Long itemId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<ItemCollect> getByUserId(Long userId) {
        // 现有代码保持不变
    }
}
```

### 用户模块

#### 9. UserServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/user/service/impl/UserServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @DS("slave")
    public User getByPhone(String phone) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public boolean existsByPhone(String phone) {
        // 现有代码保持不变
    }
}
```

### 聊天模块

#### 10. ChatServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/chat/service/impl/ChatServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class ChatServiceImpl extends ServiceImpl<MessageMapper, Message> implements ChatService {

    @Override
    @DS("slave")
    public Integer getTotalUnreadCount(Long userId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<Conversation> getConversations(Long userId) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<Message> getMessages(Long conversationId, Integer page, Integer size) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public List<Message> getMessagesWithUser(Long userId, Long otherUserId, Integer page, Integer size) {
        // 现有代码保持不变
    }
}
```

### 管理模块

#### 11. AdminServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/admin/service/impl/AdminServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    @DS("slave")
    public Admin getByUsername(String username) {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public boolean isSuperAdmin(Long adminId) {
        // 现有代码保持不变
    }
}
```

#### 12. DashboardServiceImpl.java
**路径**: `backend/src/main/java/com/campus/modules/admin/service/impl/DashboardServiceImpl.java`

**修改内容**：
```java
import com.baomidou.dynamic.datasource.annotation.DS;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    @DS("slave")
    public Map<String, Object> getStats() {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public Map<String, Object> getTrendData() {
        // 现有代码保持不变
    }

    @Override
    @DS("slave")
    public Map<String, Object> getRecentActivity() {
        // 现有代码保持不变
    }
}
```

## 验证步骤

### 1. 编译检查
```bash
cd backend
mvn clean compile
```

### 2. 启动服务
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 查看日志
启动时观察日志，应该看到类似：
```
[DEBUG] 切换数据源到: slave
```

### 4. API 测试
```bash
# 测试读取操作（应该走从库）
curl http://localhost:8080/api/boards

# 测试写入操作（应该走主库）
curl -X POST http://localhost:8080/api/posts
```

## 常见问题

### Q1: 编译错误 "Duplicate method"
**原因**: 文件中方法重复定义
**解决**: 删除重复的方法定义

### Q2: 找不到符号 "getUserId()" 等
**原因**: Lombok 注解未正确处理
**解决**: 
1. 确保安装了 Lombok 插件
2. 重新导入项目
3. 执行 `mvn clean`

### Q3: @DS 注解不生效
**原因**: AOP 切面顺序问题
**解决**: 确保 `DsAspectConfig` 的 `@Order(0)` 注解存在

## 注意事项

1. **读写分离只影响查询性能**，写入操作不受影响
2. **主从同步有延迟**，实时性要求高的查询可能需要走主库
3. **事务内的查询会走主库**，以保证数据一致性
4. **建议先在开发环境测试**，确认无误后再部署到生产环境

## 监控读写分离效果

可以在 `application.yml` 中添加日志配置：

```yaml
logging:
  level:
    com.baomidou.dynamic.datasource: DEBUG
```

这样可以观察到每次数据库操作使用的数据源。

## 联系

如有问题，请检查：
1. 数据库连接配置是否正确
2. 主从数据库是否正常同步
3. 网络连接是否正常

---
**生成时间**: 2026-02-04
