package com.campus.modules.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.trade.entity.ItemCollect;

import java.util.List;

/**
 * 物品收藏服务接口
 */
public interface ItemCollectService extends IService<ItemCollect> {

    /**
     * 检查用户是否已收藏物品
     */
    boolean hasCollected(Long userId, Long itemId);

    /**
     * 切换收藏状态
     * @return true=收藏成功, false=取消收藏成功
     */
    boolean toggleCollect(Long userId, Long itemId);

    /**
     * 获取用户收藏的物品列表
     */
    List<ItemCollect> getByUserId(Long userId);
}
