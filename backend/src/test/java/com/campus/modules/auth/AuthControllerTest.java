package com.campus.modules.auth;

import com.campus.modules.auth.controller.AuthController;
import com.campus.modules.auth.dto.LoginRequest;
import com.campus.modules.auth.dto.RegisterRequest;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private static final String BASE_URL = "/api/auth";

    @Nested
    @DisplayName("POST /api/auth/register - 用户注册")
    class RegisterTests {

        @Test
        @DisplayName("注册成功")
        void shouldRegisterSuccessfully() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setPhone("13800000001");
            request.setPassword("password123");
            request.setNickname("测试用户");

            User user = new User();
            user.setId(1L);
            user.setPhone("13800000001");
            user.setNickname("测试用户");

            when(authService.register(anyString(), anyString(), anyString())).thenReturn(user);
            when(authService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

            mockMvc.perform(post(BASE_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.phone").value("13800000001"))
                    .andExpect(jsonPath("$.token").value("mock-jwt-token"));
        }

        @Test
        @DisplayName("注册失败 - 手机号格式不正确")
        void shouldFailRegisterWhenPhoneInvalid() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setPhone("12345");
            request.setPassword("password123");
            request.setNickname("测试用户");

            mockMvc.perform(post(BASE_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("注册失败 - 密码为空")
        void shouldFailRegisterWhenPasswordEmpty() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setPhone("13800000001");
            request.setPassword("");
            request.setNickname("测试用户");

            mockMvc.perform(post(BASE_URL + "/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login - 用户登录")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void shouldLoginSuccessfully() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("13800000001");
            request.setPassword("password123");

            when(authService.login(anyString(), anyString())).thenReturn("mock-jwt-token");

            mockMvc.perform(post(BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"))
                    .andExpect(jsonPath("$.data").value("mock-jwt-token"))
                    .andExpect(jsonPath("$.token").value("mock-jwt-token"));
        }

        @Test
        @DisplayName("登录失败 - 手机号格式不正确")
        void shouldFailLoginWhenPhoneInvalid() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("12345");
            request.setPassword("password123");

            mockMvc.perform(post(BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("登录失败 - 密码为空")
        void shouldFailLoginWhenPasswordEmpty() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("13800000001");
            request.setPassword("");

            mockMvc.perform(post(BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("登录失败 - 用户名或密码错误")
        void shouldFailLoginWhenCredentialsInvalid() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setPhone("13800000001");
            request.setPassword("wrongpassword");

            when(authService.login(anyString(), anyString()))
                    .thenThrow(new RuntimeException("用户名或密码错误"));

            mockMvc.perform(post(BASE_URL + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - 用户登出")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void shouldLogoutSuccessfully() throws Exception {
            mockMvc.perform(post(BASE_URL + "/logout")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh - 刷新Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("刷新Token成功")
        void shouldRefreshTokenSuccessfully() throws Exception {
            when(authService.refreshToken(anyString())).thenReturn("new-jwt-token");

            mockMvc.perform(post(BASE_URL + "/refresh")
                    .header("Authorization", "Bearer valid-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("new-jwt-token"))
                    .andExpect(jsonPath("$.token").value("new-jwt-token"));
        }

        @Test
        @DisplayName("刷新Token失败 - 无Authorization头")
        void shouldFailRefreshWhenNoAuthHeader() throws Exception {
            // When Authorization header is missing, Spring throws exception
            // which is handled by GlobalExceptionHandler
            mockMvc.perform(post(BASE_URL + "/refresh")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("刷新Token失败 - Authorization头格式错误")
        void shouldFailRefreshWhenInvalidAuthFormat() throws Exception {
            // Invalid format (no "Bearer " prefix) returns error result
            when(authService.refreshToken(anyString())).thenReturn("new-token");
            
            mockMvc.perform(post(BASE_URL + "/refresh")
                    .header("Authorization", "InvalidFormat")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无效的令牌"));
        }

        @Test
        @DisplayName("刷新Token失败 - Token已过期")
        void shouldFailRefreshWhenTokenExpired() throws Exception {
            when(authService.refreshToken(anyString())).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/refresh")
                    .header("Authorization", "Bearer expired-token")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("令牌已过期，请重新登录"));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/check-phone - 检查手机号")
    class CheckPhoneTests {

        @Test
        @DisplayName("手机号已注册")
        void shouldCheckPhoneRegistered() throws Exception {
            when(authService.isPhoneRegistered("13800000001")).thenReturn(true);

            mockMvc.perform(get(BASE_URL + "/check-phone")
                    .param("phone", "13800000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.registered").value(true))
                    .andExpect(jsonPath("$.data.wasDeleted").value(false));
        }

        @Test
        @DisplayName("手机号未注册")
        void shouldCheckPhoneNotRegistered() throws Exception {
            when(authService.isPhoneRegistered("13900000001")).thenReturn(false);
            when(authService.getDeletedUserByPhone("13900000001")).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/check-phone")
                    .param("phone", "13900000001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.registered").value(false))
                    .andExpect(jsonPath("$.data.wasDeleted").value(false));
        }

        @Test
        @DisplayName("手机号曾注册后被删除")
        void shouldCheckPhoneWasDeleted() throws Exception {
            User deletedUser = new User();
            deletedUser.setPhone("13800000002");
            deletedUser.setNickname("之前的用户");

            when(authService.isPhoneRegistered("13800000002")).thenReturn(false);
            when(authService.getDeletedUserByPhone("13800000002")).thenReturn(deletedUser);

            mockMvc.perform(get(BASE_URL + "/check-phone")
                    .param("phone", "13800000002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.registered").value(false))
                    .andExpect(jsonPath("$.data.wasDeleted").value(true))
                    .andExpect(jsonPath("$.data.previousNickname").value("之前的用户"));
        }
    }
}
