package com.campus.modules.forum.post;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.PostCreateRequest;
import com.campus.modules.forum.dto.PostUpdateRequest;
import com.campus.modules.forum.entity.Board;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.forum.service.PostService;
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
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private BoardService boardService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private static final String BASE_URL = "/api/posts";

    @Nested
    @DisplayName("GET /api/posts - 分页获取帖子列表")
    class GetPostsListTests {

        @Test
        @DisplayName("获取帖子列表成功")
        void shouldGetPostsListSuccessfully() throws Exception {
            Post post = createTestPost(1L, 1L, "测试帖子", "测试内容");
            Page<Post> postPage = new Page<>(1, 10);
            postPage.setRecords(Collections.singletonList(post));
            postPage.setTotal(1);

            lenient().when(postService.page(any(Page.class), any())).thenReturn(postPage);
            lenient().when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("按板块获取帖子列表成功")
        void shouldGetPostsListByBoardId() throws Exception {
            Post post = createTestPost(1L, 1L, "测试帖子", "测试内容");
            Page<Post> postPage = new Page<>(1, 10);
            postPage.setRecords(Collections.singletonList(post));
            postPage.setTotal(1);

            when(postService.page(any(Page.class), any())).thenReturn(postPage);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                    .param("boardId", "1")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取空帖子列表")
        void shouldGetEmptyPostsList() throws Exception {
            Page<Post> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(postService.page(any(Page.class), any())).thenReturn(emptyPage);

            mockMvc.perform(get(BASE_URL)
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{id} - 获取帖子详情")
    class GetPostDetailTests {

        @Test
        @DisplayName("获取帖子详情成功")
        void shouldGetPostDetailSuccessfully() throws Exception {
            Post post = createTestPost(1L, 1L, "测试帖子", "测试内容");
            post.setStatus(1);

            when(postService.getById(1L)).thenReturn(post);
            doNothing().when(postService).incrementViewCount(1L);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("测试帖子"));
        }

        @Test
        @DisplayName("获取帖子详情失败 - 帖子不存在")
        void shouldFailGetPostWhenNotFound() throws Exception {
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在或已被删除"));
        }

        @Test
        @DisplayName("获取帖子详情失败 - 帖子已删除")
        void shouldFailGetPostWhenDeleted() throws Exception {
            Post post = createTestPost(1L, 1L, "测试帖子", "测试内容");
            post.setStatus(0);

            when(postService.getById(1L)).thenReturn(post);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在或已被删除"));
        }
    }

    @Nested
    @DisplayName("POST /api/posts - 创建帖子")
    class CreatePostTests {

        @Test
        @DisplayName("创建帖子成功")
        void shouldCreatePostSuccessfully() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setBoardId(1L);
            request.setTitle("新帖子标题");
            request.setContent("新帖子内容");
            request.setImages("[]");

            Board board = new Board();
            board.setId(1L);
            board.setName("测试板块");

            Post savedPost = createTestPost(1L, 1L, "新帖子标题", "新帖子内容");
            savedPost.setId(1L);

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(boardService.getById(1L)).thenReturn(board);
            when(postService.save(any(Post.class))).thenReturn(true);
            when(postService.getById(1L)).thenReturn(savedPost);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("新帖子标题"));
        }

        @Test
        @DisplayName("创建帖子失败 - 板块不存在")
        void shouldFailCreatePostWhenBoardNotFound() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setBoardId(999L);
            request.setTitle("新帖子标题");
            request.setContent("新帖子内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(boardService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("板块不存在"));
        }

        @Test
        @DisplayName("创建帖子失败 - 标题为空")
        void shouldFailCreatePostWhenTitleEmpty() throws Exception {
            PostCreateRequest request = new PostCreateRequest();
            request.setBoardId(1L);
            request.setTitle("");
            request.setContent("测试内容");

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/posts/{id} - 更新帖子")
    class UpdatePostTests {

        @Test
        @DisplayName("更新帖子成功")
        void shouldUpdatePostSuccessfully() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("更新后的标题");
            request.setContent("更新后的内容");

            Post existingPost = createTestPost(1L, 1L, "原标题", "原内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(existingPost);
            when(postService.isAuthor(1L, 1L)).thenReturn(true);
            when(postService.updateById(any(Post.class))).thenReturn(true);
            when(postService.getById(1L)).thenReturn(existingPost);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新帖子失败 - 帖子不存在")
        void shouldFailUpdateWhenPostNotFound() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("更新后的标题");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/999")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("更新帖子失败 - 非作者无权更新")
        void shouldFailUpdateWhenNotAuthor() throws Exception {
            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("更新后的标题");

            Post existingPost = createTestPost(1L, 1L, "原标题", "原内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(999L);
            when(postService.getById(1L)).thenReturn(existingPost);
            when(postService.isAuthor(1L, 999L)).thenReturn(false);

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只能编辑自己的帖子"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{id} - 删除帖子")
    class DeletePostTests {

        @Test
        @DisplayName("删除帖子成功")
        void shouldDeletePostSuccessfully() throws Exception {
            Post existingPost = createTestPost(1L, 1L, "测试帖子", "测试内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(1L)).thenReturn(existingPost);
            when(postService.isAuthor(1L, 1L)).thenReturn(true);
            when(postService.updateById(any(Post.class))).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("删除帖子失败 - 帖子不存在")
        void shouldFailDeleteWhenPostNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(postService.getById(999L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/999")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("帖子不存在"));
        }

        @Test
        @DisplayName("删除帖子失败 - 非作者无权删除")
        void shouldFailDeleteWhenNotAuthor() throws Exception {
            Post existingPost = createTestPost(1L, 1L, "测试帖子", "测试内容");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(999L);
            when(postService.getById(1L)).thenReturn(existingPost);
            when(postService.isAuthor(1L, 999L)).thenReturn(false);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("只能删除自己的帖子"));
        }
    }

    private Post createTestPost(Long id, Long userId, String title, String content) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setBoardId(1L);
        post.setTitle(title);
        post.setContent(content);
        post.setImages("[]");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setStatus(1);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }
}
