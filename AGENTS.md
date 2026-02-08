# AGENTS.md - 智能体编码规范

## 项目概述

**全栈校园平台 (Vue 3 + Spring Boot 3.2)**

```
campus/
├── backend/           # Spring Boot 3.2 + Java 17 + MyBatis-Plus (端口 8080)
├── frontend-user/     # Vue 3 + JavaScript + Tailwind CSS v4 (端口 3000)
└── frontend-admin/    # Vue 3 + JavaScript + Element Plus + SCSS (端口 3001)
```

### 技术栈版本

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端 | Spring Boot | 3.2.0 | 自动配置、约定优于配置 |
| 后端 | Java | 17 | 开发语言 |
| 后端 | MyBatis-Plus | 3.5.5 | ORM 框架 |
| 前端-user | Vue 3 | 3.4+ | 组合式 API |
| 前端-user | Tailwind CSS | 4.x | 原子化 CSS |
| 前端-user | Vant UI | 4.x | 移动端组件库 |
| 前端-admin | Element Plus | 2.x | PC 端组件库 |
| 数据库 | MySQL | 8.0 | 关系型数据库 |
| 缓存 | Redis | 7.x | 缓存/会话存储 |

---

## 文档导航

项目文档位于 `docs/` 目录下，提供了更详细的技术说明和使用指南：

