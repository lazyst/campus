package com.campus.modules.e2e;

import com.campus.config.TestBaseIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 简单E2E测试 - 用于调试
 */
@ActiveProfiles("mysql-test")
class SimpleE2ETest extends TestBaseIntegrationTest {

    @Test
    void testSimpleUserFlow() throws Exception {
        // 1. 注册
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("phone", "139" + System.currentTimeMillis() % 100000000);
        registerRequest.put("password", "password123");
        registerRequest.put("nickname", "测试用户");

        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String registerJson = registerResult.getResponse().getContentAsString();
        JsonNode registerRoot = new com.fasterxml.jackson.databind.ObjectMapper().readTree(registerJson);
        String token = registerRoot.get("token").asText();
        Long userId = registerRoot.get("data").get("id").asLong();

        System.out.println("✅ 注册成功: userId=" + userId + ", token=" + token.substring(0, 20) + "...");

        // 2. 登录
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("phone", registerRequest.get("phone"));
        loginRequest.put("password", "password123");

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        System.out.println("✅ 登录成功");

        // 3. 获取个人资料
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ 获取个人资料成功");

        // 4. 更新个人资料
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("nickname", "新昵称");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/user/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ 更新个人资料成功");

        // 5. 验证更新
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"));

        System.out.println("✅ 验证更新成功");
    }

    @Test
    void testAdminBanUnban() throws Exception {
        // 1. 创建测试用户
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("phone", "139" + System.currentTimeMillis() % 100000000);
        registerRequest.put("password", "password123");
        registerRequest.put("nickname", "测试用户");

        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String registerJson = registerResult.getResponse().getContentAsString();
        JsonNode registerRoot = new com.fasterxml.jackson.databind.ObjectMapper().readTree(registerJson);
        Long userId = registerRoot.get("data").get("id").asLong();

        System.out.println("✅ 创建用户成功: userId=" + userId);

        // 2. 管理员登录
        Map<String, String> adminLogin = new HashMap<>();
        adminLogin.put("username", "admin");
        adminLogin.put("password", "admin123");

        MvcResult adminResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminJson = adminResult.getResponse().getContentAsString();
        JsonNode adminRoot = new com.fasterxml.jackson.databind.ObjectMapper().readTree(adminJson);
        String adminToken = adminRoot.get("data").get("token").asText();

        System.out.println("✅ 管理员登录成功");

        // 3. 禁用用户
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + userId + "/ban")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ 禁用用户成功");

        // 4. 验证用户无法登录
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("phone", registerRequest.get("phone"));
        loginRequest.put("password", "password123");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest()); // 被禁用用户无法登录

        System.out.println("✅ 验证禁用用户无法登录成功");

        // 5. 解封用户
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + userId + "/unban")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ 解封用户成功");

        // 6. 验证用户可以重新登录
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        System.out.println("✅ 验证解封用户可以登录成功");
    }
}
