package com.campus.modules.forum.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Post;
import com.campus.modules.forum.mapper.PostMapper;
import com.campus.modules.forum.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId) {
        updateCount(postId, "view_count", 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementLikeCount(Long postId) {
        updateCount(postId, "like_count", 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementLikeCount(Long postId) {
        if (!hasPositiveValue(postId, "like_count")) {
            return;
        }
        updateCount(postId, "like_count", -1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCommentCount(Long postId) {
        updateCount(postId, "comment_count", 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementCollectCount(Long postId) {
        updateCount(postId, "collect_count", 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decrementCollectCount(Long postId) {
        if (!hasPositiveValue(postId, "collect_count")) {
            return;
        }
        updateCount(postId, "collect_count", -1);
    }

    @Override
    @DS("slave")
    public boolean isAuthor(Long postId, Long userId) {
        Post post = this.getById(postId);
        return post != null && post.getUserId().equals(userId);
    }

    private void updateCount(Long postId, String field, int delta) {
        if (postId == null) {
            return;
        }
        LambdaUpdateWrapper<Post> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Post::getId, postId);
        wrapper.setSql(field + " = " + field + " + " + delta);
        this.update(wrapper);
    }

    private boolean hasPositiveValue(Long postId, String field) {
        Post post = this.getById(postId);
        if (post == null) {
            return false;
        }
        return switch (field) {
            case "like_count" -> post.getLikeCount() != null && post.getLikeCount() > 0;
            case "collect_count" -> post.getCollectCount() != null && post.getCollectCount() > 0;
            default -> true;
        };
    }
}
