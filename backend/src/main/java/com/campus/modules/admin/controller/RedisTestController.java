package com.campus.modules.admin.controller;

import com.campus.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis 测试控制器
 */
@Tag(name = "Redis测试")
@RestController
@RequestMapping("/api/test")
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisTestController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "测试 Redis 连接")
    @GetMapping("/redis")
    public Result<?> testRedis() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试写入
            String testKey = "test:connection";
            String testValue = "Hello Redis! " + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, testValue);

            // 测试读取
            Object readValue = redisTemplate.opsForValue().get(testKey);

            result.put("write", "SUCCESS");
            result.put("read", readValue);
            result.put("message", "Redis 连接正常！");

            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return Result.error("Redis 连接失败: " + e.getMessage());
        }
    }

    @Operation(summary = "测试 Redis Pub/Sub")
    @GetMapping("/redis/pubsub")
    public Result<?> testPubSub() {
        return Result.success("Redis Pub/Sub 已配置，请查看控制台日志验证消息订阅");
    }
}
