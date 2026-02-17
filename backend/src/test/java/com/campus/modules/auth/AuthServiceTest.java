package com.campus.modules.auth;

import com.campus.config.JwtConfig;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.auth.service.impl.AuthServiceImpl;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private AuthServiceImpl authService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800000000");
        testUser.setNickname("测试用户");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setGender(1);
        testUser.setStatus(1);
    }

    @Nested
    @DisplayName("login 方法测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void shouldLoginSuccessfully() {
            when(userService.getByPhone("13800000000")).thenReturn(testUser);
            when(jwtConfig.generateToken("13800000000")).thenReturn("test-jwt-token");

            String token = authService.login("13800000000", "password123");

            assertNotNull(token);
            assertEquals("test-jwt-token", token);
            verify(jwtConfig).generateToken("13800000000");
        }

        @Test
        @DisplayName("用户不存在时登录失败")
        void shouldFailLoginWhenUserNotFound() {
            when(userService.getByPhone("13999999999")).thenReturn(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login("13999999999", "password123")
            );

            assertEquals("用户不存在", exception.getMessage());
        }

        @Test
        @DisplayName("密码错误时登录失败")
        void shouldFailLoginWhenPasswordWrong() {
            when(userService.getByPhone("13800000000")).thenReturn(testUser);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login("13800000000", "wrongpassword")
            );

            assertEquals("用户名或密码错误", exception.getMessage());
        }

        @Test
        @DisplayName("用户被禁用时登录失败")
        void shouldFailLoginWhenUserDisabled() {
            testUser.setStatus(0);
            when(userService.getByPhone("13800000000")).thenReturn(testUser);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> authService.login("13800000000", "password123")
            );

            assertEquals("用户已被禁用", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("register 方法测试")
    class RegisterTests {

        @Test
        @DisplayName("注册新用户")
        void shouldRegisterNewUser() {
            when(userService.register("13900000000", "password123", "新用户")).thenReturn(testUser);

            User result = authService.register("13900000000", "password123", "新用户");

            assertNotNull(result);
            assertEquals("13800000000", result.getPhone());
            verify(userService).register("13900000000", "password123", "新用户");
        }
    }

    @Nested
    @DisplayName("generateToken 方法测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("生成JWT令牌")
        void shouldGenerateToken() {
            when(jwtConfig.generateToken("13800000000")).thenReturn("jwt-token-123");

            String token = authService.generateToken(testUser);

            assertNotNull(token);
            assertEquals("jwt-token-123", token);
            verify(jwtConfig).generateToken("13800000000");
        }
    }

    @Nested
    @DisplayName("logout 方法测试")
    class LogoutTests {

        @Test
        @DisplayName("登出不执行任何操作")
        void shouldLogoutDoNothing() {
            // JWT是无状态的，登出只需要客户端删除令牌
            assertDoesNotThrow(() -> authService.logout());
        }
    }

    @Nested
    @DisplayName("validateToken 方法测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("验证有效令牌")
        void shouldValidateTokenSuccessfully() {
            when(jwtConfig.getUsernameFromToken("valid-token")).thenReturn("13800000000");
            when(userService.getByPhone("13800000000")).thenReturn(testUser);

            boolean result = authService.validateToken("valid-token");

            assertTrue(result);
        }

        @Test
        @DisplayName("验证令牌时用户不存在返回false")
        void shouldReturnFalseWhenUserNotFound() {
            when(jwtConfig.getUsernameFromToken("token")).thenReturn("13999999999");
            when(userService.getByPhone("13999999999")).thenReturn(null);

            boolean result = authService.validateToken("token");

            assertFalse(result);
        }

        @Test
        @DisplayName("验证令牌时用户被禁用返回false")
        void shouldReturnFalseWhenUserDisabled() {
            testUser.setStatus(0);
            when(jwtConfig.getUsernameFromToken("token")).thenReturn("13800000000");
            when(userService.getByPhone("13800000000")).thenReturn(testUser);

            boolean result = authService.validateToken("token");

            assertFalse(result);
        }

        @Test
        @DisplayName("验证无效令牌返回false")
        void shouldReturnFalseWhenTokenInvalid() {
            when(jwtConfig.getUsernameFromToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

            boolean result = authService.validateToken("invalid-token");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken 方法测试")
    class GetUserIdFromTokenTests {

        @Test
        @DisplayName("从令牌获取用户ID成功")
        void shouldGetUserIdFromToken() {
            when(jwtConfig.getUsernameFromToken("token")).thenReturn("13800000000");
            when(userService.getByPhone("13800000000")).thenReturn(testUser);

            Long userId = authService.getUserIdFromToken("token");

            assertEquals(1L, userId);
        }

        @Test
        @DisplayName("从令牌获取用户ID用户不存在返回null")
        void shouldReturnNullWhenUserNotFound() {
            when(jwtConfig.getUsernameFromToken("token")).thenReturn("13999999999");
            when(userService.getByPhone("13999999999")).thenReturn(null);

            Long userId = authService.getUserIdFromToken("token");

            assertNull(userId);
        }
    }
}
