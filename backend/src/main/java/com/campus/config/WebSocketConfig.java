package com.campus.config;

import com.campus.modules.chat.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置STOMP消息代理和端点
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单代理，广播消息到指定目标
        config.enableSimpleBroker("/topic", "/queue");
        // 设置应用程序目的地前缀
        config.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 原生 WebSocket 端点（不使用 SockJS）
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");

        // SockJS 端点（用于不支持原生 WebSocket 的浏览器）
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();

        // 支持 /ws/ 路径（与 nginx location 和前端 URL 一致）
        registry.addEndpoint("/ws/")
                .setAllowedOrigins("*");

        registry.addEndpoint("/ws/")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
