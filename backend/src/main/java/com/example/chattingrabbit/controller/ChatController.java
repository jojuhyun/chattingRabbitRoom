package com.example.chattingrabbit.controller;

import com.example.chattingrabbit.dto.ChatDTO;
import com.example.chattingrabbit.dto.ChatRoomDTO;
import com.example.chattingrabbit.dto.ChatRoomDetailDTO;
import com.example.chattingrabbit.dto.ChatRoomListDTO;
import com.example.chattingrabbit.dto.InviteUserDTO;
import com.example.chattingrabbit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class ChatController {

    private final ChatService chatService;

    // 채팅방 목록 조회 (참여한 방과 다른 방으로 분리)
    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getChatRoomList(@RequestParam String userSession) {
        try {
            ChatRoomListDTO roomList = chatService.getChatRoomList(userSession);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "roomList", roomList));
        } catch (Exception e) {
            log.error("채팅방 목록 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 목록을 불러올 수 없습니다."));
        }
    }

    // 채팅방 생성 (타입 지정)
    @PostMapping("/rooms")
    public ResponseEntity<Map<String, Object>> createChatRoom(@RequestBody Map<String, String> request) {
        try {
            String roomName = request.get("name");
            String roomType = request.get("roomType");
            String creatorNickname = request.get("creatorNickname");

            if (roomName == null || roomName.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "채팅방 이름을 입력해주세요."));
            }

            if (roomType == null || (!roomType.equals("OPEN") && !roomType.equals("PRIVATE"))) {
                roomType = "OPEN"; // 기본값
            }

            ChatRoomDTO newRoom = chatService.createChatRoom(roomName.trim(), roomType, creatorNickname);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "room", newRoom));
        } catch (Exception e) {
            log.error("채팅방 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 생성에 실패했습니다: " + e.getMessage()));
        }
    }

    // 채팅방 상세 정보 조회 (참여자 목록 포함)
    @GetMapping("/rooms/detail")
    public ResponseEntity<Map<String, Object>> getChatRoomDetail(@RequestParam String roomId,
            @RequestParam String userSession) {
        try {
            ChatRoomDetailDTO roomDetail = chatService.getChatRoomDetail(roomId);
            if (roomDetail == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "존재하지 않는 채팅방입니다."));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "roomDetail", roomDetail));
        } catch (Exception e) {
            log.error("채팅방 상세 정보 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 상세 정보를 불러올 수 없습니다."));
        }
    }

    // 채팅방 참여
    @PostMapping("/rooms/join")
    public ResponseEntity<Map<String, Object>> joinChatRoom(@RequestParam String roomId,
            @RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");
            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            // userSession으로 실제 nickname 조회
            String nickname = chatService.getNicknameByUserSession(userSession);
            if (nickname == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "유효하지 않은 사용자 세션입니다."));
            }

            chatService.enterChatRoom(roomId, userSession, nickname);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "채팅방에 입장했습니다."));
        } catch (Exception e) {
            log.error("채팅방 입장 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 입장에 실패했습니다."));
        }
    }

    // 채팅방 떠나기
    @PostMapping("/rooms/leave")
    public ResponseEntity<Map<String, Object>> leaveChatRoom(@RequestParam String roomId,
            @RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");
            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            chatService.leaveChatRoom(roomId, userSession);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "채팅방을 떠났습니다."));
        } catch (Exception e) {
            log.error("채팅방 퇴장 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 퇴장에 실패했습니다."));
        }
    }

    // 사용자 입장 이후 메시지 조회
    @GetMapping("/rooms/messages")
    public ResponseEntity<Map<String, Object>> getChatMessages(@RequestParam String roomId,
            @RequestParam String userSession) {
        try {
            List<ChatDTO> messages = chatService.getChatMessages(roomId, userSession);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messages));
        } catch (Exception e) {
            log.error("채팅 메시지 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅 메시지를 불러올 수 없습니다."));
        }
    }

    // 메시지 전송
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> sendChatMessage(@RequestBody ChatDTO chatDTO) {
        try {
            chatService.saveMessage(chatDTO);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "메시지가 전송되었습니다."));
        } catch (Exception e) {
            log.error("메시지 전송 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "메시지가 전송되었습니다."));
        }
    }

    // 사용자 초대
    @PostMapping("/rooms/invite")
    public ResponseEntity<InviteUserDTO> inviteUserToRoom(@RequestParam String roomId,
            @RequestBody Map<String, String> request) {
        try {
            String targetNickname = request.get("nickname");
            String inviterNickname = request.get("inviterNickname");

            if (targetNickname == null || inviterNickname == null) {
                return ResponseEntity.ok(InviteUserDTO.builder()
                        .success(false)
                        .message("필수 정보가 누락되었습니다.")
                        .build());
            }

            InviteUserDTO result = chatService.inviteUserToRoom(roomId, targetNickname, inviterNickname);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("사용자 초대 실패", e);
            return ResponseEntity.ok(InviteUserDTO.builder()
                    .success(false)
                    .message("사용자 초대에 실패했습니다.")
                    .build());
        }
    }
}
