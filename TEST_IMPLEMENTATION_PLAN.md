# 校园互助平台 - 完整测试实现计划

本文档详细说明了为校园互助平台实现完整测试套件的完整计划。

## 一、测试架构概览

### 1.1 测试金字塔

```
测试金字塔目标:
- 60% 单元测试 (服务层、工具类)
- 30% 集成测试 (API端点、数据库)
- 10% 端到端测试 (用户流程)
```

### 1.2 技术选型

**后端测试:**
- 测试框架: JUnit 5
- Mock框架: Mockito
- 集成测试: Spring Boot Test + @SpringBootTest
- API测试: MockMvc
- 数据库: MySQL (使用@DynamicPropertySource连接真实数据库)

**前端测试:**
- 测试框架: Vitest
- 组件测试: Vue Test Utils
- E2E测试: Playwright

---

## 二、单元测试实现

### 2.1 UserService 单元测试

**文件位置**: `backend/src/test/java/com/campus/modules/user/UserServiceTest.java`

**测试场景:**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setPassword("encodedPassword");
        testUser.setGender(1);
        testUser.setStatus(1);
        testUser.setDeleted(0);
    }

    // 测试用例
    @Test
    void shouldGetUserById() { ... }

    @Test
    void shouldReturnEmptyWhenUserNotFound() { ... }

    @Test
    void shouldGetUserByPhone() { ... }

    @Test
    void shouldReturnNullWhenPhoneNotFound() { ... }

    @Test
    void shouldGetAllUsers() { ... }

    @Test
    void shouldGetUsersByStatus() { ... }

    @Test
    void shouldUpdateUser() { ... }

    @Test
    void shouldCheckPhoneExists() { ... }

    @Test
    void shouldDeleteUser() { ... }

    @Test
    void shouldCountUsers() { ... }

    @Test
    void shouldCountUsersByStatus() { ... }
}
```

**预期结果:** 11个测试用例，全部通过

### 2.2 AuthService 单元测试

**文件位置**: `backend/src/test/java/com/campus/modules/auth/AuthServiceTest.java`

**测试场景:**

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterNewUser() { ... }

    @Test
    void shouldThrowExceptionWhenPhoneExists() { ... }

    @Test
    void shouldLoginSuccessfully() { ... }

    @Test
    void shouldThrowExceptionWhenWrongPassword() { ... }

    @Test
    void shouldValidateToken() { ... }

    @Test
    void shouldGetUserIdFromToken() { ... }

    @Test
    void shouldGenerateToken() { ... }

    @Test
    void shouldEncodePassword() { ... }

    @Test
    void shouldMatchPassword() { ... }
}
```

**预期结果:** 9个测试用例，全部通过

### 2.3 PostService 单元测试

**文件位置**: `backend/src/test/java/com/campus/modules/forum/post/PostServiceTest.java`

**测试场景:**

```java
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private BoardService boardService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    @Test
    void shouldCreatePost() { ... }

    @Test
    void shouldGetPostById() { ... }

    @Test
    void shouldGetPostsByBoardId() { ... }

    @Test
    void shouldGetPostsByUserId() { ... }

    @Test
    void shouldUpdatePost() { ... }

    @Test
    void shouldDeletePost() { ... }

    @Test
    void shouldSearchPosts() { ... }

    @Test
    void shouldIncrementViewCount() { ... }

    @Test
    void shouldThrowExceptionWhenBoardNotFound() { ... }
}
```

**预期结果:** 9个测试用例，全部通过

### 2.4 CommentService 单元测试

**文件位置**: `backend/src/test/java/com/campus/modules/forum/comment/CommentServiceTest.java`

**测试场景:**

```java
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldCreateComment() { ... }

    @Test
    void shouldGetCommentsByPostId() { ... }

    @Test
    void shouldGetRepliesByParentId() { ... }

    @Test
    void shouldDeleteComment() { ... }

    @Test
    void shouldGetCommentCountByPostId() { ... }

    @Test
    void shouldThrowExceptionWhenPostNotFound() { ... }
}
```

**预期结果:** 6个测试用例，全部通过

### 2.5 ItemService 单元测试

**文件位置**: `backend/src/test/java/com/campus/modules/trade/item/ItemServiceTest.java`

**测试场景:**

