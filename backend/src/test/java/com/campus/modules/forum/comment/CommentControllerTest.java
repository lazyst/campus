package com.campus.modules.forum.comment;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.CommentCreateRequest;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.publisher.NotificationPublisher;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
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

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthService authService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationPublisher notificationPublisher;

    @MockBean
    private UserService userService;

    private static final String BASE_URL = "/api/comments";

    @Nested
    @DisplayName("GET /api/comments/post/{postId} - 获取帖子的评论列表")
    class GetCommentsByPostTests {

        @Test
        @DisplayName("获取帖子评论列表成功")
        void shouldGetCommentsByPostSuccessfully() throws Exception {
            Comment comment = createTestComment(1L, 1L, 1L, "测试评论内容");

            when(commentService.getByPostId(1L)).thenReturn(Collections.singletonList(comment));

            User user = createTestUser(1L, "测试用户", "http://example.com/avatar.jpg");
            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/post/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].content").value("测试评论内容"));
        }

        @Test
        @DisplayName("获取帖子评论列表成功 - 带用户信息")
        void shouldGetCommentsWithUserInfo() throws Exception {
            Comment comment = createTestComment(1L, 1L, 1L, "测试评论内容");

            when(commentService.getByPostId(1L)).thenReturn(Collections.singletonList(comment));

            User user = createTestUser(1L, "测试用户", "http://example.com/avatar.jpg");
            when(userService.getById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/post/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].userNickname").value("测试用户"))
                    .andExpect(jsonPath("$.data[0].userAvatar").value("http://example.com/avatar.jpg"));
        }

        @Test
        @DisplayName("获取帖子评论列表成功 - 空列表")
        void shouldGetEmptyCommentsList() throws Exception {
            when(commentService.getByPostId(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/post/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/comments - 创建评论")
    class CreateCommentTests {

        @Test
        @DisplayName("创建评论成功")
        void shouldCreateCommentSuccessfully() throws Exception {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setPostId(1L);
            request.setContent("新评论内容");

            Post post = createTestPost(1L, 2L, "测试帖子");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(post);
            when(commentService.save(any(Comment.class))).thenReturn(true);

            Comment savedComment = createTestComment(1L, 1L, 1L, "新评论内容");
            savedComment.setId(1L);
            when(commentService.getById(1L)).thenReturn(savedComment);

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("新评论内容"));
        }

        @Test
        @DisplayName("创建评论失败 - 未登录")
        void shouldFailCreateCommentWhenNotLoggedIn() throws Exception {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setPostId(1L);
            request.setContent("评论内容");

            mockMvc.perform(post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("请先登录后再评论"));
        }

        @Test
        @DisplayName("创建评论失败 - 帖子不存在")
        void shouldFailCreateCommentWhenPostNotFound() throws Exception {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setPostId(999L);
            request.setContent("评论内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("创建评论失败 - 评论内容为空")
        void shouldFailCreateCommentWhenContentEmpty() throws Exception {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setPostId(1L);
            request.setContent("");

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/comments/{id} - 删除评论")
    class DeleteCommentTests {

        @Test
        @DisplayName("删除评论成功")
        void shouldDeleteCommentSuccessfully() throws Exception {
            Comment existingComment = createTestComment(1L, 1L, 1L, "测试评论");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(commentService.getById(1L)).thenReturn(existingComment);
            when(commentService.isAuthor(1L, 1L)).thenReturn(true);
            when(commentService.updateById(any(Comment.class))).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("删除评论失败 - 评论不存在")
        void shouldFailDeleteWhenCommentNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(commentService.getById(999L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/999")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("评论不存在"));
        }

        @Test
        @DisplayName("删除评论失败 - 非作者无权删除")
        void shouldFailDeleteWhenNotAuthor() throws Exception {
            Comment existingComment = createTestComment(1L, 1L, 1L, "测试评论");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(999L);
            when(commentService.getById(1L)).thenReturn(existingComment);
            when(commentService.isAuthor(1L, 999L)).thenReturn(false);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只能删除自己的评论"));
        }
    }

    private Comment createTestComment(Long id, Long userId, Long postId, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setUserId(userId);
        comment.setPostId(postId);
        comment.setContent(content);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
    }

    private Post createTestPost(Long id, Long userId, String title) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent("测试内容");
        post.setStatus(1);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    private User createTestUser(Long id, String nickname, String avatar) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setPhone("13800000000");
        return user;
    }
}
