package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.service.LikeService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
@Slf4j
@Tag(name = "点赞管理")
@RestController
@RequestMapping("/api/posts")
public class LikeController {

    private final LikeService likeService;
    private final PostService postService;
    private final AuthService authService;

    public LikeController(LikeService likeService, PostService postService, AuthService authService) {
        this.likeService = likeService;
        this.postService = postService;
        this.authService = authService;
    }

    @Operation(summary = "点赞/取消点赞帖子")
    @PostMapping("/{postId}/like")
    public Result<Boolean> toggleLike(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long postId) {
        // 处理未登录或token为空的情况
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("请先登录后再点赞");
        }
        
        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);
            
            if (userId == null) {
                // Token无效或用户不存在
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            // Verify post exists
            if (postService.getById(postId) == null) {
                return Result.error("帖子不存在");
            }

            boolean isLiked = likeService.toggleLike(userId, postId);
            return Result.success(isLiked);
        } catch (Exception e) {
            log.error("点赞操作失败: {}", e.getMessage());
            return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
    }

    @Operation(summary = "检查用户是否已点赞帖子")
    @GetMapping("/{postId}/like/check")
    public Result<Boolean> checkLiked(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long postId) {
        // 处理未登录或token为空的情况
        if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
            return Result.success(false);
        }

        try {
            Long userId = authService.getUserIdFromAuthHeader(authHeader);
            boolean hasLiked = likeService.hasLiked(userId, postId);
            return Result.success(hasLiked);
        } catch (Exception e) {
            log.error("检查点赞状态失败: {}", e.getMessage());
            return Result.success(false);
        }
    }
}
