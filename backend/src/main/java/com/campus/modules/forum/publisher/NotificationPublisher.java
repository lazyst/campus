package com.campus.modules.forum.publisher;

import com.campus.modules.forum.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知推送服务
 * 通过 WebSocket 实时推送通知给用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 通知消息目标地址
     */
    private static final String QUEUE_NOTIFICATIONS = "/queue/notifications";

    /**
     * 推送通知给指定用户
     *
     * @param userId       接收用户ID
     * @param notification 通知内容
     */
    public void publish(Long userId, NotificationDTO notification) {
        try {
            String destination = "/user/" + userId + QUEUE_NOTIFICATIONS;
            log.info("准备推送通知给用户 {}, 目标地址: {}, 通知内容: {}", userId, destination, notification);
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    QUEUE_NOTIFICATIONS,
                    notification
            );
            log.info("通知已推送给用户 {}: type={}", userId, notification.getType());
        } catch (Exception e) {
            log.error("推送通知给用户 {} 失败: {}", userId, e.getMessage(), e);
        }
    }
}
