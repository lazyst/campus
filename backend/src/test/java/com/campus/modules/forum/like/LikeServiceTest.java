package com.campus.modules.forum.like;

import com.campus.config.TestUtils;
import com.campus.modules.forum.entity.Like;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.LikeMapper;
import com.campus.modules.forum.publisher.NotificationPublisher;
import com.campus.modules.forum.service.impl.LikeServiceImpl;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.service.UserService;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 点赞服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LikeServiceTest {

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private PostService postService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private UserService userService;

    @InjectMocks
    private LikeServiceImpl likeService;

    private Like testLike;
    private Post testPost;

    @BeforeEach
    void setUp() {
        TestUtils.setBaseMapper(likeService, likeMapper);

        testLike = new Like();
        testLike.setId(1L);
        testLike.setUserId(1L);
        testLike.setPostId(100L);
        testLike.setDeleted(0);

        testPost = new Post();
        testPost.setId(100L);
        testPost.setUserId(2L); // 帖子作者ID，与点赞用户不同
        testPost.setTitle("测试帖子");
    }

    @Nested
    @DisplayName("hasLiked 方法测试")
    class HasLikedTests {

        @Test
        @DisplayName("用户已点赞时返回true")
        void shouldReturnTrueWhenUserHasLiked() {
            when(likeMapper.selectCount(any())).thenReturn(1L);

            boolean result = likeService.hasLiked(1L, 100L);

            assertTrue(result);
        }

        @Test
        @DisplayName("用户未点赞时返回false")
        void shouldReturnFalseWhenUserHasNotLiked() {
            when(likeMapper.selectCount(any())).thenReturn(0L);

            boolean result = likeService.hasLiked(1L, 100L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("toggleLike 方法测试")
    class ToggleLikeTests {

        @Test
        @DisplayName("添加点赞成功返回true")
        void shouldAddLikeSuccessfully() {
            when(likeMapper.selectOne(any(), eq(true))).thenReturn(null);
            when(likeMapper.insert(any(Like.class))).thenReturn(1);

            boolean result = likeService.toggleLike(1L, 100L);

            assertTrue(result);
            verify(likeMapper).insert(any(Like.class));
            verify(postService).incrementLikeCount(100L);
        }

        @Test
        @DisplayName("取消点赞成功返回false")
        void shouldRemoveLikeSuccessfully() {
            when(likeMapper.selectOne(any(), eq(true))).thenReturn(testLike);
            when(likeMapper.updateById(any(Like.class))).thenReturn(1);

            boolean result = likeService.toggleLike(1L, 100L);

            assertFalse(result);
            verify(likeMapper).updateById(any(Like.class));
            verify(postService).decrementLikeCount(100L);
        }

        @Test
        @DisplayName("点赞自己的帖子不发送通知")
        void shouldNotSendNotificationWhenLikingOwnPost() {
            when(likeMapper.selectOne(any(), eq(true))).thenReturn(null);

            Post ownPost = new Post();
            ownPost.setId(100L);
            ownPost.setUserId(1L);
            when(postService.getById(100L)).thenReturn(ownPost);

            when(likeMapper.insert(any(Like.class))).thenReturn(1);

            boolean result = likeService.toggleLike(1L, 100L);

            assertTrue(result);
            verify(notificationService, never()).save(any());
            verify(notificationPublisher, never()).publish(anyLong(), any());
        }

        @Test
        @DisplayName("点赞他人帖子发送通知")
        void shouldSendNotificationWhenLikingOtherPost() {
            when(likeMapper.selectOne(any(), eq(true))).thenReturn(null);
            when(postService.getById(100L)).thenReturn(testPost);
            when(likeMapper.insert(any(Like.class))).thenReturn(1);
            when(notificationService.save(any())).thenReturn(true);

            boolean result = likeService.toggleLike(1L, 100L);

            assertTrue(result);
            verify(notificationService).save(any());
            verify(notificationPublisher).publish(eq(2L), any());
        }

        @Test
        @DisplayName("恢复已删除的点赞记录")
        void shouldRestoreDeletedLike() {
            Like deletedLike = new Like();
            deletedLike.setId(1L);
            deletedLike.setUserId(1L);
            deletedLike.setPostId(100L);
            deletedLike.setDeleted(1);

            when(likeMapper.selectOne(any(), eq(true)))
                    .thenReturn(null, deletedLike);
            when(likeMapper.updateById(any(Like.class))).thenReturn(1);

            boolean result = likeService.toggleLike(1L, 100L);

            assertTrue(result);
            verify(likeMapper).updateById(any(Like.class));
            verify(likeMapper, never()).insert(any(Like.class));
        }
    }
}
