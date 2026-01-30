package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 收藏实体类
 * 注意：collect 表没有 deleted 字段，使用物理删除
 * JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("`collect`")
public class Collect extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 注意：由于 collect 表没有 deleted 字段，这里不再使用 @TableLogic
     * 收藏功能采用物理删除方式
     */
}
