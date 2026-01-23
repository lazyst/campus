# AGENTS.md - 代理开发指南

本文档为 agentic coding agents 提供此代码库的开发规范和最佳实践。

---

## 📦 项目概览

**项目类型**: 前后端分离的全栈应用（校园互助平台）

```
campus/
├── backend/           # Spring Boot 3.2 + Java 17 + MyBatis-Plus
├── frontend-user/     # Vue 3 + Vant UI (移动端用户界面)
└── frontend-admin/    # Vue 3 + Element Plus (PC端管理界面)
```

---

## 🚀 常用命令

### Backend (Spring Boot)

```bash
cd backend

# 编译项目
mvn clean compile

# 启动服务 (端口: 8080)
mvn spring-boot:run

# 打包 (跳过测试)
mvn package -DskipTests

# 运行测试 (所有测试)
mvn test

# 运行单个测试类
mvn test -Dtest=PostServiceTest

# 运行单个测试方法
mvn test -Dtest=PostServiceTest#shouldIncrementViewCountSuccessfully
```

**后端服务端口**: 8080
**API 文档**: http://localhost:8080/swagger-ui.html

### Frontend - User (移动端)

```bash
cd frontend-user

# 启动开发服务器 (端口: 3000)
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行测试 (Vitest + Happy-DOM)
npm run test

# 运行单个测试文件
npm run test src/__tests__/example.spec.ts

# 代码检查和自动修复
npm run lint
```

### Frontend - Admin (管理端)

```bash
cd frontend-admin

# 启动开发服务器 (端口: 3001)
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行测试 (Vitest + Happy-DOM)
npm run test

# 代码检查和自动修复
npm run lint
```

---

## 🎨 代码风格指南

### 前端 (Vue 3 + TypeScript)

#### Import 语句组织

```typescript
// 1. 第三方库导入
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 2. 组件库导入 (按需)
import { ElMessage, ElCard } from 'element-plus'

// 3. 项目内部导入（使用@别名）
import MainLayout from '@/layouts/MainLayout.vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
```

**规则**:
- 按类别分组导入（第三方库 → 组件库 → 项目内部）
- 类型导入使用 `import type`
- 内部导入统一使用 `@/` 别名指向 src 目录

#### 命名约定

**文件名**:
- 组件: `PascalCase.vue` (如: `NavBar.vue`, `ForumList.vue`)
- 页面: `index.vue` (嵌套在功能目录下，如: `views/forum/detail/index.vue`)
- 工具/配置: 小驼峰或 kebab-case

**变量和函数**:
```typescript
// 小驼峰命名
const userStats = ref({ total: 0 })
const isLoading = ref(false)

// 函数名使用动词开头或 handle 前缀
function fetchUserInfo() { }
function handleSubmit() { }
const handleClick = () => { }
```

**接口和类型**:
```typescript
// PascalCase
export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  nickname: string
  avatar: string
}

export type UserStatus = 'normal' | 'banned' | 'deleted'
```

**API 函数**:
```typescript
// 动词 + 名词，简洁清晰
export function login(data: LoginParams): Promise<LoginResult>
export function getPosts(params: QueryParams): Promise<PostPage>
export function createPost(data: CreatePostDto): Promise<Post>
export function deletePost(id: number): Promise<void>
```

#### Vue 组件结构

```vue
<template>
  <div class="component-name">
    <!-- 模板内容 -->
  </div>
</template>

<script setup lang="ts">
// 1. 导入依赖
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 2. 定义 props
interface Props {
  title?: string
  disabled?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  title: '',
  disabled: false
})

// 3. 定义 emits
const emit = defineEmits<{
  click: [event: MouseEvent]
  submit: [data: FormData]
}>()

// 4. 响应式状态
const loading = ref(false)
const form = reactive({ username: '', password: '' })

// 5. 计算属性
const isValid = computed(() => form.username && form.password)

// 6. 方法定义
function handleSubmit() {
  emit('submit', form)
}

// 7. 生命周期钩子
onMounted(() => {
  // 初始化逻辑
})
</script>

<style scoped lang="scss">
// 样式代码
.component-name {
  // 使用 scoped 避免样式污染
}
</style>
```

