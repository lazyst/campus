package com.campus.modules.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.admin.service.AdminService;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.PostUpdateRequest;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台帖子管理控制器
 */
@Tag(name = "后台帖子管理")
@RestController
@RequestMapping("/api/admin/posts")
public class PostManagementController {

    private final PostService postService;
    private final CommentService commentService;
    private final AdminService adminService;
    private final AuthService authService;
    private final UserService userService;

    public PostManagementController(PostService postService, CommentService commentService,
                                     AdminService adminService, AuthService authService,
                                     UserService userService) {
        this.postService = postService;
        this.commentService = commentService;
        this.adminService = adminService;
        this.authService = authService;
        this.userService = userService;
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
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {

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

        // 处理排序
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "viewCount" -> wrapper.orderBy(true, isAsc, Post::getViewCount);
                case "likeCount" -> wrapper.orderBy(true, isAsc, Post::getLikeCount);
                case "commentCount" -> wrapper.orderBy(true, isAsc, Post::getCommentCount);
                case "createdAt" -> wrapper.orderBy(true, isAsc, Post::getCreatedAt);
                default -> wrapper.orderByDesc(Post::getCreatedAt);
            }
        } else {
            wrapper.orderByDesc(Post::getCreatedAt);
        }

        Page<Post> result = postService.page(pageParam, wrapper);

        // 填充每个帖子的用户信息
        result.getRecords().forEach(this::enrichPostWithUserInfo);

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

        // 填充用户信息
        enrichPostWithUserInfo(post);

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

    @Operation(summary = "更新帖子")
    @PutMapping("/{postId}")
    public Result<Post> update(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {

        verifyAdmin(authHeader);

        Post post = postService.getById(postId);
        if (post == null || (post.getStatus() != null && post.getStatus() == 0)) {
            return Result.error("帖子不存在");
        }

        // 更新字段
        if (request.getTitle() != null) {
            post.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }
        if (request.getImages() != null) {
            post.setImages(request.getImages());
        }
        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        postService.updateById(post);
        return Result.success(post);
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

    @Operation(summary = "获取帖子的评论列表")
    @GetMapping("/{postId}/comments")
    public Result<List<Comment>> getComments(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long postId) {

        verifyAdmin(authHeader);

        Post post = postService.getById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        List<Comment> comments = commentService.getByPostId(postId);
        return Result.success(comments);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long commentId) {

        verifyAdmin(authHeader);

        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // Soft delete - set status to 0
        comment.setStatus(0);
        commentService.updateById(comment);

        return Result.success();
    }

    /**
     * 验证管理员权限
     */
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

    /**
     * 填充帖子的用户信息（昵称和头像）
     */
    private void enrichPostWithUserInfo(Post post) {
        if (post == null || post.getUserId() == null) {
            return;
        }

        User user = userService.getById(post.getUserId());
        if (user != null) {
            post.setUserNickname(user.getNickname());
            post.setUserAvatar(user.getAvatar());
        } else {
            post.setUserNickname("匿名用户");
        }
    }
}
