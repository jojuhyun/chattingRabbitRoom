package com.example.chattingrabbit.dto;

import java.time.LocalDateTime;

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
    private LocalDateTime regDate;

    public static ChatRoomDTO create(String name) {
        return ChatRoomDTO.builder()
                .roomId(name)
                .name(name)
                .regDate(LocalDateTime.now())
                .build();
    }
}
