package com.campus.modules.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.CommentCreateRequest;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;
    private final AuthService authService;

    public CommentController(CommentService commentService, PostService postService, AuthService authService) {
        this.commentService = commentService;
        this.postService = postService;
        this.authService = authService;
    }

    @Operation(summary = "获取帖子的评论列表")
    @GetMapping("/post/{postId}")
    public Result<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.getByPostId(postId);
        return Result.success(comments);
    }

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Comment> createComment(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CommentCreateRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify post exists
        if (postService.getById(request.getPostId()) == null) {
            return Result.error("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setPostId(request.getPostId());
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setStatus(1);

        commentService.save(comment);
        return Result.success(comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        // Check if user is the author
        if (!commentService.isAuthor(id, userId)) {
            return Result.error("只能删除自己的评论");
        }

        // Soft delete
        comment.setStatus(0);
        commentService.updateById(comment);
        return Result.success();
    }
}
