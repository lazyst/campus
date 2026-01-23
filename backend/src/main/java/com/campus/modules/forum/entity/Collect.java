package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}
