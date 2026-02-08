package com.campus.modules.trade.service.impl;
import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.entity.ItemCollect;
import com.campus.modules.trade.mapper.ItemCollectMapper;
import com.campus.modules.trade.service.ItemCollectService;
import com.campus.modules.trade.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物品收藏服务实现
 */
@Service
@DS("slave")
public class ItemCollectServiceImpl extends ServiceImpl<ItemCollectMapper, ItemCollect> implements ItemCollectService {

    private final ItemService itemService;

    public ItemCollectServiceImpl(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public boolean hasCollected(Long userId, Long itemId) {
        LambdaQueryWrapper<ItemCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCollect::getUserId, userId)
               .eq(ItemCollect::getItemId, itemId)
               .eq(ItemCollect::getDeleted, false);
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public boolean toggleCollect(Long userId, Long itemId) {
        if (hasCollected(userId, itemId)) {
            // 已收藏，取消收藏
            LambdaQueryWrapper<ItemCollect> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ItemCollect::getUserId, userId)
                   .eq(ItemCollect::getItemId, itemId);
            this.remove(wrapper);
            return false;
        } else {
            // 未收藏，添加收藏
            ItemCollect collect = new ItemCollect();
            collect.setUserId(userId);
            collect.setItemId(itemId);
            this.save(collect);
            return true;
        }
    }

    @Override
    public List<ItemCollect> getByUserId(Long userId) {
        LambdaQueryWrapper<ItemCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ItemCollect::getUserId, userId)
               .eq(ItemCollect::getDeleted, false)
               .orderByDesc(ItemCollect::getCreatedAt);
        return this.list(wrapper);
    }
}
