package com.example.chattingrabbit.controller;

import com.example.chattingrabbit.dto.UserProfileDTO;
import com.example.chattingrabbit.dto.UserSessionDTO;
import com.example.chattingrabbit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rabbit/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class UserController {

    private final ChatService chatService;

    // 닉네임 등록 및 세션 생성 (비밀번호 포함)
    @PostMapping("/register")
    public ResponseEntity<UserSessionDTO> registerNickname(@RequestBody Map<String, String> request) {
        try {
            String nickname = request.get("nickname");
            String introduction = request.get("introduction");
            String password = request.get("password");

            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.ok(UserSessionDTO.builder()
                        .success(false)
                        .message("닉네임을 입력해주세요.")
                        .build());
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.ok(UserSessionDTO.builder()
                        .success(false)
                        .message("비밀번호를 입력해주세요.")
                        .build());
            }

            UserSessionDTO result = chatService.registerNickname(nickname.trim(), introduction, password.trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("닉네임 등록 실패", e);
            return ResponseEntity.ok(UserSessionDTO.builder()
                    .success(false)
                    .message("닉네임 등록에 실패했습니다.")
                    .build());
        }
    }

    // 닉네임 로그인
    @PostMapping("/login")
    public ResponseEntity<UserSessionDTO> loginNickname(@RequestBody Map<String, String> request) {
        try {
            String nickname = request.get("nickname");
            String password = request.get("password");

            if (nickname == null || nickname.trim().isEmpty()) {
                return ResponseEntity.ok(UserSessionDTO.builder()
                        .success(false)
                        .message("닉네임을 입력해주세요.")
                        .build());
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.ok(UserSessionDTO.builder()
                        .success(false)
                        .message("비밀번호를 입력해주세요.")
                        .build());
            }

            UserSessionDTO result = chatService.loginNickname(nickname.trim(), password.trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("닉네임 로그인 실패", e);
            return ResponseEntity.ok(UserSessionDTO.builder()
                    .success(false)
                    .message("로그인에 실패했습니다.")
                    .build());
        }
    }

    // 세션 갱신
    @PostMapping("/refresh-session")
    public ResponseEntity<UserSessionDTO> refreshSession(@RequestBody Map<String, String> request) {
        try {
            String nickname = request.get("nickname");
            String userSession = request.get("userSession");

            if (nickname == null || nickname.trim().isEmpty() || userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(UserSessionDTO.builder()
                        .success(false)
                        .message("닉네임과 세션 정보가 필요합니다.")
                        .build());
            }

            UserSessionDTO result = chatService.refreshSession(nickname.trim(), userSession.trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("세션 갱신 실패", e);
            return ResponseEntity.ok(UserSessionDTO.builder()
                    .success(false)
                    .message("세션 갱신에 실패했습니다.")
                    .build());
        }
    }

    // 사용자 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@RequestParam String userSession) {
        try {
            UserProfileDTO profile = chatService.getUserProfile(userSession);
            if (profile != null) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "profile", profile));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "사용자를 찾을 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("사용자 프로필 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "사용자 프로필 조회에 실패했습니다."));
        }
    }

    // 사용자 프로필 수정
    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");
            String introduction = request.get("introduction");

            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            // userSession으로 nickname 조회
            UserProfileDTO profile = chatService.getUserProfile(userSession);
            if (profile == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "사용자를 찾을 수 없습니다."));
            }

            boolean updated = chatService.updateUserIntroduction(profile.getNickname(), introduction);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "프로필이 수정되었습니다."));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "프로필 수정에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("프로필 수정 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "프로필 수정에 실패했습니다."));
        }
    }

    // 사용자 초대 허용 설정 수정
    @PutMapping("/update-allow-invite")
    public ResponseEntity<Map<String, Object>> updateAllowInvite(@RequestBody Map<String, Object> request) {
        try {
            String userSession = (String) request.get("userSession");
            Boolean allowInvite = (Boolean) request.get("allowInvite");

            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            if (allowInvite == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "초대 허용 설정이 필요합니다."));
            }

            // userSession으로 nickname 조회
            UserProfileDTO profile = chatService.getUserProfile(userSession);
            if (profile == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "사용자를 찾을 수 없습니다."));
            }

            boolean updated = chatService.updateUserAllowInvite(profile.getNickname(), allowInvite);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "초대 허용 설정이 업데이트되었습니다."));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "초대 허용 설정 업데이트에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("초대 허용 설정 업데이트 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "초대 허용 설정 업데이트에 실패했습니다."));
        }
    }

    // 모든 활성 닉네임 목록 조회
    @GetMapping("/all-nicknames")
    public ResponseEntity<Map<String, Object>> getAllNicknames() {
        try {
            List<String> nicknames = chatService.getAllActiveNicknames();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "nicknames", nicknames));
        } catch (Exception e) {
            log.error("닉네임 목록 조회 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "닉네임 목록 조회에 실패했습니다."));
        }
    }

    // 닉네임 삭제
    @DeleteMapping("/delete-nickname")
    public ResponseEntity<Map<String, Object>> deleteNickname(@RequestBody Map<String, String> request) {
        try {
            String userSession = request.get("userSession");

            if (userSession == null || userSession.trim().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "세션 정보가 필요합니다."));
            }

            // userSession으로 nickname 조회
            UserProfileDTO profile = chatService.getUserProfile(userSession);
            if (profile == null) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "사용자를 찾을 수 없습니다."));
            }

            boolean deleted = chatService.deleteUser(profile.getNickname());
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "닉네임이 삭제되었습니다."));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "닉네임 삭제에 실패했습니다."));
            }
        } catch (Exception e) {
            log.error("닉네임 삭제 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "닉네임 삭제에 실패했습니다."));
        }
    }
}
