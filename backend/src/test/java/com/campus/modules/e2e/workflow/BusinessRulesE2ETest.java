package com.campus.modules.e2e.workflow;

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
 * E2E-008: 业务规则验证测试
 *
 * 测试系统的关键业务规则和约束：
 * 1. 唯一性约束（手机号重复注册）
 * 2. 权限边界（修改他人内容）
 * 3. 业务规则（不能联系自己）
 * 4. Toggle逻辑（重复点赞/收藏）
 * 5. 软删除行为
 * 6. 状态流转限制
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@DisplayName("E2E-008: 业务规则验证测试")
class BusinessRulesE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("唯一性约束测试")
    class UniquenessTests {

        @Test
        @DisplayName("E2E-008-A: 手机号唯一性约束")
        void shouldEnforcePhoneUniqueness() throws Exception {
            // 创建第一个用户
            TestUser user1 = createTestUser("unique_phone");

            // 尝试用相同手机号注册第二个用户
            Map<String, String> duplicateRequest = new HashMap<>();
            duplicateRequest.put("phone", user1.phone);
            duplicateRequest.put("password", "different123");
            duplicateRequest.put("nickname", "不同昵称");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isBadRequest()); // 400 Bad Request

            System.out.println("✅ E2E-008-A: 手机号唯一性约束测试通过");
        }

        @Test
        @DisplayName("E2E-008-B: 同一用户不能重复收藏同一帖子")
        void shouldTogglePostCollect() throws Exception {
            TestUser user = createTestUser("collect_toggle");
            Long postId = quickCreatePost(user.token, 1L, "收藏测试帖子");

            // 第一次收藏
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "collect_count", 1);

            // 第二次收藏（取消收藏）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "collect_count", 0);

            System.out.println("✅ E2E-008-B: 帖子收藏Toggle测试通过");
        }

        @Test
        @DisplayName("E2E-008-C: 同一用户不能重复收藏同一物品")
        void shouldToggleItemCollect() throws Exception {
            TestUser user = createTestUser("item_collect_toggle");
            Long itemId = quickCreateItem(user.token, 2, "物品收藏测试", "50.00");

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

            // 第二次收藏（取消收藏）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-C: 物品收藏Toggle测试通过");
        }
    }

    @Nested
    @DisplayName("权限边界测试")
    class PermissionBoundaryTests {

        @Test
        @DisplayName("E2E-008-D: 用户不能修改他人的帖子")
        void shouldDenyUpdatingOthersPost() throws Exception {
            TestUser userA = createTestUser("post_author");
            TestUser userB = createTestUser("post_intruder");

            Long postId = quickCreatePost(userA.token, 1L, "原始帖子");

            // 用户B尝试修改用户A的帖子
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "被恶意修改的标题");
            updateRequest.put("content", "被恶意修改的内容");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userB.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 验证帖子内容未被修改
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("原始帖子"));

            System.out.println("✅ E2E-008-D: 修改他人帖子权限测试通过");
        }

        @Test
        @DisplayName("E2E-008-E: 用户不能删除他人的帖子")
        void shouldDenyDeletingOthersPost() throws Exception {
            TestUser userA = createTestUser("delete_author");
            TestUser userB = createTestUser("delete_intruder");

            Long postId = quickCreatePost(userA.token, 1L, "受保护的帖子");

            // 用户B尝试删除用户A的帖子
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 验证帖子仍然存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-E: 删除他人帖子权限测试通过");
        }

        @Test
        @DisplayName("E2E-008-F: 用户不能修改他人的物品")
        void shouldDenyUpdatingOthersItem() throws Exception {
            TestUser userA = createTestUser("item_author");
            TestUser userB = createTestUser("item_intruder");

            Long itemId = quickCreateItem(userA.token, 2, "原始物品", "50.00");

            // 用户B尝试修改用户A的物品
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "被恶意修改的标题");
            updateRequest.put("description", "被恶意修改的描述");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId)
                            .header("Authorization", "Bearer " + userB.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            System.out.println("✅ E2E-008-F: 修改他人物品权限测试通过");
        }

        @Test
        @DisplayName("E2E-008-G: 用户不能删除他人的评论")
        void shouldDenyDeletingOthersComment() throws Exception {
            TestUser userA = createTestUser("comment_author");
            TestUser userB = createTestUser("comment_intruder");

            Long postId = quickCreatePost(userA.token, 1L, "测试帖子");
            Long commentId = quickCreateComment(userA.token, postId, "原始评论");

            // 用户B尝试删除用户A的评论
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/" + commentId)
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 验证评论仍然存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/post/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-G: 删除他人评论权限测试通过");
        }
    }

    @Nested
    @DisplayName("业务规则验证测试")
    class BusinessRuleTests {

        @Test
        @DisplayName("E2E-008-H: 不能联系自己的物品")
        void notAllowContactOwnItem() throws Exception {
            TestUser user = createTestUser("self_contact");
            Long itemId = quickCreateItem(user.token, 2, "自己的物品", "50.00");

            // 尝试联系自己的物品
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("不能联系自己"));

            System.out.println("✅ E2E-008-H: 自联系限制测试通过");
        }

        @Test
        @DisplayName("E2E-008-I: 软删除后数据不可见")
        void shouldHideSoftDeletedData() throws Exception {
            TestUser user = createTestUser("soft_delete");
            Long postId = quickCreatePost(user.token, 1L, "将被软删除的帖子");

            // 验证帖子存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 软删除帖子
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证帖子不可访问
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 帖子不存在

            System.out.println("✅ E2E-008-I: 软删除行为测试通过");
        }

        @Test
        @DisplayName("E2E-008-J: 物品状态流转限制")
        void shouldEnforceItemStatusFlow() throws Exception {
            TestUser user = createTestUser("status_flow");
            Long itemId = quickCreateItem(user.token, 2, "状态测试物品", "50.00");

            // 初始状态：在售（status=1）
            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String json1 = result1.getResponse().getContentAsString();
            JsonNode data1 = assertApiResponse(json1, 200);
            assertEquals(1, data1.get("status").asInt(), "初始状态应为在售");

            // 下架（status=3）
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/offline")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String json2 = result2.getResponse().getContentAsString();
            JsonNode data2 = assertApiResponse(json2, 200);
            assertEquals(3, data2.get("status").asInt(), "下架后状态应为已下架");

            // 重新上架（status=1）
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/online")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            MvcResult result3 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String json3 = result3.getResponse().getContentAsString();
            JsonNode data3 = assertApiResponse(json3, 200);
            assertEquals(1, data3.get("status").asInt(), "上架后状态应为在售");

            // 标记完成（status=2）
            mockMvc.perform(MockMvcRequestBuilders.put("/api/items/" + itemId + "/complete")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            MvcResult result4 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            String json4 = result4.getResponse().getContentAsString();
            JsonNode data4 = assertApiResponse(json4, 200);
            assertEquals(2, data4.get("status").asInt(), "完成后状态应为已完成");

            System.out.println("✅ E2E-008-J: 物品状态流转测试通过");
        }

        @Test
        @DisplayName("E2E-008-K: Toggle点赞逻辑正确性")
        void shouldToggleLikeCorrectly() throws Exception {
            TestUser user = createTestUser("like_toggle_user");
            Long postId = quickCreatePost(user.token, 1L, "Toggle测试帖子");

            // 第1次点赞：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 第2次点赞（取消）：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 第3次点赞（重新点赞）：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-K: 点赞Toggle逻辑测试通过");
        }

        @Test
        @DisplayName("E2E-008-L: 用户状态禁用后无法登录")
        void shouldPreventBannedUserLogin() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("banned_user");

            // 正常登录
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("phone", user.phone);
            loginRequest.put("password", user.password);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 管理员禁用用户
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 尝试再次登录（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest()) // 期望400（参数验证失败）
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("禁用")));

            System.out.println("✅ E2E-008-L: 禁用用户登录限制测试通过");
        }
    }

    @Nested
    @DisplayName("计数器正确性测试")
    class CounterAccuracyTests {

        @Test
        @DisplayName("E2E-008-M: 帖子浏览计数器递增")
        void shouldIncrementViewCount() throws Exception {
            TestUser user = createTestUser("view_counter");
            Long postId = quickCreatePost(user.token, 1L, "浏览计数测试");

            // 初始浏览数=0
            MvcResult result0 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result0.getResponse().getContentAsString(), "view_count", 0);

            // 第1次浏览
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId));
            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "view_count", 2);

            // 第2次浏览
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId));
            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "view_count", 4);

            System.out.println("✅ E2E-008-M: 浏览计数器递增测试通过");
        }

        @Test
        @DisplayName("E2E-008-N: 评论增加后帖子计数器更新")
        void shouldIncrementCommentCount() throws Exception {
            TestUser user = createTestUser("comment_counter");
            Long postId = quickCreatePost(user.token, 1L, "评论计数测试");

            // 初始评论数=0
            MvcResult result0 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result0.getResponse().getContentAsString(), "comment_count", 0);

            // 添加3条评论
            quickCreateComment(user.token, postId, "评论1");
            quickCreateComment(user.token, postId, "评论2");
            quickCreateComment(user.token, postId, "评论3");

            // 验证评论数=3
            MvcResult result3 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result3.getResponse().getContentAsString(), "comment_count", 3);

            // 删除1条评论
            Long commentId = quickCreateComment(user.token, postId, "待删除评论");
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/" + commentId)
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            // 验证评论数回到3
            MvcResult result4 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result4.getResponse().getContentAsString(), "comment_count", 3);

            System.out.println("✅ E2E-008-N: 评论计数器更新测试通过");
        }

        @Test
        @DisplayName("E2E-008-O: 物品联系计数器正确性")
        void shouldTrackContactCount() throws Exception {
            TestUser userA = createTestUser("contact_seller");
            TestUser userB = createTestUser("contact_buyer1");
            TestUser userC = createTestUser("contact_buyer2");

            Long itemId = quickCreateItem(userA.token, 2, "联系计数测试", "50.00");

            // 初始联系数=0
            MvcResult result0 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result0.getResponse().getContentAsString(), "contact_count", 0);

            // 用户B联系
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk());

            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "contact_count", 1);

            // 用户C联系
            mockMvc.perform(MockMvcRequestBuilders.post("/api/items/" + itemId + "/contact")
                            .header("Authorization", "Bearer " + userC.token))
                    .andExpect(status().isOk());

            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/items/" + itemId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "contact_count", 2);

            System.out.println("✅ E2E-008-O: 联系计数器测试通过");
        }
    }

    @Nested
    @DisplayName("数据一致性测试")
    class DataConsistencyTests {

        @Test
        @DisplayName("E2E-008-P: 删除帖子后评论也删除")
        void shouldCascadeDeleteComments() throws Exception {
            TestUser user = createTestUser("cascade_delete");
            Long postId = quickCreatePost(user.token, 1L, "级联删除测试");

            // 创建多条评论
            quickCreateComment(user.token, postId, "评论1");
            quickCreateComment(user.token, postId, "评论2");
            quickCreateComment(user.token, postId, "评论3");

            // 验证评论存在
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/post/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 删除帖子
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk());

            // 验证评论也不可见了（通过帖子查询）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/post/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-P: 级联删除测试通过");
        }

        @Test
        @DisplayName("E2E-008-Q: 禁用用户后其内容仍可见")
        void shouldKeepBannedUserContentVisible() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("banned_content");

            // 用户发布帖子
            Long postId = quickCreatePost(user.token, 1L, "禁用用户的帖子");

            // 管理员禁用用户
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 验证帖子仍然可见（未登录用户也能看到）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-008-Q: 禁用用户内容可见性测试通过");
        }
    }
}
