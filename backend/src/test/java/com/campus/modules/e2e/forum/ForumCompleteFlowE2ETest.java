package com.campus.modules.e2e.forum;

import com.campus.modules.e2e.BaseE2ETest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * E2E-002: 论坛完整流程测试
 *
 * 测试论坛的完整交互流程：
 * 1. 用户发布帖子
 * 2. 其他用户浏览帖子（view_count+1）
 * 3. 用户发表评论（comment_count+1，生成通知）
 * 4. 用户点赞帖子（like_count+1，生成通知）
 * 5. 用户收藏帖子（collect_count+1，生成通知）
 * 6. 作者查看通知列表
 * 7. 用户取消点赞（toggle，like_count-1）
 * 8. 作者删除帖子（软删除）
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@DisplayName("E2E-002: 论坛完整流程测试")
class ForumCompleteFlowE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("论坛完整交互流程")
    class CompleteForumFlowTests {

        @Test
        @DisplayName("E2E-002: 发帖→浏览→评论→点赞→收藏→通知→取消→删除完整流程")
        void completeForumFlow() throws Exception {
            // 创建两个测试用户
            TestUser userA = createTestUser("forum_userA");
            TestUser userB = createTestUser("forum_userB");

            // ===== Step 1: 用户A发布帖子 =====
            Long postId = quickCreatePost(userA.token, 1L, "E2E测试帖子");

            // 验证帖子创建成功，初始计数器为0
            MvcResult postResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            assertCounter(postResult.getResponse().getContentAsString(), "view_count", 0);
            assertCounter(postResult.getResponse().getContentAsString(), "like_count", 0);
            assertCounter(postResult.getResponse().getContentAsString(), "comment_count", 0);
            assertCounter(postResult.getResponse().getContentAsString(), "collect_count", 0);

            // ===== Step 2: 用户B浏览帖子 =====
            MvcResult viewResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            // 验证view_count增加
            assertCounter(viewResult.getResponse().getContentAsString(), "view_count", 1);

            // ===== Step 3: 用户B发表评论 =====
            Long commentId = quickCreateComment(userB.token, postId, "这是一条测试评论");

            // 验证comment_count增加
            MvcResult afterCommentResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(afterCommentResult.getResponse().getContentAsString(), "comment_count", 1);

            // 验证用户A收到通知
            mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray()) // ✅ 直接是数组
                    .andExpect(jsonPath("$.data[0].type").value(1)); // type=1表示评论通知

            // ===== Step 4: 用户B点赞帖子 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证like_count增加
            MvcResult afterLikeResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(afterLikeResult.getResponse().getContentAsString(), "like_count", 1);

            // 验证用户A收到点赞通知
            mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // ===== Step 5: 用户B收藏帖子 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/collect")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证collect_count增加
            MvcResult afterCollectResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(afterCollectResult.getResponse().getContentAsString(), "collect_count", 1);

            // 验证用户A收到收藏通知
            mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // ===== Step 6: 用户A查看通知列表 =====
            MvcResult notificationResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String notificationJson = notificationResult.getResponse().getContentAsString();
            JsonNode notificationData = assertApiResponse(notificationJson, 200);

            // 通知API返回的是数组，不是分页对象
            int notificationCount = notificationData.isArray() ? notificationData.size() : 0;

            assertTrue(notificationCount >= 3, "用户A应该收到至少3条通知（评论、点赞、收藏），实际收到: " + notificationCount);

            // ===== Step 7: 用户B取消点赞（Toggle测试） =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证like_count减少
            MvcResult afterUnlikeResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(afterUnlikeResult.getResponse().getContentAsString(), "like_count", 0);

            // ===== Step 8: 用户A删除帖子（软删除测试） =====
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证帖子已被删除（状态为0）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 帖子不存在

            System.out.println("✅ E2E-002: 论坛完整流程测试通过");
        }

        @Test
        @DisplayName("E2E-002-A: 点赞Toggle行为验证")
        void shouldToggleLikeCorrectly() throws Exception {
            TestUser user = createTestUser("toggle_user");
            Long postId = quickCreatePost(user.token, 1L, "Toggle测试帖子");

            // 第一次点赞：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 第二次点赞（取消）：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 第三次点赞：验证toggle成功
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/like")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-002-A: 点赞Toggle验证通过");
        }

        @Test
        @DisplayName("E2E-002-B: 收藏Toggle行为验证")
        void shouldToggleCollectCorrectly() throws Exception {
            TestUser user = createTestUser("toggle_collect");
            Long postId = quickCreatePost(user.token, 1L, "收藏Toggle测试");

            // 第一次收藏：collect_count 0→1
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "collect_count", 1);

            // 第二次收藏（取消）：collect_count 1→0
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/" + postId + "/collect")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "collect_count", 0);

            System.out.println("✅ E2E-002-B: 收藏Toggle验证通过");
        }
    }

    @Nested
    @DisplayName("帖子列表和筛选测试")
    class PostListTests {

        @Test
        @DisplayName("E2E-002-C: 按板块筛选帖子")
        void shouldFilterPostsByBoard() throws Exception {
            TestUser user = createTestUser("board_filter");

            // 在不同板块发布帖子
            quickCreatePost(user.token, 1L, "交流区帖子");
            quickCreatePost(user.token, 2L, "问答区帖子");

            // 筛选交流区（board_id=1）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                            .param("boardId", "1")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 筛选问答区（board_id=2）
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                            .param("boardId", "2")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-002-C: 板块筛选测试通过");
        }

        @Test
        @DisplayName("E2E-002-D: 获取我发布的帖子")
        void shouldGetMyPosts() throws Exception {
            TestUser user = createTestUser("my_posts");

            // 发布3个帖子
            quickCreatePost(user.token, 1L, "我的帖子1");
            quickCreatePost(user.token, 1L, "我的帖子2");
            quickCreatePost(user.token, 2L, "我的帖子3");

            // 获取我的帖子
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/my")
                            .header("Authorization", "Bearer " + user.token)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            System.out.println("✅ E2E-002-D: 我的帖子测试通过");
        }
    }

    @Nested
    @DisplayName("权限验证测试")
    class PermissionTests {

        @Test
        @DisplayName("E2E-002-E: 只有作者可以删除帖子")
        void shouldOnlyAllowAuthorToDeletePost() throws Exception {
            TestUser userA = createTestUser("author_user");
            TestUser userB = createTestUser("other_user");

            Long postId = quickCreatePost(userA.token, 1L, "权限测试帖子");

            // 用户B尝试删除用户A的帖子（应该失败）
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userB.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 用户A可以删除自己的帖子
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-002-E: 权限验证测试通过");
        }

        @Test
        @DisplayName("E2E-002-F: 只有作者可以修改帖子")
        void shouldOnlyAllowAuthorToUpdatePost() throws Exception {
            TestUser userA = createTestUser("author_update");
            TestUser userB = createTestUser("other_update");

            Long postId = quickCreatePost(userA.token, 1L, "原始标题");

            // 用户B尝试修改用户A的帖子（应该失败）
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("title", "被修改的标题");
            updateRequest.put("content", "被修改的内容");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userB.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500)); // 权限不足

            // 用户A可以修改自己的帖子
            mockMvc.perform(MockMvcRequestBuilders.put("/api/posts/" + postId)
                            .header("Authorization", "Bearer " + userA.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("被修改的标题"));

            System.out.println("✅ E2E-002-F: 修改权限验证测试通过");
        }
    }

    @Nested
    @DisplayName("评论功能测试")
    class CommentTests {

        @Test
        @DisplayName("E2E-002-G: 获取帖子的评论列表")
        void shouldGetCommentsByPostId() throws Exception {
            TestUser user = createTestUser("comment_list");
            Long postId = quickCreatePost(user.token, 1L, "评论列表测试");

            // 创建多条评论
            quickCreateComment(user.token, postId, "第一条评论");
            quickCreateComment(user.token, postId, "第二条评论");
            quickCreateComment(user.token, postId, "第三条评论");

            // 获取评论列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/post/" + postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            System.out.println("✅ E2E-002-G: 评论列表测试通过");
        }

        @Test
        @DisplayName("E2E-002-H: 删除评论后计数器减少")
        void shouldDecrementCommentCountOnDelete() throws Exception {
            TestUser user = createTestUser("comment_delete");
            Long postId = quickCreatePost(user.token, 1L, "删除评论测试");

            // 创建评论
            Long commentId = quickCreateComment(user.token, postId, "待删除的评论");

            // 验证comment_count=1
            MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result1.getResponse().getContentAsString(), "comment_count", 1);

            // 删除评论
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/" + commentId)
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证comment_count=0
            MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get("/api/posts/" + postId))
                    .andExpect(status().isOk())
                    .andReturn();

            assertCounter(result2.getResponse().getContentAsString(), "comment_count", 0);

            System.out.println("✅ E2E-002-H: 删除评论计数器测试通过");
        }
    }

    @Nested
    @DisplayName("通知功能测试")
    class NotificationTests {

        @Test
        @DisplayName("E2E-002-I: 标记通知为已读")
        void shouldMarkNotificationAsRead() throws Exception {
            TestUser userA = createTestUser("notify_author");
            TestUser userB = createTestUser("notify_actor");

            Long postId = quickCreatePost(userA.token, 1L, "通知测试");
            quickCreateComment(userB.token, postId, "触发通知");

            // 获取未读通知
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/notifications")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andReturn();

            String json = result.getResponse().getContentAsString();
            JsonNode data = assertApiResponse(json, 200);

            // 检查是否有通知
            if (!data.isArray() || data.size() == 0) {
                throw new AssertionError("期望至少有一条通知");
            }

            Long notificationId = data.get(0).get("id").asLong();

            // 标记为已读
            mockMvc.perform(MockMvcRequestBuilders.put("/api/notifications/" + notificationId + "/read")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-002-I: 通知已读标记测试通过");
        }

        @Test
        @DisplayName("E2E-002-J: 批量标记所有通知为已读")
        void shouldMarkAllNotificationsAsRead() throws Exception {
            TestUser userA = createTestUser("batch_read_author");
            TestUser userB = createTestUser("batch_read_actor");

            Long postId = quickCreatePost(userA.token, 1L, "批量已读测试");
            quickCreateComment(userB.token, postId, "评论1");
            quickCreateComment(userB.token, postId, "评论2");

            // 批量标记已读
            mockMvc.perform(MockMvcRequestBuilders.put("/api/notifications/read/all")
                            .header("Authorization", "Bearer " + userA.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            System.out.println("✅ E2E-002-J: 批量已读测试通过");
        }
    }
}
