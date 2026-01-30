package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Collect;
import com.campus.modules.forum.entity.Post;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface CollectService extends IService<Collect> {

    /**
     * 检查用户是否已收藏帖子
     */
    boolean hasCollected(Long userId, Long postId);

    /**
     * 切换收藏状态（如果已收藏则取消，未收藏则添加）
     * @return true=收藏成功, false=取消收藏成功
     */
    boolean toggleCollect(Long userId, Long postId);

    /**
     * 获取用户收藏的帖子列表
     */
    List<Post> getCollectedPosts(Long userId);

    /**
     * 获取用户收藏的帖子ID列表
     */
    List<Long> getCollectedPostIds(Long userId);
}
