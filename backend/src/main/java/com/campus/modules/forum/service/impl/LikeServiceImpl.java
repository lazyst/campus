package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Like;
import com.campus.modules.forum.mapper.LikeMapper;
import com.campus.modules.forum.service.LikeService;
import com.campus.modules.forum.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现类
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    private final PostService postService;

    public LikeServiceImpl(PostService postService) {
        this.postService = postService;
    }

    @Override
    public boolean hasLiked(Long userId, Long postId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getPostId, postId);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getPostId, postId);

        if (this.count(wrapper) > 0) {
            // Already liked, remove like
            this.remove(wrapper);
            postService.decrementLikeCount(postId);
            return false; // Like removed
        } else {
            // Not liked, add like
            Like like = new Like();
            like.setUserId(userId);
            like.setPostId(postId);
            this.save(like);
            postService.incrementLikeCount(postId);
            return true; // Like added
        }
    }
}
