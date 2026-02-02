package com.campus.modules.chat.controller;

import com.campus.common.Result;
import com.campus.modules.auth.service.AuthService;
import com.campus.modules.chat.websocket.WebSocketAuthInterceptor.ChatPrincipal;
import com.campus.modules.chat.service.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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

    /**
     * 发送聊天消息
     * 客户端发送消息到: /app/chat.send/{receiverId}
     */
    @MessageMapping("/chat.send/{receiverId}")
    public void sendMessage(
            @DestinationVariable Long receiverId,
            Map<String, Object> message,
            Principal principal) {
        
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal chatPrincipal = (ChatPrincipal) principal;
            Long senderId = chatPrincipal.getUserId();
            
            // 保存消息到数据库（离线消息存储）
            com.campus.modules.chat.entity.Message savedMessage = 
                chatService.saveMessage(senderId, receiverId, 
                    (String) message.get("content"));
            
            // 发送到接收者的个人队列（点对点）
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(), 
                "/queue/messages", 
                savedMessage
            );
        }
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/api/conversations")
    public Result<?> getConversations(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        
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
        
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
        
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

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = authService.getUserIdFromToken(token);

        return Result.success(chatService.getMessagesWithUser(currentUserId, userId, page, size));
    }

    /**
     * 发送消息给特定用户（REST API）
     */
    @PostMapping("/api/messages/{userId}")
    public Result<?> sendMessageToUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = authService.getUserIdFromToken(token);
        String content = request.get("content");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        return Result.success(chatService.saveMessage(currentUserId, userId, content));
    }

    /**
     * 创建会话（如果不存在）
     */
    @PostMapping("/api/conversations")
    public Result<?> createConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Long> request) {

        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);
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

        String token = authHeader.replace("Bearer ", "");
        Long currentUserId = authService.getUserIdFromToken(token);

        chatService.clearUnreadCountByUserIds(currentUserId, userId);

        return Result.success(null);
    }

    /**
     * 获取用户的总未读消息数
     */
    @GetMapping("/api/conversations/unread/count")
    public Result<?> getTotalUnreadCount(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = authService.getUserIdFromToken(token);

        Integer totalUnread = chatService.getTotalUnreadCount(userId);

        return Result.success(totalUnread);
    }
}
