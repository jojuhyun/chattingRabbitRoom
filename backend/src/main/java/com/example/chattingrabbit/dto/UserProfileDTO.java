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
public class UserProfileDTO {
    
    private String nickname;
    private String introduction;
    private Boolean allowInvite;
    private LocalDateTime lastUpdate;
    private boolean isActive;
}