**规则**:
- 优先使用组合式 API (`<script setup>`)
- 使用 TypeScript 定义 props 和 emits 类型
- 组件内部样式使用 `scoped`
- 用户端使用 Tailwind CSS，管理端使用 SCSS

#### 错误处理

```typescript
// API 调用错误处理
async function fetchData() {
  loading.value = true
  try {
    const data = await getPosts()
    posts.value = data
  } catch (error: any) {
    // 使用 ElMessage (管理端) 或 showToast (用户端)
    ElMessage.error(error.message || '获取数据失败')
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 表单验证错误处理
const formRef = ref<FormInstance>()
async function handleSubmit() {
  try {
    await formRef.value?.validate()
    // 验证通过后的逻辑
  } catch (error) {
    // 验证失败
  }
}
```

#### 状态管理 (Pinia)

```typescript
// stores/user.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  async function login(params: LoginParams) {
    const data = await loginApi(params)
    setToken(data.token)
    userInfo.value = data.user
  }

  return {
    token,
    userInfo,
    login,
    setToken
  }
})
```

**规则**:
- 使用 Setup Stores 风格（返回函数）
- 状态持久化到 localStorage
- 在组件中使用 `const userStore = useUserStore()`

---

### 后端 (Spring Boot + Java)

#### 命名约定

**类名**: PascalCase
```java
@RestController
public class PostController { }

@Service
public class PostServiceImpl implements PostService { }

@Data
public class Post extends BaseEntity { }
```

**方法名**: 小驼峰，动词开头
```java
public Result<Post> createPost(@RequestBody PostCreateRequest request) { }
public Result<Void> deletePost(@PathVariable Long id) { }
public Result<Page<Post>> getPosts(@RequestParam Integer page) { }
```

**常量**: 全大写下划线分隔
```java
private static final long serialVersionUID = 1L;
private static final Integer STATUS_NORMAL = 1;
```

**包名**: 全小写
```java
package com.campus.modules.forum.controller;
package com.campus.modules.auth.service.impl;
```

#### 代码结构

**Controller 层**:
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@Tag(name = "帖子管理")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 构造函数注入
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "分页获取帖子列表")
    @GetMapping
    public Result<Page<Post>> pagePosts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
               .orderByDesc(Post::getCreatedAt);
        postService.page(postPage, wrapper);
        return Result.success(postPage);
    }
}
```

**Service 层**:
```java
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            this.updateById(post);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementLikeCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            this.updateById(post);
        }
    }
}
```

**Entity 层**:
```java
import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
public class Post extends BaseEntity {

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 板块ID
     */
    private Long boardId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 状态：0删除 1正常
     */
    private Integer status;

    /**
     * 非数据库字段
     */
    @TableField(exist = false)
    private String userNickname;
}
```

#### API 文档注解

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

// Controller 级别
@Tag(name = "帖子管理")

// 方法级别
@Operation(summary = "创建帖子")
@Operation(summary = "分页获取帖子列表")
```

#### 统一响应格式

```java
// 成功响应
return Result.success(data);
return Result.success();

// 失败响应
return Result.error("错误消息");
return Result.error(ResultCode.PARAM_ERROR, "参数错误");

// 自定义 token
return Result.success(data).setToken(token);
```

#### 事务处理

```java
// 所有写操作必须添加事务注解
@Transactional(rollbackFor = Exception.class)
public void savePost(Post post) {
    this.save(post);
    // 相关业务逻辑
}

// 查询方法不需要事务
public Post getPostById(Long id) {
    return this.getById(id);
}
```

