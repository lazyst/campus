package com.campus.modules.forum.comment;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.config.TestUtils;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.mapper.CommentMapper;
import com.campus.modules.forum.service.impl.CommentServiceImpl;
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
 * 评论服务单元测试 - JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;

    @BeforeEach
    void setUp() {
        // 注入baseMapper
        TestUtils.setBaseMapper(commentService, commentMapper);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setUserId(1L);
        testComment.setPostId(1L);
        testComment.setParentId(null);
        testComment.setContent("测试评论内容");
        testComment.setStatus(1);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getByPostId 方法测试")
    class GetByPostIdTests {

        @Test
        @DisplayName("根据帖子ID查询评论列表成功")
        void shouldGetCommentsByPostId() {
            Comment replyComment = new Comment();
            replyComment.setId(2L);
            replyComment.setUserId(2L);
            replyComment.setPostId(1L);
            replyComment.setParentId(1L);
            replyComment.setContent("回复评论");
            replyComment.setStatus(1);
            
            List<Comment> comments = Arrays.asList(testComment, replyComment);
            when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(comments);

            List<Comment> result = commentService.getByPostId(1L);

            assertEquals(2, result.size());
            assertEquals("测试评论内容", result.get(0).getContent());
            assertEquals("回复评论", result.get(1).getContent());
            verify(commentMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("帖子无评论时返回空列表")
        void shouldReturnEmptyListWhenNoComments() {
            when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList());

            List<Comment> result = commentService.getByPostId(999L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("查询时只返回状态为1的评论")
        void shouldOnlyReturnActiveComments() {
            when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testComment));

            commentService.getByPostId(1L);

            verify(commentMapper).selectList(argThat(wrapper -> {
                LambdaQueryWrapper<Comment> w = (LambdaQueryWrapper<Comment>) wrapper;
                // 验证查询条件包含 status = 1
                return w.getCustomSqlSegment() != null && w.getCustomSqlSegment().contains("status = 1");
            }));
        }
    }

    @Nested
    @DisplayName("isAuthor 方法测试")
    class IsAuthorTests {

        @Test
        @DisplayName("用户是评论作者")
        void shouldReturnTrueWhenUserIsAuthor() {
            when(commentMapper.selectById(1L)).thenReturn(testComment);

            boolean result = commentService.isAuthor(1L, 1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("用户不是评论作者")
        void shouldReturnFalseWhenUserIsNotAuthor() {
            when(commentMapper.selectById(1L)).thenReturn(testComment);

            boolean result = commentService.isAuthor(1L, 999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("评论不存在时返回false")
        void shouldReturnFalseWhenCommentNotFound() {
            when(commentMapper.selectById(999L)).thenReturn(null);

            boolean result = commentService.isAuthor(999L, 1L);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("IService 基础方法测试")
    class IServiceTests {

        @Test
        @DisplayName("根据ID查询评论")
        void shouldGetById() {
            when(commentMapper.selectById(1L)).thenReturn(testComment);

            Comment result = commentService.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
        }

        @Test
        @DisplayName("查询所有评论")
        void shouldListAllComments() {
            List<Comment> comments = Arrays.asList(testComment);
            when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(comments);

            List<Comment> results = commentService.list();

            assertEquals(1, results.size());
            assertEquals("测试评论内容", results.get(0).getContent());
        }

        @Test
        @DisplayName("删除评论")
        void shouldRemoveById() {
            when(commentMapper.deleteById(1L)).thenReturn(1);

            boolean result = commentService.removeById(1L);

            assertTrue(result);
            verify(commentMapper).deleteById(1L);
        }

        @Test
        @DisplayName("统计评论数量")
        void shouldCountComments() {
            when(commentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);

            long count = commentService.count();

            assertEquals(50L, count);
        }
    }
}
