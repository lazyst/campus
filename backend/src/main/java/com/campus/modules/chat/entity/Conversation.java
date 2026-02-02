package com.campus.modules.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("conversation")
public class Conversation extends BaseEntity {

    /**
     * 用户1 ID
     */
    private Long userId1;

    /**
     * 用户2 ID
     */
    private Long userId2;

    /**
     * 最后一条消息ID
     */
    private Long lastMessageId;

    /**
     * 用户1的未读消息数量
     */
    private Integer unreadCount1;

    /**
     * 用户2的未读消息数量
     */
    private Integer unreadCount2;

    /**
     * 最后一条消息内容（非数据库字段）
     */
    @TableField(exist = false)
    private String lastMessageContent;

    /**
     * 最后一条消息时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime lastMessageTime;

    /**
     * 对方用户昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String otherUserNickname;

    /**
     * 对方用户头像（非数据库字段）
     */
    @TableField(exist = false)
    private String otherUserAvatar;

    /**
     * 对方用户ID（非数据库字段）
     */
    @TableField(exist = false)
    private Long otherUserId;

    /**
     * 当前用户的未读消息数量（非数据库字段，从 unreadCount1/unreadCount2 获取）
     */
    @TableField(exist = false)
    private Integer unreadCount;

    /**
     * 获取当前用户的未读数量
     * @param currentUserId 当前用户ID
     * @return 未读消息数量
     */
    public Integer getUnreadCount(Long currentUserId) {
        if (userId1 != null && userId1.equals(currentUserId)) {
            return unreadCount1 != null ? unreadCount1 : 0;
        } else if (userId2 != null && userId2.equals(currentUserId)) {
            return unreadCount2 != null ? unreadCount2 : 0;
        }
        return 0;
    }

    /**
     * 设置当前用户的未读数量
     * @param currentUserId 当前用户ID
     * @param count 未读消息数量
     */
    public void setUnreadCount(Long currentUserId, Integer count) {
        if (userId1 != null && userId1.equals(currentUserId)) {
            this.unreadCount1 = count;
        } else if (userId2 != null && userId2.equals(currentUserId)) {
            this.unreadCount2 = count;
        }
        this.unreadCount = count;
    }

    /**
     * 增加当前用户的未读数量
     * @param currentUserId 当前用户ID
     */
    public void incrementUnreadCount(Long currentUserId) {
        if (userId1 != null && userId1.equals(currentUserId)) {
            this.unreadCount1 = (unreadCount1 != null ? unreadCount1 : 0) + 1;
            this.unreadCount = this.unreadCount1;
        } else if (userId2 != null && userId2.equals(currentUserId)) {
            this.unreadCount2 = (unreadCount2 != null ? unreadCount2 : 0) + 1;
            this.unreadCount = this.unreadCount2;
        }
    }
}
