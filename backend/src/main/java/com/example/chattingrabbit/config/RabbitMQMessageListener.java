package com.example.chattingrabbit.config;

import com.example.chattingrabbit.dto.ChatDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQMessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * RabbitMQ에서 채팅 메시지를 수신하고 WebSocket을 통해 클라이언트에게 전송
     */
    @RabbitListener(queues = "chat.queue")
    public void handleChatMessage(String messageJson) {
        try {
            // JSON 메시지를 ChatDTO로 변환
            ChatDTO chatMessage = objectMapper.readValue(messageJson, ChatDTO.class);

            // ChatDTO의 chatRoomId를 사용하여 채팅방 식별
            String roomId = chatMessage.getChatRoomId();
            if (roomId != null && !roomId.trim().isEmpty()) {
                // WebSocket을 통해 클라이언트에게 메시지 전송
                String destination = "/topic/chat.room." + roomId;
                messagingTemplate.convertAndSend(destination, chatMessage);

                log.info("RabbitMQ에서 수신한 메시지를 WebSocket으로 전송: Room: {}, Message: {}",
                        roomId, chatMessage.getMessage());
            } else {
                log.warn("채팅방 ID가 없는 메시지: {}", chatMessage);
            }
        } catch (Exception e) {
            log.error("RabbitMQ 메시지 처리 실패: {}", e.getMessage(), e);
        }
    }
}
