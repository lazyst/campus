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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        // collect 表没有 deleted 字段，直接查询
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .eq(Collect::getPostId, postId);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long postId) {
        // 查找收藏记录（物理删除模式，无 deleted 字段）
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .eq(Collect::getPostId, postId);

        Collect existingCollect = this.getOne(wrapper);

        if (existingCollect != null) {
            // 已收藏，取消收藏（物理删除）
            this.removeById(existingCollect.getId());

            // 帖子收藏数减1
            Post post = postService.getById(postId);
            if (post != null) {
                post.setCollectCount(Math.max(0, post.getCollectCount() - 1));
                postService.updateById(post);
            }

            return false;
        } else {
            // 未收藏，添加收藏（直接新建）
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
                        notification.setPostId(postId);
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

    @Override
    public List<Post> getCollectedPosts(Long userId) {
        // 获取用户收藏的所有帖子ID
        List<Long> postIds = getCollectedPostIds(userId);
        
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 直接查询帖子
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Post::getId, postIds)
               .eq(Post::getStatus, 1);
        
        List<Post> posts = postService.list(wrapper);
        
        // 按收藏时间排序
        posts.sort((p1, p2) -> {
            int idx1 = postIds.indexOf(p1.getId());
            int idx2 = postIds.indexOf(p2.getId());
            return Integer.compare(idx2, idx1);
        });
        
        return posts;
    }

    @Override
    public List<Long> getCollectedPostIds(Long userId) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Collect::getPostId);
        wrapper.eq(Collect::getUserId, userId);
        wrapper.orderByDesc(Collect::getCreatedAt);
        
        List<Collect> collects = this.list(wrapper);
        
        if (collects == null) {
            return Collections.emptyList();
        }
        
        return collects.stream()
                .map(Collect::getPostId)
                .collect(Collectors.toList());
    }
}
