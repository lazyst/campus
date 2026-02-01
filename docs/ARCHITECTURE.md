# 校园互助平台架构设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | 校园互助平台（Campus） |
| 文档版本 | 1.0.0 |
| 创建日期 | 2026年2月1日 |
| 文档状态 | 正式发布 |

## 1. 架构总览

### 1.1 系统整体架构

本节将从宏观角度介绍校园互助平台的整体系统架构，包括系统的分层结构、模块划分以及各组件之间的交互关系。系统采用现代化的前后端分离架构设计，前端基于 Vue 3 构建，后端基于 Spring Boot 3.2 构建，通过 RESTful API 进行数据交互。整个系统遵循高内聚、低耦合的设计原则，确保各模块可以独立开发、测试和部署，同时保证系统的可扩展性和可维护性。

系统的整体架构采用经典的分层架构模式，从上到下依次为表现层、业务逻辑层、数据访问层和数据存储层。这种分层架构的优势在于每一层都有明确的职责边界，层与层之间通过定义良好的接口进行通信，从而实现了关注点分离的设计目标。表现层负责用户界面的呈现和用户交互的处理，业务逻辑层负责实现系统的核心业务规则，数据访问层负责与数据库进行交互，数据存储层则负责数据的持久化存储。

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              客户端层（Client Layer）                         │
├─────────────────────────────┬─────────────────────────────┬─────────────────┤
│       用户端应用（3000端口）      │       管理端应用（3001端口）      │    第三方应用    │
│   Vue 3 + Tailwind CSS      │   Vue 3 + Element Plus      │      API        │
└──────────────┬──────────────┴──────────────┬──────────────┴────────┬────────┘
               │                              │                       │
               └──────────────────────────────┼───────────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              表现层（Presentation Layer）                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                          Nginx 反向代理与静态资源服务                          │
│                          （负载均衡、SSL终结、缓存控制）                        │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              业务层（Business Layer）                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                              Spring Boot 3.2 服务                            │
│                         （8080端口，Java 17运行环境）                          │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────────────────┤
│   用户模块   │   论坛模块   │   交易模块   │   聊天模块   │      管理模块        │
│   User      │   Forum     │   Trade     │   Chat      │      Admin         │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              数据访问层（Data Access Layer）                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                           MyBatis-Plus ORM 框架                              │
│                         （数据持久化、缓存管理、事务控制）                       │
└────────────────────────────────┬────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              数据存储层（Data Storage Layer）                 │
├───────────────────────────────┬─────────────────────────────────────────────┤
│         MySQL 8.0 数据库       │            Redis 缓存（可选）                │
│      （主数据持久化存储）        │           （会话缓存、热点数据）              │
└───────────────────────────────┴─────────────────────────────────────────────┘
```

### 1.2 核心设计原则

在设计校园互助平台的架构时，我们遵循了一系列经过实践验证的软件设计原则，这些原则指导着系统的每一个设计决策，确保系统能够满足当前的功能需求的同时，也为未来的扩展和演进预留了足够的空间。首先，系统遵循约定优于配置的原则，通过制定统一的开发规范和约定，减少不必要的配置工作，提高开发效率。其次，系统遵循面向接口编程的原则，各层之间通过接口进行通信，而不是直接依赖于具体的实现类，这样当需要替换实现时，只需要提供新的实现类而不需要修改调用方的代码。

系统的另一个重要设计原则是单一职责原则，每个模块、每个类、每个方法都只承担一个明确的职责，这样可以提高代码的可读性和可维护性，同时也便于进行单元测试。在处理复杂的业务逻辑时，我们采用领域驱动设计的方法，将业务领域建模作为设计的核心，通过建立清晰的领域模型来反映业务规则和业务流程。此外，系统还遵循不可变对象的设计模式，在设计实体类时尽可能使用不可变对象，这样可以避免并发访问时的数据竞争问题，简化多线程环境下的编程模型。

### 1.3 系统边界与集成

校园互助平台作为一个完整的全栈应用系统，其系统边界涵盖前端用户界面、后端业务服务、数据存储以及运维部署等各个层面。在系统边界之外，平台通过标准化的 RESTful API 接口与外部系统进行集成，支持第三方应用通过开放接口访问平台的部分功能和数据。同时，系统预留了与校园统一身份认证系统的集成接口，可以实现与学校现有信息系统的单点登录集成，避免学生重复注册账号的繁琐流程。

系统还支持与主流社交平台的集成，用户可以选择使用微信、QQ 等社交账号进行快捷登录，这种设计不仅提升了用户体验，也增加了用户注册和登录的便捷性。在消息推送方面，系统支持与第三方推送服务的集成，确保用户能够及时收到重要消息的推送通知。所有这些外部集成都通过适配器模式进行封装，确保核心业务逻辑与外部依赖解耦，当需要更换外部服务提供商时，只需要修改适配器的实现而不需要影响业务逻辑代码。

## 2. 技术选型详解

### 2.1 前端技术栈

前端技术栈的选择充分考虑了开发效率、运行时性能、社区生态以及团队技术储备等多个维度。用户端应用和管理端应用都采用 Vue 3 作为核心框架，充分利用 Vue 3 提供的 Composition API、更好的 TypeScript 支持以及更快的渲染性能。Vue 3 的响应式系统基于 Proxy 实现，相比 Vue 2 的 Object.defineProperty 方案，具有更好的性能表现和更完整的响应式支持，可以监听数组的变化以及新属性的添加，这大大简化了状态管理的实现复杂度。

用户端应用选择 Tailwind CSS v4 作为样式框架，这是一个 Utility-First 思路的 CSS 框架，通过组合原子化的工具类来构建用户界面。Tailwind CSS 的优势在于高度的可定制性和极小的打包体积，通过 PurgeCSS 技术可以在构建时移除未使用的样式，确保最终产出的 CSS 文件只包含实际使用的样式规则。Tailwind CSS v4 版本引入了更快的增量编译、改进的 JIT 引擎以及对 CSS 变量的原生支持，进一步提升了开发体验和运行性能。

管理端应用选择 Element Plus 作为组件库，这是 Element UI 的 Vue 3 版本，提供了丰富的高质量组件，特别适合构建企业内部管理系统。Element Plus 组件库经过多年的发展和社区验证，具有完善的文档、丰富的示例和活跃的社区支持，可以显著加速管理端应用的开发进度。选择 Element Plus 而非继续使用 Tailwind CSS 的主要考虑是管理端应用需要大量复杂的表单、表格、弹窗等交互组件，使用成熟的组件库可以避免重复造轮子，同时保证用户体验的一致性和组件的稳定性。

```
前端技术栈架构图：

