package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.config.JwtConfig;
import com.campus.modules.admin.entity.Admin;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.admin.service.DashboardService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtConfig jwtConfig;

    @MockBean
    private AdminService adminService;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BoardService boardService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private String validAdminToken;

    @BeforeEach
    void setUp() {
        String jwtSecret = "campus-helping-platform-jwt-secret-key-2024-very-long-and-secure-123";
        validAdminToken = io.jsonwebtoken.Jwts.builder()
            .subject("1")
            .claim("role", 1)
            .claim("username", "admin")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .compact();
        
        when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
        when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
    }

    private static final String ADMIN_BASE_URL = "/api/admin";
    private String getAuthHeader() {
        return "Bearer " + validAdminToken;
    }

    // ==================== AdminAuthController Tests ====================

    @Nested
    @DisplayName("POST /api/admin/auth/login - 管理员登录")
    class AdminLoginTests {

        @Test
        @DisplayName("登录成功")
        void shouldLoginSuccessfully() throws Exception {
            AdminAuthController.LoginRequest request = new AdminAuthController.LoginRequest();
            request.setUsername("admin");
            request.setPassword("admin123");

            Admin admin = new Admin();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setNickname("超级管理员");
            admin.setRole(1);

            when(adminService.login(anyString(), anyString())).thenReturn("mock-jwt-token");
            when(adminService.getByUsername(anyString())).thenReturn(admin);

            mockMvc.perform(post(ADMIN_BASE_URL + "/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"))
                    .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                    .andExpect(jsonPath("$.data.admin.id").value(1))
                    .andExpect(jsonPath("$.data.admin.username").value("admin"));
        }

        @Test
        @DisplayName("登录失败 - 用户名不存在")
        void shouldFailLoginWhenUsernameNotExist() throws Exception {
            AdminAuthController.LoginRequest request = new AdminAuthController.LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword("admin123");

            when(adminService.login(anyString(), anyString()))
                    .thenThrow(new IllegalArgumentException("用户名或密码错误"));

            mockMvc.perform(post(ADMIN_BASE_URL + "/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/auth/init-admin - 初始化超级管理员")
    class InitAdminTests {

        @Test
        @DisplayName("初始化成功")
        void shouldInitAdminSuccessfully() throws Exception {
            AdminAuthController.InitRequest request = new AdminAuthController.InitRequest();
            request.setUsername("admin");
            request.setPassword("admin123");
            request.setNickname("超级管理员");

            when(adminService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(adminService.save(any(Admin.class))).thenReturn(true);

            mockMvc.perform(post(ADMIN_BASE_URL + "/auth/init-admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.username").value("admin"));
        }

        @Test
        @DisplayName("初始化失败 - 超级管理员已存在")
        void shouldFailInitWhenAdminExists() throws Exception {
            AdminAuthController.InitRequest request = new AdminAuthController.InitRequest();
            request.setUsername("admin");
            request.setPassword("admin123");

            Admin existingAdmin = new Admin();
            existingAdmin.setId(1L);
            existingAdmin.setUsername("admin");

            when(adminService.getOne(any(LambdaQueryWrapper.class))).thenReturn(existingAdmin);

            mockMvc.perform(post(ADMIN_BASE_URL + "/auth/init-admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("超级管理员已存在"));
        }
    }

    // ==================== DashboardController Tests ====================

    @Nested
    @DisplayName("GET /api/admin/dashboard/stats - 获取首页统计数据")
    class DashboardStatsTests {

        @Test
        @DisplayName("获取统计数据成功")
        void shouldGetStatsSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
            when(dashboardService.getStats()).thenReturn(Map.of(
                    "totalUsers", 100,
                    "totalPosts", 50,
                    "totalItems", 30
            ));

            mockMvc.perform(get(ADMIN_BASE_URL + "/dashboard/stats")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalUsers").value(100))
                    .andExpect(jsonPath("$.data.totalPosts").value(50));
        }

        @Test
        @DisplayName("获取统计数据失败 - 未授权")
        void shouldFailGetStatsWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/dashboard/stats"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/dashboard/overview - 获取完整首页数据")
    class DashboardOverviewTests {

        @Test
        @DisplayName("获取首页概览成功")
        void shouldGetOverviewSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
            when(dashboardService.getOverview()).thenReturn(Map.of(
                    "stats", Map.of("totalUsers", 100),
                    "trend", List.of(),
                    "recent", List.of()
            ));

            mockMvc.perform(get(ADMIN_BASE_URL + "/dashboard/overview")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.stats.totalUsers").value(100));
        }
    }

    // ==================== UserManagementController Tests ====================

    @Nested
    @DisplayName("GET /api/admin/users - 获取用户列表")
    class UserListTests {

        @Test
        @DisplayName("获取用户列表成功")
        void shouldGetUserListSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            Page<User> page = new Page<>(1, 10);
            User user = new User();
            user.setId(1L);
            user.setNickname("测试用户");
            user.setPhone("13800000001");
            user.setStatus(1);
            page.setRecords(List.of(user));
            page.setTotal(1);

            when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(ADMIN_BASE_URL + "/users")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].nickname").value("测试用户"));
        }

        @Test
        @DisplayName("获取用户列表失败 - 未授权")
        void shouldFailGetUserListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{userId} - 获取用户详情")
    class UserDetailTests {

        @Test
        @DisplayName("获取用户详情成功")
        void shouldGetUserDetailSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            User user = new User();
            user.setId(1L);
            user.setNickname("测试用户");
            user.setPhone("13800000001");
            user.setStatus(1);
            user.setDeleted(0);

            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(ADMIN_BASE_URL + "/users/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.nickname").value("测试用户"));
        }

        @Test
        @DisplayName("获取用户详情失败 - 用户不存在")
        void shouldFailGetUserDetailWhenNotFound() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
            when(userService.getById(99L)).thenReturn(null);

            mockMvc.perform(get(ADMIN_BASE_URL + "/users/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{userId}/ban - 封禁用户")
    class UserBanTests {

        @Test
        @DisplayName("封禁用户成功")
        void shouldBanUserSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            User user = new User();
            user.setId(1L);
            user.setStatus(1);

            when(userService.getById(1L)).thenReturn(user);
            when(userService.updateById(any(User.class))).thenReturn(true);

            mockMvc.perform(put(ADMIN_BASE_URL + "/users/1/ban")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/stats - 获取用户统计")
    class UserStatsTests {

        @Test
        @DisplayName("获取用户统计成功")
        void shouldGetUserStatsSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            when(userService.count()).thenReturn(100L);
            when(userService.count(any(LambdaQueryWrapper.class))).thenReturn(90L);

            mockMvc.perform(get(ADMIN_BASE_URL + "/users/stats")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(100));
        }
    }

    // ==================== PostManagementController Tests ====================

    @Nested
    @DisplayName("GET /api/admin/posts - 获取帖子列表")
    class PostListTests {

        @Test
        @DisplayName("获取帖子列表成功")
        void shouldGetPostListSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            Page<Post> page = new Page<>(1, 20);
            Post post = new Post();
            post.setId(1L);
            post.setTitle("测试帖子");
            post.setStatus(1);
            page.setRecords(List.of(post));
            page.setTotal(1);

            when(postService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(ADMIN_BASE_URL + "/posts")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].title").value("测试帖子"));
        }

        @Test
        @DisplayName("获取帖子列表失败 - 未授权")
        void shouldFailGetPostListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/posts"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/posts/{postId} - 删除帖子")
    class PostDeleteTests {

        @Test
        @DisplayName("删除帖子成功")
        void shouldDeletePostSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            Post post = new Post();
            post.setId(1L);
            post.setStatus(1);

            when(postService.getById(1L)).thenReturn(post);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/posts/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除帖子失败 - 帖子不存在")
        void shouldFailDeletePostWhenNotFound() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
            when(postService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/posts/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/posts/stats - 获取帖子统计")
    class PostStatsTests {

        @Test
        @DisplayName("获取帖子统计成功")
        void shouldGetPostStatsSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            when(postService.count(any(LambdaQueryWrapper.class))).thenReturn(50L);

            mockMvc.perform(get(ADMIN_BASE_URL + "/posts/stats")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(50));
        }
    }

    // ==================== ItemManagementController Tests ====================

    @Nested
    @DisplayName("GET /api/admin/items - 获取物品列表")
    class ItemListTests {

        @Test
        @DisplayName("获取物品列表成功")
        void shouldGetItemListSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            Page<Item> page = new Page<>(1, 20);
            Item item = new Item();
            item.setId(1L);
            item.setTitle("测试物品");
            item.setStatus(1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(ADMIN_BASE_URL + "/items")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].title").value("测试物品"));
        }

        @Test
        @DisplayName("获取物品列表失败 - 未授权")
        void shouldFailGetItemListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(ADMIN_BASE_URL + "/items"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/items/{itemId} - 删除物品")
    class ItemDeleteTests {

        @Test
        @DisplayName("删除物品成功")
        void shouldDeleteItemSuccessfully() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);

            Item item = new Item();
            item.setId(1L);
            item.setStatus(1);

            when(itemService.getById(1L)).thenReturn(item);
            when(itemService.updateById(any(Item.class))).thenReturn(true);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/items/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除物品失败 - 物品不存在")
        void shouldFailDeleteItemWhenNotFound() throws Exception {
            when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
            when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
            when(itemService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/items/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    // ==================== BoardManagementController Tests ====================

    @Nested
    @DisplayName("GET /api/admin/boards - 获取板块列表")
    class BoardListTests {

        @Test
        @DisplayName("获取板块列表成功")
        void shouldGetBoardListSuccessfully() throws Exception {
            Page<Board> page = new Page<>(1, 20);
            Board board = new Board();
            board.setId(1L);
            board.setName("学习交流");
            page.setRecords(List.of(board));
            page.setTotal(1);

            when(boardService.page(any(Page.class))).thenReturn(page);

            mockMvc.perform(get(ADMIN_BASE_URL + "/boards")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records[0].name").value("学习交流"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/boards - 创建板块")
    class BoardCreateTests {

        @Test
        @DisplayName("创建板块成功")
        void shouldCreateBoardSuccessfully() throws Exception {
            BoardManagementController.BoardCreateRequest request = 
                    new BoardManagementController.BoardCreateRequest();
            request.setName("新板块");
            request.setDescription("这是一个新板块");

            Board board = new Board();
            board.setId(1L);
            board.setName("新板块");

            when(boardService.save(any(Board.class))).thenReturn(true);

            mockMvc.perform(post(ADMIN_BASE_URL + "/boards")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("新板块"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/boards/{boardId} - 删除板块")
    class BoardDeleteTests {

        @Test
        @DisplayName("删除板块成功")
        void shouldDeleteBoardSuccessfully() throws Exception {
            Board board = new Board();
            board.setId(1L);
            board.setDeleted(0);

            when(boardService.getById(1L)).thenReturn(board);
            when(boardService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/boards/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除板块失败 - 板块不存在")
        void shouldFailDeleteBoardWhenNotFound() throws Exception {
            when(boardService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(ADMIN_BASE_URL + "/boards/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/boards/stats - 获取板块统计")
    class BoardStatsTests {

        @Test
        @DisplayName("获取板块统计成功")
        void shouldGetBoardStatsSuccessfully() throws Exception {
            when(boardService.count()).thenReturn(10L);
            when(boardService.count(any(LambdaQueryWrapper.class))).thenReturn(8L);

            mockMvc.perform(get(ADMIN_BASE_URL + "/boards/stats")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(10))
                    .andExpect(jsonPath("$.data.active").value(8));
        }
    }
}
