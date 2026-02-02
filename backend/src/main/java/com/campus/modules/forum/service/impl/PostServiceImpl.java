package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.forum.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子服务实现类
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    /**
     * 增加帖子浏览次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId) {
        // VERSION 2026-02-01 FIXED - direct implementation
        Post post = this.getById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            this.updateById(post);
        }
    }

    /**
     * 增加帖子点赞次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementLikeCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            this.updateById(post);
        }
    }

    /**
     * 减少帖子点赞次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementLikeCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            this.updateById(post);
        }
    }

    /**
     * 增加帖子评论次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCommentCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            this.updateById(post);
        }
    }

    /**
     * 增加帖子收藏次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCollectCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null) {
            post.setCollectCount(post.getCollectCount() + 1);
            this.updateById(post);
        }
    }

    /**
     * 减少帖子收藏次数
     *
     * @param postId 帖子ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementCollectCount(Long postId) {
        Post post = this.getById(postId);
        if (post != null && post.getCollectCount() > 0) {
            post.setCollectCount(post.getCollectCount() - 1);
            this.updateById(post);
        }
    }

    /**
     * 判断用户是否为帖子作者
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 如果是作者返回true，否则返回false
     */
    @Override
    public boolean isAuthor(Long postId, Long userId) {
        Post post = this.getById(postId);
        return post != null && post.getUserId().equals(userId);
    }
}
