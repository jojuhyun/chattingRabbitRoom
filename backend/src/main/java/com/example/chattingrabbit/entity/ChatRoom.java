package com.example.chattingrabbit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "room_type", nullable = false)
    private String roomType; // "OPEN" 또는 "PRIVATE"

    @Column(name = "creator_nickname", nullable = false)
    private String creatorNickname;

    @Column(name = "reg_date", nullable = false)
    private String regDate; // String으로 변경하여 직렬화 문제 해결

    @Column(name = "last_message_time")
    private String lastMessageTime; // String으로 변경하여 직렬화 문제 해결

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        // LocalDateTime을 String으로 변환하여 저장
        String currentTime = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        regDate = currentTime;
        lastMessageTime = currentTime;
        isActive = true;
    }
}
