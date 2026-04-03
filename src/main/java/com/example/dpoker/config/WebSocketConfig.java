package com.example.dpoker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Slf4j
@EnableWebSocketMessageBroker // 启用STOMP WebSocket消息代理，核心开关！
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")          // 前端连接地址：/ws
                .setAllowedOriginPatterns("*") // 允许跨域（开发用，生产请限制）
                .withSockJS();               // 启用 SockJS 回退
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue");   // 广播前缀
        registry.setApplicationDestinationPrefixes("/app"); // 客户端发送消息前缀
        registry.setUserDestinationPrefix("/user");
    }
}
