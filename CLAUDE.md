# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Campus Helping Platform (校园互助平台) - A full-stack campus social platform with forum, second-hand trading, and real-time chat features.

**Architecture:**
- **Backend**: Spring Boot 3.2 + Java 21 + MyBatis-Plus + MySQL
- **Frontend User**: Vue 3 + Tailwind CSS (recently migrated from Vant UI)
- **Frontend Admin**: Vue 3 + Element Plus
- **Authentication**: JWT (7-day expiration) + Spring Security
- **Real-time**: WebSocket (STOMP protocol)

---

## Common Commands

### Backend (Spring Boot + Maven)

```bash
# Build and run
cd backend
mvn clean compile              # Compile project
mvn spring-boot:run            # Start backend (port 8080)
mvn package -DskipTests        # Build JAR without tests
java -jar target/backend-1.0.0.jar  # Run packaged JAR

# Testing
mvn test                       # Run all tests
mvn test -Dtest=PostServiceTest           # Run specific test class
mvn test -Dtest=PostServiceTest\$IncrementViewCountTests  # Run nested test class
mvn test -Dtest=*IntegrationTest          # Run integration tests

# Database
mysql -h localhost -P 3306 -u root -p123 < backend/sql/init.sql  # Initialize database
```

### Frontend User (Vue 3 + Tailwind CSS)

```bash
cd frontend-user
npm install                    # Install dependencies
npm run dev                    # Start dev server (port 3000)
npm run build                  # Build for production
npm run preview                # Preview production build
npm run lint                   # Run ESLint
npm run test                   # Run Vitest tests
```

### Frontend Admin (Vue 3 + Element Plus)

```bash
cd frontend-admin
npm install                    # Install dependencies
npm run dev                    # Start dev server (port 3001)
npm run build                  # Build for production
npm run lint                   # Run ESLint
npm run test                   # Run Vitest tests
```

### Quick Start (All Services)

Windows:
```bash
start-all.bat                  # Start backend + both frontends
```

---

## Backend Architecture

### Module Structure

The backend follows a modular structure under `backend/src/main/java/com/campus/modules/`:

