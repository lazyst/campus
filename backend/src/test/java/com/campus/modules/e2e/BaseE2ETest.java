package com.campus.modules.e2e;

import com.campus.config.TestBaseIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 端到端测试基类
 * 提供E2E测试所需的通用工具方法
 *
 * @author Claude Code
 * @since 2025-01-26
 */
@ActiveProfiles("mysql-test")
public abstract class BaseE2ETest extends TestBaseIntegrationTest {

    /**
     * 测试用户信息
     */
    protected static class TestUser {
        public String phone;
        public String password;
        public String nickname;
        public String token;
        public Long userId;
        public Integer status;

        TestUser(String phone, String password, String nickname) {
            this.phone = phone;
            this.password = password;
            this.nickname = nickname;
            this.status = 1; // 默认正常状态
        }
    }

    /**
     * 测试数据清理列表
     */
    protected final List<Long> cleanupPostIds = new ArrayList<>();
    protected final List<Long> cleanupItemIds = new ArrayList<>();
    protected final List<Long> cleanupCommentIds = new ArrayList<>();
    protected final List<Long> cleanupUserIds = new ArrayList<>();

    /**
     * 创建测试用户并返回用户信息
     *
     * @param prefix 用户名前缀（用于区分不同测试用户）
     * @return TestUser 包含用户信息和token
     * @throws Exception 注册失败时抛出异常
     */
    protected TestUser createTestUser(String prefix) throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String phone = "138" + timestamp.substring(timestamp.length() - 8);
        String password = "password123";

        // 生成符合验证规则的昵称（2-20位）
        String shortPrefix = prefix.length() > 10 ? prefix.substring(0, 10) : prefix;
        String nickname = shortPrefix + timestamp.substring(timestamp.length() - 4);

        // 确保昵称长度不超过20位
        if (nickname.length() > 20) {
            nickname = nickname.substring(0, 20);
        }

        Map<String, String> request = new HashMap<>();
        request.put("phone", phone);
        request.put("password", password);
        request.put("nickname", nickname);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = new ObjectMapper().readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");

        TestUser user = new TestUser(phone, password, nickname);
        user.userId = dataNode.get("id").asLong();
        user.token = rootNode.get("token").asText(); // token在根级别

        // 添加到清理列表
        cleanupUserIds.add(user.userId);