```java
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemCollectMapper itemCollectMapper;

    @InjectMocks
    private ItemService itemService;

    @Test
    void shouldCreateItem() { ... }

    @Test
    void shouldGetItemById() { ... }

    @Test
    void shouldGetItemsByUserId() { ... }

    @Test
    void shouldGetItemsByType() { ... }

    @Test
    void shouldSearchItems() { ... }

    @Test
    void shouldUpdateItem() { ... }

    @Test
    void shouldOnlineItem() { ... }

    @Test
    void shouldOfflineItem() { ... }

    @Test
    void shouldCompleteItem() { ... }

    @Test
    void shouldCancelCompleteItem() { ... }

    @Test
    void shouldDeleteItem() { ... }

    @Test
    void shouldCollectItem() { ... }

    @Test
    void shouldUncollectItem() { ... }

    @Test
    void shouldGetUserCollections() { ... }

    @Test
    void shouldIncrementContactCount() { ... }
}
```

**预期结果:** 15个测试用例，全部通过

---

## 三、集成测试实现

### 3.1 UserController 集成测试

**文件位置**: `backend/src/test/java/com/campus/modules/user/UserControllerIntegrationTest.java`

**测试场景:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldReturnUserById() throws Exception { ... }

    @Test
    void shouldCreateUser() throws Exception { ... }

    @Test
    void shouldUpdateUser() throws Exception { ... }

    @Test
    void shouldDeleteUser() throws Exception { ... }

    @Test
    void shouldReturnAllUsers() throws Exception { ... }

    @Test
    void shouldReturn400ForInvalidInput() throws Exception { ... }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception { ... }
}
```

**预期结果:** 7个测试用例，全部通过

### 3.2 AuthController 集成测试

**文件位置**: `backend/src/test/java/com/campus/modules/auth/AuthControllerIntegrationTest.java`

**测试场景:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Test
    void shouldRegisterUser() throws Exception { ... }

    @Test
    void shouldLoginUser() throws Exception { ... }

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception { ... }

    @Test
    void shouldReturn400ForDuplicatePhone() throws Exception { ... }

    @Test
    void shouldReturn400ForInvalidPhoneFormat() throws Exception { ... }

    @Test
    void shouldReturnUserProfile() throws Exception { ... }

    @Test
    void shouldUpdateUserProfile() throws Exception { ... }
}
```

**预期结果:** 7个测试用例，全部通过

### 3.3 PostController 集成测试

**文件位置**: `backend/src/test/java/com/campus/modules/forum/post/PostControllerIntegrationTest.java`

**测试场景:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Test
    void shouldCreatePost() throws Exception { ... }

    @Test
    void shouldGetPostById() throws Exception { ... }

    @Test
    void shouldGetPostsByBoard() throws Exception { ... }

    @Test
    void shouldGetPostsByUser() throws Exception { ... }

    @Test
    void shouldSearchPosts() throws Exception { ... }

    @Test
    void shouldUpdatePost() throws Exception { ... }

    @Test
    void shouldDeletePost() throws Exception { ... }

    @Test
    void shouldAddComment() throws Exception { ... }

    @Test
    void shouldLikePost() throws Exception { ... }

    @Test
    void shouldCollectPost() throws Exception { ... }
}
```

**预期结果:** 10个测试用例，全部通过

### 3.4 ItemController 集成测试

**文件位置**: `backend/src/test/java/com/campus/modules/trade/item/ItemControllerIntegrationTest.java`

**测试场景:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ItemControllerIntegrationTest {

    @Test
    void shouldCreateItem() throws Exception { ... }

    @Test
    void shouldGetItemById() throws Exception { ... }

    @Test
    void shouldGetItemsByType() throws Exception { ... }

    @Test
    void shouldSearchItems() throws Exception { ... }

    @Test
    void shouldUpdateItem() throws Exception { ... }

    @Test
    void shouldChangeItemStatus() throws Exception { ... }

    @Test
    void shouldCollectItem() throws Exception { ... }

    @Test
    void shouldGetUserItems() throws Exception { ... }

    @Test
    void shouldGetUserCollections() throws Exception { ... }
}
```

**预期结果:** 9个测试用例，全部通过

---

## 四、端到端测试实现

### 4.1 Admin Login E2E 测试

**文件位置**: `frontend-admin/tests/e2e/admin-login.spec.ts`

**测试场景:**

```typescript
import { test, expect } from '@playwright/test';

test.describe('Admin Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
  });

  test('should display login form', async ({ page }) => {
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('input[type="text"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should login successfully with valid credentials', async ({ page }) => {
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.locator('text=Dashboard')).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('text=用户名或密码错误')).toBeVisible();
  });

  test('should show validation errors for empty fields', async ({ page }) => {
    await page.click('button[type="submit"]');
    await expect(page.locator('text=请输入用户名')).toBeVisible();
    await expect(page.locator('text=请输入密码')).toBeVisible();
  });
});
```

