package com.example.chattingrabbit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_participations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private String chatRoomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "enter_time", nullable = false)
    private LocalDateTime enterTime;

    @Column(name = "leave_time")
    private LocalDateTime leaveTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "join_order", nullable = false)
    private Integer joinOrder;

    @Column(name = "is_invited", nullable = false)
    private Boolean isInvited;

    @Column(name = "invite_time")
    private LocalDateTime inviteTime;

    @PrePersist
    protected void onCreate() {
        enterTime = LocalDateTime.now();
        isActive = true;
        isInvited = false;
    }
}
