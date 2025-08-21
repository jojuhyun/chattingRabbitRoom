package com.example.chattingrabbit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private LocalDateTime regDate;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        regDate = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
        isActive = true;
    }
}
