package com.campus.modules.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.trade.entity.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物品Mapper接口
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
