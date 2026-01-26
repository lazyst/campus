package com.campus.modules.e2e.user;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E-001: 用户生命周期测试
 *
 * 测试完整的用户生命周期流程：
 * 1. 用户注册
 * 2. 用户登录
 * 3. 完善个人资料
 * 4. 管理员禁用用户
 * 5. 验证禁用后无法登录
 * 6. 管理员解封用户
 * 7. 验证解封后可以重新登录
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@DisplayName("E2E-001: 用户生命周期测试")
class UserLifecycleE2ETest extends BaseE2ETest {

    @Nested
    @DisplayName("完整生命周期流程")
    class CompleteLifecycleTests {

        @Test
        @DisplayName("E2E-001: 用户注册→登录→完善资料→禁用→解封完整流程")
        void completeUserLifecycle() throws Exception {
            String adminToken = createAdminToken();

            // ===== Step 1: 用户注册 =====
            TestUser user = createTestUser("lifecycle");
            assertNotNull(user.userId, "用户ID不应为空");
            assertNotNull(user.token, "Token不应为空");
            assertEquals(1, user.status, "用户状态应为正常（1）");

            // ===== Step 2: 用户登录 =====
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("phone", user.phone);
            loginRequest.put("password", user.password);

            MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists()) // data就是token字符串
                    .andReturn();

            String loginJson = loginResult.getResponse().getContentAsString();
            JsonNode loginData = assertApiResponse(loginJson, 200);
            String newToken = loginData.asText(); // data就是token

            // ===== Step 3: 访问需要登录的接口 =====
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                            .header("Authorization", "Bearer " + newToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(user.userId))
                    .andExpect(jsonPath("$.data.phone").value(user.phone))
                    .andExpect(jsonPath("$.data.nickname").value(user.nickname));

            // ===== Step 4: 修改个人资料 =====
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("nickname", "更新后的昵称");
            updateRequest.put("bio", "这是我的个人简介");
            updateRequest.put("gender", 1);

            mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile")
                            .header("Authorization", "Bearer " + newToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证更新成功，重新获取个人资料
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                            .header("Authorization", "Bearer " + newToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.nickname").value("更新后的昵称"))
                    .andExpect(jsonPath("$.data.bio").value("这是我的个人简介"))
                    .andExpect(jsonPath("$.data.gender").value(1));

            // ===== Step 5: 管理员禁用用户 =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user.userId + "/ban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // ===== Step 6: 验证禁用后无法登录 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest())  // 被禁用用户返回400
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("禁用")));

            // ===== Step 7: 管理员解封用户 =====
            mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + user.userId + "/unban")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // ===== Step 8: 验证解封后可以重新登录 =====
            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists()); // data就是token

            System.out.println("✅ E2E-001: 用户生命周期测试通过");
        }

        @Test
        @DisplayName("E2E-001-A: 用户注册时手机号唯一性验证")
        void shouldEnforcePhoneUniqueness() throws Exception {
            // 创建第一个用户
            TestUser user1 = createTestUser("unique");

            // 尝试用相同手机号注册第二个用户
            Map<String, String> duplicateRequest = new HashMap<>();
            duplicateRequest.put("phone", user1.phone);
            duplicateRequest.put("password", "different123");
            duplicateRequest.put("nickname", "不同昵称");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isBadRequest()); // 400 Bad Request

            System.out.println("✅ E2E-001-A: 手机号唯一性验证通过");
        }

        @Test
        @DisplayName("E2E-001-B: 管理员获取用户列表和详情")
        void shouldGetUserListAndDetail() throws Exception {
            String adminToken = createAdminToken();
            TestUser user = createTestUser("admin_test");

            // 获取用户列表
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());

            // 获取用户详情
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users/" + user.userId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(user.userId))
                    .andExpect(jsonPath("$.data.phone").value(user.phone));

            System.out.println("✅ E2E-001-B: 管理员获取用户信息测试通过");
        }

        @Test
        @DisplayName("E2E-001-C: 普通用户无法访问管理员接口")
        void shouldDenyUserAccessToAdminEndpoints() throws Exception {
            TestUser user = createTestUser("normal_user");

            // 尝试访问管理员接口
            mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isForbidden()); // 期望403 Forbidden

            System.out.println("✅ E2E-001-C: 权限控制测试通过");
        }
    }

    @Nested
    @DisplayName("用户资料管理测试")
    class ProfileManagementTests {

        @Test
        @DisplayName("E2E-001-D: 用户查看和更新个人资料")
        void shouldViewAndUpdateProfile() throws Exception {
            TestUser user = createTestUser("profile");

            // 查看个人资料
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(user.userId))
                    .andExpect(jsonPath("$.data.nickname").value(user.nickname));

            // 更新昵称
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("nickname", "新昵称");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile")
                            .header("Authorization", "Bearer " + user.token)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 再次查看验证更新成功
            mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                            .header("Authorization", "Bearer " + user.token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.nickname").value("新昵称"));

            System.out.println("✅ E2E-001-D: 用户资料管理测试通过");
        }
    }

    @Nested
    @DisplayName("公开信息查询测试")
    class PublicInfoTests {

        @Test
        @DisplayName("E2E-001-E: 未登录用户可以查看公开信息")
        void shouldAccessPublicInfoWithoutAuth() throws Exception {
            TestUser user = createTestUser("public_info");

            // 未登录可以查看用户公开信息
            mockMvc.perform(MockMvcRequestBuilders.get("/api/users/" + user.userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(user.userId))
                    .andExpect(jsonPath("$.data.nickname").value(user.nickname))
                    .andExpect(jsonPath("$.data.phone").doesNotExist()); // 不应返回手机号

            System.out.println("✅ E2E-001-E: 公开信息查询测试通过");
        }
    }
}
