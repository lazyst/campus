package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@Tag(name = "通知管理")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<List<com.campus.modules.forum.entity.Notification>> getNotifications(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        List<com.campus.modules.forum.entity.Notification> notifications = notificationService.getByUserId(userId);
        return Result.success(notifications);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/unread/count")
    public Result<Map<String, Object>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        int count = notificationService.getUnreadCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{notificationId}/read")
    public Result<Void> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long notificationId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify ownership
        com.campus.modules.forum.entity.Notification notification = notificationService.getById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return Result.error("通知不存在");
        }

        notificationService.markAsRead(notificationId);
        return Result.success();
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read/all")
    public Result<Void> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}
