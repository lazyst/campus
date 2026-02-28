package com.campus.modules.forum.controller;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏控制器
 */
@Slf4j
@Tag(name = "收藏管理")
@RestController
@RequestMapping("/api/posts")
public class CollectController {

    private final CollectService collectService;
    private final PostService postService;
    private final AuthService authService;
    private final UserService userService;

    public CollectController(CollectService collectService, PostService postService, AuthService authService, UserService userService) {
        this.collectService = collectService;
        this.postService = postService;
        this.authService = authService;
        this.userService = userService;
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
            Long userId = authService.getUserIdFromAuthHeader(authHeader);

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
            log.error("收藏操作失败: {}", e.getMessage());
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
            Long userId = authService.getUserIdFromAuthHeader(authHeader);
            boolean hasCollected = collectService.hasCollected(userId, postId);
            return Result.success(hasCollected);
        } catch (Exception e) {
            log.error("检查收藏状态失败: {}", e.getMessage());
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
            Long userId = authService.getUserIdFromAuthHeader(authHeader);

            if (userId == null) {
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            List<Post> collectedPosts = collectService.getCollectedPosts(userId);
            // 填充用户信息
            enrichPostsWithUserInfo(collectedPosts);
            return Result.success(collectedPosts);
        } catch (Exception e) {
            log.error("获取收藏列表失败: {}", e.getMessage());
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * 填充帖子的用户信息（昵称和头像）
     */
    private void enrichPostsWithUserInfo(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // 收集所有userId
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 填充到每个帖子
        for (Post post : posts) {
            if (post.getUserId() != null) {
                User user = userMap.get(post.getUserId());
                if (user != null) {
                    post.setUserNickname(user.getNickname());
                    post.setUserAvatar(user.getAvatar());
                } else {
                    post.setUserNickname("匿名用户");
                }
            } else {
                post.setUserNickname("匿名用户");
            }
        }
    }
}
