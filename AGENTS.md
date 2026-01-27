# AGENTS.md - Agentic Coding Guidelines

## Project Overview

**Type**: Full-stack campus platform (Vue 3 + Spring Boot 3.2)

```
campus/
├── backend/           # Spring Boot 3.2 + Java 17 + MyBatis-Plus (Port 8080)
├── frontend-user/     # Vue 3 + Vant UI + Tailwind (Port 3000)
└── frontend-admin/    # Vue 3 + Element Plus + SCSS (Port 3001)
```

---

## Build Commands

### Backend
```bash
cd backend
mvn clean compile          # Compile
mvn spring-boot:run        # Start (Port 8080)
mvn package -DskipTests    # Package
mvn test                   # All tests
mvn test -Dtest=PostServiceTest                    # Single test class
mvn test -Dtest=PostServiceTest#shouldIncrementViewCount # Single method
```

### Frontend (both user & admin)
```bash
cd frontend-user  # or frontend-admin
npm run dev       # Dev server (3000/3001)
npm run build     # Production build
npm run preview   # Preview build
npm run test      # Vitest
npm run test src/__tests__/example.spec.ts  # Single file
npm run lint      # ESLint + auto-fix
```

**Ports**: Backend 8080 | User 3000 | Admin 3001

---

## Code Style

### Frontend (Vue 3 + TypeScript)

**Imports**: Third-party → UI Libs → Internal (@/)
```typescript
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import type { UserInfo } from '@/types'
```

**Naming**:
- Components: `PascalCase.vue`
- Variables: `camelCase`
- Functions: `handleSubmit()` / `fetchData()`
- Types/Interfaces: `PascalCase`
- Constants: `UPPER_SNAKE_CASE`

**Structure**: Use `<script setup lang="ts">` with defineProps/emits typed

**Error Handling**: Try-catch all async calls, show `ElMessage.error()` or `showToast()`

**State**: Pinia with Setup Stores pattern, persist to localStorage

---

### Backend (Spring Boot + Java)

**Naming**:
- Classes: `PascalCase`
- Methods: `camelCase` with verb prefix
- Constants: `UPPER_SNAKE_CASE`
- Packages: lowercase

**Structure**:
- Controller → Service → Mapper (inject via constructor)
- Use `@RestController`, `@Service`, `@Component`
- All write ops require `@Transactional(rollbackFor = Exception.class)`

**Response**: Use `Result<T>` wrapper (success/error methods)

**API Docs**: Add `@Tag` (class) and `@Operation` (method) annotations

**Logging**: Use `@Slf4j` + `log.info()` for key operations

---

## Development Rules

1. **Type Safety**: No `as any`, `@ts-ignore`, `@ts-expect-error`
2. **Error Handling**: Never leave empty catch blocks
3. **Testing**: Don't delete failing tests to "pass"
4. **Git**: No commits unless explicitly requested
5. **Refactoring**: Fix minimally when bug-fixing, no scope creep

---

## Tools & Utilities

### Backend
- `Result<T>` - Unified response
- `BaseEntity` - Common fields (id, createdAt, updatedAt)
- `LambdaQueryWrapper<T>` - Type-safe queries

### Frontend
- `request` - Axios instance with interceptors
- `useUserStore()` - Auth state
- `useRouter()` - Navigation

---

## References
- Backend: http://localhost:8080/swagger-ui.html
- User App: http://localhost:3000
- Admin App: http://localhost:3001