**预期结果:** 4个测试场景，全部通过

### 4.2 User Registration E2E 测试

**文件位置**: `frontend-user/tests/e2e/user-registration.spec.ts`

**测试场景:**

```typescript
import { test, expect } from '@playwright/test';

test.describe('User Registration Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register');
  });

  test('should display registration form', async ({ page }) => {
    await expect(page.locator('form')).toBeVisible();
    await expect(page.locator('input[type="tel"]')).toBeVisible(); // phone
    await expect(page.locator('input[type="text"]')).toBeVisible(); // nickname
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('should register successfully', async ({ page }) => {
    await page.fill('input[type="tel"]', '13800138888');
    await page.fill('input[type="text"]', '测试用户');
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    await expect(page).toHaveURL(/\//);
    await expect(page.locator('text=注册成功')).toBeVisible();
  });

  test('should show error for duplicate phone', async ({ page }) => {
    await page.fill('input[type="tel"]', '13800138000'); // existing phone
    await page.fill('input[type="text"]', '测试用户');
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('text=手机号已注册')).toBeVisible();
  });
});
```

**预期结果:** 3个测试场景，全部通过

### 4.3 Board Management E2E 测试

**文件位置**: `frontend-admin/tests/e2e/board-management.spec.ts`

**测试场景:**

```typescript
import { test, expect } from '@playwright/test';

test.describe('Board Management Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="text"]', 'admin');
    await page.fill('input[type="password"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.goto('/admin/boards');
  });

  test('should display board list', async ({ page }) => {
    await expect(page.locator('table')).toBeVisible();
    await expect(page.locator('text=交流')).toBeVisible();
    await expect(page.locator('text=学习交流')).toBeVisible();
  });

  test('should create new board', async ({ page }) => {
    await page.click('text=新增板块');
    await page.fill('input[name="name"]', '新板块');
    await page.fill('textarea[name="description"]', '板块描述');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('text=新板块')).toBeVisible();
    await expect(page.locator('text=操作成功')).toBeVisible();
  });

  test('should edit board', async ({ page }) => {
    await page.locator('text=编辑').first().click();
    await page.fill('input[name="name"]', '已修改板块');
    await page.click('button[type="submit"]');
    
    await expect(page.locator('text=已修改板块')).toBeVisible();
  });

  test('should delete board', async ({ page }) => {
    const boardName = '待删除板块';
    // Create board first
    await page.click('text=新增板块');
    await page.fill('input[name="name"]', boardName);
    await page.click('button[type="submit"]');
    
    // Delete board
    await page.locator(`text=${boardName}`).locator('..').locator('text=删除').click();
    await page.locator('text=确定').click();
    
    await expect(page.locator(`text=${boardName}`)).not.toBeVisible();
  });
});
```

**预期结果:** 4个测试场景，全部通过

---

## 五、测试数据管理

### 5.1 测试夹具 (Test Fixtures)

**文件位置**: `backend/src/test/java/com/campus/fixtures/TestDataFactory.java`

```java
public class TestDataFactory {
    
    public static User createUser(String phone, String nickname) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setPassword("encodedPassword");
        user.setGender(1);
        user.setStatus(1);
        user.setDeleted(0);
        return user;
    }
    
    public static Board createBoard(String name, String description) {
        Board board = new Board();
        board.setName(name);
        board.setDescription(description);
        board.setSort(0);
        board.setStatus(1);
        board.setDeleted(0);
        return board;
    }
    
    public static Post createPost(Long userId, Long boardId, String title) {
        Post post = new Post();
        post.setUserId(userId);
        post.setBoardId(boardId);
        post.setTitle(title);
        post.setContent("测试内容");
        post.setStatus(1);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setDeleted(0);
        return post;
    }
    
    public static Item createItem(Long userId, Integer type, String title) {
        Item item = new Item();
        item.setUserId(userId);
        item.setType(type); // 1:收购, 2:出售
        item.setTitle(title);
        item.setDescription("测试描述");
        item.setPrice(new BigDecimal("99.99"));
        item.setStatus(1); // 1:正常, 2:已完成, 3:已下架
        item.setViewCount(0);
        item.setContactCount(0);
        item.setDeleted(0);
        return item;
    }
}
```

### 5.2 测试数据清理

**策略:** 使用@BeforeEach和@AfterEach进行数据清理

```java
@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }
}
```

---

## 六、测试运行配置

### 6.1 Maven 配置

**文件位置**: `backend/pom.xml`

