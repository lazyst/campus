package com.campus.config;

import com.campus.modules.chat.websocket.WebSocketAuthInterceptor;
import com.campus.modules.chat.websocket.WebSocketAuthInterceptor.ChatPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws/")
                .setAllowedOriginPatterns("*");

        registry.addEndpoint("/ws/")
                .setAllowedOriginPatterns("*")
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

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        Principal principal = event.getMessage().getHeaders().get("simpUser", Principal.class);
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal user = (ChatPrincipal) principal;
            log.info("WebSocket 用户连接: userId={}, sessionId={}", 
                user.getUserId(), event.getMessage().getHeaders().get("simpSessionId"));
        } else {
            log.info("WebSocket 匿名连接: sessionId={}", 
                event.getMessage().getHeaders().get("simpSessionId"));
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal user = (ChatPrincipal) principal;
            log.info("WebSocket 用户断开: userId={}, sessionId={}", 
                user.getUserId(), event.getSessionId());
        } else {
            log.info("WebSocket 匿名断开: sessionId={}", event.getSessionId());
        }
    }

    @EventListener
    public void handleSessionSubscribe(SessionSubscribeEvent event) {
        Principal principal = event.getMessage().getHeaders().get("simpUser", Principal.class);
        String destination = event.getMessage().getHeaders().get("simpDestination", String.class);
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal user = (ChatPrincipal) principal;
            log.debug("WebSocket 用户订阅: userId={}, destination={}", user.getUserId(), destination);
        }
    }

    @EventListener
    public void handleSessionUnsubscribe(SessionUnsubscribeEvent event) {
        Principal principal = event.getMessage().getHeaders().get("simpUser", Principal.class);
        String destination = event.getMessage().getHeaders().get("simpDestination", String.class);
        if (principal instanceof ChatPrincipal) {
            ChatPrincipal user = (ChatPrincipal) principal;
            log.debug("WebSocket 用户取消订阅: userId={}, destination={}", user.getUserId(), destination);
        }
    }
}
