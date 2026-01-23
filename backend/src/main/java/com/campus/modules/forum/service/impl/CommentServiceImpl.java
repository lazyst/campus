package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Override
    public List<Comment> getByPostId(Long postId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId)
               .eq(Comment::getStatus, 1)
               .orderByAsc(Comment::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    public boolean isAuthor(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        return comment != null && comment.getUserId().equals(userId);
    }
}
