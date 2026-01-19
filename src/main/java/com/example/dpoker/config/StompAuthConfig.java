package com.example.dpoker.config;

import com.example.dpoker.Utils.LoginTokenManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
public class StompAuthConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) {
                    return message;
                }

                // 只在 CONNECT 阶段校验
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {

                    String token = accessor.getFirstNativeHeader("token");
                    Integer userId = LoginTokenManager.validateToken(token);

                    if (token == null || userId == null) {
                        throw new IllegalArgumentException("STOMP CONNECT 鉴权失败");
                    }

                    // 绑定身份到 WS Session
                    accessor.getSessionAttributes().put("userId", userId);
                }

                return message;
            }
        });
    }
}
