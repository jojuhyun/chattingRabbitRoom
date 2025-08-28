package com.example.chattingrabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatDTO {

    private Long id;
    private String chatRoomId;
    private String userId;
    private String nickname;
    private String message;
    private String region;
    private String messageType; // "ENTER", "MESSAGE", "BROADCAST", "LEAVE"

    private String regDate; // String으로 변경하여 직렬화 문제 해결
}
