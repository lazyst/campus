package com.campus.modules.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 闲置物品实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("item")
public class Item extends BaseEntity {

    /**
     * 发布者ID
     */
    private Long userId;

    /**
     * 类型：1收购 2出售
     */
    private Integer type;

    /**
     * 分类
     */
    private String category;

    /**
     * 物品标题
     */
    private String title;

    /**
     * 物品描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 图片JSON数组
     */
    private String images;

    /**
     * 状态：0删除 1正常 2已完成 3已下架
     */
    private Integer status;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 联系次数
     */
    private Integer contactCount;

    /**
     * 交易地点
     */
    private String location;

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
    
    /**
     * 兼容前端字段名：卖家昵称
     */
    @TableField(exist = false)
    private String sellerName;
    
    /**
     * 兼容前端字段名：卖家头像
     */
    @TableField(exist = false)
    private String sellerAvatar;
}
