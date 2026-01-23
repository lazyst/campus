package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 后台帖子管理控制器
 */
@Tag(name = "后台帖子管理")
@RestController
@RequestMapping("/api/admin/posts")
public class PostManagementController {

    private final PostService postService;
    private final AdminService adminService;
    private final AuthService authService;

    public PostManagementController(PostService postService, AdminService adminService, AuthService authService) {
        this.postService = postService;
        this.adminService = adminService;
        this.authService = authService;
    }

    @Operation(summary = "获取帖子列表")
    @GetMapping
    public Result<Page<Post>> list(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        
        verifyAdmin(authHeader);

        Page<Post> pageParam = new Page<>(page, size);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        wrapper.eq(Post::getStatus, 1); // 默认只查询正常状态的帖子
        
        if (boardId != null) {
            wrapper.eq(Post::getBoardId, boardId);
        }
        
        if (userId != null) {
            wrapper.eq(Post::getUserId, userId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Post::getTitle, keyword)
                            .or()
                            .like(Post::getContent, keyword));
        }
        
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        
        wrapper.orderByDesc(Post::getCreatedAt);
        
        Page<Post> result = postService.page(pageParam, wrapper);
        
        // Clear sensitive fields if needed
        return Result.success(result);
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{postId}")
    public Result<Post> detail(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        
        verifyAdmin(authHeader);
        
        Post post = postService.getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() == 0)) {
            return Result.error("帖子不存在");
        }
        
        // Increment view count
        postService.incrementViewCount(postId);
        
        return Result.success(post);
    }

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{postId}")
    public Result<Void> delete(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {
        
        verifyAdmin(authHeader);
        
        Post post = postService.getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() == 0)) {
            return Result.error("帖子不存在");
        }
        
        // Soft delete - set status to 0 (deleted)
        post.setStatus(0);
        postService.updateById(post);
        
        return Result.success();
    }

    @Operation(summary = "获取帖子统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(
            @RequestHeader("Authorization") String authHeader) {
        
        verifyAdmin(authHeader);
        
        long total = postService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                .ne(Post::getStatus, 0)); // Exclude deleted
        
        long today = postService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                .ne(Post::getStatus, 0)
                .ge(Post::getCreatedAt, java.time.LocalDateTime.now().minusDays(1)));
        
        long thisWeek = postService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Post>()
                .ne(Post::getStatus, 0)
                .ge(Post::getCreatedAt, java.time.LocalDateTime.now().minusWeeks(1)));
        
        Map<String, Object> result = Map.of(
            "total", total,
            "today", today,
            "thisWeek", thisWeek
        );
        
        return Result.success(result);
    }

    /**
     * 验证管理员权限
     */
    private void verifyAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("未授权访问");
        }
        
        String token = authHeader.replace("Bearer ", "");
        Long adminId = authService.getUserIdFromToken(token);
        
        if (!adminService.isSuperAdmin(adminId)) {
            throw new SecurityException("权限不足");
        }
    }
}
