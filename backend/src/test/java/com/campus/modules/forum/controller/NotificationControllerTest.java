package com.campus.modules.forum.controller;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private static final String TEST_TOKEN = generateValidToken();
    
    private static String generateValidToken() {
        String secret = "test-jwt-secret-key-for-testing-purposes-only-2024-very-long";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject("13800000001")
                .claim("role", 0)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    private static final String BASE_URL = "/api/notifications";

    @Nested
    @DisplayName("GET /api/notifications - 获取通知列表")
    class GetNotificationsTests {

        @Test
        @DisplayName("获取通知列表成功")
        void shouldGetNotificationsSuccessfully() throws Exception {
            Notification notification = createTestNotification(1L, 1L, 2L, 1, "有人评论了你的帖子");
            User fromUser = createTestUser(2L, "评论者昵称", "http://avatar.url/2.png");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getByUserId(1L)).thenReturn(Collections.singletonList(notification));
            when(userService.getById(2L)).thenReturn(fromUser);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].content").value("有人评论了你的帖子"));

            verify(authService).getUserIdFromAuthHeader(anyString());
            verify(notificationService).getByUserId(1L);
        }

        @Test
        @DisplayName("获取通知列表 - 多个通知")
        void shouldGetMultipleNotifications() throws Exception {
            Notification notification1 = createTestNotification(1L, 1L, 2L, 1, "评论通知");
            Notification notification2 = createTestNotification(2L, 1L, 3L, 2, "点赞通知");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getByUserId(1L)).thenReturn(Arrays.asList(notification1, notification2));
            when(userService.getById(2L)).thenReturn(createTestUser(2L, "用户A", null));
            when(userService.getById(3L)).thenReturn(createTestUser(3L, "用户B", null));

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(notificationService).getByUserId(1L);
        }

        @Test
        @DisplayName("获取通知列表 - 空列表")
        void shouldReturnEmptyNotificationsList() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getByUserId(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));

            verify(notificationService).getByUserId(1L);
        }

        @Test
        @DisplayName("获取通知列表 - 触发用户不存在")
        void shouldHandleMissingFromUser() throws Exception {
            Notification notification = createTestNotification(1L, 1L, 999L, 1, "通知内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getByUserId(1L)).thenReturn(Collections.singletonList(notification));
            when(userService.getById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].fromUserNickname").doesNotExist());

            verify(notificationService).getByUserId(1L);
        }
    }

    // ==================== GET /api/notifications/unread/count - 获取未读数量 ====================

    @Nested
    @DisplayName("GET /api/notifications/unread/count - 获取未读通知数量")
    class GetUnreadCountTests {

        @Test
        @DisplayName("获取未读数量成功")
        void shouldGetUnreadCountSuccessfully() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getUnreadCount(1L)).thenReturn(5);

            mockMvc.perform(get(BASE_URL + "/unread/count")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.count").value(5));

            verify(notificationService).getUnreadCount(1L);
        }

        @Test
        @DisplayName("获取未读数量 - 无未读")
        void shouldReturnZeroWhenNoUnread() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getUnreadCount(1L)).thenReturn(0);

            mockMvc.perform(get(BASE_URL + "/unread/count")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.count").value(0));

            verify(notificationService).getUnreadCount(1L);
        }
    }

    // ==================== PUT /api/notifications/{id}/read - 标记单条已读 ====================

    @Nested
    @DisplayName("PUT /api/notifications/{id}/read - 标记通知为已读")
    class MarkAsReadTests {

        @Test
        @DisplayName("标记通知为已读成功")
        void shouldMarkAsReadSuccessfully() throws Exception {
            Notification notification = createTestNotification(1L, 1L, 2L, 1, "通知内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getById(1L)).thenReturn(notification);
            doNothing().when(notificationService).markAsRead(1L);

            mockMvc.perform(put(BASE_URL + "/1/read")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));

            verify(notificationService).markAsRead(1L);
        }

        @Test
        @DisplayName("标记已读失败 - 通知不存在")
        void shouldFailMarkAsReadWhenNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getById(999L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/999/read")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("通知不存在"));

            verify(notificationService, never()).markAsRead(anyLong());
        }

        @Test
        @DisplayName("标记已读失败 - 无权限")
        void shouldFailMarkAsReadWhenNoPermission() throws Exception {
            Notification notification = createTestNotification(1L, 999L, 2L, 1, "通知内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(notificationService.getById(1L)).thenReturn(notification);

            mockMvc.perform(put(BASE_URL + "/1/read")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("通知不存在"));

            verify(notificationService, never()).markAsRead(anyLong());
        }
    }

    // ==================== PUT /api/notifications/read/all - 标记全部已读 ====================

    @Nested
    @DisplayName("PUT /api/notifications/read/all - 标记所有通知为已读")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("标记所有通知为已读成功")
        void shouldMarkAllAsReadSuccessfully() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            doNothing().when(notificationService).markAllAsRead(1L);

            mockMvc.perform(put(BASE_URL + "/read/all")
                    .header("Authorization", TEST_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));

            verify(notificationService).markAllAsRead(1L);
        }
    }

    // ==================== Helper Methods ====================

    private Notification createTestNotification(Long id, Long userId, Long fromUserId, Integer type, String content) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setFromUserId(fromUserId);
        notification.setType(type);
        notification.setContent(content);
        notification.setIsRead(0);
        notification.setPostId(null);
        notification.setCommentId(null);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        return notification;
    }

    private User createTestUser(Long id, String nickname, String avatar) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        return user;
    }
}
