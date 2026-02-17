package com.campus.modules.admin.post;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AdminService adminService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private String validAdminToken;

    @BeforeEach
    void setUp() {
        String jwtSecret = "campus-helping-platform-jwt-secret-key-2024-very-long-and-secure-123";
        validAdminToken = io.jsonwebtoken.Jwts.builder()
            .subject("1")
            .claim("role", 1)
            .claim("username", "admin")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000))
            .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(jwtSecret.getBytes()))
            .compact();
        
        when(adminService.getAdminIdFromToken(anyString())).thenReturn(1L);
        when(adminService.isSuperAdmin(anyLong())).thenReturn(true);
    }

    private static final String BASE_URL = "/api/admin/posts";

    private String getAuthHeader() {
        return "Bearer " + validAdminToken;
    }

    @Nested
    @DisplayName("GET /api/admin/posts - 获取帖子列表")
    class GetPostListTests {

        @Test
        @DisplayName("获取帖子列表成功")
        void shouldGetPostListSuccessfully() throws Exception {
            Page<Post> page = new Page<>(1, 20);
            Post post = createTestPost(1L, "测试帖子", "这是测试内容", 1);
            page.setRecords(List.of(post));
            page.setTotal(1);

            when(postService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].title").value("测试帖子"));
        }

        @Test
        @DisplayName("获取帖子列表带板块筛选")
        void shouldGetPostListWithBoardFilter() throws Exception {
            Page<Post> page = new Page<>(1, 20);
            Post post = createTestPost(1L, "测试帖子", "这是测试内容", 1);
            page.setRecords(List.of(post));
            page.setTotal(1);

            when(postService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("boardId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取帖子列表失败 - 未授权")
        void shouldFailGetPostListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/posts/{id} - 更新帖子")
    class UpdatePostStatusTests {

        @Test
        @DisplayName("更新帖子成功")
        void shouldUpdatePostStatusSuccessfully() throws Exception {
            Post post = createTestPost(1L, "测试帖子", "这是测试内容", 1);

            com.campus.modules.forum.dto.PostUpdateRequest request = 
                    new com.campus.modules.forum.dto.PostUpdateRequest();
            request.setTitle("更新后的标题");

            when(postService.getById(1L)).thenReturn(post);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新帖子失败 - 帖子不存在")
        void shouldFailUpdatePostStatusWhenNotFound() throws Exception {
            com.campus.modules.forum.dto.PostUpdateRequest request = 
                    new com.campus.modules.forum.dto.PostUpdateRequest();
            request.setTitle("更新后的标题");

            when(postService.getById(99L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/posts/{id} - 删除帖子")
    class DeletePostTests {

        @Test
        @DisplayName("删除帖子成功")
        void shouldDeletePostSuccessfully() throws Exception {
            Post post = createTestPost(1L, "测试帖子", "这是测试内容", 1);

            when(postService.getById(1L)).thenReturn(post);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除帖子失败 - 帖子不存在")
        void shouldFailDeletePostWhenNotFound() throws Exception {
            when(postService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("删除帖子失败 - 未授权")
        void shouldFailDeletePostWhenNotAuthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }

    private Post createTestPost(Long id, String title, String content, Integer status) {
        Post post = new Post();
        post.setId(id);
        post.setTitle(title);
        post.setContent(content);
        post.setStatus(status);
        post.setBoardId(1L);
        post.setUserId(1L);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        return post;
    }
}
