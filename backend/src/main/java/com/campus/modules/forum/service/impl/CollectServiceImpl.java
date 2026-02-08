package com.campus.modules.forum.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Collect;
import com.campus.modules.forum.entity.CollectSimple;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.CollectMapper;
import com.campus.modules.forum.mapper.CollectSimpleMapper;
import com.campus.modules.forum.service.CollectService;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.forum.service.PostService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * 收藏服务实现类
 */
@Service
@DS("slave")
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    private final PostService postService;
    private final NotificationService notificationService;
    private final CollectSimpleMapper collectSimpleMapper;

    public CollectServiceImpl(PostService postService, NotificationService notificationService, CollectSimpleMapper collectSimpleMapper) {
        this.postService = postService;
        this.notificationService = notificationService;
        this.collectSimpleMapper = collectSimpleMapper;
    }

    @Override
    public boolean hasCollected(Long userId, Long postId) {
        // 使用 CollectSimpleMapper 查询，绕过 @TableLogic 逻辑删除
        LambdaQueryWrapper<CollectSimple> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectSimple::getUserId, userId)
               .eq(CollectSimple::getPostId, postId);
        return collectSimpleMapper.selectCount(wrapper) > 0;
    }

    @Override
    // @Transactional  // 暂时移除事务注解测试
    public boolean toggleCollect(Long userId, Long postId) {
        // 使用 CollectSimpleMapper 查询，绕过 @TableLogic 逻辑删除
        LambdaQueryWrapper<CollectSimple> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectSimple::getUserId, userId)
               .eq(CollectSimple::getPostId, postId);

        CollectSimple existingCollect = collectSimpleMapper.selectOne(wrapper);

        if (existingCollect != null) {
            // 已收藏，取消收藏（物理删除）
            collectSimpleMapper.deleteById(existingCollect.getId());

            // 帖子收藏数减1
            Post post = postService.getById(postId);
            if (post != null) {
                post.setCollectCount(Math.max(0, post.getCollectCount() - 1));
                postService.updateById(post);
            }

            return false;
        } else {
            // 未收藏，添加收藏（直接新建）
            CollectSimple collect = new CollectSimple();
            collect.setUserId(userId);
            collect.setPostId(postId);
            // 使用 CollectSimpleMapper 直接插入
            collectSimpleMapper.insert(collect);

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
        // 使用 CollectSimpleMapper 查询，绕过 @TableLogic 逻辑删除
        LambdaQueryWrapper<CollectSimple> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(CollectSimple::getPostId);
        wrapper.eq(CollectSimple::getUserId, userId);
        wrapper.orderByDesc(CollectSimple::getId); // 按ID倒序等同于按创建时间倒序

        List<CollectSimple> collects = collectSimpleMapper.selectList(wrapper);

        if (collects == null) {
            return Collections.emptyList();
        }

        return collects.stream()
                .map(CollectSimple::getPostId)
                .collect(Collectors.toList());
    }
}
