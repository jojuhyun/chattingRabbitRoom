package com.example.chattingrabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomDTO {

    private String roomId;
    private String name;
    private String regDate; // String으로 변경하여 직렬화 문제 해결

    public static ChatRoomDTO create(String name) {
        return ChatRoomDTO.builder()
                .roomId(name)
                .name(name)
                .regDate(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}
