package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Collect;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.CollectMapper;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收藏服务实现
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    private final PostService postService;
    private final NotificationService notificationService;

    public CollectServiceImpl(PostService postService, NotificationService notificationService) {
        this.postService = postService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean hasCollected(Long userId, Long postId) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .eq(Collect::getPostId, postId)
               .eq(Collect::getDeleted, false);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long postId) {
        if (hasCollected(userId, postId)) {
            // 已收藏，取消收藏
            LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collect::getUserId, userId)
                   .eq(Collect::getPostId, postId);
            this.remove(wrapper);

            // 帖子收藏数减1
            Post post = postService.getById(postId);
            if (post != null) {
                post.setCollectCount(post.getCollectCount() - 1);
                postService.updateById(post);
            }

            return false;
        } else {
            // 未收藏，添加收藏
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setPostId(postId);
            this.save(collect);

            // 帖子收藏数加1
            Post post = postService.getById(postId);
            if (post != null) {
                post.setCollectCount(post.getCollectCount() + 1);
                postService.updateById(post);

                // 创建通知（如果收藏的不是自己的帖子）
                if (!post.getUserId().equals(userId)) {
                    try {
                        Notification notification = new Notification();
                        notification.setUserId(post.getUserId()); // 通知给帖子作者
                        notification.setFromUserId(userId); // 收藏者
                        notification.setTargetId(postId); // 使用targetId存储帖子ID
                        notification.setType(3); // 3=收藏通知
                        notification.setIsRead(0);
                        notification.setContent("收藏了你的帖子");
                        notificationService.save(notification);
                    } catch (Exception e) {
                        // 通知创建失败不影响收藏功能
                        System.err.println("Failed to create collect notification: " + e.getMessage());
                    }
                }
            }

            return true;
        }
    }
}
