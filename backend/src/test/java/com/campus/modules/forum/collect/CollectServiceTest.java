package com.campus.modules.forum.collect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.modules.forum.entity.CollectSimple;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.CollectSimpleMapper;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.forum.service.impl.CollectServiceImpl;
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
 * 收藏服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CollectServiceTest {

    @Mock
    private PostService postService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CollectSimpleMapper collectSimpleMapper;

    @InjectMocks
    private CollectServiceImpl collectService;

    private CollectSimple testCollect;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testCollect = new CollectSimple();
        testCollect.setId(1L);
        testCollect.setUserId(1L);
        testCollect.setPostId(100L);

        testPost = new Post();
        testPost.setId(100L);
        testPost.setUserId(2L);
        testPost.setTitle("测试帖子");
        testPost.setCollectCount(0);
    }

    @Nested
    @DisplayName("hasCollected 方法测试")
    class HasCollectedTests {

        @Test
        @DisplayName("用户已收藏时返回true")
        void shouldReturnTrueWhenUserHasCollected() {
            when(collectSimpleMapper.selectCount(any())).thenReturn(1L);

            boolean result = collectService.hasCollected(1L, 100L);

            assertTrue(result);
            verify(collectSimpleMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("用户未收藏时返回false")
        void shouldReturnFalseWhenUserHasNotCollected() {
            when(collectSimpleMapper.selectCount(any())).thenReturn(0L);

            boolean result = collectService.hasCollected(1L, 100L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("toggleCollect 方法测试")
    class ToggleCollectTests {

        @Test
        @DisplayName("添加收藏成功返回true")
        void shouldAddCollectSuccessfully() {
            when(collectSimpleMapper.selectOne(any())).thenReturn(null);
            when(collectSimpleMapper.insert(any(CollectSimple.class))).thenReturn(1);
            when(postService.getById(100L)).thenReturn(testPost);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            boolean result = collectService.toggleCollect(1L, 100L);

            assertTrue(result);
            verify(collectSimpleMapper).insert(any(CollectSimple.class));
            verify(postService).updateById(any(Post.class));
        }

        @Test
        @DisplayName("取消收藏成功返回false")
        void shouldRemoveCollectSuccessfully() {
            when(collectSimpleMapper.selectOne(any())).thenReturn(testCollect);
            when(collectSimpleMapper.deleteById(1L)).thenReturn(1);
            when(postService.getById(100L)).thenReturn(testPost);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            boolean result = collectService.toggleCollect(1L, 100L);

            assertFalse(result);
            verify(collectSimpleMapper).deleteById(1L);
        }

        @Test
        @DisplayName("取消收藏时帖子收藏数减少")
        void shouldDecrementCollectCountWhenRemoving() {
            testPost.setCollectCount(5);
            when(collectSimpleMapper.selectOne(any())).thenReturn(testCollect);
            when(collectSimpleMapper.deleteById(1L)).thenReturn(1);
            when(postService.getById(100L)).thenReturn(testPost);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            boolean result = collectService.toggleCollect(1L, 100L);

            assertFalse(result);
            assertEquals(4, testPost.getCollectCount());
        }

        @Test
        @DisplayName("收藏他人帖子发送通知")
        void shouldSendNotificationWhenCollectingOtherPost() {
            when(collectSimpleMapper.selectOne(any())).thenReturn(null);
            when(collectSimpleMapper.insert(any(CollectSimple.class))).thenReturn(1);
            when(postService.getById(100L)).thenReturn(testPost);
            when(postService.updateById(any(Post.class))).thenReturn(true);
            when(notificationService.save(any(Notification.class))).thenReturn(true);

            boolean result = collectService.toggleCollect(1L, 100L);

            assertTrue(result);
            verify(notificationService).save(any(Notification.class));
        }

        @Test
        @DisplayName("收藏自己帖子不发送通知")
        void shouldNotSendNotificationWhenCollectingOwnPost() {
            Post ownPost = new Post();
            ownPost.setId(100L);
            ownPost.setUserId(1L);
            ownPost.setTitle("我的帖子");
            ownPost.setCollectCount(0);

            when(collectSimpleMapper.selectOne(any())).thenReturn(null);
            when(collectSimpleMapper.insert(any(CollectSimple.class))).thenReturn(1);
            when(postService.getById(100L)).thenReturn(ownPost);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            boolean result = collectService.toggleCollect(1L, 100L);

            assertTrue(result);
            verify(notificationService, never()).save(any());
        }
    }
}
