package com.example.chattingrabbit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.websocket.stomp.broker-relay.host:localhost}")
    private String stompHost;

    @Value("${spring.websocket.stomp.broker-relay.port:61613}")
    private int stompPort;

    @Value("${spring.websocket.stomp.broker-relay.login:admin}")
    private String stompLogin;

    @Value("${spring.websocket.stomp.broker-relay.passcode:admin123}")
    private String stompPasscode;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/stomp")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // RabbitMQ STOMP 브로커 릴레이 설정
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(stompHost)
                .setRelayPort(stompPort)
                .setClientLogin(stompLogin)
                .setClientPasscode(stompPasscode)
                .setSystemLogin(stompLogin)
                .setSystemPasscode(stompPasscode)
                .setVirtualHost("/");

        // 클라이언트에서 서버로 메시지를 보낼 때 사용할 prefix
        registry.setApplicationDestinationPrefixes("/app");
        
        // 사용자별 메시지 prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024) // 64KB
                  .setSendBufferSizeLimit(512 * 1024) // 512KB
                  .setSendTimeLimit(20000); // 20초
    }
}
