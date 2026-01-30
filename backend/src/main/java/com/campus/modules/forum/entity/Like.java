package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 点赞实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("`like`")
public class Like extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private Long postId;
}
