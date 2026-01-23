package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Post;

/**
 * 帖子服务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 增加帖子浏览次数
     */
    void incrementViewCount(Long postId);

    /**
     * 增加帖子点赞次数
     */
    void incrementLikeCount(Long postId);

    /**
     * 减少帖子点赞次数
     */
    void decrementLikeCount(Long postId);

    /**
     * 增加帖子评论次数
     */
    void incrementCommentCount(Long postId);

    /**
     * 增加帖子收藏次数
     */
    void incrementCollectCount(Long postId);

    /**
     * 减少帖子收藏次数
     */
    void decrementCollectCount(Long postId);

    /**
     * 检查用户是否是帖子作者
     */
    boolean isAuthor(Long postId, Long userId);
}
