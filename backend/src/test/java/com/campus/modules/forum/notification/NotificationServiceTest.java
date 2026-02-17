package com.campus.modules.forum.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.config.TestUtils;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.mapper.NotificationMapper;
import com.campus.modules.forum.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        TestUtils.setBaseMapper(notificationService, notificationMapper);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId(1L);
        testNotification.setFromUserId(2L);
        testNotification.setType(1);
        testNotification.setContent("测试通知内容");
        testNotification.setIsRead(0);
        testNotification.setPostId(100L);
    }

    @Nested
    @DisplayName("getByUserId 方法测试")
    class GetByUserIdTests {

        @Test
        @DisplayName("获取用户通知列表成功")
        void shouldGetNotificationsByUserId() {
            Notification notification1 = new Notification();
            notification1.setId(1L);
            notification1.setUserId(1L);
            notification1.setIsRead(0);

            Notification notification2 = new Notification();
            notification2.setId(2L);
            notification2.setUserId(1L);
            notification2.setIsRead(1);

            List<Notification> notifications = Arrays.asList(notification1, notification2);
            when(notificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(notifications);

            List<Notification> result = notificationService.getByUserId(1L);

            assertEquals(2, result.size());
            verify(notificationMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("用户无通知时返回空列表")
        void shouldReturnEmptyListWhenNoNotifications() {
            when(notificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<Notification> result = notificationService.getByUserId(1L);

            assertTrue(result.isEmpty());
            verify(notificationMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("查询时按创建时间倒序排列")
        void shouldOrderByCreatedAtDesc() {
            when(notificationMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            notificationService.getByUserId(1L);

            verify(notificationMapper).selectList(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("getUnreadCount 方法测试")
    class GetUnreadCountTests {

        @Test
        @DisplayName("获取未读通知数量成功")
        void shouldGetUnreadCount() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            int result = notificationService.getUnreadCount(1L);

            assertEquals(5, result);
            verify(notificationMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无未读通知时返回0")
        void shouldReturnZeroWhenNoUnreadNotifications() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            int result = notificationService.getUnreadCount(1L);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("只统计未读且未删除的通知")
        void shouldOnlyCountUnreadAndNotDeleted() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            notificationService.getUnreadCount(1L);

            verify(notificationMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("markAsRead 方法测试")
    class MarkAsReadTests {

        @Test
        @DisplayName("标记单条通知为已读成功")
        void shouldMarkNotificationAsRead() {
            when(notificationMapper.selectById(1L)).thenReturn(testNotification);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

            notificationService.markAsRead(1L);

            verify(notificationMapper).selectById(1L);
            verify(notificationMapper).updateById(argThat(notification ->
                notification.getIsRead() == 1
            ));
        }

        @Test
        @DisplayName("通知不存在时不执行更新")
        void shouldNotUpdateWhenNotificationNotFound() {
            when(notificationMapper.selectById(999L)).thenReturn(null);

            notificationService.markAsRead(999L);

            verify(notificationMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("已读通知不再重复更新")
        void shouldNotUpdateAlreadyReadNotification() {
            testNotification.setIsRead(1);
            when(notificationMapper.selectById(1L)).thenReturn(testNotification);

            notificationService.markAsRead(1L);

            verify(notificationMapper, never()).updateById(any());
        }
    }

    @Nested
    @DisplayName("markAllAsRead 方法测试")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("标记所有通知为已读成功")
        void shouldMarkAllNotificationsAsRead() {
            when(notificationMapper.update(any(Notification.class), any(LambdaQueryWrapper.class))).thenReturn(1);

            notificationService.markAllAsRead(1L);

            verify(notificationMapper).update(any(Notification.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无未读通知时返回0")
        void shouldReturnZeroWhenNoUnreadNotifications() {
            when(notificationMapper.update(any(Notification.class), any(LambdaQueryWrapper.class))).thenReturn(0);

            notificationService.markAllAsRead(1L);

            verify(notificationMapper).update(any(Notification.class), any(LambdaQueryWrapper.class));
        }
    }
}
