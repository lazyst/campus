package com.campus.modules.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.chat.service.ChatService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.service.ItemService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private ChatService chatService;

    private static final String BASE_URL = "/api/items";

    @Nested
    @DisplayName("GET /api/items - 获取物品列表")
    class GetItemsListTests {

        @Test
        @DisplayName("获取物品列表成功")
        void shouldGetItemsListSuccessfully() throws Exception {
            Item item = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));
            Page<Item> itemPage = new Page<>(1, 10);
            itemPage.setRecords(Collections.singletonList(item));
            itemPage.setTotal(1);

            lenient().when(itemService.page(any(Page.class), any())).thenReturn(itemPage);
            lenient().when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("按类型获取物品列表成功")
        void shouldGetItemsListByType() throws Exception {
            Item item = createTestItem(1L, 1L, "出售物品", new BigDecimal("99.99"));
            Page<Item> itemPage = new Page<>(1, 10);
            itemPage.setRecords(Collections.singletonList(item));
            itemPage.setTotal(1);

            when(itemService.page(any(Page.class), any())).thenReturn(itemPage);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL)
                    .param("type", "1")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }

        @Test
        @DisplayName("获取空物品列表")
        void shouldGetEmptyItemsList() throws Exception {
            Page<Item> emptyPage = new Page<>(1, 10);
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setTotal(0);

            when(itemService.page(any(Page.class), any())).thenReturn(emptyPage);

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
    @DisplayName("GET /api/items/{itemId} - 获取物品详情")
    class GetItemDetailTests {

        @Test
        @DisplayName("获取物品详情成功")
        void shouldGetItemDetailSuccessfully() throws Exception {
            Item item = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));
            item.setStatus(1);

            when(itemService.getDetail(1L)).thenReturn(item);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("测试物品"));
        }

        @Test
        @DisplayName("获取物品详情失败 - 物品不存在")
        void shouldFailGetItemWhenNotFound() throws Exception {
            when(itemService.getDetail(999L)).thenReturn(null);

            mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/items/search - 搜索物品")
    class SearchItemsTests {

        @Test
        @DisplayName("搜索物品成功")
        void shouldSearchItemsSuccessfully() throws Exception {
            Item item = createTestItem(1L, 1L, "iPhone 14", new BigDecimal("3999.00"));
            Page<Item> itemPage = new Page<>(1, 10);
            itemPage.setRecords(Collections.singletonList(item));
            itemPage.setTotal(1);

            when(itemService.page(any(Page.class), any())).thenReturn(itemPage);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/search")
                    .param("keyword", "iPhone")
                    .param("page", "1")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    @Nested
    @DisplayName("POST /api/items - 创建物品")
    class CreateItemTests {

        @Test
        @DisplayName("创建物品成功")
        void shouldCreateItemSuccessfully() throws Exception {
            ItemController.ItemCreateRequest request = new ItemController.ItemCreateRequest();
            request.setType(1);
            request.setCategory("电子产品");
            request.setTitle("iPhone 14 Pro");
            request.setDescription("自用9成新");
            request.setPrice(new BigDecimal("3999.00"));
            request.setImages("[\"http://example.com/img1.jpg\"]");
            request.setLocation("北京市海淀区");

            Item savedItem = createTestItem(1L, 1L, "iPhone 14 Pro", new BigDecimal("3999.00"));
            savedItem.setId(1L);

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.save(any(Item.class))).thenReturn(true);
            when(itemService.getById(1L)).thenReturn(savedItem);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(post(BASE_URL)
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("iPhone 14 Pro"));
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{itemId} - 更新物品")
    class UpdateItemTests {

        @Test
        @DisplayName("更新物品成功")
        void shouldUpdateItemSuccessfully() throws Exception {
            ItemController.ItemUpdateRequest request = new ItemController.ItemUpdateRequest();
            request.setTitle("更新后的标题");
            request.setDescription("更新后的描述");
            request.setPrice(new BigDecimal("2999.00"));

            Item existingItem = createTestItem(1L, 1L, "原标题", new BigDecimal("3999.00"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 1L)).thenReturn(true);
            when(itemService.getById(1L)).thenReturn(existingItem);
            when(itemService.updateById(any(Item.class))).thenReturn(true);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("更新物品失败 - 非作者无权操作")
        void shouldFailUpdateWhenNotAuthor() throws Exception {
            ItemController.ItemUpdateRequest request = new ItemController.ItemUpdateRequest();
            request.setTitle("更新后的标题");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(999L);
            when(itemService.isAuthor(999L, 1L)).thenReturn(false);

            mockMvc.perform(put(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无权操作"));
        }

        @Test
        @DisplayName("更新物品失败 - 物品不存在")
        void shouldFailUpdateWhenItemNotFound() throws Exception {
            ItemController.ItemUpdateRequest request = new ItemController.ItemUpdateRequest();
            request.setTitle("更新后的标题");

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 999L)).thenReturn(true);
            when(itemService.getById(999L)).thenReturn(null);

            mockMvc.perform(put(BASE_URL + "/999")
                    .header("Authorization", "Bearer mock-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/items/{itemId} - 删除物品")
    class DeleteItemTests {

        @Test
        @DisplayName("删除物品成功")
        void shouldDeleteItemSuccessfully() throws Exception {
            Item existingItem = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 1L)).thenReturn(true);
            when(itemService.getById(1L)).thenReturn(existingItem);
            when(itemService.removeById(1L)).thenReturn(true);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("操作成功"));
        }

        @Test
        @DisplayName("删除物品失败 - 非作者无权操作")
        void shouldFailDeleteWhenNotAuthor() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(999L);
            when(itemService.isAuthor(999L, 1L)).thenReturn(false);

            mockMvc.perform(delete(BASE_URL + "/1")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("无权操作"));
        }

        @Test
        @DisplayName("删除物品失败 - 物品不存在")
        void shouldFailDeleteWhenItemNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 999L)).thenReturn(true);
            when(itemService.getById(999L)).thenReturn(null);

            mockMvc.perform(delete(BASE_URL + "/999")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{itemId}/online - 上架物品")
    class OnlineItemTests {

        @Test
        @DisplayName("上架物品成功")
        void shouldOnlineItemSuccessfully() throws Exception {
            Item existingItem = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 1L)).thenReturn(true);
            when(itemService.getById(1L)).thenReturn(existingItem);
            doNothing().when(itemService).online(1L);

            mockMvc.perform(put(BASE_URL + "/1/online")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("PUT /api/items/{itemId}/offline - 下架物品")
    class OfflineItemTests {

        @Test
        @DisplayName("下架物品成功")
        void shouldOfflineItemSuccessfully() throws Exception {
            Item existingItem = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.isAuthor(1L, 1L)).thenReturn(true);
            when(itemService.getById(1L)).thenReturn(existingItem);
            doNothing().when(itemService).offline(1L);

            mockMvc.perform(put(BASE_URL + "/1/offline")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/items/{itemId}/contact - 联系发布者")
    class ContactItemTests {

        @Test
        @DisplayName("联系发布者成功")
        void shouldContactItemSuccessfully() throws Exception {
            Item item = createTestItem(1L, 2L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.getById(1L)).thenReturn(item);
            when(chatService.getOrCreateConversation(1L, 2L)).thenReturn(null);
            doNothing().when(itemService).incrementContactCount(1L);

            mockMvc.perform(post(BASE_URL + "/1/contact")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.sellerId").value(2));
        }

        @Test
        @DisplayName("联系发布者失败 - 不能联系自己")
        void shouldFailContactWhenOwnItem() throws Exception {
            Item item = createTestItem(1L, 1L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.getById(1L)).thenReturn(item);

            mockMvc.perform(post(BASE_URL + "/1/contact")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("不能联系自己"));
        }

        @Test
        @DisplayName("联系发布者失败 - 物品不存在")
        void shouldFailContactWhenItemNotFound() throws Exception {
            when(authService.getUserIdFromAuthHeader(anyString())).thenReturn(1L);
            when(itemService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/999/contact")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    private Item createTestItem(Long id, Long userId, String title, BigDecimal price) {
        Item item = new Item();
        item.setId(id);
        item.setUserId(userId);
        item.setType(1);
        item.setCategory("电子产品");
        item.setTitle(title);
        item.setDescription("测试描述");
        item.setPrice(price);
        item.setImages("[]");
        item.setLocation("测试地点");
        item.setStatus(1);
        item.setViewCount(0);
        item.setContactCount(0);
        item.setDeleted(0);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }
}
