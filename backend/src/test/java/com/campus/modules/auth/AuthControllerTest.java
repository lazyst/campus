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
}
