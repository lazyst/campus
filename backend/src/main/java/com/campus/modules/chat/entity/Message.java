package com.campus.modules.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.campus.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 注意：JPA/Hibernate 实体使用 @Getter/@Setter，不使用 @Data
 */
@Getter
@Setter
@TableName("message")
public class Message extends BaseEntity {

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1文本 2图片
     */
    private Integer type;

    /**
     * 发送时间（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private LocalDateTime sendTime;

    /**
     * 发送者昵称（非数据库字段）
     */
    @TableField(exist = false)
    private String senderNickname;

    /**
     * 发送者头像（非数据库字段）
     */
    @TableField(exist = false)
    private String senderAvatar;
}
