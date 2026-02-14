package com.campus.modules.forum.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campus.common.Result;
import com.campus.common.ResultCode;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.forum.dto.CommentCreateRequest;
import com.campus.modules.forum.dto.NotificationDTO;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.publisher.NotificationPublisher;
import com.campus.modules.forum.service.CommentService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;
    private final UserService userService;

    public CommentController(CommentService commentService, PostService postService,
            AuthService authService, NotificationService notificationService,
            NotificationPublisher notificationPublisher, UserService userService) {
        this.commentService = commentService;
        this.postService = postService;
        this.authService = authService;
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
        this.userService = userService;
    }

    @Operation(summary = "获取帖子的评论列表")
    @GetMapping("/post/{postId}")
    public Result<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.getByPostId(postId);

        // 填充评论者的昵称和头像
        for (Comment comment : comments) {
            if (comment.getUserId() != null) {
                User user = userService.getById(comment.getUserId());
                if (user != null) {
                    comment.setUserNickname(user.getNickname());
                    comment.setUserAvatar(user.getAvatar());
                }
            }
        }

        return Result.success(comments);
    }

    @Operation(summary = "创建评论")
    @PostMapping
    public Result<Comment> createComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CommentCreateRequest request) {
        // 处理未登录或token为空的情况
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("请先登录后再评论");
        }

        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = authService.getUserIdFromToken(token);

            if (userId == null) {
                // Token无效或用户不存在
                return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }

            // Verify post exists
            Post post = postService.getById(request.getPostId());
            if (post == null) {
                return Result.error("帖子不存在");
            }

            Comment comment = new Comment();
            comment.setUserId(userId);
            comment.setPostId(request.getPostId());
            comment.setParentId(request.getParentId());
            comment.setContent(request.getContent());
            comment.setStatus(1);

            commentService.save(comment);

            // 创建通知并推送
            Long notifyUserId = null;
            String notifyContent = "";

            if (request.getParentId() != null) {
                // 楼中楼回复：通知被回复的评论作者
                Comment parentComment = commentService.getById(request.getParentId());
                if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                    notifyUserId = parentComment.getUserId();
                    notifyContent = "回复了你的评论";
                }
            } else if (!post.getUserId().equals(userId)) {
                // 普通评论：通知帖子作者
                notifyUserId = post.getUserId();
                notifyContent = "评论了你的帖子";
            }

            // 发送通知
            if (notifyUserId != null) {
                try {
                    Notification notification = new Notification();
                    notification.setUserId(notifyUserId);
                    notification.setFromUserId(userId);
                    notification.setPostId(request.getPostId());
                    notification.setCommentId(comment.getId());
                    notification.setType(1); // 1=评论通知
                    notification.setIsRead(0);
                    notification.setContent(notifyContent);
                    notificationService.save(notification);

                    // 通过 WebSocket 实时推送通知
                    NotificationDTO dto = NotificationDTO.builder()
                            .id(notification.getId())
                            .userId(notification.getUserId())
                            .fromUserId(notification.getFromUserId())
                            .postId(notification.getPostId())
                            .type(notification.getType())
                            .content(notification.getContent())
                            .isRead(notification.getIsRead())
                            .createdAt(notification.getCreatedAt())
                            .build();

                    notificationPublisher.publish(notifyUserId, dto);

                } catch (Exception e) {
                    // 通知创建失败不影响评论功能
                }
            }

            return Result.success(comment);
        } catch (Exception e) {
            // Token无效时返回未授权错误
            return Result.error(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
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