┌─────────────────────────────────────────────────────────────┐
│                      Vue 3 核心框架                          │
│  Composition API  │  Reactivity System  │  Vue Router       │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────┐     ┌─────────────────────────┐
│     用户端应用           │     │      管理端应用          │
│   Vite 构建工具          │     │   Vite 构建工具          │
│   Pinia 状态管理         │     │   Pinia 状态管理         │
│   Tailwind CSS v4       │     │   Element Plus          │
│   Axios 网络请求         │     │   Axios 网络请求         │
│   VueUse 工具库          │     │   VueUse 工具库          │
└─────────────────────────┘     └─────────────────────────┘
```

### 2.2 后端技术栈

后端技术栈基于 Spring Boot 3.2 构建，选择这个版本的主要原因是其对 Java 17 的原生支持以及对 GraalVM 原生编译的优化。Spring Boot 3.2 引入了新的 AOT（Ahead-of-Time）编译支持，可以将 Spring 应用编译为原生可执行文件，显著减少应用启动时间和内存占用。Java 17 作为 LTS（长期支持）版本，提供了密封类、模式匹配、记录类等现代语言特性，这些特性可以帮助我们编写更简洁、更安全的代码。

数据访问层采用 MyBatis-Plus 作为 ORM 框架，这是 MyBatis 的增强版本，在 MyBatis 的基础上提供了更便捷的 CRUD 操作、自动填充、逻辑删除、分页插件等功能。MyBatis-Plus 的优势在于它保留了 MyBatis 的灵活性和可控性，同时通过自动生成的基础 CRUD 方法和条件构造器大幅减少了样板代码的编写。相比于 JPA 或 Hibernate 等全功能 ORM 框架，MyBatis-Plus 更适合需要精细控制 SQL 查询的场景，这对于性能敏感的系统来说尤为重要。

数据库选择 MySQL 8.0 作为主数据存储，MySQL 8.0 引入了窗口函数、公用表表达式、JSON 表函数等新特性，同时在查询优化器、字符集支持、索引类型等方面都有显著改进。MySQL 8.0 的默认字符集改为 utf8mb4，可以存储完整的 Unicode 字符，包括 Emoji 表情符号，这对于校园社交场景来说是非常必要的。系统还集成了 Redis 作为可选的缓存层，用于缓存热点数据、会话信息以及实现分布式锁等场景。

```
后端技术栈架构图：

┌─────────────────────────────────────────────────────────────────┐
│                     Spring Boot 3.2 运行环境                      │
│              Java 17 + Spring Framework 6.x                      │
├─────────────────────────────────────────────────────────────────┤
│                     Web 服务层                                   │
│         Spring MVC  │  Spring WebFlux  │  WebSocket             │
├─────────────────────────────────────────────────────────────────┤
│                     安全控制层                                   │
│         Spring Security  │  JWT 认证  │  OAuth 2.0              │
├─────────────────────────────────────────────────────────────────┤
│                     数据访问层                                   │
│            MyBatis-Plus  │  PageHelper  │  事务管理               │
├─────────────────────────────────────────────────────────────────┤
│                     基础设施层                                   │
│       Redis 缓存  │  文件存储  │  消息队列（可选）  │  定时任务            │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 开发与运维工具

在开发工具链方面，项目采用 Maven 作为构建工具，Maven 的约定优于配置特性以及丰富的插件生态可以有效管理项目的依赖和构建流程。项目结构遵循 Maven 的标准目录布局，将源代码、测试代码、资源文件等组织在明确的目录结构中，便于团队成员快速理解和维护代码。代码质量方面，项目集成了 SonarQube 进行静态代码分析，Checkstyle 进行代码风格检查，SpotBugs 进行潜在缺陷检测，这些工具共同保障了代码的质量和一致性。

测试框架方面，后端单元测试采用 JUnit 5 作为测试框架，结合 Mockito 进行模拟测试，AssertJ 进行断言验证。集成测试使用 Spring Boot Test 提供的测试支持，可以加载完整的 Spring 上下文进行端到端测试。前端测试采用 Vitest 作为测试框架，这是专门为 Vite 项目设计的测试工具，具有与 Vite 相同的极速启动体验。前端组件测试采用 Testing Library 库，它鼓励以用户行为为中心编写测试，确保测试覆盖真实的使用场景。

运维部署方面，系统采用 Docker 容器化部署，通过 Docker Compose 管理多容器部署，包括后端服务、数据库、缓存等组件。容器化部署的优势在于环境一致性、快速部署和弹性伸缩。项目还集成了 GitHub Actions 进行持续集成和持续部署，每次代码提交都会触发自动化构建、测试和部署流程，确保代码变更能够快速、安全地交付到生产环境。

