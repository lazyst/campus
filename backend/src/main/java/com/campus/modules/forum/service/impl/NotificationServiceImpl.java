package com.campus.modules.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.modules.forum.entity.Notification;
import com.campus.modules.forum.mapper.NotificationMapper;
import com.campus.modules.forum.service.NotificationService;
import com.campus.modules.user.entity.User;
import com.campus.modules.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final UserService userService;

    public NotificationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<Notification> getByUserId(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getDeleted, false)
               .orderByDesc(Notification::getCreatedAt);
        List<Notification> notifications = this.list(wrapper);

        // 填充用户信息
        for (Notification notification : notifications) {
            User fromUser = userService.getById(notification.getFromUserId());
            if (fromUser != null) {
                notification.setFromUserNickname(fromUser.getNickname());
                notification.setFromUserAvatar(fromUser.getAvatar());
            }
        }

        return notifications;
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .eq(Notification::getDeleted, false);
        return (int) this.count(wrapper);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification != null && notification.getIsRead() == 0) {
            notification.setIsRead(1);
            this.updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .eq(Notification::getDeleted, false);

        Notification update = new Notification();
        update.setIsRead(1);
        this.update(update, wrapper);
    }
}
