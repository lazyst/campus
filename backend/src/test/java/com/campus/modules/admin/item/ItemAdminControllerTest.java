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

    @Nested
    @DisplayName("GET /api/admin/items/{id} - 获取物品详情")
    class GetItemDetailTests {

        @Test
        @DisplayName("获取物品详情成功")
        void shouldGetItemDetailSuccessfully() throws Exception {
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);

            when(itemService.getById(1L)).thenReturn(item);

            mockMvc.perform(get(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("二手书籍"));
        }

        @Test
        @DisplayName("获取物品详情失败 - 物品不存在")
        void shouldFailGetItemDetailWhenNotFound() throws Exception {
            when(itemService.getById(99L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/items/{id} - 更新物品")
    class UpdateItemTests {

        @Test
        @DisplayName("更新物品成功")
        void shouldUpdateItemSuccessfully() throws Exception {
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);

            when(itemService.getById(1L)).thenReturn(item);
            when(itemService.updateById(any(Item.class))).thenReturn(true);

            String requestBody = "{\"title\":\"更新后的书籍\",\"price\":30.00}";

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", getAuthHeader())
                    .contentType("application/json")
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新物品失败 - 物品不存在")
        void shouldFailUpdateItemWhenNotFound() throws Exception {
            when(itemService.getById(99L)).thenReturn(null);

            String requestBody = "{\"title\":\"更新后的书籍\"}";

            mockMvc.perform(put(BASE_URL + "/99")
                    .header("Authorization", getAuthHeader())
                    .contentType("application/json")
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/items/{id}/online - 上架物品")
    class OnlineItemTests {

        @Test
        @DisplayName("上架物品成功")
        void shouldOnlineItemSuccessfully() throws Exception {
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 3);

            when(itemService.getById(1L)).thenReturn(item);
            when(itemService.updateById(any(Item.class))).thenReturn(true);

            mockMvc.perform(put(BASE_URL + "/1/online")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("上架物品失败 - 物品不存在")
        void shouldFailOnlineItemWhenNotFound() throws Exception {
            when(itemService.getById(99L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/99/online")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/items/stats - 获取物品统计")
    class GetItemStatsTests {

        @Test
        @DisplayName("获取物品统计成功")
        void shouldGetItemStatsSuccessfully() throws Exception {
            when(itemService.count(any(LambdaQueryWrapper.class)))
                    .thenReturn(100L)
                    .thenReturn(50L)
                    .thenReturn(30L)
                    .thenReturn(20L);

            mockMvc.perform(get(BASE_URL + "/stats")
                    .header("Authorization", getAuthHeader()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(100))
                    .andExpect(jsonPath("$.data.selling").value(50))
                    .andExpect(jsonPath("$.data.completed").value(30))
                    .andExpect(jsonPath("$.data.offline").value(20));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/items - 排序和价格筛选")
    class GetItemListWithSortAndPriceTests {

        @Test
        @DisplayName("按价格升序排序")
        void shouldSortByPriceAsc() throws Exception {
            Page<Item> page = new Page<>(1, 20);
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("sortBy", "price_asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("按浏览量降序排序")
        void shouldSortByViewCountDesc() throws Exception {
            Page<Item> page = new Page<>(1, 20);
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("sortBy", "viewCount_desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("价格区间筛选")
        void shouldFilterByPriceRange() throws Exception {
            Page<Item> page = new Page<>(1, 20);
            Item item = createTestItem(1L, "二手书籍", new BigDecimal("25.00"), 1);
            page.setRecords(List.of(item));
            page.setTotal(1);

            when(itemService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            mockMvc.perform(get(BASE_URL)
                    .header("Authorization", getAuthHeader())
                    .param("minPrice", "10")
                    .param("maxPrice", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
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
