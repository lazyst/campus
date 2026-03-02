package com.campus.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息 DTO
 * 用于 Redis Pub/Sub 传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 会话 ID
     */
    private Long conversationId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-文本，2-图片，3-商品卡片
     */
    private Integer type;

    /**
     * 关联的商品 ID（消息类型为 3 时使用）
     */
    private Long itemId;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 商品标题（非数据库字段，消息类型为 3 时使用）
     */
    private String itemTitle;

    /**
     * 商品价格（非数据库字段，消息类型为 3 时使用）
     */
    private Double itemPrice;

    /**
     * 商品图片（非数据库字段，消息类型为 3 时使用）
     */
    private String itemImage;

    /**
     * 商品类型（非数据库字段，消息类型为 3 时使用）
     */
    private Integer itemType;

    /**
     * 商品所有者昵称（非数据库字段，消息类型为 3 时使用）
     */
    private String itemUserNickname;

    /**
     * 商品所有者头像（非数据库字段，消息类型为 3 时使用）
     */
    private String itemUserAvatar;
}
