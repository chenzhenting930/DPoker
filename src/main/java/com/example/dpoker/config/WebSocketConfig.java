package com.example.dpoker.config;

import com.example.dpoker.Utils.LoginTokenManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker // 启用STOMP WebSocket消息代理，核心开关！
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")          // 前端连接地址：/ws
                .setAllowedOriginPatterns("*") // 允许跨域（开发用，生产请限制）
                .addInterceptors(new HandshakeInterceptor(){

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // 在握手前获取token，并验证token是否有效
                        String token = null;
                        token = request.getHeaders().getFirst("token");
                        Integer userId = LoginTokenManager.validateToken(token);
                        if (token.isBlank() || userId == null) {
                            // token无效，拒绝WebSocket连接
                            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                            return false;
                        }
                        attributes.put("userId", userId); // 将用户ID放入WebSocket会话属性中
                        return true;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

                    }
                })
                .withSockJS();               // 启用 SockJS 回退
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue");   // 广播前缀
        registry.setApplicationDestinationPrefixes("/app"); // 客户端发送消息前缀
        registry.setUserDestinationPrefix("/user");
    }
}
