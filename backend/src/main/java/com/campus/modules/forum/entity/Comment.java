package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    /**
     * 评论者ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 父评论ID（用于回复）
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态：0删除 1正常
     */
    private Integer status;

    /**
     * 评论者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String userNickname;

    /**
     * 评论者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String userAvatar;
}
