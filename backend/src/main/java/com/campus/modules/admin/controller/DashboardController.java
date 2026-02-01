package com.campus.modules.admin.controller;

import com.campus.common.Result;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.admin.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台首页数据聚合控制器
 */
@Tag(name = "后台首页")
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AdminService adminService;

    public DashboardController(DashboardService dashboardService, AdminService adminService) {
        this.dashboardService = dashboardService;
        this.adminService = adminService;
    }

    @Operation(summary = "获取首页统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestHeader("Authorization") String authHeader) {
        verifyAdmin(authHeader);
        return Result.success(dashboardService.getStats());
    }

    @Operation(summary = "获取近7天趋势数据")
    @GetMapping("/trend")
    public Result<Map<String, Object>> getTrend(@RequestHeader("Authorization") String authHeader) {
        verifyAdmin(authHeader);
        return Result.success(dashboardService.getTrendData());
    }

    @Operation(summary = "获取最近活跃数据")
    @GetMapping("/recent")
    public Result<Map<String, Object>> getRecent(@RequestHeader("Authorization") String authHeader) {
        verifyAdmin(authHeader);
        return Result.success(dashboardService.getRecentActivity());
    }

    @Operation(summary = "获取系统状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(@RequestHeader("Authorization") String authHeader) {
        verifyAdmin(authHeader);
        return Result.success(dashboardService.getSystemStatus());
    }

    @Operation(summary = "获取完整首页数据")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview(@RequestHeader("Authorization") String authHeader) {
        verifyAdmin(authHeader);
        return Result.success(dashboardService.getOverview());
    }

    private void verifyAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("未授权访问");
        }

        String token = authHeader.replace("Bearer ", "");
        Long adminId = adminService.getAdminIdFromToken(token);

        if (adminId == null || !adminService.isSuperAdmin(adminId)) {
            throw new SecurityException("权限不足");
        }
    }
}
