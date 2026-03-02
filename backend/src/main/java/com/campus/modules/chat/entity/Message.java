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
     * 消息类型：1 文本 2 图片 3 商品卡片
     */
    private Integer type;

    /**
     * 关联的商品 ID（消息类型为 3 时使用）
     */
    private Long itemId;

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

    /**
     * 商品标题（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private String itemTitle;

    /**
     * 商品价格（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private Double itemPrice;

    /**
     * 商品图片（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private String itemImage;

    /**
     * 商品类型（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private Integer itemType;

    /**
     * 商品所有者昵称（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private String itemUserNickname;

    /**
     * 商品所有者头像（非数据库字段，消息类型为 3 时使用）
     */
    @TableField(exist = false)
    private String itemUserAvatar;
}