| 文档 | 说明 |
|------|------|
| [ENV_SETUP.md](docs/ENV_SETUP.md) | 环境配置指南，包含 VM 远程数据库配置 |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | 系统架构设计文档 |
| [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | 完整目录结构说明 |
| [CODING_STANDARDS.md](docs/CODING_STANDARDS.md) | 详细代码规范 |
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Docker 生产部署指南 |
| [ENVIRONMENT_VARIABLES.md](docs/ENVIRONMENT_VARIABLES.md) | 环境变量完整说明 |
| [TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | 常见问题排查指南 |

**开发前建议阅读顺序**：
1. @docs/ENV_SETUP.md - 环境搭建
2. @docs/ARCHITECTURE.md - 架构理解
3. @docs/CODING_STANDARDS.md - 代码规范

---

## 环境配置（开发环境）

本项目使用虚拟机 `192.168.100.100` 上的 Docker 容器运行数据库服务：

### 数据库连接信息

| 服务 | 地址 | 端口 | 用户名 | 密码 | 数据库名 |
|------|------|------|--------|------|----------|
| MySQL | 192.168.100.100 | 3306 | root | 123 | campus_fenbushi |
| Redis | 192.168.100.100 | 6379 | - | 123 | - |

### 数据库初始化

```bash
# 初始化数据库脚本位置
backend/sql/init.sql

# 执行初始化
mysql -h 192.168.100.100 -uroot -p123 campus_fenbushi < backend/sql/init.sql
```

**注意**：`init.sql` 包含 11 张核心数据表结构：
- `admin` - 管理员表
- `user` - 用户表
- `board` - 论坛板块表
- `post` - 帖子表
- `comment` - 评论表
- `collect` - 帖子收藏表
- `like` - 帖子点赞表
- `item` - 闲置物品表
- `item_collect` - 物品收藏表
- `message` - 消息表
- `conversation` - 会话表
- `notification` - 通知表
---

## 构建命令

### 后端
```bash
cd backend
mvn clean compile              # 编译
mvn spring-boot:run            # 启动 (端口 8080)
mvn package -DskipTests        # 打包
mvn test                       # 所有测试
mvn test -Dtest=PostServiceTest                    # 单个测试类
mvn test -Dtest=PostServiceTest#shouldIncrementViewCount # 单个测试方法
mvn jacoco:report              # 生成代码覆盖率报告 (target/site/jacoco)
```

**代码覆盖率要求**：
- **行覆盖率 (Line Coverage)**: ≥ 70%
- **分支覆盖率 (Branch Coverage)**: ≥ 60%

报告位置：`backend/target/site/jacoco/index.html`

### 前端 (user 和 admin)
```bash
cd frontend-user  # 或 frontend-admin
npm run dev       # 开发服务器
npm run build     # 生产构建
npm run test      # Vitest 所有测试
npm run test src/__tests__/example.spec.js  # 单个测试文件
npm run lint      # ESLint 并自动修复
```

---

## 设计约束

**禁止**：图标(SVG/图片/字体)、Emoji、紫色/橙色(#6366F1)  
**使用 #1E3A8A，所有**：靛蓝色颜色间距从 `src/styles/design-tokens.css` 获取

---

## 前端规范 (Vue 3 + JavaScript)

### 导入顺序
```javascript
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { API_ENDPOINTS } from '@/constants'
import { login } from '@/api/auth'
```

### 命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase.vue | `UserProfile.vue` |
| 变量 | camelCase | `userInfo`, `isLoading` |
| 函数 | handle动词名词() | `handleSubmit()` |
| 常量 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| CSS类 | kebab-case | `.user-profile-card` |

### 错误处理 (强制)
```javascript
try {
  const res = await fetchUserData(userId)
} catch (error) {
  console.error('获取用户失败:', error)
  showToast('加载失败，请重试')
}
```
**禁止**留空 catch 块。

---

## 后端规范 (Spring Boot + Java 17)

### 项目结构
```
src/main/java/com/campus/
├── config/           # 配置类
├── common/           # Result<T>, ResultCode, 异常处理器
├── modules/          # 业务模块
│   ├── auth/         # 认证
│   ├── forum/        # 论坛
│   ├── trade/        # 交易
│   ├── chat/         # 聊天
│   ├── user/         # 用户
│   └── admin/        # 管理
└── CampusApplication.java
```

### 命名规范
| 类型 | 规范 | 示例 |
|------|------|------|
| 类 | PascalCase | `PostService`, `UserController` |
| 方法 | camelCase + 动词 | `createPost()`, `getUserById()` |
| 变量 | camelCase | `postId`, `pageNum` |
| 常量 | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| 包 | lowercase | `com.campus.modules.auth` |

### 分层模式
`Controller → Service → Mapper → Entity`

**注解**：使用构造函数注入，`@Transactional(rollbackFor = Exception.class)` 用于写操作

### API 文档
```java
@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
  @Operation(summary = "获取用户详情")
  @GetMapping("/{id}")
  public Result<UserVO> getUser(@PathVariable Long id) { /* ... */ }
}
```

### 响应包装
```java
Result.success(data)      // 200 OK 带数据
Result.success()          // 200 OK 无数据
Result.error("message")   // 错误信息
Result.error(ResultCode.NOT_FOUND) // 带错误码
```

### 常用 ResultCode
```java
SUCCESS(200, "操作成功"),
ERROR(500, "操作失败"),
UNAUTHORIZED(401, "未授权"),
FORBIDDEN(403, "禁止访问"),
NOT_FOUND(404, "资源不存在"),
PARAM_ERROR(400, "参数错误"),
```

---

## 强制规则

1. **类型安全**：禁止 `as any`, `@ts-ignore`, `@ts-expect-error`
2. **错误处理**：禁止空 catch 块
3. **测试**：禁止删除失败的测试来"通过"
4. **Git**：禁止主动提交，除非被要求
5. **设计**：无图标、无 Emoji、无紫色/橙色

---

## 常用工具

### 后端
| 类 | 用途 |
|----|------|
| `Result<T>` | 统一 API 响应包装 |
| `ResultCode` | 响应状态码枚举 |
| `BaseEntity` | 公共字段 (id, createdAt, updatedAt) |
| `LambdaQueryWrapper<T>` | 类型安全查询 |
| `GlobalExceptionHandler` | 全局异常处理 |

### 前端
| 钩子/仓库 | 用途 |
|----------|------|
| `request` | Axios 实例 |
| `useUserStore()` | 认证状态管理 (Pinia) |
| `showToast()` | 用户反馈 |

---

## 开发工作流建议

1. **理解需求**
   - 阅读需求文档和设计文档
   - 查看相关模块的 API 文档
   - 与产品/设计确认疑问

2. **查看相关模块代码**
   - 理解现有代码结构和模式
   - 遵循项目编码规范
   - 参考相似功能的实现

3. **编写/更新测试**
   - 遵循测试覆盖率要求
   - 覆盖正常流程和边界情况

4. **实现功能**
   - 遵循分层架构模式
   - 保持代码简洁和可读性

5. **运行验证**
   - 执行相关测试用例
   - 检查代码规范
   - 验证功能完整性

---

## 参考链接

| 链接 | 说明 |
|------|------|
| 后端 API 文档 | http://localhost:8080/swagger-ui.html |
| 用户应用 | http://localhost:3000 |
| 管理应用 | http://localhost:3001 |
| 环境配置 | @docs/ENV_SETUP.md |
| 架构设计 | @docs/ARCHITECTURE.md |
| 代码规范 | @docs/CODING_STANDARDS.md |
| 部署指南 | @docs/DEPLOYMENT.md |
| 故障排查 | @docs/TROUBLESHOOTING.md |
