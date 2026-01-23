package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Like;

/**
 * 点赞服务接口
 */
public interface LikeService extends IService<Like> {

    /**
     * 检查用户是否已点赞帖子
     */
    boolean hasLiked(Long userId, Long postId);

    /**
     * 切换点赞状态（如果已点赞则取消，未点赞则添加）
     * @return true=点赞成功, false=取消点赞成功
     */
    boolean toggleLike(Long userId, Long postId);
}
