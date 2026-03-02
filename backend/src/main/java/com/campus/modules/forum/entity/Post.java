package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.StringTypeHandler;

import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("post", indexes = {
    @TableIndex(name = "idx_post_status", value = {"status"}),
    @TableIndex(name = "idx_post_board", value = {"board_id"}),
    @TableIndex(name = "idx_post_user", value = {"user_id"}),
    @TableIndex(name = "idx_post_created", value = {"created_at"})
})
public class Post extends BaseEntity {

    /**
     * 帖子状态：已删除
     */
    public static final Integer STATUS_DELETED = 0;

    /**
     * 帖子状态：正常
     */
    public static final Integer STATUS_NORMAL = 1;

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
    @TableField(typeHandler = StringTypeHandler.class)
    private String content;

    /**
     * 图片JSON数组
     */
    @TableField(typeHandler = StringTypeHandler.class)
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
     * 缩略图URL（非数据库字段）
     */
    @TableField(exist = false)
    private String thumbnail;

    /**
     * 发布者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String userAvatar;
}
