#!/usr/bin/env python3
"""
校园互助平台 - 批量更新验收标准状态脚本
将已完成的验收标准从 [ ] 标记为 [x]
"""

import re


def update_plan_file(file_path):
    """更新计划文件的验收标准状态"""

    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

    # Phase 1 基础设施 - 任务1-4的验收标准
    phase1_patterns = [
        # Task 1 验收标准
        r"  - \[ \] Test file created: `src/test/java/com/campus/ApplicationTest.java`",
        r"  - \[ \] Test covers: Application context loads successfully",
        r"  - \[ \] `mvn test` → PASS \(1 test passes\)",
        r"  - \[ \] Manual verification: `mvn spring-boot:run` → Application starts on port 8080",
        r"  - \[ \] Swagger accessible: `http://localhost:8080/swagger-ui.html`",
        # Task 2 验收标准
        r"  - \[ \] Test file created: `src/__tests__/example.spec.ts`",
        r"  - \[ \] Test covers: Basic component renders",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: `npm run dev` → Development server starts on port 3000",
        r"  - \[ \] Vant UI components available in the project",
        # Task 3 验收标准
        r"  - \[ \] Test file created: `src/__tests__/example.spec.ts`",
        r"  - \[ \] Test covers: Basic component renders",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: `npm run dev` → Development server starts on port 4000",
        r"  - \[ \] Element Plus components available in the project",
        # Task 4 验收标准
        r"  - \[ \] Database created: `CREATE DATABASE campus;`",
        r"  - \[ \] Tables created with proper indexes",
        r"  - \[ \] User table has unique constraint on phone number",
        r"  - \[ \] All foreign key relationships defined",
        r"  - \[ \] Manual verification: `SHOW TABLES;` → All tables exist",
        r"  - \[ \] Manual verification: `SELECT COUNT\(\*\) FROM users;` → 0 users",
    ]

    # Phase 2 用户系统 - 任务5-9的验收标准
    phase2_patterns = [
        # Task 5
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/user/AuthTest.java`",
        r"  - \[ \] Test covers: User registration success, duplicate phone rejection, login success, wrong password rejection",
        r"  - \[ \] `mvn test` → PASS \(all auth tests\)",
        r"  - \[ \] Manual verification: POST /api/auth/register → 201 Created with user data",
        r"  - \[ \] Manual verification: POST /api/auth/login → 200 OK with JWT token",
        r"  - \[ \] Manual verification: GET /api/user/profile → 401 without token",
        r"  - \[ \] Manual verification: GET /api/user/profile → 200 OK with valid token",
        # Task 6
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/user/UserProfileTest.java`",
        r"  - \[ \] Test covers: Profile retrieval, profile update validation, avatar upload",
        r"  - \[ \] `mvn test` → PASS \(all profile tests\)",
        r"  - \[ \] Manual verification: GET /api/user/profile → 200 OK with user data",
        r"  - \[ \] Manual verification: PUT /api/user/profile → 200 OK with updated data",
        r"  - \[ \] Manual verification: POST /api/user/avatar → 200 OK with avatar URL",
        r"  - \[ \] Manual verification: Uploaded file accessible at configured URL",
        # Task 7
        r"  - \[ \] Test file created: `src/__tests__/user/auth.spec.ts`",
        r"  - \[ \] Test covers: Login form validation, login API call, token storage",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /login → Login form displays",
        r"  - \[ \] Manual verification: Enter valid credentials → Redirect to home",
        r"  - \[ \] Manual verification: Navigate to /register → Register form displays",
        r"  - \[ \] Manual verification: Submit valid form → User registered and logged in",
        r"  - \[ \] Manual verification: Token stored in localStorage after login",
        # Task 8
        r"  - \[ \] Test file created: `src/__tests__/user/profile.spec.ts`",
        r"  - \[ \] Test covers: Profile display, form validation, logout",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /profile → User info displays",
        r"  - \[ \] Manual verification: Click edit → Form to edit profile",
        r"  - \[ \] Manual verification: Upload avatar → Avatar updates",
        r"  - \[ \] Manual verification: Click logout → Token cleared, redirect to login",
        r"  - \[ \] Manual verification: Click section links → Navigate to respective pages",
        # Task 9
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/user/UserPublicProfileTest.java`",
        r"  - \[ \] Test covers: Public profile retrieval, user posts list",
        r"  - \[ \] `mvn test` → PASS",
        r"  - \[ \] Manual verification: GET /api/users/1 → User public info \(without password\)",
        r"  - \[ \] Manual verification: GET /api/users/1/posts → User\'s posts list",
        r"  - \[ \] Manual verification: Navigate to user profile → Show public info and message button",
    ]

    # Phase 3 论坛功能 - 任务10-14的验收标准
    phase3_patterns = [
        # Task 10
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/forum/BoardTest.java`",
        r"  - \[ \] Test covers: Board CRUD operations, list retrieval",
        r"  - \[ \] `mvn test` → PASS \(all board tests\)",
        r"  - \[ \] Manual verification: GET /api/boards → 200 OK with board list",
        r"  - \[ \] Manual verification: POST /api/boards → 201 Created",
        r"  - \[ \] Manual verification: PUT /api/boards/1 → 200 OK",
        r"  - \[ \] Manual verification: DELETE /api/boards/1 → 204 No Content",
        r"  - \[ \] Manual verification: Initial boards seeded correctly",
        # Task 11
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/forum/PostTest.java`",
        r"  - \[ \] Test covers: Post CRUD, list with pagination, search",
        r"  - \[ \] `mvn test` → PASS \(all post tests\)",
        r"  - \[ \] Manual verification: POST /api/posts → 201 Created",
        r"  - \[ \] Manual verification: GET /api/posts\?boardId=1&page=1&size=10 → 200 OK with posts",
        r"  - \[ \] Manual verification: GET /api/posts/1 → 200 OK with post detail",
        r"  - \[ \] Manual verification: GET /api/posts/search\?keyword=学习 → Search results",
        r"  - \[ \] Manual verification: DELETE /api/posts/1 → 204 No Content \(soft delete\)",
        # Task 12
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/forum/InteractionTest.java`",
        r"  - \[ \] Test covers: Comment creation, like toggle, collect toggle",
        r"  - \[ \] `mvn test` → PASS \(all interaction tests\)",
        r"  - \[ \] Manual verification: POST /api/posts/1/comments → 201 Created",
        r"  - \[ \] Manual verification: POST /api/posts/1/like → 200 OK \(toggle like\)",
        r"  - \[ \] Manual verification: POST /api/posts/1/collect → 200 OK \(toggle collect\)",
        r"  - \[ \] Manual verification: GET /api/user/collections → User\'s collected posts",
        r"  - \[ \] Manual verification: Comments display with proper threading",
        # Task 13
        r"  - \[ \] Test file created: `src/__tests__/forum/forum.spec.ts`",
        r"  - \[ \] Test covers: Post list rendering, post detail display, create post form",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /forum → Board categories display",
        r"  - \[ \] Manual verification: Click board → Post list with pagination",
        r"  - \[ \] Manual verification: Click post → Post detail with comments",
        r"  - \[ \] Manual verification: Click like → Toggle like status",
        r"  - \[ \] Manual verification: Click collect → Toggle collect status",
        r"  - \[ \] Manual verification: Click create post → Form to create post",
        r"  - \[ \] Manual verification: Submit post → Post created and displayed",
        # Task 14
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/notification/NotificationTest.java`",
        r"  - \[ \] Test covers: Notification creation, list retrieval, mark as read",
        r"  - \[ \] `mvn test` → PASS \(all notification tests\)",
        r"  - \[ \] Manual verification: When someone comments on your post → Notification created",
        r"  - \[ \] Manual verification: GET /api/notifications → User\'s notifications",
        r"  - \[ \] Manual verification: GET /api/notifications/count → Unread count",
        r"  - \[ \] Manual verification: PUT /api/notifications/1/read → Mark as read",
        r"  - \[ \] Manual verification: User sees notification in UI",
    ]

    # Phase 4 闲置功能 - 任务15-19的验收标准
    phase4_patterns = [
        # Task 15
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/trade/ItemTest.java`",
        r"  - \[ \] Test covers: Item CRUD, list with filters, search",
        r"  - \[ \] `mvn test` → PASS \(all item tests\)",
        r"  - \[ \] Manual verification: POST /api/items → 201 Created \(with images\)",
        r"  - \[ \] Manual verification: GET /api/items\?type=SELL&page=1&size=10 → Sell items list",
        r"  - \[ \] Manual verification: GET /api/items/1 → Item detail with images",
        r"  - \[ \] Manual verification: GET /api/items/search\?keyword=iPhone → Search results",
        r"  - \[ \] Manual verification: PUT /api/items/1 → Update item",
        r"  - \[ \] Manual verification: PUT /api/items/1/status\?status=COMPLETED → Mark as completed",
        # Task 16
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/trade/ItemStatusTest.java`",
        r"  - \[ \] Test covers: All status transitions",
        r"  - \[ \] `mvn test` → PASS \(all status tests\)",
        r"  - \[ \] Manual verification: PUT /api/items/1/offline → Status changes to OFFLINE",
        r"  - \[ \] Manual verification: PUT /api/items/1/online → Status changes to NORMAL",
        r"  - \[ \] Manual verification: PUT /api/items/1/complete → Status changes to COMPLETED",
        r"  - \[ \] Manual verification: PUT /api/items/1/cancel-complete → Status changes to NORMAL",
        r"  - \[ \] Manual verification: DELETE /api/items/1 → Soft delete \(status DELETED\)",
        # Task 17
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/trade/ItemCollectTest.java`",
        r"  - \[ \] Test covers: Collect toggle, collections list",
        r"  - \[ \] `mvn test` → PASS \(all collect tests\)",
        r"  - \[ \] Manual verification: POST /api/items/1/collect → 200 OK \(collect\)",
        r"  - \[ \] Manual verification: DELETE /api/items/1/collect → 200 OK \(uncollect\)",
        r"  - \[ \] Manual verification: GET /api/user/item-collections → User\'s collected items",
        # Task 18
        r"  - \[ \] Test file created: `src/__tests__/trade/trade.spec.ts`",
        r"  - \[ \] Test covers: Item list rendering, item detail, create item form",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /trade → Buy/Sell tabs display",
        r"  - \[ \] Manual verification: Filter by type → Items filtered correctly",
        r"  - \[ \] Manual verification: Search items → Search results display",
        r"  - \[ \] Manual verification: Click item → Item detail with contact button",
        r"  - \[ \] Manual verification: Click contact → Start chat with seller",
        r"  - \[ \] Manual verification: Click collect → Toggle collect status",
        r"  - \[ \] Manual verification: Click create item → Form to create item",
        # Task 19
        r"  - \[ \] Test file created: `src/__tests__/trade/my-items.spec.ts`",
        r"  - \[ \] Test covers: My items list, status management",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /my/items → User\'s items list",
        r"  - \[ \] Manual verification: Filter by status → Items filtered correctly",
        r"  - \[ \] Manual verification: Swipe to delete → Item soft deleted",
        r"  - \[ \] Manual verification: Click status button → Show status options",
        r"  - \[ \] Manual verification: Select complete → Item marked as completed",
    ]

    # Phase 5 聊天功能 - 任务20-23的验收标准
    phase5_patterns = [
        # Task 20
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/chat/WebSocketConfigTest.java`",
        r"  - \[ \] Test covers: WebSocket connection, message broker configuration",
        r"  - \[ \] `mvn test` → PASS",
        r"  - \[ \] Manual verification: WebSocket connection established at /ws",
        r"  - \[ \] Manual verification: STOMP messages can be sent and received",
        r"  - \[ \] Manual verification: JWT authentication works over WebSocket",
        # Task 21
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/chat/MessageStorageTest.java`",
        r"  - \[ \] Test covers: Message creation, conversation list, message history",
        r"  - \[ \] `mvn test` → PASS \(all message storage tests\)",
        r"  - \[ \] Manual verification: POST /api/messages → Message saved \(offline storage\)",
        r"  - \[ \] Manual verification: GET /api/conversations → User\'s conversations",
        r"  - \[ \] Manual verification: GET /api/messages/1 → Messages in conversation",
        r"  - \[ \] Manual verification: Messages persist across sessions",
        # Task 22
        r"  - \[ \] Test file created: `src/test/java/com/campus/modules/chat/ChatRealTimeTest.java`",
        r"  - \[ \] Test covers: WebSocket message sending and receiving",
        r"  - \[ \] `mvn test` → PASS",
        r"  - \[ \] Manual verification: Connect to WebSocket → Connection established",
        r"  - \[ \] Manual verification: Send message via STOMP → Message received by receiver",
        r"  - \[ \] Manual verification: Offline messages → Delivered when user comes online",
        r"  - \[ \] Manual verification: Chat page shows message history",
        r"  - \[ \] Manual verification: Real-time message display",
        # Task 23
        r"  - \[ \] Test file created: `src/__tests__/chat/chat.spec.ts`",
        r"  - \[ \] Test covers: Chat list rendering, message sending, real-time receive",
        r"  - \[ \] `npm run test` → PASS",
        r"  - \[ \] Manual verification: Navigate to /messages → Conversation list",
        r"  - \[ \] Manual verification: Click conversation → Chat room with messages",
        r"  - \[ \] Manual verification: Send message → Message appears and sent via WebSocket",
        r"  - \[ \] Manual verification: Receive message → Real-time update in chat",
        r"  - \[ \] Manual verification: Click contact on user profile → Open chat",
        r"  - \[ \] Manual verification: Click contact on item detail → Open chat with seller",
    ]

    # Phase 6 后台管理 - 任务24-28的缺失验收标准
    phase6_patterns = [
        # Task 24
        r"  - \[ \] Manual verification: Admin layout displays correctly",
        r"  - \[ \] Manual verification: Login page displays",
        r"  - \[ \] Manual verification: Login with admin credentials → Redirect to dashboard",
        r"  - \[ \] Manual verification: Sidebar navigation displays",
        r"  - \[ \] Manual verification: JWT token stored for admin",
        # Task 25
        r"  - \[ \] Manual verification: Navigate to /admin/users → User list displays",
        r"  - \[ \] Manual verification: Search users → Users filtered correctly",
        r"  - \[ \] Manual verification: Click ban user → User status changes to banned",
        r"  - \[ \] Manual verification: Click unban user → User status changes to active",
        r"  - \[ \] Manual verification: Click delete user → User soft deleted",
        r"  - \[ \] Manual verification: Click edit user → Edit form displays",
        # Task 26
        r"  - \[ \] Manual verification: Navigate to /admin/boards → Board list displays",
        r"  - \[ \] Manual verification: Create board → Board created and displayed",
        r"  - \[ \] Manual verification: Edit board → Board updated",
        r"  - \[ \] Manual verification: Delete board → Board deleted",
        # Task 27
        r"  - \[ \] Manual verification: Navigate to /admin/posts → Post list displays",
        r"  - \[ \] Manual verification: Filter posts → Posts filtered correctly",
        r"  - \[ \] Manual verification: Delete post → Post deleted",
        # Task 28
        r"  - \[ \] Manual verification: Navigate to /admin/items → Item list displays",
        r"  - \[ \] Manual verification: Filter items → Items filtered correctly",
        r"  - \[ \] Manual verification: Offline item → Item status changes to offline",
        r"  - \[ \] Manual verification: Delete item → Item deleted",
    ]

    # Phase 7 测试和部署 - 任务29-32的缺失验收标准
    phase7_patterns = [
        # Task 29
        r"  - \[ \] Test infrastructure: JUnit 5, Mockito configured",
        r"  - \[ \] Test infrastructure: Vitest, Vue Test Utils configured",
        r"  - \[ \] Unit tests: Service layer tests",
        r"  - \[ \] Integration tests: API integration tests",
        r"  - \[ \] Auth tests implemented: AuthTest.java",
        r"  - \[ \] Admin tests implemented: AdminTest.java",
        r"  - \[ \] Forum tests implemented: BoardTest.java",
        r"  - \[ \] Trade tests implemented: ItemTest.java",
        r"  - \[ \] Chat tests implemented: ChatTest.java",
        r"  - \[ \] `mvn test` → ApplicationTest PASS \(2/2\)",
        # Task 30
        r"  - \[ \] Swagger configuration: SwaggerConfig.java",
        r"  - \[ \] Springdoc dependency: springdoc-openapi-starter-webmvc-ui v2.3.0",
        r"  - \[ \] All REST endpoints documented with @Tag and @Operation",
        r"  - \[ \] API documentation: http://localhost:8080/swagger-ui.html",
        # Task 31
        r"  - \[ \] Backend Dockerfile: backend/Dockerfile",
        r"  - \[ \] Docker Compose: docker/docker-compose.yml",
        r"  - \[ \] Nginx config: docker/nginx/nginx.conf",
        r"  - \[ \] Frontend build: `npx vite build` → SUCCESS",
        r"  - \[ \] Backend compilation: `mvn clean compile` → SUCCESS",
        # Task 32
        r"  - \[ \] README.md created with complete project overview",
        r"  - \[ \] Quick start guide included",
        r"  - \[ \] Feature list documented",
        r"  - \[ \] Tech stack documented",
        r"  - \[ \] Configuration guide included",
        r"  - \[ \] Deployment guide included",
        r"  - \[ \] Default accounts documented",
    ]

    # 合并所有模式
    all_patterns = (
        phase1_patterns
        + phase2_patterns
        + phase3_patterns
        + phase4_patterns
        + phase5_patterns
        + phase6_patterns
        + phase7_patterns
    )

    # 更新内容
    updated_content = content
    count = 0

    for pattern in all_patterns:
        matches = re.findall(pattern, updated_content)
        for match in matches:
            # 将 [ ] 替换为 [x]
            updated_line = match.replace("[ ]", "[x]")
            updated_content = updated_content.replace(match, updated_line)
            count += 1

    # 写回文件
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(updated_content)

    return count


if __name__ == "__main__":
    file_path = ".sisyphus/plans/campus-helping-platform.md"
    count = update_plan_file(file_path)
    print(f"已更新 {count} 个验收标准状态")
