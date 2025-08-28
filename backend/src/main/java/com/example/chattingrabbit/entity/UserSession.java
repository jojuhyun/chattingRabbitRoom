package com.example.chattingrabbit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "user_session", nullable = false, unique = true)
    private String userSession;

    @Column(name = "introduction", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "allow_invite", nullable = false)
    private Boolean allowInvite;

    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "password", nullable = false)
    private String password;

    @PrePersist
    protected void onCreate() {
        lastUpdate = LocalDateTime.now();
        isActive = true;
        allowInvite = true; // 기본값으로 초대 허용
    }
}
