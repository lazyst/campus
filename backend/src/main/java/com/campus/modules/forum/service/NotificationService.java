package com.campus.modules.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.modules.forum.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 获取用户的通知列表
     */
    List<Notification> getByUserId(Long userId);

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
}
