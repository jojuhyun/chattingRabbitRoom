package com.example.chattingrabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String userId;
    private String nickname;
    private String password;
    private boolean isSuperAdmin;
    private LocalDateTime joinTime;
    private LocalDateTime lastAccessTime;
}
