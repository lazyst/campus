package com.campus.modules.trade.item;

import com.campus.modules.auth.service.AuthService;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.entity.ItemCollect;
import com.campus.modules.trade.service.ItemCollectService;
import com.campus.modules.trade.service.ItemService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemCollectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemCollectService itemCollectService;

    @MockBean
    private ItemService itemService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    private static final String BASE_URL = "/api/items";

    @Nested
    @DisplayName("POST /api/items/{itemId}/collect - 收藏/取消收藏物品")
    class ToggleCollectTests {

        @Test
        @DisplayName("收藏物品成功")
        void shouldToggleCollectSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemService.getById(1L)).thenReturn(createTestItem(1L, 2L, "测试物品", new BigDecimal("99.99")));
            when(itemCollectService.toggleCollect(1L, 1L)).thenReturn(true);

            mockMvc.perform(post(BASE_URL + "/1/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("取消收藏成功")
        void shouldUncollectSuccessfully() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemService.getById(1L)).thenReturn(createTestItem(1L, 2L, "测试物品", new BigDecimal("99.99")));
            when(itemCollectService.toggleCollect(1L, 1L)).thenReturn(false);

            mockMvc.perform(post(BASE_URL + "/1/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("收藏失败 - 未登录")
        void shouldFailCollectWhenNotLoggedIn() throws Exception {
            mockMvc.perform(post(BASE_URL + "/1/collect"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("收藏失败 - 物品不存在")
        void shouldFailCollectWhenItemNotFound() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemService.getById(999L)).thenReturn(null);

            mockMvc.perform(post(BASE_URL + "/999/collect")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("物品不存在"));
        }
    }

    @Nested
    @DisplayName("GET /api/items/{itemId}/collect/check - 检查是否已收藏")
    class CheckCollectedTests {

        @Test
        @DisplayName("已收藏返回true")
        void shouldReturnTrueWhenCollected() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemCollectService.hasCollected(1L, 1L)).thenReturn(true);

            mockMvc.perform(get(BASE_URL + "/1/collect/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("未收藏返回false")
        void shouldReturnFalseWhenNotCollected() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemCollectService.hasCollected(1L, 1L)).thenReturn(false);

            mockMvc.perform(get(BASE_URL + "/1/collect/check")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @DisplayName("未登录返回false")
        void shouldReturnFalseWhenNotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/1/collect/check"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/items/collected - 获取我收藏的物品")
    class GetCollectedItemsTests {

        @Test
        @DisplayName("获取收藏列表成功")
        void shouldGetCollectedItemsSuccessfully() throws Exception {
            ItemCollect collect = new ItemCollect();
            collect.setId(1L);
            collect.setUserId(1L);
            collect.setItemId(1L);
            collect.setCreatedAt(LocalDateTime.now());

            Item item = createTestItem(1L, 2L, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemCollectService.getByUserId(1L)).thenReturn(Collections.singletonList(collect));
            when(itemService.getById(1L)).thenReturn(item);
            when(userService.listByIds(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/collected")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取空收藏列表")
        void shouldGetEmptyCollectedItems() throws Exception {
            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemCollectService.getByUserId(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/collected")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("获取收藏列表失败 - 未登录")
        void shouldFailGetCollectedItemsWhenNotLoggedIn() throws Exception {
            mockMvc.perform(get(BASE_URL + "/collected"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401));
        }

        @Test
        @DisplayName("收藏列表中物品用户ID为空")
        void shouldHandleItemWithNullUserId() throws Exception {
            ItemCollect collect = new ItemCollect();
            collect.setId(1L);
            collect.setUserId(1L);
            collect.setItemId(1L);
            collect.setCreatedAt(LocalDateTime.now());

            Item item = createTestItem(1L, null, "测试物品", new BigDecimal("99.99"));

            when(authService.getUserIdFromToken(anyString())).thenReturn(1L);
            when(itemCollectService.getByUserId(1L)).thenReturn(Collections.singletonList(collect));
            when(itemService.getById(1L)).thenReturn(item);

            mockMvc.perform(get(BASE_URL + "/collected")
                    .header("Authorization", "Bearer mock-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
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
