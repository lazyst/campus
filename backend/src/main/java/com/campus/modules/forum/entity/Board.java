package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 板块实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("board")
public class Board extends BaseEntity {

    /**
     * 板块名称
     */
    private String name;

    /**
     * 板块描述
     */
    private String description;

    /**
     * 板块图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0禁用 1正常
     */
    private Integer status;
}
