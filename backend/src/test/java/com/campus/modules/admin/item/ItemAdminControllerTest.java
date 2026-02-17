package com.campus.modules.admin.item;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

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

    private static final String BASE_URL = "/api/admin/items";

    private String getAuthHeader() {
        return "Bearer " + validAdminToken;
    }

    @Nested
    @DisplayName("GET /api/admin/items - 获取物品列表")
    class GetItemListTests {

        @Test
        @DisplayName("获取物品列表成功")
        void shouldGetItemListSuccessfully() throws Exception {
            Page<Item> page = new Page<>(1, 20);
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.records[0].title").value("二手书籍"));
        }

        @Test
        @DisplayName("获取物品列表带状态筛选")
        void shouldGetItemListWithStatusFilter() throws Exception {
            Page<Item> page = new Page<>(1, 20);
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取物品列表失败 - 未授权")
        void shouldFailGetItemListWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/items/{id}/offline - 下架物品")
    class UpdateItemStatusTests {

        @Test
        @DisplayName("下架物品成功")
        void shouldUpdateItemStatusSuccessfully() throws Exception {
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);

            when(itemService.getById(1L)).thenReturn(item);
            when(itemService.updateById(any(Item.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1/offline")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("下架物品失败 - 物品不存在")
        void shouldFailUpdateItemStatusWhenNotFound() throws Exception {
            when(itemService.getById(99L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/99/offline")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/items/{id} - 删除物品")
    class DeleteItemTests {

        @Test
        @DisplayName("删除物品成功")
        void shouldDeleteItemSuccessfully() throws Exception {
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);

            when(itemService.getById(1L)).thenReturn(item);
            when(itemService.updateById(any(Item.class))).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("删除物品失败 - 物品不存在")
        void shouldFailDeleteItemWhenNotFound() throws Exception {
            when(itemService.getById(99L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }

        @Test
        @DisplayName("删除物品失败 - 未授权")
        void shouldFailDeleteItemWhenNotAuthorized() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }

    private Item createTestItem(Long id, String title, BigDecimal price, Integer status) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("这是一本二手书");
        item.setPrice(price);
        item.setStatus(status);
        item.setUserId(1L);
        item.setType(1);
        return item;
    }
}
