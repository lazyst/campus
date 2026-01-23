package com.campus.modules.chat.websocket;

import com.campus.modules.auth.service.AuthService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * WebSocket认证拦截器
 * 在连接建立时验证JWT Token
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final AuthService authService;

    public WebSocketAuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从STOMP头中获取Token
            String authorization = accessor.getFirstNativeHeader("Authorization");
            
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                
                try {
                    Long userId = authService.getUserIdFromToken(token);
                    
                    // 将用户ID设置到WebSocket会话中
                    accessor.setUser(new ChatPrincipal(userId));
                    
                } catch (Exception e) {
                    // Token无效，连接将被拒绝
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            }
        }
        
        return message;
    }

    /**
     * WebSocket聊天用户标识
     */
    public static class ChatPrincipal implements Principal {
        private final Long userId;

        public ChatPrincipal(Long userId) {
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }

        @Override
        public String getName() {
            return userId.toString();
        }
    }
}
