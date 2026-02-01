package com.campus.modules.admin.service;

import java.util.Map;

/**
 * Dashboard服务接口
 */
public interface DashboardService {

    /**
     * 获取首页统计数据
     */
    Map<String, Object> getStats();

    /**
     * 获取近7天趋势数据
     */
    Map<String, Object> getTrendData();

    /**
     * 获取最近活跃数据
     */
    Map<String, Object> getRecentActivity();

    /**
     * 获取系统状态
     */
    Map<String, Object> getSystemStatus();

    /**
     * 获取完整首页数据（聚合接口）
     */
    Map<String, Object> getOverview();
}
