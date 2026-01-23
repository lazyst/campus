package com.campus.modules.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.trade.entity.Item;
import com.campus.modules.trade.mapper.ItemMapper;
import com.campus.modules.trade.service.ItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 物品服务实现
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    @Override
    public List<Item> getByUserId(Long userId) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getUserId, userId)
               .eq(Item::getDeleted, false)
               .orderByDesc(Item::getCreatedAt);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public Item getDetail(Long itemId) {
        Item item = this.getById(itemId);
        if (item != null) {
            // 增加浏览次数
            item.setViewCount(item.getViewCount() + 1);
            this.updateById(item);
        }
        return item;
    }

    @Override
    public boolean isAuthor(Long userId, Long itemId) {
        Item item = this.getById(itemId);
        return item != null && item.getUserId().equals(userId);
    }

    @Override
    public void online(Long itemId) {
        Item item = this.getById(itemId);
        if (item != null && item.getStatus() == 3) {
            item.setStatus(1);  // 上架
            this.updateById(item);
        }
    }

    @Override
    public void offline(Long itemId) {
        Item item = this.getById(itemId);
        if (item != null && item.getStatus() == 1) {
            item.setStatus(3);  // 下架
            this.updateById(item);
        }
    }

    @Override
    public void complete(Long itemId) {
        Item item = this.getById(itemId);
        if (item != null && item.getStatus() == 1) {
            item.setStatus(2);  // 已完成
            this.updateById(item);
        }
    }

    @Override
    @Transactional
    public void incrementContactCount(Long itemId) {
        Item item = this.getById(itemId);
        if (item != null) {
            item.setContactCount(item.getContactCount() + 1);
            this.updateById(item);
        }
    }
}
