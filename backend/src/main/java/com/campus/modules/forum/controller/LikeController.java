package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.service.LikeService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 */
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
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify post exists
        if (postService.getById(postId) == null) {
            return Result.error("帖子不存在");
        }

        boolean isLiked = likeService.toggleLike(userId, postId);
        return Result.success(isLiked);
    }

    @Operation(summary = "检查用户是否已点赞帖子")
    @GetMapping("/{postId}/like/check")
    public Result<Boolean> checkLiked(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        boolean hasLiked = likeService.hasLiked(userId, postId);
        return Result.success(hasLiked);
    }
}
