# 校园互助平台后端开发规范指南

## 一、项目结构规范

### 1.1 目录结构

后端项目采用标准的 Maven 项目结构，代码组织遵循分层架构原则。主源代码目录为 `src/main/java/com/campus/`，配置文件目录为 `src/main/resources/`。

```
backend/src/main/java/com/campus/
├── CampusApplication.java          # 应用启动类
├── config/                          # 配置类包
│   ├── WebConfig.java              # Web 配置（CORS、静态资源等）
│   ├── SecurityConfig.java         # 安全配置（认证、授权）
│   └── MybatisConfig.java          # MyBatis 配置
├── common/                          # 公共组件包
│   ├── Result.java                 # 统一响应包装类
│   ├── ResultCode.java             # 响应状态码枚举
│   ├── GlobalExceptionHandler.java # 全局异常处理器
│   ├── BusinessException.java      # 业务异常类
│   └── BaseEntity.java             # 实体基类（公共字段）
└── modules/                        # 业务模块包
    ├── auth/                       # 认证模块
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   ├── entity/
    │   └── dto/
    ├── forum/                      # 论坛模块
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   └── entity/
    ├── trade/                      # 交易模块
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   └── entity/
    ├── chat/                       # 聊天模块
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   └── entity/
    ├── user/                       # 用户模块
    │   ├── controller/
    │   ├── service/
    │   ├── mapper/
    │   └── entity/
    └── admin/                      # 后台管理模块
        ├── controller/
        ├── service/
        ├── mapper/
        └── entity/
```

### 1.2 模块划分规则

每个业务模块包含完整的代码层级：Controller、Service、Mapper、Entity、DTO。模块之间通过接口或依赖注入进行通信，避免直接依赖具体实现。

模块命名使用小写英文单词，准确表达模块的业务含义。模块内的类按照类型分别放置在对应的子包中。相关的 DTO（数据传输对象）统一放置在 `dto/` 目录下，按功能命名。

新增模块时，按照以下步骤操作：

1. 在 `modules/` 目录下创建新的模块包
2. 创建 Controller、Service、Mapper、Entity 子包
3. 在配置类中扫描新的 Mapper 接口
4. 在主类或配置类中声明新的 Service Bean

## 二、分层架构规范

### 2.1 Controller 层规范

Controller 层是请求入口，负责接收 HTTP 请求、参数校验、调用业务层、封装响应。Controller 层应该保持简洁，不包含业务逻辑代码。

**职责范围**：

- 接收请求参数，进行基本的格式校验
- 调用 Service 层方法执行业务逻辑
- 捕获异常并转换为统一的响应格式
- 封装返回结果为 `Result<T>` 对象

**代码规范**：

```java
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    // 使用构造函数注入依赖
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public Result<PostVO> getPost(@PathVariable Long id) {
        // 参数校验
        if (id == null || id <= 0) {
            return Result.error(ResultCode.PARAM_ERROR);
        }

        // 调用业务层
        PostVO post = postService.getPostById(id);

        // 响应封装
        return Result.success(post);
    }

    @PostMapping
    public Result<Long> createPost(
            @RequestBody @Valid CreatePostRequest request,
            @AuthenticationPrincipal Long userId) {
        // @Valid 触发参数校验
        // @AuthenticationPrincipal 获取登录用户ID
        Long postId = postService.createPost(request, userId);
        return Result.success(postId);
    }
}
```

**参数校验**：使用 `@Valid` 注解配合 Bean Validation 注解进行参数校验。常见的校验注解包括 `@NotNull`、`@NotBlank`、`@Size`、`@Min`、`@Max` 等。校验失败时，异常处理器会自动捕获并返回友好的错误信息。

**响应封装**：所有接口统一返回 `Result<T>` 类型。成功时使用 `Result.success(data)`，失败时使用 `Result.error(message)` 或 `Result.error(ResultCode)`。

### 2.2 Service 层规范

Service 层是业务逻辑的核心，负责处理业务规则、数据校验、事务管理和数据操作。Service 层应该具有原子性，一个方法完成一个完整的业务操作。

**职责范围**：

- 封装业务逻辑，实现业务规则
- 调用多个 Mapper 完成复杂的数据操作
- 管理事务边界，确保数据一致性
- 处理缓存逻辑（如果需要）
- 执行数据校验和权限验证

**代码规范**：

