package com.campus.modules.trade.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物品收藏实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("item_collect")
public class ItemCollect extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 物品ID
     */
    private Long itemId;
}