## 3. 前端架构设计

### 3.1 项目结构设计

前端项目采用 Monorepo 的组织方式，用户端应用（frontend-user）和管理端应用（frontend-admin）共享同一个代码仓库，但作为独立的 Vite 项目分别构建。这种组织方式的优势在于可以共享公共的工具函数、类型定义和配置，同时保持两个应用在技术栈和开发规范上的一致性。每个应用都遵循 Vue 3 项目的标准目录结构，按照功能模块组织代码，确保代码的可维护性和可扩展性。

```
用户端项目目录结构：

frontend-user/
├── src/
│   ├── api/                    # API 接口定义
│   │   ├── auth/              # 认证相关接口
│   │   ├── user/              # 用户相关接口
│   │   ├── forum/             # 论坛相关接口
│   │   ├── trade/             # 交易相关接口
│   │   └── chat/              # 聊天相关接口
│   ├── assets/                # 静态资源
│   │   ├── images/            # 图片资源
│   │   └── styles/            # 全局样式
│   ├── components/            # 公共组件
│   │   ├── common/            # 通用组件
│   │   └── business/          # 业务组件
│   ├── composables/           # 组合式函数
│   ├── constants/             # 常量定义
│   ├── layouts/               # 布局组件
│   ├── router/                # 路由配置
│   ├── stores/                # Pinia 状态管理
│   ├── utils/                 # 工具函数
│   └── views/                 # 页面组件
├── __tests__/                 # 测试文件
├── index.html
├── vite.config.js
└── package.json
```

路由配置采用模块化的组织方式，每个功能模块的路由定义在独立的文件中，然后通过路由汇总文件统一注册。这种方式可以有效避免单个路由配置文件过大导致的维护困难，同时也便于团队成员并行开发不同模块的路由功能。路由懒加载技术被广泛用于页面组件的加载，只有当用户访问特定路由时才会加载对应的页面组件，这样可以显著减少首屏加载时间，提升用户体验。

状态管理采用 Pinia 库，这是 Vue 官方推荐的新一代状态管理库，相比 Vuex 具有更简洁的 API、更好的 TypeScript 支持以及更小的包体积。Pinia 的设计理念是让状态管理变得简单而直观，通过定义 store 的方式来组织状态和业务逻辑，每个 store 都可以包含状态、计算属性和操作方法。项目中的用户信息、应用配置、购物车等数据都通过 Pinia 进行管理，确保跨组件的数据共享和响应式同步。

### 3.2 组件设计模式

前端组件采用组合式 API 进行开发，这是 Vue 3 推荐的组件编写方式，相比选项式 API 具有更好的逻辑复用能力和类型推断能力。组合式 API 通过 setup 函数组织组件的逻辑，可以将相关的功能代码组织在一起，而不是分散在 data、methods、computed、watch 等不同的选项中。这种方式特别适合构建复杂组件，可以将功能逻辑提取为可复用的组合式函数，并在多个组件之间共享。

组件的设计遵循高内聚、低耦合的原则，每个组件都有明确的职责边界，只负责自己应该负责的事情。公共的 UI 逻辑通过抽象为公共组件来实现，例如通用的按钮、输入框、弹窗等组件都被抽取为公共组件，在多个页面中复用。业务逻辑则通过组合式函数来封装，例如与用户认证相关的逻辑被封装在 useAuth 组合式函数中，与论坛功能相关的逻辑被封装在 useForum 组合式函数中。这种设计使得组件代码更加简洁，逻辑更加清晰，也便于进行单元测试。

组件之间的通信采用多种方式：父子组件之间通过 props 和 emits 进行通信，跨层级的组件通过 provide/inject 进行深层传递，跨组件的状态共享通过 Pinia store 实现。对于复杂的跨组件交互，项目采用事件总线模式，但更推荐使用 Pinia 的 store 来管理全局状态，因为事件总线在组件销毁时容易导致内存泄漏，而 Pinia store 会自动清理订阅和响应式状态。

```javascript
// 组合式函数示例：用户认证逻辑
// frontend-user/src/composables/useAuth.js

import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, logout, getUserProfile } from '@/api/auth'

export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()
  const isLoading = ref(false)
  const error = ref(null)

  const isLoggedIn = computed(() => !!userStore.token)
  const currentUser = computed(() => userStore.userInfo)

  async function handleLogin(credentials) {
    try {
      isLoading.value = true
      error.value = null

      const response = await login(credentials)
      const { token, user } = response.data

      userStore.setToken(token)
      userStore.setUserInfo(user)

      router.push('/')
    } catch (e) {
      error.value = e.message || '登录失败，请检查用户名和密码'
      throw e
    } finally {
      isLoading.value = false
    }
  }

  async function handleLogout() {
    try {
      await logout()
    } finally {
      userStore.clearAuth()
      router.push('/login')
    }
  }

  async function refreshUserProfile() {
    const response = await getUserProfile()
    userStore.setUserInfo(response.data)
  }

  return {
    isLoading,
    error,
    isLoggedIn,
    currentUser,
    handleLogin,
    handleLogout,
    refreshUserProfile
  }
}
```

### 3.3 网络请求封装

网络请求采用 Axios 库进行封装，项目创建了一个统一的请求实例，配置了请求拦截器和响应拦截器。请求拦截器负责在请求发送前进行统一的处理，包括添加认证 Token、处理请求参数、记录请求日志等。响应拦截器负责在收到响应后进行统一的处理，包括解析响应数据、处理错误响应、实现错误重试逻辑等。这种集中式的请求处理方式可以确保所有 API 请求都遵循统一的处理规则，便于维护和调试。

