package com.example.chattingrabbit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private String chatRoomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "message_type", nullable = false)
    private String messageType; // ENTER, MESSAGE, BROADCAST, LEAVE

    @Column(name = "reg_date", nullable = false)
    private String regDate; // String으로 변경하여 직렬화 문제 해결

    @PrePersist
    protected void onCreate() {
        // LocalDateTime을 String으로 변환하여 저장
        regDate = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
