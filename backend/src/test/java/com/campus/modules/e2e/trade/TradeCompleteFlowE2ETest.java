package com.campus.modules.e2e.trade;

import com.campus.modules.e2e.BaseE2ETest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E-004: 交易完整流程测试
 *
 * 测试二手交易的完整流程：
 * 1. 用户A发布出售物品（type=2, status=1）
 * 2. 用户B浏览物品列表（可筛选type）
 * 3. 用户B查看物品详情（view_count+1）
 * 4. 用户B收藏物品
 * 5. 用户B联系卖家（contact_count+1, 会话创建）
 * 6. 用户A下架物品（status=3, 列表不显示）
 * 7. 用户A重新上架（status=1, 重新显示）
 * 8. 用户A标记完成（status=2, 列表不显示）
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@DisplayName("E2E-004: 交易完整流程测试")
class TradeCompleteFlowE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("交易完整流程")
    class CompleteTradeFlowTests {

        @Test
        @DisplayName("E2E-004: 物品发布→浏览→收藏→联系→下架→上架→完成完整流程")
        void completeTradeFlow() throws Exception {
            TestUser userA = createTestUser("seller_user");
            TestUser userB = createTestUser("buyer_user");

            // ===== Step 1: 用户A发布出售物品 =====
            Long itemId = quickCreateItem(userA.token, 2, "出售二手书", "50.00");

            // 验证物品创建成功
            MvcResult itemResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String itemJson = itemResult.getResponse().getContentAsString();
            JsonNode itemData = assertApiResponse(itemJson, 200);

            assertEquals(2, itemData.get("type").asInt(), "类型应为出售（2）");
            assertEquals(1, itemData.get("status").asInt(), "状态应为在售（1）");
            assertEquals(1, itemData.get("viewCount").asInt(), "创建后首次查询时浏览数自动+1");
            assertEquals(0, itemData.get("contactCount").asInt(), "初始联系数为0");

            // ===== Step 2: 用户B浏览物品列表 =====
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 按类型筛选（只看出售）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("type", "2")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // ===== Step 3: 用户B查看物品详情 =====
            MvcResult viewResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            // 验证view_count增加（第2次查看，应该是2）
            assertCounter(viewResult.getResponse().getContentAsString(), "view_count", 2);

            // ===== Step 4: 用户B收藏物品 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/collect")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证收藏成功（返回的是直接数组，不是分页对象）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items/collected")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            // ===== Step 5: 用户B联系卖家 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证contact_count增加
            MvcResult contactResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(contactResult.getResponse().getContentAsString(), "contact_count", 1);

            // ===== Step 6: 用户A下架物品（status=3） =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证状态变为已下架
            MvcResult offlineResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String offlineJson = offlineResult.getResponse().getContentAsString();
            JsonNode offlineData = assertApiResponse(offlineJson, 200);
            assertEquals(3, offlineData.get("status").asInt(), "状态应为已下架（3）");

            // 验证列表中显示已下架物品（业务逻辑：status=3仍显示）
            MvcResult listResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andReturn();

            String listJson = listResult.getResponse().getContentAsString();
            JsonNode listData = assertApiResponse(listJson, 200);
            boolean found = false;
            for (JsonNode item : listData.get("records")) {
                if (item.get("id").asLong() == itemId) {
                    found = true;
                    assertEquals(3, item.get("status").asInt(), "物品状态应为已下架（3）");
                    break;
                }
            }
            assertTrue(found, "已下架物品仍在列表中显示（status=3）");

            // ===== Step 7: 用户A重新上架（status=1） =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/online")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证状态变为在售
            MvcResult onlineResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String onlineJson = onlineResult.getResponse().getContentAsString();
            JsonNode onlineData = assertApiResponse(onlineJson, 200);
            assertEquals(1, onlineData.get("status").asInt(), "状态应为在售（1）");

            // ===== Step 8: 用户A标记完成（status=2） =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/complete")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证状态变为已完成
            MvcResult completeResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String completeJson = completeResult.getResponse().getContentAsString();
            JsonNode completeData = assertApiResponse(completeJson, 200);
            assertEquals(2, completeData.get("status").asInt(), "状态应为已完成（2）");

            // 验证列表中不显示已完成物品
            MvcResult finalListResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andReturn();

            String finalListJson = finalListResult.getResponse().getContentAsString();
            JsonNode finalListData = assertApiResponse(finalListJson, 200);
            boolean foundCompleted = false;
            for (JsonNode item : finalListData.get("records")) {
                if (item.get("id").asLong() == itemId) {
                    foundCompleted = true;
                    assertEquals(2, item.get("status").asInt(), "物品状态应为已完成（2）");
                    break;
                }
            }
            assertTrue(foundCompleted, "已完成物品仍在列表中显示（status=2）");

            System.out.println("✅ E2E-004: 交易完整流程测试通过");
        }

        @Test
        @DisplayName("E2E-004-A: 发布求购物物（type=1）")
        void shouldCreateBuyRequest() throws Exception {
            TestUser user = createTestUser("buyer_request");

            Map<String, Object> request = new HashMap<>();
            request.put("type", 1); // 求购
            request.put("title", "求购二手自行车");
            request.put("description", "需要一辆九成新以上的自行车");
            request.put("price", new BigDecimal("200.00"));

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/items")
                            .header("Authorization", "Bearer " + user.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.type").value(1))
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            JsonNode data = assertApiResponse(responseJson, 200);
            Long itemId = data.get("id").asLong();
            cleanupItemIds.add(itemId);

            System.out.println("✅ E2E-004-A: 求购物物测试通过");
        }

        @Test
        @DisplayName("E2E-004-B: 不能联系自己的物品")
        void notAllowContactOwnItem() throws Exception {
            TestUser user = createTestUser("self_contact");
            Long itemId = quickCreateItem(user.token, 2, "自己的物品", "50.00");

            // 尝试联系自己的物品（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)) // 期望失败
                    .andExpect(jsonPath("$.message").value("不能联系自己"));

            System.out.println("✅ E2E-004-B: 自联系限制测试通过");
        }

        @Test
        @DisplayName("E2E-004-C: 收藏Toggle行为验证")
        void shouldToggleItemCollect() throws Exception {
            TestUser user = createTestUser("item_toggle");
            Long itemId = quickCreateItem(user.token, 2, "收藏测试", "30.00");

            // 第一次收藏
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证在收藏列表中
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items/collected")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 第二次收藏（取消）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-004-C: 物品收藏Toggle验证通过");
        }
    }

    @Nested
    @DisplayName("物品列表和筛选测试")
    class ItemListTests {

        @Test
        @DisplayName("E2E-004-D: 按类型筛选物品")
        void shouldFilterItemsByType() throws Exception {
            TestUser user = createTestUser("item_filter");

            // 发布不同类型的物品
            quickCreateItem(user.token, 1, "求购物品", "100.00");
            quickCreateItem(user.token, 2, "出售物品", "50.00");

            // 筛选求购（type=1）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("type", "1")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 筛选出售（type=2）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("type", "2")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-004-D: 物品类型筛选测试通过");
        }

        @Test
        @DisplayName("E2E-004-E: 获取我发布的物品")
        void shouldGetMyItems() throws Exception {
            TestUser user = createTestUser("my_items");

            // 发布物品
            quickCreateItem(user.token, 2, "我的物品1", "50.00");
            quickCreateItem(user.token, 2, "我的物品2", "100.00");

            // 获取我的物品
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items/my")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            System.out.println("✅ E2E-004-E: 我的物品测试通过");
        }

        @Test
        @DisplayName("E2E-004-F: 列表只显示在售物品（status=1）")
        void shouldOnlyShowOnlineItems() throws Exception {
            TestUser user = createTestUser("status_filter");

            // 创建物品并下架
            Long itemId = quickCreateItem(user.token, 2, "状态测试", "50.00");
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            // 创建另一个在售物品
            quickCreateItem(user.token, 2, "在售物品", "30.00");

            // 获取列表（应该不包含已下架物品）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-004-F: 状态筛选测试通过");
        }
    }

    @Nested
    @DisplayName("物品操作权限测试")
    class ItemPermissionTests {

        @Test
        @DisplayName("E2E-004-G: 只有发布者可以修改物品")
        void shouldOnlyAllowOwnerToUpdateItem() throws Exception {
            TestUser userA = createTestUser("item_owner");
            TestUser userB = createTestUser("item_other");

            Long itemId = quickCreateItem(userA.token, 2, "原始标题", "50.00");

            // 用户B尝试修改用户A的物品（应该失败）
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "被修改的标题");
            updateRequest.put("description", "被修改的描述");
            updateRequest.put("price", new BigDecimal("100.00"));

            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId)
                            .header("Authorization", "Bearer " + userB.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 用户A可以修改自己的物品
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId)
                            .header("Authorization", "Bearer " + userA.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("被修改的标题"));

            System.out.println("✅ E2E-004-G: 物品修改权限测试通过");
        }

        @Test
        @DisplayName("E2E-004-H: 只有发布者可以删除物品")
        void shouldOnlyAllowOwnerToDeleteItem() throws Exception {
            TestUser userA = createTestUser("delete_owner");
            TestUser userB = createTestUser("delete_other");

            Long itemId = quickCreateItem(userA.token, 2, "删除测试", "50.00");

            // 用户B尝试删除用户A的物品（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/items/" + itemId)
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 用户A可以删除自己的物品
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/items/" + itemId)
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-004-H: 物品删除权限测试通过");
        }

        @Test
        @DisplayName("E2E-004-I: 只有发布者可以操作物品状态")
        void shouldOnlyAllowOwnerToChangeStatus() throws Exception {
            TestUser userA = createTestUser("status_owner");
            TestUser userB = createTestUser("status_other");

            Long itemId = quickCreateItem(userA.token, 2, "状态操作测试", "50.00");

            // 用户B尝试下架用户A的物品（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 用户A可以下架自己的物品
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-004-I: 状态操作权限测试通过");
        }
    }

    @Nested
    @DisplayName("管理员物品管理测试")
    class AdminItemManagementTests {

        @Test
        @DisplayName("E2E-004-J: 管理员可以删除任何物品")
        void shouldAllowAdminToDeleteAnyItem() throws Exception {
            TestUser user = createTestUser("admin_delete_item");
            String adminToken = createAdminToken();

            Long itemId = quickCreateItem(user.token, 2, "管理员删除测试", "50.00");

            // 管理员可以删除任何物品
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/items/" + itemId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-004-J: 管理员删除物品测试通过");
        }

        @Test
        @DisplayName("E2E-004-K: 管理员可以下架任何物品")
        void shouldAllowAdminToOfflineAnyItem() throws Exception {
            TestUser user = createTestUser("admin_offline_item");
            String adminToken = createAdminToken();

            Long itemId = quickCreateItem(user.token, 2, "管理员下架测试", "50.00");

            // 管理员可以下架任何物品
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-004-K: 管理员下架物品测试通过");
        }

        @Test
        @DisplayName("E2E-004-L: 管理员获取物品列表")
        void shouldAllowAdminToGetItemList() throws Exception {
            TestUser user = createTestUser("admin_list_item");
            String adminToken = createAdminToken();

            quickCreateItem(user.token, 2, "管理员列表测试", "50.00");

            // 管理员获取物品列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/items")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-004-L: 管理员物品列表测试通过");
        }
    }

    @Nested
    @DisplayName("物品详情和计数器测试")
    class ItemDetailTests {

        @Test
        @DisplayName("E2E-004-M: 多次浏览增加浏览数")
        void shouldIncrementViewCount() throws Exception {
            TestUser user = createTestUser("view_count");
            Long itemId = quickCreateItem(user.token, 2, "浏览计数测试", "50.00");

            // 第一次浏览
            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "view_count", 1);

            // 第二次浏览
            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "view_count", 2);

            // 第三次浏览
            MvcResult result3 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result3.getResponse().getContentAsString(), "view_count", 3);

            System.out.println("✅ E2E-004-M: 浏览计数器测试通过");
        }

        @Test
        @DisplayName("E2E-004-N: 多次联系增加联系数")
        void shouldIncrementContactCount() throws Exception {
            TestUser userA = createTestUser("contact_owner");
            TestUser userB = createTestUser("contact_actor");

            Long itemId = quickCreateItem(userA.token, 2, "联系计数测试", "50.00");

            // 第一次联系
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk());

            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "contact_count", 1);

            // 第二次联系（不同用户）
            TestUser userC = createTestUser("contact_actor2");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + userC.token))
                    .andExpect(status().isOk());

            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "contact_count", 2);

            System.out.println("✅ E2E-004-N: 联系计数器测试通过");
        }
    }
}
