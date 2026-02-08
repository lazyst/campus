package com.campus.modules.forum.service.impl;
import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Comment;
import com.campus.modules.forum.mapper.CommentMapper;
import com.campus.modules.forum.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论服务实现类
 */
@Service
@DS("slave")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    /**
     * 根据帖子ID获取评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表（按创建时间升序排列）
     */
    @Override
    public List<Comment> getByPostId(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getStatus, 1)
               .orderByAsc(Comment::getCreatedAt);
        return this.list(wrapper);
    }

    /**
     * 判断用户是否为评论作者
     *
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 如果是作者返回true，否则返回false
     */
    @Override
    public boolean isAuthor(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        return comment != null && comment.getUserId().equals(userId);
    }
}
