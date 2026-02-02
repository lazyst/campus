package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.dto.NotificationDTO;
import com.campus.modules.forum.entity.Like;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.LikeMapper;
import com.campus.modules.forum.publisher.NotificationPublisher;
import com.campus.modules.forum.service.LikeService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import com.campus.modules.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现类
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    private final PostService postService;
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;
    private final UserService userService;

    public LikeServiceImpl(PostService postService,
                          NotificationService notificationService,
                          NotificationPublisher notificationPublisher,
                          UserService userService) {
        this.postService = postService;
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
        this.userService = userService;
    }

    @Override
    public boolean hasLiked(Long userId, Long postId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getPostId, postId)
               .eq(Like::getDeleted, false); // 只检查未删除的记录
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        // 查找未删除的点赞记录
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getPostId, postId)
               .eq(Like::getDeleted, false);

        Like existingLike = this.getOne(wrapper);

        if (existingLike != null) {
            // Already liked, remove like (使用软删除)
            existingLike.setDeleted(1);
            this.updateById(existingLike);
            postService.decrementLikeCount(postId);
            return false; // Like removed
        } else {
            // Not liked, add like (先检查是否有已删除的记录)
            LambdaQueryWrapper<Like> deletedWrapper = new LambdaQueryWrapper<>();
            deletedWrapper.eq(Like::getUserId, userId)
                    .eq(Like::getPostId, postId)
                    .eq(Like::getDeleted, 1);

            Like deletedLike = this.getOne(deletedWrapper);
            if (deletedLike != null) {
                // 恢复已删除的点赞记录
                deletedLike.setDeleted(0);
                this.updateById(deletedLike);
            } else {
                // 创建新的点赞记录
                Like like = new Like();
                like.setUserId(userId);
                like.setPostId(postId);
                this.save(like);
            }
            postService.incrementLikeCount(postId);

            // 创建通知并推送（如果点赞的不是自己的帖子）
            Post post = postService.getById(postId);
            if (post != null && !post.getUserId().equals(userId)) {
                try {
                    Notification notification = new Notification();
                    notification.setUserId(post.getUserId()); // 通知给帖子作者
                    notification.setFromUserId(userId); // 点赞者
                    notification.setPostId(postId); // 使用targetId存储帖子ID
                    notification.setType(2); // 2=点赞通知
                    notification.setIsRead(0);
                    notification.setContent("点赞了你的帖子");
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

                    notificationPublisher.publish(post.getUserId(), dto);

                } catch (Exception e) {
                    // 通知创建失败不影响点赞功能
                }
            }

            return true; // Like added
        }
    }
}
