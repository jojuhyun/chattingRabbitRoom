package com.example.chattingrabbit.service;

import com.example.chattingrabbit.dto.ChatDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQMessageService {

    private final RabbitTemplate rabbitTemplate;

    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private static final String ROUTING_KEY_PREFIX = "chat.room.";

    /**
     * 채팅방에 메시지 전송
     */
    public void sendChatMessage(String roomId, ChatDTO chatMessage) {
        try {
            String routingKey = ROUTING_KEY_PREFIX + roomId;
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, chatMessage);
            log.info("메시지가 RabbitMQ를 통해 전송되었습니다. Room: {}, Message: {}", roomId, chatMessage);
        } catch (Exception e) {
            log.error("RabbitMQ 메시지 전송 실패. Room: {}, Error: {}", roomId, e.getMessage(), e);
            throw new RuntimeException("메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * 채팅방 입장 메시지 전송
     */
    public void sendEnterMessage(String roomId, ChatDTO enterMessage) {
        try {
            String routingKey = ROUTING_KEY_PREFIX + roomId;
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, enterMessage);
            log.info("입장 메시지가 RabbitMQ를 통해 전송되었습니다. Room: {}", roomId);
        } catch (Exception e) {
            log.error("RabbitMQ 입장 메시지 전송 실패. Room: {}, Error: {}", roomId, e.getMessage(), e);
            throw new RuntimeException("입장 메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * 채팅방 퇴장 메시지 전송
     */
    public void sendLeaveMessage(String roomId, ChatDTO leaveMessage) {
        try {
            String routingKey = ROUTING_KEY_PREFIX + roomId;
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, leaveMessage);
            log.info("퇴장 메시지가 RabbitMQ를 통해 전송되었습니다. Room: {}", roomId);
        } catch (Exception e) {
            log.error("RabbitMQ 퇴장 메시지 전송 실패. Room: {}, Error: {}", roomId, e.getMessage(), e);
            throw new RuntimeException("퇴장 메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * 방송 메시지 전송
     */
    public void sendBroadcastMessage(String roomId, ChatDTO broadcastMessage) {
        try {
            String routingKey = ROUTING_KEY_PREFIX + roomId;
            rabbitTemplate.convertAndSend(CHAT_EXCHANGE_NAME, routingKey, broadcastMessage);
            log.info("방송 메시지가 RabbitMQ를 통해 전송되었습니다. Room: {}", roomId);
        } catch (Exception e) {
            log.error("RabbitMQ 방송 메시지 전송 실패. Room: {}, Error: {}", roomId, e.getMessage(), e);
            throw new RuntimeException("방송 메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * RabbitMQ 연결 상태 확인
     */
    public boolean isConnected() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive("chat.queue");
                return true;
            });
            return true;
        } catch (Exception e) {
            log.warn("RabbitMQ 연결 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}
