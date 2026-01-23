package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Comment;

/**
 * 评论服务接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 根据帖子ID查询评论列表
     */
    java.util.List<Comment> getByPostId(Long postId);

    /**
     * 检查用户是否是评论作者
     */
    boolean isAuthor(Long commentId, Long userId);
}
