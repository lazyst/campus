package com.campus.modules.chat.websocket;

import com.campus.modules.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuthInterceptor - WebSocket认证拦截器")
class WebSocketAuthInterceptorTest {

    @Mock
    private AuthService authService;

    private WebSocketAuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketAuthInterceptor(authService);
    }

    @Test
    @DisplayName("preSend - CONNECT命令带有效Token时设置用户")
    void shouldSetUserOnConnectWithValidToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer valid-token");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(authService.getUserIdFromToken("valid-token")).thenReturn(1L);

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
        assertTrue(result.getHeaders().get("simpUser") instanceof WebSocketAuthInterceptor.ChatPrincipal);
    }

    @Test
    @DisplayName("preSend - CONNECT命令无Token时放行")
    void shouldPassThroughWhenNoTokenOnConnect() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("preSend - CONNECT命令带无效Token格式时放行")
    void shouldPassThroughWhenInvalidTokenFormat() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "InvalidFormat");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("preSend - CONNECT命令带无效Token时抛异常")
    void shouldThrowExceptionOnInvalidToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer invalid-token");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(authService.getUserIdFromToken("invalid-token")).thenThrow(new RuntimeException("Invalid token"));

        assertThrows(IllegalArgumentException.class, () -> interceptor.preSend(message, null));
    }

    @Test
    @DisplayName("preSend - 非CONNECT命令时放行")
    void shouldPassThroughForNonConnectCommand() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("ChatPrincipal - 获取用户ID和名称")
    void shouldGetUserIdAndName() {
        WebSocketAuthInterceptor.ChatPrincipal principal = new WebSocketAuthInterceptor.ChatPrincipal(123L);

        assertEquals(123L, principal.getUserId());
        assertEquals("123", principal.getName());
    }

    @Test
    @DisplayName("preSend - accessor为null时放行")
    void shouldPassThroughWhenAccessorIsNull() {
        // 创建一个无法获取StompHeaderAccessor的消息
        Message<?> message = new org.springframework.messaging.support.GenericMessage<>(new byte[0]);

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("preSend - CONNECT命令带空Bearer Token时放行")
    void shouldPassThroughWhenEmptyBearerToken() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer ");
        accessor.setLeaveMutable(true);

        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        assertNotNull(result);
    }
}
