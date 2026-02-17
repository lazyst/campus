package com.campus.modules.admin.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.config.JwtConfig;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.dto.UpdateProfileRequest;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 后台用户管理控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AdminService adminService;

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

    private static final String BASE_URL = "/api/admin/users";

    private String getAuthHeader() {
        return "Bearer " + validAdminToken;
    }

    @Nested
    @DisplayName("GET /api/admin/users - 获取用户列表")
    class GetUserListTests {

        @Test
        @DisplayName("获取用户列表成功")
        void shouldGetUserListSuccessfully() throws Exception {
            Page<User> page = new Page<>(1, 10);
            User user = createTestUser(1L, "测试用户", "13800000001", 1);
            page.setRecords(List.of(user));
            page.setTotal(1);

            when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].nickname").value("测试用户"));
        }

        @Test
        @DisplayName("获取用户列表带关键字查询")
        void shouldGetUserListWithKeyword() throws Exception {
            Page<User> page = new Page<>(1, 10);
            User user = createTestUser(1L, "测试用户", "13800000001", 1);
            page.setRecords(List.of(user));
            page.setTotal(1);

            when(userService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("keyword", "测试"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取用户列表失败 - 未授权")
        void shouldFailGetUserListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{id} - 获取用户详情")
    class GetUserDetailTests {

        @Test
        @DisplayName("获取用户详情成功")
        void shouldGetUserDetailSuccessfully() throws Exception {
            User user = createTestUser(1L, "测试用户", "13800000001", 1);

            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.nickname").value("测试用户"))
                    .andExpect(jsonPath("$.data.password").doesNotExist());
        }

        @Test
        @DisplayName("获取用户详情失败 - 用户不存在")
        void shouldFailGetUserDetailWhenNotFound() throws Exception {
            when(userService.getById(99L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{id}/status - 更新用户状态")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("更新用户状态成功 - 禁用用户")
        void shouldUpdateUserStatusToDisabled() throws Exception {
            User user = createTestUser(1L, "测试用户", "13800000001", 1);

            when(userService.getById(1L)).thenReturn(user);
            when(userService.updateById(any(User.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1/status")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\": 0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新用户状态成功 - 启用用户")
        void shouldUpdateUserStatusToEnabled() throws Exception {
            User user = createTestUser(1L, "测试用户", "13800000001", 0);

            when(userService.getById(1L)).thenReturn(user);
            when(userService.updateById(any(User.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1/status")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\": 1}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新用户状态失败 - 无效状态值")
        void shouldFailUpdateUserStatusWithInvalidStatus() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1/status")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\": 5}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("状态值无效"));
        }

        @Test
        @DisplayName("更新用户状态失败 - 用户不存在")
        void shouldFailUpdateUserStatusWhenUserNotFound() throws Exception {
            when(userService.getById(99L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/99/status")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\": 0}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{id}/delete - 删除用户")
    class DeleteUserTests {

        @Test
        @DisplayName("删除用户成功")
        void shouldDeleteUserSuccessfully() throws Exception {
            User user = createTestUser(1L, "测试用户", "13800000001", 1);

            when(userService.getById(1L)).thenReturn(user);
            when(userService.updateById(any(User.class))).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1/delete")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除用户失败 - 用户不存在")
        void shouldFailDeleteUserWhenNotFound() throws Exception {
            when(userService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/99/delete")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("删除用户失败 - 未授权")
        void shouldFailDeleteUserWhenNotAuthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1/delete"))
                    .andExpect(status().isForbidden());
        }
    }

    private User createTestUser(Long id, String nickname, String phone, Integer status) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}
