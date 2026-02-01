package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.StringTypeHandler;

/**
 * 评论实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
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
    @TableField(typeHandler = StringTypeHandler.class)
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
