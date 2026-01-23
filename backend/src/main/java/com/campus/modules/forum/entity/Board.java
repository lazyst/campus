package com.campus.modules.forum.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 板块实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