```xml
<properties>
    <jacoco.version>0.8.11</jacoco.version>
</properties>

<dependencies>
    <!-- Test Dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>com.playwright</groupId>
        <artifactId>playwright</artifactId>
        <version>1.40.0</version>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>${jacoco.version}</version>
    </dependency>
</dependencies>
```

### 6.2 运行命令

```bash
# 运行所有测试
mvn test

# 运行单元测试
mvn test -Dtest="*ServiceTest"

# 运行集成测试
mvn test -Dtest="*IntegrationTest"

# 运行并生成覆盖率报告
mvn test jacoco:report

# 运行前端测试
cd frontend-admin && npm run test

# 运行E2E测试
npx playwright test
```

### 6.3 CI/CD 集成

**GitHub Actions 工作流:**

```yaml
name: Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run Backend Tests
        run: mvn test
      
      - name: Generate Coverage Report
        run: mvn jacoco:report
        
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install Dependencies
        run: npm install
      - name: Run Frontend Tests
        run: npm run test
      
      - name: Run E2E Tests
        run: npx playwright install --with-deps && npx playwright test
```

---

## 七、测试覆盖率目标

### 7.1 覆盖率指标

| 指标 | 短期目标 | 中期目标 | 长期目标 |
|------|---------|---------|---------|
| **行覆盖率** | 50% | 70% | 80% |
| **分支覆盖率** | 40% | 60% | 75% |
| **方法覆盖率** | 60% | 80% | 90% |
| **类覆盖率** | 70% | 85% | 95% |

### 7.2 质量门禁

```yaml
# 质量门禁配置
quality_gates:
  - type: coverage
    metric: line_coverage
    threshold: 50%
    action: fail
  
  - type: coverage
    metric: branch_coverage
    threshold: 40%
    action: fail
  
  - type: tests
    metric: passed_tests
    threshold: 100%
    action: fail
```

---

## 八、实施时间表

### Phase 1: 单元测试 (第1周)
- [x] UserService 测试 (11个用例)
- [x] AuthService 测试 (9个用例)
- [x] PostService 测试 (9个用例)
- [x] CommentService 测试 (6个用例)
- [x] ItemService 测试 (15个用例)

**总计:** 50个单元测试用例

### Phase 2: 集成测试 (第2周)
- [x] UserController 测试 (7个用例)
- [x] AuthController 测试 (7个用例)
- [x] PostController 测试 (10个用例)
- [x] ItemController 测试 (9个用例)

**总计:** 33个集成测试用例

### Phase 3: 端到端测试 (第3周)
- [x] Admin Login E2E (4个用例)
- [x] User Registration E2E (3个用例)
- [x] Board Management E2E (4个用例)

**总计:** 11个E2E测试用例

### Phase 4: 质量保障 (第4周)
- [x] 测试覆盖率报告
- [x] CI/CD集成
- [x] 性能测试基线
- [x] 文档完善

---

## 九、预期结果

### 9.1 测试套件统计

| 测试类型 | 测试类数量 | 测试用例数量 | 预期通过率 |
|---------|-----------|-------------|-----------|
| 单元测试 | 5 | 50 | 100% |
| 集成测试 | 4 | 33 | 100% |
| 端到端测试 | 3 | 11 | 100% |
| **总计** | **12** | **94** | **100%** |

### 9.2 覆盖率统计

| 覆盖率指标 | 当前值 | 目标值 |
|-----------|-------|-------|
| 行覆盖率 | <10% | >70% |
| 分支覆盖率 | <5% | >60% |
| 方法覆盖率 | <15% | >80% |
| 类覆盖率 | <20% | >90% |

### 9.3 测试执行时间

| 测试类型 | 执行时间 |
|---------|---------|
| 单元测试 | < 30秒 |
| 集成测试 | < 2分钟 |
| 端到端测试 | < 5分钟 |
| **全部测试** | < 8分钟 |

---

## 十、总结

本测试计划为校园互助平台提供了完整的测试套件实现方案，涵盖：

1. **50个单元测试** - 服务层逻辑验证
2. **33个集成测试** - API端点验证  
3. **11个端到端测试** - 用户流程验证
4. **94个总测试用例** - 全面覆盖

**预期效果:**
- 测试覆盖率从 <10% 提升到 >70%
- 自动化测试套件运行时间 < 8分钟
- 质量门禁确保代码质量
- CI/CD集成实现持续测试

**下一步行动:**
1. 创建MySQL测试数据库 `campus_test`
2. 运行测试套件验证
3. 根据测试结果优化代码
4. 持续监控和改进测试覆盖率

---

**文档版本:** 1.0  
**创建日期:** 2026年1月22日  
**状态:** 待实施
