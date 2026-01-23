package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    /**
     * 通知类型：1评论 2点赞 3收藏
     */
    private Integer type;

    /**
     * 接收者ID
     */
    private Long userId;

    /**
     * 触发者ID（谁触发的通知）
     */
    private Long fromUserId;

    /**
     * 相关帖子ID
     */
    private Long postId;

    /**
     * 相关评论ID（评论通知时使用）
     */
    private Long commentId;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读：0未读 1已读
     */
    private Integer isRead;

    /**
     * 触发者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String fromUserNickname;

    /**
     * 触发者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String fromUserAvatar;

    /**
     * 帖子标题（非数据库字段）
     */
    @TableField(exist = false)
    private String postTitle;
}