请求实例的配置充分考虑了实际业务需求，包括请求超时时间、基础 URL、请求头配置等。Token 自动刷新机制被实现为响应拦截器的一部分，当检测到 401 未授权错误时，会自动尝试使用刷新 Token 获取新的访问 Token，然后重试原来的请求。这种设计可以提升用户体验，避免用户在操作过程中突然被登出需要重新登录的尴尬情况。对于并发请求，项目实现了请求取消机制，当用户离开页面或切换路由时，会自动取消未完成的请求，避免无效的响应处理。

错误处理是网络请求封装的重要组成部分，每个 API 请求都需要进行完善的错误处理，包括网络错误、服务端错误、业务错误等。错误信息会被统一格式化为包含错误码、错误消息和详细信息的结构，便于上层组件进行统一的错误展示。项目封装了 showToast 函数用于展示错误提示，结合 Pinia 状态管理中的错误状态，可以实现全局错误提示和局部错误提示的灵活切换。

```javascript
// Axios 请求封装示例
// frontend-user/src/utils/request.js

import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { showToast } from '@/components/common/Toast'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, data, message } = response.data

    if (code === 200) {
      return { data, message }
    }

    showToast(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  async (error) => {
    const { response } = error

    if (response) {
      switch (response.status) {
        case 401:
          await handleUnauthorized(error)
          break
        case 403:
          showToast('没有权限访问此资源')
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误，请稍后重试')
          break
        default:
          showToast(response.data?.message || '请求失败')
      }
    } else if (!window.navigator.onLine) {
      showToast('网络连接失败，请检查网络设置')
    } else {
      showToast('请求超时，请稍后重试')
    }

    return Promise.reject(error)
  }
)

export default request
```

## 4. 后端架构设计

### 4.1 项目结构设计

后端项目采用标准的 Maven 多模块结构，将不同功能领域的代码组织在独立的模块中。主模块负责项目的启动和配置，其他功能模块负责实现具体的业务逻辑。这种模块化的结构可以有效地组织大型项目的代码，同时支持模块级别的依赖管理和独立部署。项目遵循 Spring Boot 的最佳实践，将配置与代码分离，敏感信息通过环境变量或配置文件管理，实现十二要素应用的部署原则。

```
后端项目目录结构：

backend/
├── src/main/java/com/campus/
│   ├── CampusApplication.java       # 应用启动类
│   ├── config/                       # 配置类
│   │   ├── SecurityConfig.java      # 安全配置
│   │   ├── CorsConfig.java          # 跨域配置
│   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 配置
│   │   └── SwaggerConfig.java       # API 文档配置
│   ├── common/                       # 公共模块
│   │   ├── Result.java              # 统一响应包装
│   │   ├── ResultCode.java          # 响应状态码枚举
│   │   ├── BaseEntity.java          # 实体基类
│   │   ├── PageQuery.java           # 分页查询基类
│   │   └── GlobalExceptionHandler.java  # 全局异常处理
│   └── modules/                      # 业务模块
│       ├── auth/                     # 认证模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   └── dto/
│       ├── user/                     # 用户模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   └── dto/
│       ├── forum/                    # 论坛模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   └── dto/
│       ├── trade/                    # 交易模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   └── dto/
│       ├── chat/                     # 聊天模块
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   └── dto/
│       └── admin/                    # 管理模块
│           ├── controller/
│           ├── service/
│           ├── mapper/
│           ├── entity/
│           └── dto/
├── src/main/resources/
│   ├── application.yml              # 配置文件
│   ├── application-dev.yml          # 开发环境配置
│   ├── application-prod.yml         # 生产环境配置
│   └── mapper/                      # MyBatis XML 映射文件
└── pom.xml
```

### 4.2 分层架构设计

后端采用经典的分层架构设计，从上到下依次为 Controller 层、Service 层、Mapper 层和 Entity 层，每一层都有明确的职责边界。Controller 层负责接收 HTTP 请求、参数校验、调用 Service 层处理业务逻辑、并将结果封装为统一响应格式返回给前端。Service 层负责实现具体的业务逻辑，包括业务规则校验、事务管理、调用多个 Mapper 完成数据操作等。Mapper 层负责与数据库交互，执行 SQL 语句并将结果映射为 Java 对象。Entity 层定义数据实体，对应数据库表结构。

分层架构的优势在于各层之间的低耦合和高内聚，每一层只需要关注自己的职责，不需要了解其他层的实现细节。Controller 层不需要知道数据是如何存储的，只需要调用 Service 层的方法获取结果；Service 层不需要知道 HTTP 请求的细节，只需要处理业务逻辑；Mapper 层不需要知道业务规则，只需要执行 SQL 语句。这种设计使得代码易于测试、维护和扩展，当需要修改某一层的实现时，只要接口保持不变，就不会影响到其他层。

