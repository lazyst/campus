package com.campus.modules.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.trade.entity.Item;

import java.util.List;

/**
 * 物品服务接口
 */
public interface ItemService extends IService<Item> {

    /**
     * 根据用户ID获取物品列表
     */
    List<Item> getByUserId(Long userId);

    /**
     * 获取物品详情（增加浏览次数）
     */
    Item getDetail(Long itemId);

    /**
     * 检查用户是否是物品发布者
     */
    boolean isAuthor(Long userId, Long itemId);

    /**
     * 上架物品（设置为正常状态）
     */
    void online(Long itemId);

    /**
     * 下架物品
     */
    void offline(Long itemId);

    /**
     * 标记物品为已完成
     */
    void complete(Long itemId);

    /**
     * 增加联系次数
     */
    void incrementContactCount(Long itemId);
}
