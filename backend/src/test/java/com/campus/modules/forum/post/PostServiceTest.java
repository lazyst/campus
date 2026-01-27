package com.campus.modules.forum.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.config.TestUtils;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.forum.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 帖子服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        // 注入baseMapper
        TestUtils.setBaseMapper(postService, postMapper);

        testPost = new Post();
        testPost.setId(1L);
        testPost.setUserId(1L);
        testPost.setBoardId(1L);
        testPost.setTitle("测试帖子标题");
        testPost.setContent("测试帖子内容");
        testPost.setImages("[]");
        testPost.setViewCount(10);
        testPost.setLikeCount(5);
        testPost.setCommentCount(3);
        testPost.setCollectCount(2);
        testPost.setStatus(1);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("incrementViewCount 方法测试")
    class IncrementViewCountTests {

        @Test
        @DisplayName("增加浏览次数成功")
        void shouldIncrementViewCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.incrementViewCount(1L);

            assertEquals(11, testPost.getViewCount());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("帖子不存在时不执行操作")
        void shouldDoNothingWhenPostNotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            postService.incrementViewCount(999L);

            verify(postMapper, never()).updateById(any(Post.class));
        }
    }

    @Nested
    @DisplayName("incrementLikeCount 方法测试")
    class IncrementLikeCountTests {

        @Test
        @DisplayName("增加点赞次数成功")
        void shouldIncrementLikeCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.incrementLikeCount(1L);

            assertEquals(6, testPost.getLikeCount());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("帖子不存在时不执行操作")
        void shouldDoNothingWhenPostNotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            postService.incrementLikeCount(999L);

            verify(postMapper, never()).updateById(any(Post.class));
        }
    }

    @Nested
    @DisplayName("decrementLikeCount 方法测试")
    class DecrementLikeCountTests {

        @Test
        @DisplayName("减少点赞次数成功")
        void shouldDecrementLikeCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.decrementLikeCount(1L);

            assertEquals(4, testPost.getLikeCount());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("点赞次数为0时不减少")
        void shouldNotDecrementWhenLikeCountIsZero() {
            testPost.setLikeCount(0);
            when(postMapper.selectById(1L)).thenReturn(testPost);

            postService.decrementLikeCount(1L);

            verify(postMapper, never()).updateById(any(Post.class));
        }

        @Test
        @DisplayName("帖子不存在时不执行操作")
        void shouldDoNothingWhenPostNotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            postService.decrementLikeCount(999L);

            verify(postMapper, never()).updateById(any(Post.class));
        }
    }

    @Nested
    @DisplayName("incrementCommentCount 方法测试")
    class IncrementCommentCountTests {

        @Test
        @DisplayName("增加评论次数成功")
        void shouldIncrementCommentCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.incrementCommentCount(1L);

            assertEquals(4, testPost.getCommentCount());
            verify(postMapper).updateById(testPost);
        }
    }

    @Nested
    @DisplayName("incrementCollectCount 方法测试")
    class IncrementCollectCountTests {

        @Test
        @DisplayName("增加收藏次数成功")
        void shouldIncrementCollectCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.incrementCollectCount(1L);

            assertEquals(3, testPost.getCollectCount());
            verify(postMapper).updateById(testPost);
        }
    }

    @Nested
    @DisplayName("decrementCollectCount 方法测试")
    class DecrementCollectCountTests {

        @Test
        @DisplayName("减少收藏次数成功")
        void shouldDecrementCollectCountSuccessfully() {
            when(postMapper.selectById(1L)).thenReturn(testPost);
            when(postMapper.updateById(any(Post.class))).thenReturn(1);

            postService.decrementCollectCount(1L);

            assertEquals(1, testPost.getCollectCount());
            verify(postMapper).updateById(testPost);
        }

        @Test
        @DisplayName("收藏次数为0时不减少")
        void shouldNotDecrementWhenCollectCountIsZero() {
            testPost.setCollectCount(0);
            when(postMapper.selectById(1L)).thenReturn(testPost);

            postService.decrementCollectCount(1L);

            verify(postMapper, never()).updateById(any(Post.class));
        }
    }

    @Nested
    @DisplayName("isAuthor 方法测试")
    class IsAuthorTests {

        @Test
        @DisplayName("用户是帖子作者")
        void shouldReturnTrueWhenUserIsAuthor() {
            when(postMapper.selectById(1L)).thenReturn(testPost);

            boolean result = postService.isAuthor(1L, 1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("用户不是帖子作者")
        void shouldReturnFalseWhenUserIsNotAuthor() {
            when(postMapper.selectById(1L)).thenReturn(testPost);

            boolean result = postService.isAuthor(1L, 999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("帖子不存在时返回false")
        void shouldReturnFalseWhenPostNotFound() {
            when(postMapper.selectById(999L)).thenReturn(null);

            boolean result = postService.isAuthor(999L, 1L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("IService 基础方法测试")
    class IServiceTests {

        @Test
        @DisplayName("根据ID查询帖子")
        void shouldGetById() {
            when(postMapper.selectById(1L)).thenReturn(testPost);

            Post result = postService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("查询所有帖子")
        void shouldListAllPosts() {
            List<Post> posts = Arrays.asList(testPost);
            when(postMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(posts);

            List<Post> results = postService.list();

            assertEquals(1, results.size());
            assertEquals("测试帖子标题", results.get(0).getTitle());
        }

        @Test
        @DisplayName("删除帖子")
        void shouldRemoveById() {
            when(postMapper.deleteById(1L)).thenReturn(1);

            boolean result = postService.removeById(1L);

            assertTrue(result);
            verify(postMapper).deleteById(1L);
        }
    }
}