```
分层架构示意图：

┌─────────────────────────────────────────────────────────────┐
│                     Controller 层                           │
│  职责：请求接收、参数校验、响应封装、异常转换                  │
│  技术：@RestController、@PostMapping、@Valid                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service 层                               │
│  职责：业务逻辑实现、事务管理、业务规则校验                    │
│  技术：@Service、@Transactional、依赖注入                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Mapper 层                                │
│  职责：数据库交互、SQL执行、结果映射                          │
│  技术：@Mapper、MyBatis-Plus、LambdaQueryWrapper             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Entity 层                                │
│  职责：数据实体定义、表结构映射                               │
│  技术：@TableName、@TableField、@TableId                     │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 核心模块设计

认证模块是整个系统的安全入口，负责处理用户的注册、登录、登出以及 Token 的签发和验证。模块采用 JWT 作为认证令牌，用户的身份信息经过数字签名后生成 Token，客户端在后续请求中携带该 Token，服务端验证 Token 的有效性来确认用户身份。认证模块还集成了 Token 刷新机制，当访问 Token 过期时，可以利用刷新 Token 获取新的访问 Token，避免用户频繁登录。密码采用 BCrypt 算法进行加密存储，这是目前推荐的密码哈希算法，具有防止彩虹表攻击和暴力破解的能力。

用户模块负责用户信息的 CRUD 操作、用户资料管理、用户关系管理等功能。用户信息采用脱敏设计，在返回用户敏感信息时，只返回部分信息，完整的敏感信息需要额外授权才能获取。用户模块还实现了用户头像上传功能，支持图片压缩和格式转换，确保头像文件不会占用过多存储空间。用户的最后登录时间、登录 IP 等信息会被记录，用于安全审计和异常登录检测。

论坛模块是平台的核心功能模块之一，负责帖子和评论的发布、浏览、点赞、收藏等功能。帖子支持富文本编辑和图片上传，评论支持多级嵌套回复。模块实现了内容的分页查询和全文搜索，通过 MyBatis-Plus 的分页插件和数据库的全文索引，可以高效地处理大量内容的查询需求。为了保证社区氛围，模块实现了敏感词过滤和内容审核机制，用户发布的内容会经过关键词检测，发现敏感内容时会自动拦截或标记待审核。

```java
// Controller 层示例：用户模块
// backend/src/main/java/com/campus/modules/user/controller/UserController.java

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileDTO dto) {
        userService.updateProfile(dto);
        return Result.success();
    }

    @Operation(summary = "上传用户头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return Result.success(avatarUrl);
    }

    @Operation(summary = "获取用户发布的帖子列表")
    @GetMapping("/{id}/posts")
    public Result<PageResult<PostVO>> getUserPosts(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<PostVO> result = userService.getUserPosts(id, pageNum, pageSize);
        return Result.success(result);
    }
}
```

## 5. 安全架构设计

### 5.1 认证与授权体系

安全架构的核心是认证和授权两大机制。认证解决的是你是谁的问题，通过验证用户的身份凭证来确认用户的真实身份。授权解决的是你能做什么的问题，在确认用户身份后，根据用户的角色和权限决定用户可以访问哪些资源和执行哪些操作。系统采用 JWT 作为认证令牌，结合基于角色的访问控制模型实现细粒度的权限管理。

JWT Token 的设计考虑了安全性和实用性的平衡，Token 中包含了用户 ID、角色信息、Token 类型等必要的声明，同时设置了合理的过期时间。Token 使用 HS256 算法进行签名，签名密钥存储在服务端配置中，不会泄露给客户端。Token 采用黑名单机制处理登出操作，当用户登出时，Token 会被加入黑名单，在 Token 验证时会检查黑名单，确保已登出的 Token 无法继续使用。

权限控制采用基于注解的方式，通过 @PreAuthorize 注解在方法级别声明访问该方法所需的权限。例如，@PreAuthorize("hasRole('ADMIN')") 表示只有管理员角色才能访问该方法，@PreAuthorize("hasPermission(#postId, 'POST_EDIT')") 表示需要拥有对特定帖子的编辑权限。权限校验逻辑被封装在自定义的权限表达式根中，支持复杂的权限表达式，可以处理资源级别的权限控制。

```
认证授权流程图：

┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  客户端   │────▶│  Nginx   │────▶│  拦截器  │────▶│ Controller│
└──────────┘     └──────────┘     └──────────┘     └──────────┘
                                         │
                                         ▼
                                   ┌──────────┐
                                   │  Token   │
                                   │  验证    │
                                   └────┬─────┘
                                        │
                    ┌───────────────────┼───────────────────┐
                    ▼                   ▼                   ▼
              ┌──────────┐        ┌──────────┐        ┌──────────┐
              │ Token有效 │        │ Token过期 │        │ Token无效 │
              │ 继续处理  │        │ 尝试刷新  │        │ 返回401   │
              └──────────┘        └────┬─────┘        └──────────┘
                                       │
                    ┌──────────────────┴──────────────────┐
                    ▼                                   ▼
              ┌──────────┐                        ┌──────────┐
              │ 刷新成功  │                        │ 刷新失败  │
              │ 更新Token │                        │ 跳转登录  │
              │ 继续处理  │                        └──────────┘
              └──────────┘
