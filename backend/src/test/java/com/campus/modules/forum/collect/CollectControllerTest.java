package com.campus.modules.forum.collect;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.controller.CollectController;
import com.campus.modules.forum.entity.Collect;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CollectController 单元测试
 * 使用@SpringBootTest + MockService模式
 */
@SpringBootTest
@AutoConfigureMockMvc
class CollectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectService collectService;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private static final String BASE_URL = "/api/posts";

    @Nested
    @DisplayName("POST /api/posts/{postId}/collect - 收藏/取消收藏帖子")
    class ToggleCollectTests {

        @Test
        @DisplayName("收藏成功 - 首次收藏")
        void shouldToggleCollectSuccessfully_FirstCollect() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(post);
            when(collectService.toggleCollect(1L, 1L)).thenReturn(true);

            mockMvc.perform(post(BASE_URL + "/1/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("收藏成功 - 取消收藏")
        void shouldToggleCollectSuccessfully_UnCollect() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(post);
            when(collectService.toggleCollect(1L, 1L)).thenReturn(false);

            mockMvc.perform(post(BASE_URL + "/1/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("收藏失败 - 未登录")
        void shouldFailToggleCollectWhenNotLoggedIn() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/collect"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("请先登录后再收藏"));
        }

        @Test
        @DisplayName("收藏失败 - 帖子不存在")
        void shouldFailToggleCollectWhenPostNotFound() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/999/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("收藏失败 - Token无效")
        void shouldFailToggleCollectWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/1/collect")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/collect/check - 检查用户是否已收藏")
    class CheckCollectTests {

        @Test
        @DisplayName("检查收藏状态成功 - 已收藏")
        void shouldCheckCollectedSuccessfully_True() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(collectService.hasCollected(1L, 1L)).thenReturn(true);

            mockMvc.perform(get(BASE_URL + "/1/collect/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("检查收藏状态成功 - 未收藏")
        void shouldCheckCollectedSuccessfully_False() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(collectService.hasCollected(1L, 1L)).thenReturn(false);

            mockMvc.perform(get(BASE_URL + "/1/collect/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("检查收藏状态成功 - 未登录返回false")
        void shouldCheckCollectedSuccessfully_NotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1/collect/check"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("检查收藏状态成功 - Token无效返回false")
        void shouldCheckCollectedSuccessfully_InvalidToken() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1/collect/check")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/collections - 获取当前用户的收藏列表")
    class GetMyCollectionsTests {

        @Test
        @DisplayName("获取收藏列表成功")
        void shouldGetMyCollectionsSuccessfully() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");
            User user = createTestUser(2L, "帖子作者", "http://example.com/avatar.jpg");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(collectService.getCollectedPosts(1L)).thenReturn(Collections.singletonList(post));
            when(userService.listByIds(any())).thenReturn(Collections.singletonList(user));

            mockMvc.perform(get(BASE_URL + "/collections")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].title").value("测试帖子"));
        }

        @Test
        @DisplayName("获取收藏列表成功 - 空列表")
        void shouldGetMyCollectionsSuccessfully_EmptyList() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(collectService.getCollectedPosts(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/collections")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("获取收藏列表失败 - 未登录")
        void shouldFailGetMyCollectionsWhenNotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/collections"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("请先登录"));
        }

        @Test
        @DisplayName("获取收藏列表失败 - Token无效")
        void shouldFailGetMyCollectionsWhenTokenInvalid() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/collections")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("获取收藏列表成功 - 带用户信息")
        void shouldGetMyCollectionsWithUserInfo() throws Exception {
            Post post = createTestPost(1L, 2L, "测试帖子");
            User user = createTestUser(2L, "帖子作者", "http://example.com/avatar.jpg");

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(collectService.getCollectedPosts(1L)).thenReturn(Collections.singletonList(post));
            when(userService.listByIds(any())).thenReturn(Collections.singletonList(user));

            mockMvc.perform(get(BASE_URL + "/collections")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].userNickname").value("帖子作者"))
                    .andExpect(jsonPath("$.data[0].userAvatar").value("http://example.com/avatar.jpg"));
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

    private User createTestUser(long id, String nickname, String avatar) {
        User user = new User();
        user.setId(id);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setPhone("13800000000");
        return user;
    }
}
