package com.campus.modules.trade.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
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
@DS("slave")
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
    @DS("master")
    public Item getDetail(Long itemId) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getId, itemId)
               .eq(Item::getDeleted, false)
               .ne(Item::getStatus, 0);  // 排除已删除（status=0）的物品

        Item item = this.getOne(wrapper);
        if (item != null) {
            // 使用原子更新增加浏览次数，避免并发问题
            this.baseMapper.incrementViewCount(itemId);
            item.setViewCount(item.getViewCount() + 1);
        }
        return item;
    }

    @Override
    public boolean isAuthor(Long userId, Long itemId) {
        Item item = this.getById(itemId);
        return item != null && item.getUserId().equals(userId);
    }

    @Override
    @DS("master")
    public void online(Long itemId) {
        changeStatus(itemId, 3, 1);
    }

    @Override
    @DS("master")
    public void offline(Long itemId) {
        changeStatus(itemId, 1, 3);
    }

    @Override
    @DS("master")
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
    @DS("master")
    public void incrementContactCount(Long itemId) {
        // 使用原子更新增加联系次数
        this.baseMapper.incrementContactCount(itemId);
    }
}