```

### 5.2 数据安全设计

数据安全是系统安全架构的重要组成部分，涵盖了数据传输安全、数据存储安全和数据访问安全三个方面。数据传输安全通过 HTTPS 协议实现，Nginx 配置了 SSL 证书，所有客户端与服务端之间的通信都经过 TLS 加密，防止中间人攻击和数据窃听。系统还实施了严格的 CORS 策略，只允许指定的域名来源访问 API 接口，防止跨站请求伪造攻击。

数据存储安全主要关注敏感数据的保护，用户密码采用 BCrypt 算法单向哈希存储，即使数据库泄露也无法直接获取用户明文密码。用户的手机号、邮箱等敏感信息在数据库中采用加密存储，使用 AES-256 算法进行对称加密，加密密钥通过密钥管理服务或环境变量进行管理。数据库的访问凭证通过环境变量注入，避免硬编码在代码中。生产环境的数据库启用了审计日志，记录所有数据变更操作，便于事后追溯。

数据访问安全通过 SQL 注入防护、参数化查询、最小权限原则等手段实现。MyBatis-Plus 默认使用 #{} 进行参数占位，可以有效防止 SQL 注入攻击。数据库用户只授予必要的最小权限，只读用户只能执行 SELECT 查询，只写用户不能执行 DELETE 操作。敏感操作需要额外的权限验证和操作确认。系统还实现了数据脱敏机制，在日志记录和错误展示时不会输出完整的敏感数据。

### 5.3 应用安全设计

应用安全关注的是应用程序层面的安全防护，包括输入验证、输出编码、安全配置等方面。输入验证是应用安全的第一道防线，系统在 Controller 层使用 @Valid 注解配合 Bean Validation 注解对请求参数进行严格校验，确保进入系统的数据符合预期格式。所有外部输入的数据都会经过 XSS 过滤和特殊字符转义处理，防止跨站脚本攻击。对于富文本编辑器的内容，采用白名单机制只允许安全的 HTML 标签和属性。

输出编码是防止 XSS 攻击的另一重要手段，前端在渲染用户输入的内容时，会自动进行 HTML 实体编码，确保用户输入的可执行脚本不会被浏览器执行。项目还实现了内容安全策略响应头，限制页面可以加载的资源来源，进一步增强 XSS 攻击的防护能力。安全响应头还包括 X-Frame-Options 防止点击劫持、X-Content-Type-Options 防止 MIME 类型混淆等。

系统还实现了多项安全防护机制：请求频率限制防止暴力破解和 DDoS 攻击，同一 IP 在短时间内的大量请求会被拦截；操作日志记录所有的敏感操作，包括登录、修改密码、删除数据等，便于安全审计；异常处理机制确保错误信息不会泄露系统内部细节，详细的错误日志只记录在服务端而不返回给客户端。这些安全机制共同构成了纵深防御体系，即使某一层防护被突破，还有其他层可以提供保护。

## 6. 数据流设计

### 6.1 整体数据流向

数据流设计描述了数据在系统中的流转路径和处理方式，帮助开发人员理解数据从客户端到数据库的完整旅程，以及数据处理过程中各个节点的职责。整体数据流向遵循请求发起、请求接收、参数校验、业务处理、数据持久化、响应返回的标准化流程，每个环节都有明确的职责和边界。数据的流动采用单向流设计，避免循环依赖和数据不一致的问题。

对于读操作，数据流从数据库出发，经过缓存层、Service 层、Controller 层，最终返回给客户端。对于写操作，数据流从客户端出发，经过参数校验、业务逻辑处理、数据持久化，最终确认操作结果后返回响应。这种设计模式可以清晰地定义每种操作的数据处理路径，便于优化读操作和保证写操作的数据一致性。

```
整体数据流向图：

【读操作数据流】
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  数据库   │───▶│  缓存层   │───▶│ Mapper   │───▶│ Service  │───▶│Client   │
│  查询     │    │  命中返回 │    │  结果映射 │    │  业务聚合 │    │  展示    │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘

【写操作数据流】
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Client  │───▶│Controller│───▶│ Service  │───▶│ Mapper   │───▶│  数据库   │
│  提交数据 │    │  参数校验 │    │  业务处理 │    │  持久化  │    │  写入     │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
```

### 6.2 核心业务数据流

以用户发帖流程为例，详细说明数据在系统中的流转过程。当用户提交一篇新帖子时，前端首先对输入内容进行前端校验，然后通过 Axios 发送 POST 请求到后端 API。后端的帖子 Controller 接收请求后，首先使用 @Valid 注解触发参数校验，DTO 对象中的校验注解会检查标题是否为空、内容是否超过最大长度等规则。参数校验失败会立即返回错误响应，不会进入后续的业务处理流程。

参数校验通过后，Controller 调用 ForumService 的 createPost 方法。Service 层首先获取当前登录用户信息，然后创建帖子实体对象，设置标题、内容、作者 ID、创建时间等属性。在保存帖子之前，Service 层还会进行业务规则校验，如用户是否被禁言、用户今日发帖数量是否超过限制等。校验通过后，调用 Mapper 层将帖子保存到数据库。数据库操作使用事务包装，确保数据一致性。如果保存成功，Service 层可以执行后续操作，最后返回帖子 ID 给前端。

前端收到创建成功的响应后，会进行页面更新，将新创建的帖子添加到帖子列表中。同时，前端会显示成功提示，并可以引导用户进行下一步操作。如果创建失败，前端会显示错误提示，并可以根据错误类型进行相应处理。

```java
// 数据流处理示例：创建帖子
// backend/src/main/java/com/campus/modules/forum/service/impl/ForumServiceImpl.java

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ForumServiceImpl implements ForumService {

    private final PostMapper postMapper;
    private final UserService userService;
    private final SensitiveWordService sensitiveWordService;

    @Override
    public Long createPost(CreatePostDTO dto, Long userId) {
        // 1. 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 业务规则校验
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException("您已被禁言，无法发帖");
        }
        if (userService.getTodayPostCount(userId) >= user.getMaxDailyPosts()) {
            throw new BusinessException("今日发帖数量已达上限");
        }

        // 3. 内容安全检查
        String filteredContent = sensitiveWordService.filter(dto.getContent());
        if (sensitiveWordService.containsHighRiskWords(dto.getContent())) {
            throw new BusinessException("内容包含敏感信息，请修改后重试");
        }

        // 4. 创建帖子实体
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(filteredContent);
        post.setUserId(userId);
        post.setStatus(PostStatus.PUBLISHED);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // 5. 保存到数据库
        postMapper.insert(post);

        // 6. 更新用户统计
        userService.incrementPostCount(userId);

        // 7. 返回帖子ID
        return post.getId();
    }
}
```

### 6.3 缓存数据流

缓存是优化系统性能的重要手段，系统实现了多级缓存策略来平衡数据一致性和访问性能。对于读多写少的热点数据，采用 Cache-Aside 模式进行缓存管理。读取数据时，首先尝试从缓存获取，如果缓存命中则直接返回，如果缓存未命中则从数据库查询并写入缓存。更新数据时，先更新数据库，然后删除缓存，确保缓存与数据库的一致性。

缓存的过期策略采用主动过期和被动过期相结合的方式。主动过期通过设置缓存的 TTL 实现，缓存数据在一定时间后自动失效，需要重新从数据库加载。被动过期通过删除缓存实现，当数据发生变更时，相关的缓存条目会被立即删除，下次访问时重新加载。这种策略可以在保证数据一致性的同时，最大限度地利用缓存提升读取性能。

对于需要强一致性的数据，系统采用写穿透策略，即每次数据变更都同步更新数据库和缓存，确保缓存中的数据始终是最新的。分布式环境下，使用 Redis 作为分布式缓存，通过分布式锁保证并发更新时的数据一致性。缓存穿透问题通过布隆过滤器进行防护，缓存击穿问题通过互斥锁或永久键进行防护。

```
缓存数据流图：