#### 日志记录

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostServiceImpl {

    public void incrementViewCount(Long postId) {
        log.info("增加浏览次数: postId={}", postId);
        // 业务逻辑
    }
}
```

---

## 🧪 测试规范

### 后端测试 (JUnit 5 + Mockito)

```java
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        // 测试前置准备
    }

    @Nested
    @DisplayName("incrementViewCount 方法测试")
    class IncrementViewCountTests {

        @Test
        @DisplayName("增加浏览次数成功")
        void shouldIncrementViewCountSuccessfully() {
            // Given
            Post post = new Post();
            post.setId(1L);
            post.setViewCount(10);
            when(postMapper.selectById(1L)).thenReturn(post);

            // When
            postService.incrementViewCount(1L);

            // Then
            assertEquals(11, post.getViewCount());
            verify(postMapper).updateById(post);
        }
    }
}
```

### 前端测试 (Vitest)

```typescript
import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from '@/components/base/Button.vue'

describe('Button Component', () => {
  it('should render correctly', () => {
    const wrapper = mount(Button, {
      props: { type: 'primary' }
    })
    expect(wrapper.find('button').exists()).toBe(true)
  })

  it('should emit click event', async () => {
    const wrapper = mount(Button)
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted()).toHaveProperty('click')
  })
})
```

---

## 🔧 配置文件

### 前端配置

**无独立 ESLint/Prettier 配置** - 使用框架默认配置

**Vite 配置关键点**:
- frontend-user: 端口 3000, Tailwind CSS, Vant UI 自动导入
- frontend-admin: 端口 3001, Element Plus 自动导入, ECharts

**API 代理**:
- `/api` → `http://localhost:8080`
- `/ws` → `ws://localhost:8080` (WebSocket)

### 后端配置

**关键配置** (application.yml):
- 服务端口: 8080
- 数据库: MySQL 8.0 (campus/123)
- JWT 有效期: 7 天
- WebSocket 端点: /ws

**MyBatis-Plus**:
- 继承 `ServiceImpl<Mapper, Entity>` 获得基础 CRUD
- 使用 `LambdaQueryWrapper` 构建查询条件

---

## 📋 开发注意事项

### 前端开发

1. **组件复用**: 优先使用现有组件，避免重复开发
2. **类型安全**: 所有 API 接口必须有 TypeScript 类型定义
3. **错误处理**: 所有 API 调用必须添加 try-catch
4. **加载状态**: 异步操作必须显示 loading 状态
5. **响应式数据**: 使用 `ref` 或 `reactive` 管理状态

### 后端开发

1. **统一响应**: 所有接口返回 `Result<T>` 格式
2. **事务管理**: 写操作必须添加 `@Transactional` 注解
3. **参数验证**: 使用 `@Valid` + `@Validated` 验证请求参数
4. **API 文档**: 所有接口必须添加 `@Operation` 注解
5. **日志记录**: 关键操作添加 `log.info()` 或 `log.error()`

### 通用原则

1. **代码简洁**: 避免过度设计，遵循 KISS 原则
2. **命名清晰**: 变量、函数、类的名称应该表达其用途
3. **注释规范**: 添加必要的 Javadoc/注释，解释业务逻辑
4. **提交前检查**: 运行 lint 和 test，确保代码质量
5. **遵循约定**: 优先使用项目现有的模式和工具类

---

## 🛠 常用工具类

### 后端

- `Result<T>` - 统一响应格式
- `ResultCode` - 响应码常量
- `BaseEntity` - 基础实体类 (包含 id, createdAt, updatedAt 等)
- `GlobalExceptionHandler` - 全局异常处理器

### 前端

- `request` - Axios 实例 (已配置拦截器)
- `useRouter()` - Vue Router 实例
- `useUserStore()` - 用户状态管理
- `ElMessage` / `showToast` - 消息提示
- `ref()` / `reactive()` - 响应式数据

---

## 📚 参考资源

- [Spring Boot 文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [Vue 3 文档](https://vuejs.org/)
- [Vant UI 文档](https://vant-ui.github.io/vant/)
- [Element Plus 文档](https://element-plus.org/)
- [SpringDoc OpenAPI](https://springdoc.org/)
