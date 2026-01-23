package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify post exists
        if (postService.getById(postId) == null) {
            return Result.error("帖子不存在");
        }

        boolean isCollected = collectService.toggleCollect(userId, postId);
        return Result.success(isCollected);
    }

    @Operation(summary = "检查用户是否已收藏帖子")
    @GetMapping("/{postId}/collect/check")
    public Result<Boolean> checkCollected(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        boolean hasCollected = collectService.hasCollected(userId, postId);
        return Result.success(hasCollected);
    }
}
