package com.campus.modules.chat.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.chat.websocket.WebSocketAuthInterceptor.ChatPrincipal;
import com.campus.modules.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket聊天控制器
 * 处理实时消息的发送和接收
 */
@RestController
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthService authService;

    public ChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate, AuthService authService) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.authService = authService;
    }

    @MessageMapping("/chat.send/{receiverId}")
    public void sendMessage(
            @DestinationVariable Long receiverId,
            Map<String, Object> message,
            Principal principal) {
        
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal chatPrincipal = (ChatPrincipal) principal;
            Long senderId = chatPrincipal.getUserId();
            String content = (String) message.get("content");
            Integer type = (Integer) message.get("type");
            Long itemId = null;
            
            if (type != null && type == 3) {
                itemId = ((Number) message.get("itemId")).longValue();
            }
            
            chatService.saveMessage(senderId, receiverId, content, itemId);
        }
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/api/conversations")
    public Result<?> getConversations(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            return Result.error("未登录");
        }
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        return Result.success(chatService.getConversations(userId));
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/api/conversations/{conversationId}/messages")
    public Result<?> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        Long userId = authService.getUserIdFromAuthHeader(authHeader);
        
        return Result.success(chatService.getMessages(conversationId, page, size));
    }

    /**
     * 获取与特定用户的聊天消息
     */
    @GetMapping("/api/messages/{userId}")
    public Result<?> getMessagesWithUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Long currentUserId = authService.getUserIdFromAuthHeader(authHeader);

        return Result.success(chatService.getMessagesWithUser(currentUserId, userId, page, size));
    }

    /**
     * 发送消息给特定用户（REST API）
     */
    @PostMapping("/api/messages/{userId}")
    public Result<?> sendMessageToUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {

        Long currentUserId = authService.getUserIdFromAuthHeader(authHeader);
        String content = (String) request.get("content");
        Integer type = request.get("type") != null ? ((Number) request.get("type")).intValue() : 1;
        Long itemId = request.get("itemId") != null ? ((Number) request.get("itemId")).longValue() : null;

        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        return Result.success(chatService.saveMessage(currentUserId, userId, content, itemId));
    }

    /**
     * 创建会话（如果不存在）
     */
    @PostMapping("/api/conversations")
    public Result<?> createConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {

        Long userId = authService.getUserIdFromAuthHeader(authHeader);
        Long otherUserId = request.get("userId");

        return Result.success(chatService.getOrCreateConversation(userId, otherUserId));
    }

    /**
     * 清除与指定用户的未读消息数
     */
    @PostMapping("/api/conversations/{userId}/read")
    public Result<?> clearUnreadCount(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId) {

        Long currentUserId = authService.getUserIdFromAuthHeader(authHeader);

        chatService.clearUnreadCountByUserIds(currentUserId, userId);

        return Result.success(null);
    }

    /**
     * 获取用户的总未读消息数
     */
    @GetMapping("/api/conversations/unread/count")
    public Result<?> getTotalUnreadCount(@RequestHeader("Authorization") String authHeader) {
        Long userId = authService.getUserIdFromAuthHeader(authHeader);

        Integer totalUnread = chatService.getTotalUnreadCount(userId);

        return Result.success(totalUnread);
    }
}
