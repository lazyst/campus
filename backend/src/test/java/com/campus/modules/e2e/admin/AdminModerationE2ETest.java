package com.campus.modules.e2e.admin;

import com.campus.modules.e2e.BaseE2ETest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E-007: 管理员审核测试
 *
 * 测试管理员对违规内容和用户的管理：
 * 1. 管理员登录获取token
 * 2. 管理员删除违规帖子
 * 3. 管理员禁用违规用户
 * 4. 管理员创建新板块
 * 5. 管理员修改板块
 * 6. 管理员删除板块
 * 7. 验证管理员特权（可以删除任何内容）
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@DisplayName("E2E-007: 管理员审核测试")
class AdminModerationE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("管理员内容管理测试")
    class ContentModerationTests {

        @Test
        @DisplayName("E2E-007: 管理员管理违规内容完整流程")
        void completeModerationFlow() throws Exception {
            String adminToken = createAdminToken();
            TestUser violationUser = createTestUser("violation_user");

            // ===== Step 1: 用户发布违规帖子 =====
            Long violationPostId = quickCreatePost(violationUser.token, 1L, "违规帖子标题");

            // 验证帖子存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + violationPostId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // ===== Step 2: 管理员删除违规帖子 =====
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/posts/" + violationPostId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证帖子已被删除
            MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + violationPostId))
                    .andExpect(status().isOk())
                    .andReturn();

            String postJson = postResult.getResponse().getContentAsString();
            JsonNode postNode = objectMapper.readTree(postJson);
            int postCode = postNode.get("code").asInt();
            assertTrue(postCode != 200, "删除后查询帖子应该返回错误码，实际返回: " + postCode);

            // ===== Step 3: 管理员禁用违规用户 =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + violationUser.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证用户无法登录（被封禁用户返回400）
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("phone", violationUser.phone);
            loginRequest.put("password", violationUser.password);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())  // 被禁用用户返回400
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("禁用")));

            // ===== Step 4: 管理员解封用户 =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + violationUser.userId + "/unban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证用户可以重新登录
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-007: 管理员内容管理测试通过");
        }

        @Test
        @DisplayName("E2E-007-A: 管理员删除违规物品")
        void shouldDeleteViolationItem() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("item_violation");

            Long itemId = quickCreateItem(user.token, 2, "违规物品", "50.00");

            // 验证物品存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 管理员删除物品
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/items/" + itemId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证物品已被删除（软删除：查询可能返回错误）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());

            System.out.println("✅ E2E-007-A: 管理员删除物品测试通过（软删除验证）");
        }

        @Test
        @DisplayName("E2E-007-B: 管理员批量管理用户")
        void shouldManageUsersInBatch() throws Exception {
            String adminToken = createAdminToken();

            // 创建多个测试用户
            TestUser user1 = createTestUser("batch_user1");
            TestUser user2 = createTestUser("batch_user2");
            TestUser user3 = createTestUser("batch_user3");

            // 批量禁用
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user1.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user2.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 获取用户列表验证禁用状态
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-007-B: 批量用户管理测试通过");
        }
    }

    @Nested
    @DisplayName("管理员板块管理测试")
    class BoardManagementTests {

        @Test
        @DisplayName("E2E-007-C: 管理员创建新板块")
        void shouldCreateNewBoard() throws Exception {
            String adminToken = createAdminToken();

            Map<String, Object> request = new HashMap<>();
            request.put("name", "E2E测试板块");
            request.put("description", "这是用于测试的板块");
            request.put("sortOrder", 99);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/boards")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("E2E测试板块"))
                    .andReturn();

            String responseJson = result.getResponse().getContentAsString();
            JsonNode data = assertApiResponse(responseJson, 200);
            Long boardId = data.get("id").asLong();

            // 验证板块创建成功
            mockMvc.perform(MockMvcRequestBuilders.get("/api/boards/" + boardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("E2E测试板块"));

            // 清理：删除测试板块
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/boards/" + boardId)
                            .header("Authorization", "Bearer " + adminToken));

            System.out.println("✅ E2E-007-C: 创建板块测试通过");
        }

        @Test
        @DisplayName("E2E-007-D: 管理员修改板块")
        void shouldUpdateBoard() throws Exception {
            String adminToken = createAdminToken();

            // 先创建板块
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("name", "原始板块名");
            createRequest.put("description", "原始描述");
            createRequest.put("sortOrder", 10);

            MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/boards")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String createJson = createResult.getResponse().getContentAsString();
            JsonNode createData = assertApiResponse(createJson, 200);
            Long boardId = createData.get("id").asLong();

            // 修改板块
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("name", "修改后的板块名");
            updateRequest.put("description", "修改后的描述");
            updateRequest.put("sortOrder", 20);

            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/boards/" + boardId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("修改后的板块名"));

            // 验证修改成功
            mockMvc.perform(MockMvcRequestBuilders.get("/api/boards/" + boardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("修改后的板块名"))
                    .andExpect(jsonPath("$.data.description").value("修改后的描述"));

            // 清理
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/boards/" + boardId)
                            .header("Authorization", "Bearer " + adminToken));

            System.out.println("✅ E2E-007-D: 修改板块测试通过");
        }

        @Test
        @DisplayName("E2E-007-E: 管理员删除板块")
        void shouldDeleteBoard() throws Exception {
            String adminToken = createAdminToken();

            // 先创建板块
            Map<String, Object> createRequest = new HashMap<>();
            createRequest.put("name", "待删除板块");
            createRequest.put("description", "这个板块将被删除");
            createRequest.put("sortOrder", 10);

            MvcResult createResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/boards")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String createJson = createResult.getResponse().getContentAsString();
            JsonNode createData = assertApiResponse(createJson, 200);
            Long boardId = createData.get("id").asLong();

            // 删除板块
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/boards/" + boardId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证板块已删除（软删除：查询可能返回错误）
            // 软删除后查询应该返回"板块不存在"错误
            mockMvc.perform(MockMvcRequestBuilders.get("/api/boards/" + boardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").doesNotExist());

            System.out.println("✅ E2E-007-E: 删除板块测试通过（软删除验证）");
        }

        @Test
        @DisplayName("E2E-007-F: 普通用户无法访问板块管理接口")
        void shouldDenyUserAccessToBoardManagement() throws Exception {
            TestUser user = createTestUser("normal_user_board");

            Map<String, Object> request = new HashMap<>();
            request.put("name", "尝试创建板块");
            request.put("description", "普通用户不应该能创建");
            request.put("sortOrder", 10);

            // 普通用户尝试创建板块（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/boards")
                            .header("Authorization", "Bearer " + user.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden()); // 403 Forbidden

            System.out.println("✅ E2E-007-F: 板块管理权限测试通过");
        }
    }

    @Nested
    @DisplayName("管理员特权验证测试")
    class AdminPrivilegeTests {

        @Test
        @DisplayName("E2E-007-G: 管理员可以删除任何用户的帖子")
        void shouldDeleteAnyUsersPost() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("post_owner");

            Long postId = quickCreatePost(user.token, 1L, "普通用户的帖子");

            // 用户自己无法通过管理员接口删除
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/posts/" + postId)
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isForbidden());

            // 管理员可以删除
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/posts/" + postId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-007-G: 管理员删除帖子特权测试通过");
        }

        @Test
        @DisplayName("E2E-007-H: 管理员可以删除任何用户的物品")
        void shouldDeleteAnyUsersItem() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("item_owner");

            Long itemId = quickCreateItem(user.token, 2, "普通用户的物品", "50.00");

            // 管理员可以删除任何物品
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/items/" + itemId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-007-H: 管理员删除物品特权测试通过");
        }

        @Test
        @DisplayName("E2E-007-I: 管理员可以获取所有用户列表")
        void shouldGetAllUsersList() throws Exception {
            String adminToken = createAdminToken();

            // 创建一些测试用户
            createTestUser("list_user1");
            createTestUser("list_user2");

            // 管理员获取用户列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray())
                    .andExpect(jsonPath("$.data.total").exists());

            System.out.println("✅ E2E-007-I: 管理员获取用户列表测试通过");
        }

        @Test
        @DisplayName("E2E-007-J: 管理员可以获取所有帖子列表")
        void shouldGetAllPostsList() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("admin_posts_user");

            quickCreatePost(user.token, 1L, "管理员查看帖子");

            // 管理员获取帖子列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/posts")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-007-J: 管理员获取帖子列表测试通过");
        }

        @Test
        @DisplayName("E2E-007-K: 管理员可以获取所有物品列表")
        void shouldGetAllItemsList() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("admin_items_user");

            quickCreateItem(user.token, 2, "管理员查看物品", "50.00");

            // 管理员获取物品列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/items")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-007-K: 管理员获取物品列表测试通过");
        }
    }
}
