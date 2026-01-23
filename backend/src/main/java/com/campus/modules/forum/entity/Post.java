package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("post")
public class Post extends BaseEntity {

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 板块ID
     */
    private Long boardId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 图片JSON数组
     */
    private String images;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 收藏次数
     */
    private Integer collectCount;

    /**
     * 状态：0删除 1正常
     */
    private Integer status;

    /**
     * 发布者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String userNickname;

    /**
     * 发布者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String userAvatar;
}