```java
@Service
public class PostService {

    private final PostMapper postMapper;
    private final LikeService likeService;
    private final UserService userService;

    public PostService(PostMapper postMapper, LikeService likeService, UserService userService) {
        this.postMapper = postMapper;
        this.likeService = likeService;
        this.userService = userService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createPost(CreatePostRequest request, Long userId) {
        // 1. 业务数据校验
        validatePostRequest(request);

        // 2. 创建帖子
        Post post = new Post();
        BeanUtils.copyProperties(request, post);
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        postMapper.insert(post);

        // 3. 更新用户帖子数（可选）
        userService.incrementPostCount(userId);

        // 4. 记录操作日志（可选）
        logOperation(userId, "CREATE_POST", post.getId());

        return post.getId();
    }

    private void validatePostRequest(CreatePostRequest request) {
        if (request.getTitle() == null || request.getTitle().length() < 5) {
            throw new BusinessException("标题长度不能少于5个字符");
        }
        if (request.getContent() == null || request.getContent().length() < 10) {
            throw new BusinessException("内容长度不能少于10个字符");
        }
    }
}
```

**事务管理**：使用 `@Transactional` 注解声明事务，指定 `rollbackFor = Exception.class` 确保所有异常都回滚事务。事务应该声明在 Service 层方法上，控制好事务边界，避免大事务导致的性能问题。

**异常处理**：业务异常使用 `BusinessException` 类抛出，在全局异常处理器中统一处理。避免在 Service 层吞掉异常，应该让异常传播到上层处理。

### 2.3 Mapper 层规范

Mapper 层负责数据访问，与数据库进行交互。Mapper 层应该保持简单，只负责数据访问，不包含业务逻辑。

**接口定义**：

```java
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    List<PostVO> selectPostList(@Param("query") PostQuery query);

    int updateViewCount(@Param("id") Long id);

    @Select("SELECT * FROM post WHERE id = #{id}")
    Post selectById(@Param("id") Long id);
}
```

**XML 映射文件**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.campus.modules.forum.mapper.PostMapper">

    <select id="selectPostList" resultType="com.campus.modules.forum.vo.PostVO">
        SELECT p.*,
               u.nickname as user_nickname,
               u.avatar as user_avatar,
               b.name as board_name
        FROM post p
        LEFT JOIN user u ON p.user_id = u.id
        LEFT JOIN board b ON p.board_id = b.id
        <where>
            <if test="query.boardId != null">
                AND p.board_id = #{query.boardId}
            </if>
            <if test="query.keyword != null and query.keyword != ''">
                AND (p.title LIKE CONCAT('%', #{query.keyword}, '%')
                     OR p.content LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            AND p.deleted = 0
            AND p.status = 1
        </where>
        ORDER BY p.created_at DESC
        LIMIT #{query.pageNum}, #{query.pageSize}
    </select>

</mapper>
```

**SQL 编写规范**：

- 使用 `#{}` 占位符进行参数绑定，避免 SQL 注入
- 复杂的动态查询使用 MyBatis-Plus 的条件构造器
- SQL 语句保持格式化，便于阅读和维护
- 大表查询添加必要的索引

### 2.4 Entity 层规范

Entity 层定义与数据库表对应的实体类，使用 MyBatis-Plus 注解进行字段映射。

**代码规范**：

```java
@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long boardId;

    private String title;

    private String content;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

**公共字段处理**：`createdAt`、`updatedAt`、`deleted` 是公共字段，建议抽取到 `BaseEntity` 基类中：

```java
@Data
public abstract class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

实体类继承 `BaseEntity`：

```java
@Data
@TableName("post")
public class Post extends BaseEntity {
    // 字段定义
}
```

## 三、代码风格规范

### 3.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | PascalCase，以业务含义结尾 | PostController、PostService、UserMapper |
| 方法名 | camelCase，动词开头 | createPost、getUserById、updateViewCount |
| 变量名 | camelCase | postId、pageNum、isValid |
| 常量名 | UPPER_SNAKE_CASE | DEFAULT_PAGE_SIZE、MAX_FILE_SIZE |
| 包名 | 小写单词 | com.campus.modules.auth |

### 3.2 注释规范

类和方法使用 Javadoc 注释，说明功能、参数、返回值：

```java
/**
 * 帖子服务类
 * <p>
 * 提供帖子的 CRUD、点赞、收藏等业务逻辑处理
 * </p>
 *
 * @author Campus Team
 * @version 1.0
 */
@Service
public class PostService {

    /**
     * 根据ID获取帖子详情
     *
     * @param id 帖子ID
     * @return 帖子详情VO
     * @throws BusinessException 帖子不存在或已被删除
     */
    public PostVO getPostById(Long id) {
        // 实现代码
    }
}
```

复杂逻辑在代码中添加行内注释，说明实现思路和关键步骤。

### 3.3 格式规范

遵循统一的代码格式规范：

- 使用 4 空格缩进（不是 Tab）
- 大括号使用 K&R 风格（同一行）
- 方法之间空一行
- 运算符左右各空一格
- 每行代码长度不超过 120 字符

建议使用 IDE 的代码格式化功能（快捷键 `Ctrl+Alt+L`）自动格式化代码。

## 四、API 文档编写规范

使用 Swagger 注解为 API 接口添加文档说明：

```java
@Tag(name = "帖子管理", description = "帖子相关接口")
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Operation(summary = "获取帖子详情")
    @Parameter(name = "id", description = "帖子ID", required = true)
    @GetMapping("/{id}")
    public Result<PostVO> getPost(@PathVariable Long id) {
        // 实现代码
    }

    @Operation(summary = "发布帖子")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建帖子请求",
            required = true,
            content = @Content(schema = @Schema(implementation = CreatePostRequest.class))
    )
    @PostMapping
    public Result<Long> createPost(@RequestBody @Valid CreatePostRequest request) {
        // 实现代码
    }
}
```

**常用注解**：

| 注解 | 说明 |
|------|------|
| @Tag | 接口分类标签 |
| @Operation | 接口详细说明 |
| @Parameter | 参数说明 |
| @RequestBody | 请求体说明 |
| @Content | 响应内容说明 |

## 五、异常处理规范

### 5.1 异常分类

系统异常分为以下几类：

- **业务异常（BusinessException）**：业务逻辑校验失败抛出
- **参数异常（MethodArgumentNotValidException）**：参数校验失败抛出
- **访问拒绝异常（AccessDeniedException）**：权限不足抛出
- **系统异常（Exception）**：其他未知异常

### 5.2 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return Result.error(message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("访问拒绝: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(ResultCode.ERROR);
    }
}
```

### 5.3 业务异常定义

```java
@Data
public class BusinessException extends RuntimeException {

