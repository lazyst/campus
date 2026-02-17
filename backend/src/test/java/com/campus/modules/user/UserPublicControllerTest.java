package com.campus.modules.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.user.controller.UserPublicController;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private PostMapper postMapper;

    @MockBean
    private ItemMapper itemMapper;

    private static final String BASE_URL = "/api/users";

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setAvatar("/uploads/avatars/test.jpg");
        user.setBio("这是我的个人简介");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("GET /api/users/{id} - 获取用户公开信息")
    class GetUserProfileTests {

        @Test
        @DisplayName("获取用户公开信息成功")
        void shouldGetUserProfileSuccessfully() throws Exception {
            User user = createTestUser();
            when(userMapper.selectById(1L)).thenReturn(user);

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("测试用户"))
                    .andExpect(jsonPath("$.data.bio").value("这是我的个人简介"));
        }

        @Test
        @DisplayName("用户不存在时返回错误")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            when(userMapper.selectById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}/posts - 获取用户的帖子列表")
    class GetUserPostsTests {

        @Test
        @DisplayName("用户不存在时返回错误")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            when(userMapper.selectById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("获取空帖子列表")
        void shouldGetEmptyPostsList() throws Exception {
            User user = createTestUser();
            when(userMapper.selectById(1L)).thenReturn(user);

            Page<Post> postPage = new Page<>(1, 10);
            postPage.setRecords(new ArrayList<>());

            when(postMapper.selectPage(any(Page.class), any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class))).thenReturn(postPage);

            mockMvc.perform(get(BASE_URL + "/1/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}/items - 获取用户的物品列表")
    class GetUserItemsTests {

        @Test
        @DisplayName("用户不存在时返回错误")
        void shouldReturnErrorWhenUserNotFound() throws Exception {
            when(userMapper.selectById(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("获取空物品列表")
        void shouldGetEmptyItemsList() throws Exception {
            User user = createTestUser();
            when(userMapper.selectById(1L)).thenReturn(user);

            Page<Item> itemPage = new Page<>(1, 10);
            itemPage.setRecords(new ArrayList<>());

            when(itemMapper.selectPage(any(Page.class), any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class))).thenReturn(itemPage);

            mockMvc.perform(get(BASE_URL + "/1/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records").isEmpty());
        }
    }
}
