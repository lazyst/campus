# 代码规范

## 前端规范 (Vue 3 + JavaScript)

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase.vue | `UserProfile.vue` |
| 变量 | camelCase | `userInfo`, `isLoading` |
| 函数 | handle动词名词() | `handleSubmit()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| CSS 类 | kebab-case | `.user-profile-card` |
| Props | camelCase | `userName`, `isVisible` |
| 事件 | kebab-case | `handle-click` |

### 导入顺序

```javascript
// 1. Vue 核心 API
import { ref, computed, onMounted } from 'vue'

// 2. Vue Router
import { useRouter, useRoute } from 'vue-router'

// 3. Pinia Stores
import { useUserStore } from '@/stores/user'

// 4. 常量定义
import { API_ENDPOINTS } from '@/constants'

// 5. API 函数
import { login, logout } from '@/api/auth'

// 6. 公共组件
import UserCard from '@/components/UserCard.vue'

// 7. 样式文件
import '@/styles/main.css'
```

### 组件设计规范

#### 单一文件组件 (SFC) 结构

```vue
<script setup>
// 1. 导入
import { ref, computed } from 'vue'

// 2. Props 和 Emits
defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update', 'delete'])

// 3. 响应式数据
const isLoading = ref(false)

// 4. 计算属性
const userName = computed(() => user.value?.name || '')

// 5. 函数
const handleSubmit = async () => {
  // 实现
}

// 6. 生命周期
onMounted(() => {
  fetchData()
})
</script>

<template>
  <!-- 模板内容 -->
</template>

<style scoped>
/* 样式内容 */
</style>
```

#### 组合式函数命名

以 `use` 前缀开头：

```javascript
// good
useUser()
usePagination()
useRequest()

// bad
userComposable()
getUserData()
```

### 错误处理 (强制)

```javascript
// good
try {
  const res = await fetchUserData(userId)
} catch (error) {
  console.error('获取用户失败:', error)
  showToast('加载失败，请重试')
}

// bad - 禁止空 catch 块
try {
  const res = await fetchUserData(userId)
} catch (e) {}
```

### API 接口规范

```javascript
// 按模块组织 API
// api/user.js
import request from '@/api/request'

export function getUserProfile(userId) {
  return request.get(`/user/${userId}/profile`)
}

export function updateUserProfile(data) {
  return request.put('/user/profile', data)
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/user/avatar', formData)
}
```

### 样式规范

#### CSS 类命名

使用 BEM 或功能性命名：

```css
/* BEM 风格 */
.user-profile-card__header--active {}
.user-profile-card__avatar {}

/* 功能性命名 */
.message-item--unread {}
.btn-primary--large {}
```

#### Tailwind CSS 使用

- 禁止使用紫色/橙色（#6366F1）
- 使用 #1E3A8A 作为主色调
- 颜色间距从 `src/styles/design-tokens.css` 获取

## 后端规范 (Spring Boot + Java 17)

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类 | PascalCase | `PostService`, `UserController` |
| 方法 | camelCase + 动词 | `createPost()`, `getUserById()` |
| 变量 | camelCase | `postId`, `pageNum` |
| 常量 | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| 包 | lowercase | `com.campus.modules.auth` |
| 接口 | PascalCase + able | `Creatable`, `Serializable` |

### 分层模式

```
Controller → Service → Mapper → Entity
```

#### Controller 层

```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // 构造函数注入
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }
}
```

#### Service 层

```java
@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserService userService;

    public PostServiceImpl(PostMapper postMapper, UserService userService) {
        this.postMapper = postMapper;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(Long userId, PostCreateDTO dto) {
        // 1. 验证用户
        User user = userService.getUserEntity(userId);

        // 2. 创建帖子
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        postMapper.insert(post);

        // 3. 返回 VO
        return convertToVO(post);
    }
}
```

#### Mapper 层

```java
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT * FROM post WHERE id = #{id}")
    Post selectById(@Param("id") Long id);

    // 自定义查询方法
    List<PostVO> selectPostVOList(@Param("ew") LambdaQueryWrapper<Post> wrapper);
}
```

### 响应包装

```java
Result.success(data)           // 200 OK 带数据
Result.success()              // 200 OK 无数据
Result.error("message")       // 错误信息
Result.error(ResultCode.NOT_FOUND) // 带错误码
```

### 常用 ResultCode

```java
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    PARAM_ERROR(400, "参数错误"),
    TOKEN_EXPIRED(401, "Token 已过期"),
    USER_NOT_FOUND(404, "用户不存在"),
    POST_NOT_FOUND(404, "帖子不存在")
}
```

### 数据库操作规范

#### MyBatis-Plus 条件构造器

```java
// LambdaQueryWrapper - 类型安全
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper
    .eq(User::getStatus, 1)
    .like(User::getNickname, "张")
    .orderByDesc(User::getCreatedAt)
    .last("LIMIT 10");

// 或使用链式
userMapper.selectList(new LambdaQueryWrapper<User>()
    .eq(User::getDeleted, 0)
    .in(User::getId, List.of(1, 2, 3)));
```

### 异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }
}
```

## Git 提交规范

### 提交类型

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档更新 |
| `style` | 代码格式（不影响功能） |
| `refactor` | 重构 |
| `test` | 测试相关 |
| `chore` | 构建/辅助工具 |

### 提交格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 示例

```
feat(forum): 添加帖子收藏功能

- 新增收藏 API 接口
- 添加收藏状态缓存
- 优化收藏列表查询性能

Closes #123
Fix #456
```

```
fix(user): 修复用户头像上传失败问题

- 修复文件大小限制校验
- 添加文件类型校验

Resolves: #789
```

## 安全约束

### 禁止行为

| 约束 | 说明 |
|------|------|
| 禁止类型断言绕过 | `as any`, `@ts-ignore`, `@ts-expect-error` |
| 禁止空 catch 块 | 必须记录或处理异常 |
| 禁止删除测试 | 不能删除失败的测试来"通过" |
| 禁止硬编码密码 | 敏感信息使用环境变量 |
| 禁止 SQL 注入 | 必须使用参数化查询 |

### 安全实践

#### 密码处理

```java
// 使用 BCrypt 加密
@PostConstruct
public void init() {
    // 演示用，生产环境不要硬编码
    String rawPassword = "admin123456";
    String encodedPassword = passwordEncoder.encode(rawPassword);
    log.info("Encoded password: {}", encodedPassword);
}

// 验证密码
public boolean verifyPassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
}
```

#### SQL 注入防护

```java
// good - 使用参数化查询
@Select("SELECT * FROM user WHERE phone = #{phone}")
User selectByPhone(@Param("phone") String phone);

// bad - 禁止拼接 SQL
@Select("SELECT * FROM user WHERE phone = '" + phone + "'")
User selectByPhoneDangerous(@Param("phone") String phone);
```

#### 敏感数据过滤

```java
@Data
public class UserVO {
    private Long id;
    private String nickname;
    private String phone;  // 脱敏处理: 138****8888

    // 脱敏处理
    public String getPhone() {
        if (this.phone != null && this.phone.length() == 11) {
            return this.phone.substring(0, 3) + "****" + this.phone.substring(7);
        }
        return this.phone;
    }
}
```

## 代码审查清单

- [ ] 代码符合命名规范
- [ ] 错误处理完整，无空 catch 块
- [ ] 必要的注释，关键逻辑有解释
- [ ] 单元测试通过
- [ ] 无硬编码配置（密码、密钥等）
- [ ] 安全性检查（SQL 注入、XSS 等）
- [ ] 性能考虑（循环查询、N+1 问题等）
- [ ] 符合 Git 提交规范
