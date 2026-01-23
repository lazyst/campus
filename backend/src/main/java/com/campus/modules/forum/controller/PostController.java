package com.campus.modules.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.PostCreateRequest;
import com.campus.modules.forum.dto.PostUpdateRequest;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.service.BoardService;
import com.campus.modules.forum.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子控制器
 */
@Tag(name = "帖子管理")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final AuthService authService;

    public PostController(PostService postService, BoardService boardService, AuthService authService) {
        this.postService = postService;
        this.boardService = boardService;
        this.authService = authService;
    }

    @Operation(summary = "分页获取帖子列表")
    @GetMapping
    public Result<Page<Post>> pagePosts(
            @RequestParam(required = false) Long boardId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)  // Only normal status
               .eq(boardId != null, Post::getBoardId, boardId)
               .orderByDesc(Post::getCreatedAt);
        postService.page(postPage, wrapper);
        return Result.success(postPage);
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public Result<Post> getPost(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null || post.getStatus() != 1) {
            return Result.error("帖子不存在或已被删除");
        }
        // Increment view count
        postService.incrementViewCount(id);
        return Result.success(post);
    }

    @Operation(summary = "创建帖子")
    @PostMapping
    public Result<Post> createPost(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PostCreateRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        // Verify board exists
        if (boardService.getById(request.getBoardId()) == null) {
            return Result.error("板块不存在");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setBoardId(request.getBoardId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImages(request.getImages());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setStatus(1);

        postService.save(post);
        return Result.success(post);
    }

    @Operation(summary = "更新帖子")
    @PutMapping("/{id}")
    public Result<Post> updatePost(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        // Check if user is the author
        if (!postService.isAuthor(id, userId)) {
            return Result.error("只能编辑自己的帖子");
        }

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

    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        // Check if user is the author
        if (!postService.isAuthor(id, userId)) {
            return Result.error("只能删除自己的帖子");
        }

        // Soft delete - update status to 0
        post.setStatus(0);
        postService.updateById(post);
        return Result.success();
    }

    @Operation(summary = "获取我发布的帖子")
    @GetMapping("/my")
    public Result<Page<Post>> getMyPosts(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
               .eq(Post::getStatus, 1)
               .orderByDesc(Post::getCreatedAt);
        postService.page(postPage, wrapper);
        return Result.success(postPage);
    }
}
