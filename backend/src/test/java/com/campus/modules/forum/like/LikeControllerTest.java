package com.campus.modules.forum.like;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.controller.LikeController;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.LikeService;
import com.campus.modules.forum.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LikeController 单元测试
 * 使用@SpringBootTest + MockService模式
 */
@SpringBootTest
@AutoConfigureMockMvc
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthService authService;

    private static final String BASE_URL = "/api/posts";

    @Nested
    @DisplayName("POST /api/posts/{postId}/like - 点赞/取消点赞帖子")
    class ToggleLikeTests {

        @Test
        @DisplayName("点赞成功 - 首次点赞")
        void shouldToggleLikeSuccessfully_FirstLike() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(post);
            when(likeService.toggleLike(1L, 1L)).thenReturn(true);

            mockMvc.perform(post(BASE_URL + "/1/like")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("点赞成功 - 取消点赞")
        void shouldToggleLikeSuccessfully_UnLike() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(post);
            when(likeService.toggleLike(1L, 1L)).thenReturn(false);

            mockMvc.perform(post(BASE_URL + "/1/like")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("点赞失败 - 未登录")
        void shouldFailToggleLikeWhenNotLoggedIn() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/like"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("请先登录后再点赞"));
        }

        @Test
        @DisplayName("点赞失败 - 帖子不存在")
        void shouldFailToggleLikeWhenPostNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/999/like")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("点赞失败 - Token无效")
        void shouldFailToggleLikeWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/1/like")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/like/check - 检查用户是否已点赞")
    class CheckLikeTests {

        @Test
        @DisplayName("检查点赞状态成功 - 已点赞")
        void shouldCheckLikedSuccessfully_True() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(likeService.hasLiked(1L, 1L)).thenReturn(true);

            mockMvc.perform(get(BASE_URL + "/1/like/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("检查点赞状态成功 - 未点赞")
        void shouldCheckLikedSuccessfully_False() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(likeService.hasLiked(1L, 1L)).thenReturn(false);

            mockMvc.perform(get(BASE_URL + "/1/like/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("检查点赞状态成功 - 未登录返回false")
        void shouldCheckLikedSuccessfully_NotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1/like/check"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("检查点赞状态成功 - Token无效返回false")
        void shouldCheckLikedSuccessfully_InvalidToken() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1/like/check")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    private Post createTestPost(long id, long userId, String title) {
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
}