    private final String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
}
```

## 六、数据库操作规范

### 6.1 查询规范

- 分页查询必须使用 LIMIT 和 OFFSET
- 查询结果为空时返回 null 或空列表，不是异常
- 查询时明确指定返回字段，避免使用 `SELECT *`
- 大表查询添加必要的分页和条件限制

### 6.2 更新规范

- 更新操作必须带条件，禁止不带条件的全表更新
- 使用乐观锁防止并发更新冲突（可选）
- 软删除使用 `@TableLogic` 注解
- 更新时同时更新 `updated_at` 字段

### 6.3 事务规范

- 事务粒度要小，避免长事务
- 只在 Service 层声明事务
- `rollbackFor = Exception.class` 确保所有异常回滚
- 读操作不加事务，减少锁竞争

## 七、测试规范

### 7.1 单元测试

Service 层应该编写单元测试，验证业务逻辑的正确性：

```java
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void shouldCreatePostSuccessfully() {
        // Given
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("测试帖子");
        request.setContent("测试内容");
        request.setBoardId(1L);

        // When
        Long postId = postService.createPost(request, 1L);

        // Then
        assertNotNull(postId);
        verify(postMapper).insert(any(Post.class));
    }
}
```

### 7.2 接口测试

Controller 层使用 `@WebMvcTest` 进行接口测试：

```java
@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnPostWhenIdExists() throws Exception {
        mockMvc.perform(get("/api/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
```

### 7.3 测试覆盖率

项目要求核心业务代码的测试覆盖率达到 70% 以上。新增功能必须同时添加对应的测试用例。

## 八、Git 提交规范

### 8.1 提交信息格式

提交信息使用以下格式：

```
<type>(<scope>): <subject>

<body>

footer
```

**类型（type）**：

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | Bug 修复 |
| docs | 文档更新 |
| style | 代码格式调整 |
| refactor | 重构 |
| test | 测试相关 |
| chore | 构建或辅助工具更新 |

**示例**：

```
feat(post): 新增帖子收藏功能

- 实现收藏和取消收藏接口
- 添加收藏列表查询接口
- 更新帖子收藏数统计

Closes #123
```

### 8.2 提交前检查

提交代码前执行以下检查：

1. 代码编译通过
2. 测试通过
3. 代码格式规范
4. 无临时文件和调试代码

```bash
# 编译检查
mvn compile

# 测试运行
mvn test

# 代码检查
mvn checkstyle:check
```

---

**文档版本**：1.0

**最后更新**：2026年2月

**维护团队**：校园互助平台开发团队