【读操作】
┌─────────────┐
│  Client请求  │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ 检查缓存键   │────▶│  缓存命中？  │────▶│  返回缓存数据│
└─────────────┘     └──────┬──────┘     └─────────────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                   否            是
                    │             │
                    ▼             ▼
           ┌─────────────┐   ┌─────────────┐
           │ 查询数据库   │   │ 检查是否过期 │
           └──────┬──────┘   └──────┬──────┘
                  │                │
                  ▼                ▼
           ┌─────────────┐   ┌─────────────┐
           │ 写入缓存     │   │  返回缓存数据│
           └──────┬──────┘   └─────────────┘
                  │
                  ▼
           ┌─────────────┐
           │ 返回数据库数据│
           └─────────────┘

【写操作】
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  Client请求  │────▶│  更新数据库  │────▶│  删除缓存   │
└─────────────┘     └─────────────┘     └─────────────┘
```

## 7. 部署架构设计

### 7.1 容器化部署架构

系统采用 Docker 容器化部署，通过 Docker Compose 编排多个容器组件，实现一键部署和环境一致性。容器化部署的优势在于一次构建，处处运行，开发、测试、生产环境使用相同的容器镜像，避免了我机器上能运行的问题。容器之间通过 Docker 网络进行通信，服务发现和负载均衡由 Nginx 反向代理层统一处理。

后端服务打包为 Spring Boot 可执行 JAR 镜像，基于 Eclipse Temurin 的 Java 17 基础镜像构建。Dockerfile 采用多阶段构建，第一阶段编译项目，第二阶段创建运行时镜像，有效减小最终镜像体积。镜像构建时会进行依赖缓存、单元测试执行、代码质量检查等步骤，确保构建的镜像符合质量标准。镜像版本采用语义化版本号管理，支持滚动更新和回滚。

数据库采用 MySQL 8.0 官方镜像，数据通过 Docker Volume 持久化存储，确保容器重启后数据不丢失。生产环境推荐使用云服务商提供的托管 MySQL 服务，以获得更好的可用性保障和运维支持。可选的 Redis 缓存层同样采用官方镜像，通过密码认证和独立网络隔离保证安全性。

```yaml
# Docker Compose 配置示例
# docker-compose.yml

version: '3.8'

services:
  nginx:
    image: nginx:1.25-alpine
    container_name: campus-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
      - ./frontend-user/dist:/usr/share/nginx/html/user:ro
      - ./frontend-admin/dist:/usr/share/nginx/html/admin:ro
    depends_on:
      - backend
    networks:
      - campus-network
    restart: unless-stopped

  backend:
    image: campus-backend:latest
    container_name: campus-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MYSQL_HOST=mysql
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - mysql
    networks:
      - campus-network
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    container_name: campus-mysql
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=campus
      - MYSQL_CHARSET=utf8mb4
      - MYSQL_COLLATION=utf8mb4_unicode_ci
    volumes:
      - mysql-data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d:ro
    networks:
      - campus-network
    command: --default-authentication-plugin=mysql_native_password
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: campus-redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    networks:
      - campus-network
    restart: unless-stopped

networks:
  campus-network:
    driver: bridge

volumes:
  mysql-data:
  redis-data:
```

### 7.2 Nginx 配置架构

Nginx 作为系统的统一入口，承担着反向代理、负载均衡、SSL 终结、静态资源服务、缓存控制等多重职责。所有客户端请求首先到达 Nginx，Nginx 根据请求路径将请求转发到对应的后端服务或直接返回静态资源。这种架构可以将 SSL 终结放在 Nginx 层，简化后端服务的配置，同时利用 Nginx 的高效处理能力处理 HTTPS 流量。

负载均衡采用轮询策略，在单机部署时虽然效果不明显，但在后续扩展为多实例部署时可以自动分发请求到多个后端实例，提升系统吞吐量和可用性。Nginx 还配置了健康检查机制，自动检测后端实例的健康状态，将故障实例从负载均衡池中移除，待恢复后再重新加入。前端静态资源通过 gzip 压缩传输，减少网络传输量，加速页面加载。

```
Nginx 配置架构图：

                    ┌─────────────────────────┐
                    │      互联网用户          │
                    └───────────┬─────────────┘
                                │
                    ┌───────────┴─────────────┐
                    ▼                         ▼
            ┌───────────────┐         ┌───────────────┐
            │    HTTP (80)  │         │   HTTPS (443) │
            └───────┬───────┘         └───────┬───────┘
                    │                         │
                    └───────────┬─────────────┘
                                │
                    ┌───────────┴─────────────┐
                    ▼                         │
            ┌───────────────┐                 │
            │  SSL终结       │                 │
            │  重定向HTTP到HTTPS│               │
            └───────┬───────┘                 │
                    │                         │
                    └───────────┬─────────────┘
                                │
                    ┌───────────┴─────────────┐
                    ▼                         ▼
            ┌───────────────┐         ┌───────────────┐
            │  前端静态资源   │         │  API请求转发   │
            │  /user/       │         │  /api/        │
            │  /admin/      │         │  /swagger/    │
            └───────────────┘         └───────┬───────┘
                                              │
                                              ▼
                                    ┌─────────────────┐
                                    │  负载均衡       │
                                    │  upstream backend│
                                    ├─────────────────┤
                                    │  backend:8080   │
                                    │  (可扩展多实例)  │
                                    └─────────────────┘
