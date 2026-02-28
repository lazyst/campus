package com.campus.modules.trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.modules.trade.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 物品Mapper接口
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 原子递增浏览次数
     */
    @Update("UPDATE item SET view_count = view_count + 1 WHERE id = #{itemId} AND deleted = false")
    int incrementViewCount(@Param("itemId") Long itemId);

    /**
     * 原子递增联系次数
     */
    @Update("UPDATE item SET contact_count = contact_count + 1 WHERE id = #{itemId} AND deleted = false")
    int incrementContactCount(@Param("itemId") Long itemId);
}
