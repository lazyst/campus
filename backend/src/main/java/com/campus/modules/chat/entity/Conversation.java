package com.campus.modules.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天会话实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
     * 最后一条消息内容（非数据库字段）
     */
    @TableField(exist = false)
    private String lastMessageContent;

    /**
     * 最后一条消息时间（非数据库字段）
     */
    @TableField(exist = false)
    private java.time.LocalDateTime lastMessageTime;

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
     * 未读消息数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer unreadCount;
}
