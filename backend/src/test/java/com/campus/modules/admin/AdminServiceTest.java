package com.campus.modules.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.modules.admin.entity.Admin;
import com.campus.modules.admin.mapper.AdminMapper;
import com.campus.modules.admin.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminServiceTest {

    @Mock
    private AdminMapper adminMapper;

    @Spy
    @InjectMocks
    private AdminServiceImpl adminService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private Admin testAdmin;
    private static final String JWT_SECRET = "test-jwt-secret-key-for-testing-purposes-only-2024-very-long";

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(adminService, "jwtSecret", JWT_SECRET);
        
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(adminService, adminMapper);
        
        testAdmin = new Admin();
        testAdmin.setId(1L);
        testAdmin.setUsername("admin");
        testAdmin.setPassword(passwordEncoder.encode("admin123"));
        testAdmin.setNickname("超级管理员");
        testAdmin.setRole(1);
        testAdmin.setStatus(1);
    }

    @Nested
    @DisplayName("getByUsername 方法测试")
    class GetByUsernameTests {

        @Test
        @DisplayName("根据用户名查询管理员成功")
        void shouldGetAdminByUsername() {
            doReturn(testAdmin).when(adminService).getOne(any());

            Admin result = adminService.getByUsername("admin");

            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            assertEquals("超级管理员", result.getNickname());
            verify(adminService).getOne(any());
        }

        @Test
        @DisplayName("根据不存在的用户名查询返回null")
        void shouldReturnNullWhenUsernameNotExists() {
            doReturn(null).when(adminService).getOne(any());

            Admin result = adminService.getByUsername("nonexistent");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("login 方法测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功返回token")
        void shouldLoginSuccessfully() {
            doReturn(testAdmin).when(adminService).getOne(any());
            doReturn(true).when(adminService).updateById(any(Admin.class));

            String token = adminService.login("admin", "admin123");

            assertNotNull(token);
            assertTrue(token.length() > 0);
            verify(adminService).updateById(any(Admin.class));
        }

        @Test
        @DisplayName("管理员不存在时抛出异常")
        void shouldThrowExceptionWhenAdminNotExists() {
            doReturn(null).when(adminService).getOne(any());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> adminService.login("nonexistent", "password")
            );

            assertEquals("管理员不存在", exception.getMessage());
        }

        @Test
        @DisplayName("管理员被禁用时抛出异常")
        void shouldThrowExceptionWhenAdminIsDisabled() {
            Admin disabledAdmin = new Admin();
            disabledAdmin.setId(1L);
            disabledAdmin.setUsername("admin");
            disabledAdmin.setPassword(passwordEncoder.encode("admin123"));
            disabledAdmin.setRole(1);
            disabledAdmin.setStatus(0);
            
            doReturn(disabledAdmin).when(adminService).getOne(any());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> adminService.login("admin", "admin123")
            );

            assertEquals("管理员已被禁用", exception.getMessage());
        }

        @Test
        @DisplayName("密码错误时抛出异常")
        void shouldThrowExceptionWhenPasswordIsWrong() {
            doReturn(testAdmin).when(adminService).getOne(any());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> adminService.login("admin", "wrongpassword")
            );

            assertEquals("密码错误", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("isSuperAdmin 方法测试")
    class IsSuperAdminTests {

        @Test
        @DisplayName("超级管理员返回true")
        void shouldReturnTrueForSuperAdmin() {
            doReturn(testAdmin).when(adminService).getById(1L);

            boolean result = adminService.isSuperAdmin(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("普通管理员返回false")
        void shouldReturnFalseForRegularAdmin() {
            Admin regularAdmin = new Admin();
            regularAdmin.setId(1L);
            regularAdmin.setRole(2);
            doReturn(regularAdmin).when(adminService).getById(1L);

            boolean result = adminService.isSuperAdmin(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("管理员不存在返回false")
        void shouldReturnFalseWhenAdminNotExists() {
            doReturn(null).when(adminService).getById(999L);

            boolean result = adminService.isSuperAdmin(999L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("getAdminIdFromToken 方法测试")
    class GetAdminIdFromTokenTests {

        @Test
        @DisplayName("从有效token中获取管理员ID成功")
        void shouldGetAdminIdFromValidToken() {
            doReturn(testAdmin).when(adminService).getOne(any());
            doReturn(true).when(adminService).updateById(any(Admin.class));

            String token = adminService.login("admin", "admin123");
            assertNotNull(token);

            Long adminId = adminService.getAdminIdFromToken(token);

            assertEquals(1L, adminId);
        }

        @Test
        @DisplayName("无效token返回null")
        void shouldReturnNullForInvalidToken() {
            Long adminId = adminService.getAdminIdFromToken("invalid.token.string");

            assertNull(adminId);
        }

        @Test
        @DisplayName("空token返回null")
        void shouldReturnNullForEmptyToken() {
            Long adminId = adminService.getAdminIdFromToken("");

            assertNull(adminId);
        }
    }
}
