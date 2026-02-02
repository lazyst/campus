package com.campus.modules.common.controller;

import com.campus.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统健康检查控制器
 */
@Tag(name = "系统管理", description = "系统健康检查接口")
@RestController
@RequestMapping("/api/health")
public class SystemController {

    @Operation(summary = "健康检查")
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now().toString());
        healthInfo.put("service", "campus-helping-platform");
        return Result.success(healthInfo);
    }
}
