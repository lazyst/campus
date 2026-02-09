# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Campus Helping Platform** - Full-stack campus service application for college students with forum discussions, second-hand trading, and real-time messaging.

```
campus-fenbushi/
├── backend/           # Spring Boot 3.2 + Java 17 + MyBatis-Plus (port 8080)
├── frontend-user/     # Vue 3 + Vant UI + Tailwind CSS v4 (port 3000)
├── frontend-admin/    # Vue 3 + Element Plus (port 3001)
├── nginx/             # Reverse proxy with load balancing
├── docker-compose.yml # Production deployment
└── docs/              # Documentation
```

## Build Commands

### Backend (Java 17 + Spring Boot 3.2)
```bash
cd backend
mvn clean compile              # Compile
mvn spring-boot:run            # Run dev server (port 8080)
mvn package -DskipTests        # Build JAR
mvn test                       # Run all tests with JaCoCo coverage
mvn test -Dtest=PostServiceTest                    # Single test class
mvn test -Dtest=PostServiceTest#shouldIncrementViewCount # Single method
mvn jacoco:report              # Generate coverage report (target/site/jacoco)
```

**Coverage requirements**: Line ≥ 70%, Branch ≥ 60%

### Frontend (Vue 3)
```bash
cd frontend-user   # or frontend-admin
npm install        # Install dependencies
npm run dev        # Dev server (proxies to backend:8080)
npm run build      # Production build
npm run test       # Vitest tests
npm run lint       # ESLint with auto-fix
```

## Development Environment

### Remote Database (VM 192.168.100.100)
| Service | Port | Credentials |
|---------|------|-------------|
| MySQL | 3306 | root/123, database: campus_fenbushi |
| Redis | 6379 | password: 123 |

### Local Service Ports
| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080/api |
| Swagger Docs | http://localhost:8080/swagger-ui.html |
| User Frontend | http://localhost:3000 |
| Admin Frontend | http://localhost:3001 |

## Architecture Patterns

### Backend Layer Pattern
```
Controller → Service → Mapper → Entity
```

### Read-Write Splitting
- `@DS("master")` - Write operations (default)
- `@DS("slave")` - Read operations
- Configured via `DsAspectConfig` and `ReadWriteRouteAspect`

### API Response Format
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {...},
  "timestamp": 1700000000000
}
```

Use `Result.success(data)`, `Result.error("message")`, or `Result.error(ResultCode.NOT_FOUND)`.

### JWT Authentication
- Header: `Authorization: Bearer <token>`
- 7-day expiration (604800000 ms)
- Separate tokens for user (`token`) and admin (`admin_token`)

## Key Backend Modules

| Module | Path | Purpose |
|--------|------|---------|
| auth | `/api/auth/**` | Login/register |
| forum | `/api/posts/**`, `/api/comments/**` | Forum posts, comments, likes, collects |
| trade | `/api/items/**` | Second-hand items |
| chat | `/api/chat/**`, `/ws` | WebSocket messaging |
| user | `/api/user/**` | User profile |
| admin | `/api/admin/**` | Dashboard, user/board/item management |

## Design Constraints

**Prohibited**: Icons (SVG/images/fonts), Emojis, Purple/Orange colors (`#6366F1`)

**Required**: Use `#1E3A8A` (indigo) as primary color; get color/spacing tokens from `src/styles/design-tokens.css`

## Critical Rules

1. **No type assertions**: `as any`, `@ts-ignore`, `@ts-expect-error` forbidden
2. **Error handling**: Never leave catch blocks empty
3. **Testing**: Never delete failing tests to "pass"
4. **Git**: Never commit unless explicitly requested
5. **Code patterns**: Always follow `Controller → Service → Mapper → Entity` layers

## Frontend Import Order
```javascript
// 1. Vue core API
import { ref, computed } from 'vue'

// 2. Vue Router
import { useRouter } from 'vue-router'

// 3. Pinia stores
import { useUserStore } from '@/stores/user'

// 4. Constants
import { API_ENDPOINTS } from '@/constants'

// 5. API functions
import { login } from '@/api/auth'

// 6. Components
import UserCard from '@/components/UserCard.vue'

// 7. Styles
import '@/styles/main.css'
```

## Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Java Class | PascalCase | `PostService`, `UserController` |
| Java Method | camelCase + verb | `createPost()`, `getUserById()` |
| JS/Vue | camelCase | `userInfo`, `isLoading` |
| Vue Component | PascalCase.vue | `UserProfile.vue` |
| Event Handler | handleVerbNoun() | `handleSubmit()` |
| CSS Class | kebab-case | `.user-profile-card` |

## WebSocket Configuration
- Endpoint: `/ws`
- Cluster support via Redis Pub/Sub
- Topics: `/topic/messages`, `/queue/messages`

## Database Tables

| Table | Purpose |
|-------|---------|
| `admin` | Admin users (role: 1=super, 2=regular) |
| `user` | Platform users |
| `board` | Forum boards |
| `post` | Forum posts |
| `comment` | Post comments (hierarchical with parent_id) |
| `collect`/`like` | Post engagement |
| `item` | Second-hand items |
| `item_collect` | Item favorites |
| `message`/`conversation` | Chat messages |
| `notification` | User notifications |

## Test Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| User | 13800000000 | 123456 |

## Documentation Reference

| Document | Location |
|----------|----------|
| Environment Setup | `docs/ENV_SETUP.md` |
| Architecture | `docs/ARCHITECTURE.md` |
| Code Standards | `docs/CODING_STANDARDS.md` |
| Deployment | `docs/DEPLOYMENT.md` |
| Agent Standards | `AGENTS.md` |

## Common Utilities

### Backend
- `Result<T>` -统一 API 响应包装
- `ResultCode` -响应状态码枚举
- `BaseEntity` -公共字段 (id, createdAt, updatedAt)
- `LambdaQueryWrapper<T>` -类型安全查询

### Frontend
- `request` -Axios instance with interceptors
- `useUserStore()` -认证状态管理 (Pinia)
- `showToast()` -用户反馈提示
