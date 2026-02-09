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
 * 帖子控制器
 */
@Tag(name = "帖子管理")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final AuthService authService;
    private final UserService userService;

    public PostController(PostService postService, BoardService boardService, AuthService authService, UserService userService) {
        this.postService = postService;
        this.boardService = boardService;
        this.authService = authService;
        this.userService = userService;
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

        // 填充用户信息
        enrichPostsWithUserInfo(postPage.getRecords());

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

        // 填充用户信息
        enrichPostsWithUserInfo(Collections.singletonList(post));

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
        post.setStatus(Post.STATUS_NORMAL);

        postService.save(post);

        // 填充用户信息
        enrichPostsWithUserInfo(Collections.singletonList(post));

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

        // Soft delete - update status to deleted
        post.setStatus(Post.STATUS_DELETED);
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

        // 填充用户信息
        enrichPostsWithUserInfo(postPage.getRecords());

        return Result.success(postPage);
    }

    @Operation(summary = "按用户ID获取帖子列表")
    @GetMapping("/user/{userId}")
    public Result<Page<Post>> getPostsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId)
               .eq(Post::getStatus, 1)
               .orderByDesc(Post::getCreatedAt);
        postService.page(postPage, wrapper);

        // 填充用户信息
        enrichPostsWithUserInfo(postPage.getRecords());

        return Result.success(postPage);
    }

    @Operation(summary = "搜索帖子")
    @GetMapping("/search")
    public Result<Page<Post>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Post> postPage = new Page<>(page, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1)
               .and(wrapper1 -> wrapper1
                   .like(Post::getTitle, keyword)
                   .or()
                   .like(Post::getContent, keyword))
               .orderByDesc(Post::getCreatedAt);
        postService.page(postPage, wrapper);

        // 填充用户信息
        enrichPostsWithUserInfo(postPage.getRecords());

        return Result.success(postPage);
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
