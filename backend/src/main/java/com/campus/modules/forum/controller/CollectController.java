package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/api/posts")
public class CollectController {

    private final CollectService collectService;
    private final PostService postService;
    private final AuthService authService;

    public CollectController(CollectService collectService, PostService postService, AuthService authService) {
        this.collectService = collectService;
        this.postService = postService;
        this.authService = authService;
    }

    @Operation(summary = "收藏/取消收藏帖子")
    @PostMapping("/{postId}/collect")
    public Result<Boolean> toggleCollect(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long postId) {
        // 处理未登录或token为空的情况
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED, "请先登录后再收藏");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = authService.getUserIdFromToken(token);

            // userId为null说明token无效
            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            // Verify post exists
            if (postService.getById(postId) == null) {
                return Result.error("帖子不存在");
            }

            boolean isCollected = collectService.toggleCollect(userId, postId);
            return Result.success(isCollected);
        } catch (Exception e) {
            // Token无效时返回未授权错误
            return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    @Operation(summary = "检查用户是否已收藏帖子")
    @GetMapping("/{postId}/collect/check")
    public Result<Boolean> checkCollected(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long postId) {
        // 处理未登录或token为空的情况
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            return Result.success(false);
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = authService.getUserIdFromToken(token);
            boolean hasCollected = collectService.hasCollected(userId, postId);
            return Result.success(hasCollected);
        } catch (Exception e) {
            // Token无效时返回未收藏状态
            return Result.success(false);
        }
    }

    @Operation(summary = "获取当前用户的收藏帖子列表")
    @GetMapping("/collections")
    public Result<List<Post>> getMyCollections(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 处理未登录或token为空的情况
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED, "请先登录");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = authService.getUserIdFromToken(token);

            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            List<Post> collectedPosts = collectService.getCollectedPosts(userId);
            return Result.success(collectedPosts);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }
}