        return user;
    }

    /**
     * 创建管理员Token
     *
     * @return 管理员JWT token
     * @throws Exception 登录失败时抛出异常
     */
    protected String createAdminToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", "admin");
        request.put("password", "admin123");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/admin/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode rootNode = new ObjectMapper().readTree(responseJson);
        return rootNode.get("data").get("token").asText();
    }

    /**
     * 快速创建测试帖子
     *
     * @param token 用户token
     * @param boardId 板块ID
     * @param title 帖子标题
     * @return 帖子ID
     * @throws Exception 创建失败时抛出异常
     */
    protected Long quickCreatePost(String token, Long boardId, String title) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("boardId", boardId);
        request.put("title", title);
        request.put("content", "这是测试帖子内容");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode dataNode = new ObjectMapper().readTree(responseJson).get("data");
        Long postId = dataNode.get("id").asLong();

        // 添加到清理列表
        cleanupPostIds.add(postId);

        return postId;
    }

    /**
     * 快速创建测试物品
     *
     * @param token 用户token
     * @param type 物品类型（1=求购，2=出售）
     * @param title 物品标题
     * @param price 价格
     * @return 物品ID
     * @throws Exception 创建失败时抛出异常
     */
    protected Long quickCreateItem(String token, Integer type, String title, String price) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("type", type);
        request.put("title", title);
        request.put("description", "这是测试物品描述");
        request.put("price", price);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode dataNode = new ObjectMapper().readTree(responseJson).get("data");
        Long itemId = dataNode.get("id").asLong();

        // 添加到清理列表
        cleanupItemIds.add(itemId);

        return itemId;
    }

    /**
     * 快速创建测试评论
     *
     * @param token 用户token
     * @param postId 帖子ID
     * @param content 评论内容
     * @return 评论ID
     * @throws Exception 创建失败时抛出异常
     */
    protected Long quickCreateComment(String token, Long postId, String content) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("postId", postId);
        request.put("content", content);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode dataNode = new ObjectMapper().readTree(responseJson).get("data");
        Long commentId = dataNode.get("id").asLong();

        // 添加到清理列表
        cleanupCommentIds.add(commentId);

        return commentId;
    }

    /**
     * 清理测试数据
     * 在每个测试方法执行后运行
     *
     * @throws Exception 清理失败时抛出异常
     */
    @AfterEach
    void cleanUpTestData() throws Exception {
        String adminToken = createAdminToken();

        // 清理帖子（软删除）
        for (Long postId : cleanupPostIds) {
            try {
                mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/posts/" + postId)
                        .header("Authorization", "Bearer " + adminToken));
            } catch (Exception ignored) {
                // 忽略清理失败
            }
        }
        cleanupPostIds.clear();

        // 清理物品（删除）
        for (Long itemId : cleanupItemIds) {
            try {
                mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/items/" + itemId)
                        .header("Authorization", "Bearer " + adminToken));
            } catch (Exception ignored) {
                // 忽略清理失败
            }
        }
        cleanupItemIds.clear();

        // 清理评论（删除）
        for (Long commentId : cleanupCommentIds) {
            try {
                mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/" + commentId)
                        .header("Authorization", "Bearer " + adminToken));
            } catch (Exception ignored) {
                // 忽略清理失败
            }
        }
        cleanupCommentIds.clear();

        // 注意：不清理用户，因为用户可能被其他测试引用
        // 用户清理应在集成测试套件级别处理
        cleanupUserIds.clear();
    }

    /**
     * 验证API响应格式
     *
     * @param responseJson 响应JSON字符串
     * @param expectedCode 期望的响应码
     * @return JsonNode data节点
     * @throws Exception 解析失败时抛出异常
     */
    protected JsonNode assertApiResponse(String responseJson, int expectedCode) throws Exception {
        JsonNode rootNode = new ObjectMapper().readTree(responseJson);
        int actualCode = rootNode.get("code").asInt();

        if (actualCode != expectedCode) {
            String message = rootNode.has("message") ? rootNode.get("message").asText() : "未知错误";
            throw new AssertionError("期望响应码 " + expectedCode + "，实际为 " + actualCode + "，消息: " + message);
        }

        return rootNode.get("data");
    }

    /**
     * 验证计数器值
     * 支持驼峰和下划线两种命名方式
     *
     * @param responseJson 响应JSON字符串
     * @param counterName 计数器字段名（如 view_count 或 viewCount）
     * @param expectedValue 期望值
     * @throws Exception 断言失败时抛出异常
     */
    protected void assertCounter(String responseJson, String counterName, int expectedValue) throws Exception {
        JsonNode rootNode = new ObjectMapper().readTree(responseJson);
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null) {
            throw new AssertionError("响应中没有data节点: " + responseJson);
        }

        // 尝试获取计数器值，支持驼峰和下划线命名
        JsonNode counterNode = dataNode.get(counterName);

        // 如果找不到，尝试转换命名方式
        if (counterNode == null || counterNode.isNull()) {
            // 尝试驼峰命名 (viewCount -> view_count)
            String alternativeName = camelToSnake(counterName);
            counterNode = dataNode.get(alternativeName);

            // 如果还找不到，尝试下划线转驼峰 (view_count -> viewCount)
            if (counterNode == null || counterNode.isNull()) {
                alternativeName = snakeToCamel(counterName);
                counterNode = dataNode.get(alternativeName);
            }
        }

        if (counterNode == null || counterNode.isNull()) {
            throw new AssertionError("计数器字段不存在: " + counterName + "\n" +
                    "可用字段: " + getFieldNames(dataNode) + "\n" +
                    "响应数据: " + dataNode.toString());
        }

        int actualValue = counterNode.asInt();

        if (actualValue != expectedValue) {
            throw new AssertionError("期望 " + counterName + " = " + expectedValue + "，实际为 " + actualValue);
        }
    }

    /**
     * 驼峰转下划线
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 下划线转驼峰
     */
    private String snakeToCamel(String snakeCase) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                result.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        return result.toString();
    }

    /**
     * 获取JSON节点的所有字段名
     */
    private String getFieldNames(JsonNode node) {
        StringBuilder sb = new StringBuilder();
        node.fieldNames().forEachRemaining(name -> sb.append(name).append(", "));
        return sb.toString();
    }
}
