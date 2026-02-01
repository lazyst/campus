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
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getId, itemId)
               .eq(Item::getDeleted, false)
               .ne(Item::getStatus, 0);  // 排除已删除（status=0）的物品

        Item item = this.getOne(wrapper);
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
        changeStatus(itemId, 3, 1);
    }

    @Override
    public void offline(Long itemId) {
        changeStatus(itemId, 1, 3);
    }

    @Override
    public void complete(Long itemId) {
        changeStatus(itemId, 1, 2);
    }

    /**
     * 变更物品状态
     * @param itemId 物品ID
     * @param fromStatus 期望的当前状态
     * @param toStatus 目标状态
     */
    private void changeStatus(Long itemId, int fromStatus, int toStatus) {
        Item item = this.getById(itemId);
        if (item != null && item.getStatus() == fromStatus) {
            item.setStatus(toStatus);
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
