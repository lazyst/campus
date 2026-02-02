package com.campus.modules.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知消息 DTO
 * 用于 WebSocket 实时推送
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 触发通知的用户ID（谁点赞/评论了你）
     */
    private Long fromUserId;

    /**
     * 关联帖子ID（可选）
     */
    private Long postId;

    /**
     * 通知类型: 1-评论, 2-点赞, 3-收藏
     */
    private Integer type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 触发通知的用户名（用于显示）
     */
    private String fromUserNickname;

    /**
     * 帖子标题（用于显示）
     */
    private String postTitle;
}
