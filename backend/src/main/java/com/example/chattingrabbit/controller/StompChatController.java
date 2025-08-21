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

@Controller
@RequiredArgsConstructor
@Slf4j
public class StompChatController {

    private final ChatService chatService;
    private final RabbitMQMessageService rabbitMQMessageService;

    @MessageMapping("/chat.enter.{roomId}")
    public void enterRoom(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("채팅방 입장: {}", chat);

        // 입장 시간 설정
        chat.setRegDate(LocalDateTime.now());
        chat.setMessageType("ENTER");
        chat.setChatRoomId(roomId);

        // 사용자 입장 처리
        chatService.enterChatRoom(roomId, chat.getUserId(), chat.getNickname());

        // 메시지 저장
        chatService.saveMessage(chat);

        // RabbitMQ를 통해 메시지 전송
        rabbitMQMessageService.sendEnterMessage(roomId, chat);
    }

    @MessageMapping("/chat.message.{roomId}")
    public void sendMessage(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("메시지 전송: {}", chat);

        // 전송 시간 설정
        chat.setRegDate(LocalDateTime.now());
        chat.setMessageType("MESSAGE");
        chat.setChatRoomId(roomId);

        // 메시지 저장
        chatService.saveMessage(chat);

        // RabbitMQ를 통해 메시지 전송
        rabbitMQMessageService.sendChatMessage(roomId, chat);
    }

    @MessageMapping("/chat.messageall.{roomId}")
    public void sendBroadcast(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("방송 메시지: {}", chat);

        // 전송 시간 설정
        chat.setRegDate(LocalDateTime.now());
        chat.setMessageType("BROADCAST");
        chat.setChatRoomId(roomId);

        // 메시지 저장
        chatService.saveMessage(chat);

        // RabbitMQ를 통해 메시지 전송
        rabbitMQMessageService.sendBroadcastMessage(roomId, chat);
    }

    @MessageMapping("/chat.leave.{roomId}")
    public void leaveRoom(@Payload ChatDTO chat, @PathVariable String roomId) {
        log.info("채팅방 퇴장: {}", chat);

        // 퇴장 시간 설정
        chat.setRegDate(LocalDateTime.now());
        chat.setMessageType("LEAVE");
        chat.setChatRoomId(roomId);

        // 사용자 퇴장 처리
        chatService.leaveChatRoom(roomId, chat.getUserId());

        // 메시지 저장
        chatService.saveMessage(chat);

        // RabbitMQ를 통해 메시지 전송
        rabbitMQMessageService.sendLeaveMessage(roomId, chat);
    }
}
