package com.example.chattingrabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InviteUserDTO {
    
    private String roomId;
    private String targetNickname;
    private String inviterNickname;
    private boolean success;
    private String message;
}