```

### 7.3 CI/CD 流水线架构

系统实现了完整的持续集成和持续部署流水线，通过 GitHub Actions 实现自动化构建、测试和部署。流水线分为多个阶段：代码检出、依赖安装、代码构建、单元测试、代码质量检查、镜像构建、部署到测试环境、部署到生产环境。每个阶段都有明确的输入和输出，前一阶段的输出作为后一阶段的输入，形成完整的自动化流程。

代码质量检查集成 SonarQube 进行静态代码分析，检查代码异味、安全漏洞、代码重复率等指标。单元测试覆盖率要求不低于 70%，关键业务逻辑要求达到 90% 以上。SonarQube 的检查结果会影响流水线的执行，如果发现严重问题，流水线会失败并通知开发人员修复。测试环境部署采用蓝绿部署或滚动更新策略，确保测试环境的可用性。

生产环境部署采用蓝绿部署策略，同时维护两套完全相同的生产环境。新版本部署到非活动环境，经过验证后切换流量到新版本，实现零停机部署。如果新版本出现问题，可以立即切换回旧版本，实现快速回滚。生产环境的部署需要经过人工审批，防止代码未经审核直接部署到生产环境。

```
CI/CD 流水线架构图：

┌─────────────────────────────────────────────────────────────────────────────┐
│                           GitHub Actions CI/CD 流水线                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │ 代码检出  │───▶│ 依赖安装  │───▶│ 代码构建  │───▶│ 单元测试 │              │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘              │
│       │              │               │               │                      │
│       ▼              ▼               ▼               ▼                      │
│  git checkout    npm install     npm build     npm test                     │
│                  mvn compile     mvn package    mvn test                    │
│                                                                              │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐              │
│  │ 代码质量  │───▶│ 镜像构建  │───▶│ 测试部署  │───▶│ 生产部署 │              │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘              │
│       │              │               │               │                      │
│       ▼              ▼               ▼               ▼                      │
│  SonarQube      Docker build    Deploy to       Manual                      │
│  analysis       & push          test-env        Approval                    │
│                                                         │                   │
│                                                         ▼                   │
│                                                Blue-Green Deploy            │
│                                                Health Check                 │
│                                                Rollback (if needed)         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 7.4 监控与日志架构

系统监控是保障系统稳定运行的重要组成部分，监控架构包括基础设施监控、应用性能监控和业务指标监控三个层面。基础设施监控关注服务器资源使用情况、容器运行状态等。应用性能监控关注接口响应时间、吞吐量、错误率、数据库查询性能等。业务指标监控关注用户活跃度、转化率、核心业务指标的变化趋势。

日志采用集中化管理，所有服务的日志通过 Filebeat 或 Fluentd 收集，统一传输到 Elasticsearch 存储，使用 Kibana 进行日志查询和分析。日志格式采用 JSON 结构化格式，包含时间戳、日志级别、请求 ID、用户 ID、服务名称、日志内容等字段，便于后续的搜索和分析。日志级别分为 ERROR、WARN、INFO、DEBUG，生产环境默认 INFO 级别，DEBUG 日志只在排查问题时临时开启。

系统健康检查通过 /actuator/health 端点暴露健康状态，Nginx 和负载均衡器定期探测该端点，当检测到服务不可用时自动摘除故障实例。告警规则配置在监控系统中，当关键指标触发告警时，会通过邮件、钉钉、短信等渠道通知运维人员。告警包含详细的问题描述、影响范围和初步排查建议，帮助运维人员快速定位和解决问题。

## 附录

### A. 术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| RBAC | Role-Based Access Control | 基于角色的访问控制 |
| JWT | JSON Web Token | JSON 网络令牌，用于身份认证 |
| CORS | Cross-Origin Resource Sharing | 跨源资源共享 |
| CSRF | Cross-Site Request Forgery | 跨站请求伪造 |
| XSS | Cross-Site Scripting | 跨站脚本攻击 |
| TTL | Time To Live | 生存时间 |
| APM | Application Performance Monitoring | 应用性能监控 |
| CI/CD | Continuous Integration/Continuous Deployment | 持续集成/持续部署 |

### B. 参考文档

- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- Vue 3 官方文档：https://vuejs.org/
- MyBatis-Plus 官方文档：https://baomidou.com/
- Docker 官方文档：https://docs.docker.com/
- Nginx 官方文档：https://nginx.org/en/docs/

### C. 修订历史

| 版本 | 日期 | 修改内容 | 作者 |
|------|------|----------|------|
| 1.0.0 | 2026年2月1日 | 初始版本，创建架构文档 | Campus Team |
