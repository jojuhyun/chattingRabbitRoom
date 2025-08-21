package com.example.chattingrabbit.controller;

import com.example.chattingrabbit.dto.ChatDTO;
import com.example.chattingrabbit.dto.ChatRoomDTO;
import com.example.chattingrabbit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    private final ChatService chatService;

    // 관리자용 채팅방 목록 조회 (모든 활성 방)
    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> getAllChatRooms(@RequestParam String userSession) {
        try {
            List<ChatRoomDTO> rooms = chatService.getAllActiveChatRooms();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "rooms", rooms));
        } catch (Exception e) {
            log.error("관리자 채팅방 목록 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 목록을 불러올 수 없습니다."));
        }
    }

    // 관리자용 전체 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Map<String, Object>> getAllChatMessages(@PathVariable String roomId) {
        try {
            List<ChatDTO> messages = chatService.getAllChatMessages(roomId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "messages", messages));
        } catch (Exception e) {
            log.error("관리자 채팅 메시지 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅 메시지를 불러올 수 없습니다."));
        }
    }

    // 채팅방 삭제 (관리자용)
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Map<String, Object>> deleteChatRoom(@PathVariable String roomId,
            @RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");
            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            boolean deleted = chatService.deleteChatRoom(roomId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "채팅방이 삭제되었습니다."));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "참여자가 있는 채팅방은 삭제할 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("채팅방 삭제 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "채팅방 삭제에 실패했습니다."));
        }
    }

    // 자동 정리 실행 (관리자용)
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> runCleanup(@RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");
            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            // 수동으로 정리 실행
            chatService.removeUsersFromInactiveRooms();
            chatService.removeInactiveSessions();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "cleanedCount", 0, // 실제로는 정리된 수를 반환해야 함
                    "message", "정리가 완료되었습니다."));
        } catch (Exception e) {
            log.error("자동 정리 실행 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "자동 정리 실행에 실패했습니다."));
        }
    }
}
