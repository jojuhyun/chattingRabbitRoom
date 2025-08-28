package com.example.chattingrabbit.controller;

import com.example.chattingrabbit.dto.ChatDTO;
import com.example.chattingrabbit.service.ChatService;
import com.example.chattingrabbit.service.RabbitMQMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final RabbitMQMessageService rabbitMQMessageService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @MessageMapping("/chat.enter.{roomId}")
    public void enterRoom(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("채팅방 입장: roomId={}, userId={}, nickname={}", roomId, chat.getUserId(), chat.getNickname());

        try {
            // 비동기로 처리하여 WebSocket 블로킹 방지
            CompletableFuture.runAsync(() -> {
                try {
                    // 입장 시간 설정 (String으로 변환하여 직렬화 문제 방지)
                    String currentTime = LocalDateTime.now().format(FORMATTER);
                    chat.setRegDate(currentTime);
                    chat.setMessageType("ENTER");
                    chat.setChatRoomId(roomId);

                    // 사용자 입장 처리
                    chatService.enterChatRoom(roomId, chat.getUserId(), chat.getNickname());

                    // 메시지 저장
                    chatService.saveMessage(chat);

                    // RabbitMQ를 통해 메시지 전송
                    rabbitMQMessageService.sendEnterMessage(roomId, chat);
                    
                    log.info("채팅방 입장 처리 완료: roomId={}, userId={}", roomId, chat.getUserId());
                } catch (Exception e) {
                    log.error("채팅방 입장 처리 중 오류 발생: roomId={}, userId={}, error={}", roomId, chat.getUserId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("채팅방 입장 메시지 처리 실패: roomId={}, error={}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.message.{roomId}")
    public void sendMessage(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("메시지 전송: roomId={}, userId={}, message={}", roomId, chat.getUserId(), chat.getMessage());

        try {
            // 비동기로 처리하여 WebSocket 블로킹 방지
            CompletableFuture.runAsync(() -> {
                try {
                    // 전송 시간 설정 (String으로 변환하여 직렬화 문제 방지)
                    String currentTime = LocalDateTime.now().format(FORMATTER);
                    chat.setRegDate(currentTime);
                    chat.setMessageType("MESSAGE");
                    chat.setChatRoomId(roomId);

                    // 메시지 저장
                    chatService.saveMessage(chat);

                    // RabbitMQ를 통해 메시지 전송
                    rabbitMQMessageService.sendChatMessage(roomId, chat);
                    
                    log.info("메시지 전송 처리 완료: roomId={}, userId={}", roomId, chat.getUserId());
                } catch (Exception e) {
                    log.error("메시지 전송 처리 중 오류 발생: roomId={}, userId={}, error={}", roomId, chat.getUserId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("메시지 전송 메시지 처리 실패: roomId={}, error={}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.messageall.{roomId}")
    public void sendBroadcast(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("방송 메시지: roomId={}, userId={}, message={}", roomId, chat.getUserId(), chat.getMessage());

        try {
            // 비동기로 처리하여 WebSocket 블로킹 방지
            CompletableFuture.runAsync(() -> {
                try {
                    // 전송 시간 설정 (String으로 변환하여 직렬화 문제 방지)
                    String currentTime = LocalDateTime.now().format(FORMATTER);
                    chat.setRegDate(currentTime);
                    chat.setMessageType("BROADCAST");
                    chat.setChatRoomId(roomId);

                    // 메시지 저장
                    chatService.saveMessage(chat);

                    // RabbitMQ를 통해 메시지 전송
                    rabbitMQMessageService.sendBroadcastMessage(roomId, chat);
                    
                    log.info("방송 메시지 처리 완료: roomId={}, userId={}", roomId, chat.getUserId());
                } catch (Exception e) {
                    log.error("방송 메시지 처리 중 오류 발생: roomId={}, userId={}, error={}", roomId, chat.getUserId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("방송 메시지 처리 실패: roomId={}, error={}", roomId, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.leave.{roomId}")
    public void leaveRoom(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("채팅방 퇴장: roomId={}, userId={}, nickname={}", roomId, chat.getUserId(), chat.getNickname());

        try {
            // 비동기로 처리하여 WebSocket 블로킹 방지
            CompletableFuture.runAsync(() -> {
                try {
                    // 퇴장 시간 설정 (String으로 변환하여 직렬화 문제 방지)
                    String currentTime = LocalDateTime.now().format(FORMATTER);
                    chat.setRegDate(currentTime);
                    chat.setMessageType("LEAVE");
                    chat.setChatRoomId(roomId);

                    // 사용자 퇴장 처리
                    chatService.leaveChatRoom(roomId, chat.getUserId());

                    // 메시지 저장
                    chatService.saveMessage(chat);

                    // RabbitMQ를 통해 메시지 전송
                    rabbitMQMessageService.sendLeaveMessage(roomId, chat);
                    
                    log.info("채팅방 퇴장 처리 완료: roomId={}, userId={}", roomId, chat.getUserId());
                } catch (Exception e) {
                    log.error("채팅방 퇴장 처리 중 오류 발생: roomId={}, userId={}, error={}", roomId, chat.getUserId(), e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("채팅방 퇴장 메시지 처리 실패: roomId={}, error={}", roomId, e.getMessage(), e);
        }
    }
}