- **auth/** - Authentication (login, register, token generation)
- **user/** - User management (profile, status)
- **forum/** - Forum (boards, posts, comments, likes, collections, notifications)
- **trade/** - Second-hand trading (items, item collections)
- **chat/** - Real-time messaging (conversations, WebSocket)
- **admin/** - Admin panel (user/board/post/item management)

Each module follows the standard layered architecture:
```
modules/{name}/
├── controller/     # REST endpoints
├── service/        # Business logic (interface + impl/)
├── mapper/         # MyBatis-Plus data access
└── entity/         # Database entities
```

### Key Patterns

**BaseEntity** ([`BaseEntity.java`](backend/src/main/java/com/campus/entity/BaseEntity.java)):
- All entities extend `BaseEntity` providing `id`, `createdAt`, `deleted` fields
- Uses MyBatis-Plus `@TableLogic` for soft deletes
- Auto-fills `createdAt` and `deleted` on insert via `FieldFill.INSERT`

**Result** ([`Result.java`](backend/src/main/java/com/campus/common/Result.java)):
- Unified API response wrapper with `code`, `message`, `data`, `timestamp`
- Use `Result.success(data)` or `Result.error(message)` for responses

**Service Layer**:
- Extends `ServiceImpl<Mapper, Entity>` from MyBatis-Plus
- Custom business methods (e.g., `incrementViewCount`, `isAuthor`) are added here

**Global Exception Handler** ([`GlobalExceptionHandler.java`](backend/src/main/java/com/campus/common/GlobalExceptionHandler.java)):
- Centralized error handling for authentication, validation, and system exceptions

### Security Architecture

**Dual Authentication System**:
1. **User Authentication**: Phone-based JWT tokens (stored in localStorage)
2. **Admin Authentication**: Username-based JWT with role claims

**JWT Flow** ([`JwtTokenFilter.java`](backend/src/main/java/com/campus/config/JwtTokenFilter.java)):
- Filter extracts token from `Authorization: Bearer <token>` header
- Admin tokens contain `role` claim; user tokens don't
- Sets Spring Security context with `ROLE_USER` or `ROLE_ADMIN`

**Security Rules** ([`SecurityConfig.java`](backend/src/main/java/com/campus/config/SecurityConfig.java)):
- Public endpoints: `/api/auth/**`, GET `/api/posts`, `/api/boards`, `/swagger-ui/**`
- Authenticated: POST/PUT/DELETE for posts, items, comments
- Admin only: `/api/admin/**` endpoints

### Testing

**Test Structure**:
```
src/test/java/com/campus/
├── config/                 # Security/config tests
├── integration/            # Integration tests with real database
└── modules/                # Unit tests per module
```

**Unit Test Pattern**:
- Use `@ExtendWith(MockitoExtension.class)`
- Mock the `baseMapper` field via reflection in `@BeforeEach`
- Use `@Nested` classes to group related tests
- Pattern: [PostServiceTest.java](backend/src/test/java/com/campus/modules/forum/post/PostServiceTest.java)

**Integration Tests**:
- Use `application-mysql-test.yml` for real MySQL tests
- Use `application-test.yml` (H2 in-memory) for fast unit tests
- Integration test class names end with `IntegrationTest`

---

## Frontend Architecture

### Recent Migration: Vant UI to Tailwind CSS

The `frontend-user` project recently removed Vant UI components in favor of custom components with Tailwind CSS. When working on user-facing features:

- **DO NOT** use Vant UI components - they have been removed
- **USE** Tailwind CSS utility classes for styling
- **USE** existing custom components in `src/components/base/`:
  - `Button.vue`, `Card.vue`, `Cell.vue`, `Input.vue`
  - `NavBar.vue`, `TabBar.vue` (navigation components)

### Project Structure

**frontend-user** (port 3000):
- **API Layer**: `src/api/` - HTTP client with Axios, auto-intercepts for JWT
- **State**: Pinia stores in `src/stores/` (main store: `user.ts`)
- **Routing**: `src/router/index.ts` - 17 routes, login-guards for authenticated pages
- **Layouts**: `MainLayout.vue` includes bottom TabBar navigation

**frontend-admin** (port 3001):
- Element Plus components (auto-imported)
- Dashboard at `/` with stats/charts
- CRUD views for users, boards, posts, items

### Key Patterns

**Auto-imports**: Both frontends use `unplugin-auto-import` and `unplugin-vue-components`
- Vue APIs (`ref`, `computed`, etc.) are auto-imported
- Components are auto-imported (check `components.d.ts`)

**API Proxy**: Vite proxies `/api` to `http://localhost:8080`
- Frontends call `/api/auth/login` → backend `http://localhost:8080/api/auth/login`

**User Store**: `token` stored in `localStorage`, attached to requests via Axios interceptor

---

## Database Schema

Key tables: `user`, `admin`, `board`, `post`, `comment`, `like`, `collect`, `notification`, `item`, `item_collect`, `conversation`, `message`

**Important Relationships**:
- `post.user_id` → `user.id` (many-to-one)
- `post.board_id` → `board.id` (many-to-one)
- `item.user_id` → `user.id` (many-to-one)
- `conversation` uses `user_id` and `target_id` for both parties

**Soft Deletes**: Most tables have `deleted` field (0=active, 1=deleted) handled by MyBatis-Plus `@TableLogic`

---

## Configuration Files

| File | Purpose |
|------|---------|
| [`backend/pom.xml`](backend/pom.xml) | Maven dependencies (Spring Boot 3.2, MyBatis-Plus 3.5.5, JWT 0.12.3) |
| [`backend/src/main/resources/application.yml`](backend/src/main/resources/application.yml) | Main config (DB, JWT, WebSocket, Swagger) |
| [`backend/src/test/resources/application-test.yml`](backend/src/test/resources/application-test.yml) | Test config with H2 in-memory DB |
| [`frontend-user/vite.config.ts`](frontend-user/vite.config.ts) | Vite + Tailwind CSS config |
| [`frontend-admin/vite.config.ts`](frontend-admin/vite.config.ts) | Vite + Element Plus config |

---

## Development Workflow

When implementing a new feature:

1. **Backend**: Create entity → mapper → service → controller (follow module structure)
2. **Add security rules** in `SecurityConfig.java` if needed
3. **Frontend**: Add API methods in `src/api/`, then build components using Tailwind (user) or Element Plus (admin)
4. **Testing**: Write unit tests with Mockito, integration tests for complex flows
5. **Documentation**: Update API docs via Swagger annotations

---

## Important Notes

- **Java Version**: Java 21 required
- **Logical Deletes**: Never use physical deletes - MyBatis-Plus handles this via `@TableLogic`
- **JWT Expiration**: 7 days (604800000 ms) for users, configurable in `application.yml`
- **WebSocket**: Endpoint at `/ws`, uses STOMP protocol with `/topic/messages` and `/queue/messages`
- **Default Admin**: username `admin`, password `admin123` (see `backend/sql/init.sql`)

---

## Critical Integration Points

### JWT Token Flow (Backend ↔ Frontend)

**Backend Requirements**:
- Login API **must** return token at root level: `Result.success(token).setToken(token)`
- If only returning `Result.success(token)`, frontend cannot save token from `data` field
- See [`AuthController.java:39`](backend/src/main/java/com/campus/modules/auth/controller/AuthController.java)

**Frontend Auto-Handling** ([`request.js`](frontend-user/src/api/request.js)):
- Request interceptor adds `Authorization: Bearer ${token}` from localStorage
- Response interceptor auto-saves token from root-level `token` field
- 401 errors auto-clear token and redirect to `/login`

### SecurityConfig API Allowlist

**Critical**: Add new API paths to [`SecurityConfig.java`](backend/src/main/java/com/campus/config/SecurityConfig.java) or they return 403:

```java
.requestMatchers("/api/comments/**").permitAll()  // Required for comment APIs
.requestMatchers("/api/items/**").permitAll()    // Required for trade APIs
```

Pattern: If controller uses `@RequestHeader("Authorization")`, the path must be in allowlist OR use `.authenticated()` instead of `.permitAll()`.

### MyBatis-Plus Field Mapping

When entity field names differ from database columns, use `@TableField`:

```java
// Database column: target_id
// Entity field: postId
@TableField("target_id")
private Long postId;
```

See [`Notification.java:34`](backend/src/main/java/com/campus/modules/forum/entity/Notification.java) for example.

### API Module Exports

Frontend API modules must be re-exported from [`frontend-user/src/api/modules/index.js`](frontend-user/src/api/modules/index.js) or imports fail:

```javascript
export * from './auth'
export * from './post'
// ... other modules
```

---

## Common Issues and Solutions

### Issue: "Missing request header 'Authorization'"

**Cause**: API endpoint not in SecurityConfig allowlist or token not in localStorage

**Fix**:
1. Check login API returns `.setToken(token)`
2. Add API path to `SecurityConfig` permitAll
3. Verify localStorage has token: `localStorage.getItem('token')`

### Issue: "Unknown column 'post_id' in field list"

**Cause**: Entity field name doesn't match database column name

**Fix**: Add `@TableField("actual_column_name")` annotation to entity field

### Issue: Frontend cannot resolve `@/api/modules`

**Cause**: Missing barrel export in `frontend-user/src/api/modules/index.js`

**Fix**: Add export statement for the new API module

### Issue: Tests fail with "Port 8080 already in use"

**Fix**:
```bash
# Windows
TASKkill //F //PID <process_id>

# Or use Maven to stop
mvn spring-boot:stop
```
