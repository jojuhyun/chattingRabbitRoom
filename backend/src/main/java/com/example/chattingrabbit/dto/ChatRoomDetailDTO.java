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
public class ChatRoomDetailDTO {

    private String roomId;
    private String name;
    private String regDate;
    private Long participantCount;
    private List<ParticipantDTO> participants;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ParticipantDTO {
        private String nickname;
        private LocalDateTime enterTime;
        private Integer joinOrder;
    }
}
