package com.example.chattingrabbit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRoomListDTO {
    
    private List<ChatRoomDTO> participatedRooms;
    private List<ChatRoomDTO> otherRooms;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ChatRoomDTO {
        private String roomId;
        private String name;
        private String roomType;
        private String creatorNickname;
        private String regDate;
        private Long participantCount;
        private boolean isParticipating;
    }
}
