package com.example.chattingrabbit.service;

import com.example.chattingrabbit.dto.ChatDTO;
import com.example.chattingrabbit.dto.ChatRoomDTO;
import com.example.chattingrabbit.dto.ChatRoomDetailDTO;
import com.example.chattingrabbit.dto.ChatRoomListDTO;
import com.example.chattingrabbit.dto.InviteUserDTO;
import com.example.chattingrabbit.dto.UserProfileDTO;
import com.example.chattingrabbit.dto.UserSessionDTO;
import com.example.chattingrabbit.entity.ChatMessage;
import com.example.chattingrabbit.entity.ChatRoom;
import com.example.chattingrabbit.entity.UserParticipation;
import com.example.chattingrabbit.entity.UserSession;
import com.example.chattingrabbit.repository.ChatMessageRepository;
import com.example.chattingrabbit.repository.ChatRoomRepository;
import com.example.chattingrabbit.repository.UserParticipationRepository;
import com.example.chattingrabbit.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserParticipationRepository userParticipationRepository;
    private final UserSessionRepository userSessionRepository;

    // 일반 사용자용 채팅방 목록 조회 (참여자가 있는 방만)
    public List<ChatRoomDTO> getActiveChatRooms() {
        log.info("활성 채팅방 목록 조회");
        List<ChatRoom> rooms = chatRoomRepository.findActiveRoomsWithParticipants();
        return rooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 관리자용 채팅방 목록 조회 (모든 활성 방)
    public List<ChatRoomDTO> getAllActiveChatRooms() {
        log.info("모든 활성 채팅방 목록 조회");
        List<ChatRoom> rooms = chatRoomRepository.findAllActiveRooms();
        return rooms.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomDTO createChatRoom(String name) {
        log.info("채팅방 생성: name={}", name);

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(name)
                .name(name)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return convertToDTO(savedRoom);
    }

    // 채팅방 정보 조회
    public ChatRoomDTO getChatRoom(String roomId) {
        log.info("채팅방 정보 조회: roomId={}", roomId);
        Optional<ChatRoom> room = chatRoomRepository.findById(roomId);
        return room.map(this::convertToDTO).orElse(null);
    }

    // 채팅방 상세 정보 조회 (참여자 목록 포함)
    public ChatRoomDetailDTO getChatRoomDetail(String roomId) {
        log.info("채팅방 상세 정보 조회: roomId={}", roomId);

        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isEmpty()) {
            return null;
        }

        ChatRoom room = roomOpt.get();
        Long participantCount = userParticipationRepository.countActiveParticipantsByRoomId(roomId);
        List<UserParticipation> participants = userParticipationRepository.findActiveParticipantsByRoomId(roomId);

        List<ChatRoomDetailDTO.ParticipantDTO> participantDTOs = participants.stream()
                .map(this::convertToParticipantDTO)
                .collect(Collectors.toList());

        return ChatRoomDetailDTO.builder()
                .roomId(room.getRoomId())
                .name(room.getName())
                .regDate(room.getRegDate())
                .participantCount(participantCount)
                .participants(participantDTOs)
                .build();
    }

    // 사용자 입장 처리
    @Transactional
    public void enterChatRoom(String roomId, String userId, String nickname) {
        log.info("사용자 입장: roomId={}, userId={}, nickname={}", roomId, userId, nickname);

        // 기존 참여 정보가 있는지 확인
        Optional<UserParticipation> existingParticipation = userParticipationRepository
                .findActiveParticipationByRoomIdAndUserId(roomId, userId);

        if (existingParticipation.isPresent()) {
            // 이미 참여 중인 경우 아무것도 하지 않음 (재입장 불가)
            log.info("사용자가 이미 참여 중입니다: roomId={}, userId={}", roomId, userId);
            return;
        }

        // 다음 참여 순서 조회
        Integer nextJoinOrder = userParticipationRepository.getNextJoinOrder(roomId);

        // 새로운 참여 정보 생성
        UserParticipation newParticipation = UserParticipation.builder()
                .chatRoomId(roomId)
                .userId(userId)
                .nickname(nickname)
                .joinOrder(nextJoinOrder)
                .build();

        userParticipationRepository.save(newParticipation);
    }

        // 사용자 퇴장 처리 (떠나기 버튼을 통한 퇴장)
    @Transactional
    public void leaveChatRoom(String roomId, String userId) {
        log.info("사용자 퇴장: roomId={}, userId={}", roomId, userId);
        
        Optional<UserParticipation> participation = userParticipationRepository
                .findActiveParticipationByRoomIdAndUserId(roomId, userId);
        
        if (participation.isPresent()) {
            UserParticipation userParticipation = participation.get();
            userParticipation.setIsActive(false);
            userParticipation.setLeaveTime(LocalDateTime.now());
            userParticipationRepository.save(userParticipation);
            log.info("사용자가 채팅방을 떠났습니다: roomId={}, userId={}", roomId, userId);
            
            // 퇴장 후 참여자가 0인지 확인하고 즉시 삭제
            Long participantCount = userParticipationRepository.countActiveParticipantsByRoomId(roomId);
            if (participantCount == 0) {
                log.info("참여자가 0인 채팅방 즉시 삭제: roomId={}", roomId);
                deleteEmptyChatRoom(roomId);
            }
        }
    }

    // 메시지 저장
    @Transactional
    public void saveMessage(ChatDTO chatDTO) {
        log.info("메시지 저장: roomId={}, userId={}", chatDTO.getChatRoomId(), chatDTO.getUserId());

        ChatMessage message = ChatMessage.builder()
                .chatRoomId(chatDTO.getChatRoomId())
                .userId(chatDTO.getUserId())
                .nickname(chatDTO.getNickname())
                .message(chatDTO.getMessage())
                .messageType(chatDTO.getMessageType())
                .regDate(chatDTO.getRegDate())
                .build();

        chatMessageRepository.save(message);

        // 채팅방의 마지막 메시지 시간 업데이트
        ChatRoom chatRoom = chatRoomRepository.findById(chatDTO.getChatRoomId()).orElse(null);
        if (chatRoom != null) {
            chatRoom.setLastMessageTime(chatDTO.getRegDate());
            chatRoomRepository.save(chatRoom);
        }
    }

    // 사용자 입장 이후 메시지 조회 (참여한 방의 경우 전체 메시지 조회)
    public List<ChatDTO> getChatMessages(String roomId, String userId) {
        log.info("사용자 메시지 조회: roomId={}, userId={}", roomId, userId);

        Optional<UserParticipation> participation = userParticipationRepository
                .findActiveParticipationByRoomIdAndUserId(roomId, userId);

        if (participation.isPresent()) {
            // 참여한 방의 경우 전체 메시지 조회 (떠나기 전까지 모든 메시지)
            List<ChatMessage> messages = chatMessageRepository.findAllMessagesByRoomId(roomId);
            return messages.stream()
                    .map(this::convertToChatDTO)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    // 관리자용 전체 메시지 조회
    public List<ChatDTO> getAllChatMessages(String roomId) {
        log.info("관리자용 전체 메시지 조회: roomId={}", roomId);
        List<ChatMessage> messages = chatMessageRepository.findAllMessagesByRoomId(roomId);
        return messages.stream()
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

    // 닉네임 등록 및 세션 생성
    @Transactional
    public UserSessionDTO registerNickname(String nickname, String introduction) {
        log.info("닉네임 등록: nickname={}", nickname);

        // 닉네임 중복 확인
        Optional<UserSession> existingSession = userSessionRepository.findActiveSessionByNickname(nickname);
        if (existingSession.isPresent()) {
            return UserSessionDTO.builder()
                    .success(false)
                    .message("이미 사용 중인 닉네임입니다.")
                    .build();
        }

        // 새로운 UUID 생성
        String userSession = UUID.randomUUID().toString();

        // 세션 저장
        UserSession session = UserSession.builder()
                .nickname(nickname)
                .userSession(userSession)
                .introduction(introduction != null ? introduction : "")
                .build();

        userSessionRepository.save(session);

        return UserSessionDTO.builder()
                .nickname(nickname)
                .userSession(userSession)
                .success(true)
                .message("닉네임이 성공적으로 등록되었습니다.")
                .build();
    }

        // 사용자 소개 수정
    @Transactional
    public boolean updateUserIntroduction(String nickname, String introduction) {
        log.info("사용자 소개 수정: nickname={}", nickname);
        
        Optional<UserSession> session = userSessionRepository.findActiveSessionByNickname(nickname);
        if (session.isPresent()) {
            UserSession userSession = session.get();
            userSession.setIntroduction(introduction);
            userSessionRepository.save(userSession);
            return true;
        }
        return false;
    }

    // 사용자 초대 허용 설정 수정
    @Transactional
    public boolean updateUserAllowInvite(String nickname, Boolean allowInvite) {
        log.info("사용자 초대 허용 설정 수정: nickname={}, allowInvite={}", nickname, allowInvite);
        
        Optional<UserSession> session = userSessionRepository.findActiveSessionByNickname(nickname);
        if (session.isPresent()) {
            UserSession userSession = session.get();
            userSession.setAllowInvite(allowInvite);
            userSessionRepository.save(userSession);
            return true;
        }
        return false;
    }

    // 사용자 삭제
    @Transactional
    public boolean deleteUser(String nickname) {
        log.info("사용자 삭제: nickname={}", nickname);

        Optional<UserSession> session = userSessionRepository.findActiveSessionByNickname(nickname);
        if (session.isPresent()) {
            UserSession userSession = session.get();
            userSession.setIsActive(false);
            userSessionRepository.save(userSession);

            // 해당 사용자의 모든 채팅방 참여 정보 비활성화
            List<UserParticipation> participations = userParticipationRepository
                    .findActiveParticipationsByUserId(userSession.getUserSession());
            for (UserParticipation participation : participations) {
                participation.setIsActive(false);
                participation.setLeaveTime(LocalDateTime.now());
                userParticipationRepository.save(participation);
            }

            return true;
        }
        return false;
    }

    // 사용자 프로필 조회 (userSession으로 조회)
    public UserProfileDTO getUserProfile(String userSession) {
        log.info("사용자 프로필 조회: userSession={}", userSession);

        Optional<UserSession> session = userSessionRepository.findActiveSessionByUserSession(userSession);
        if (session.isPresent()) {
            UserSession userSessionEntity = session.get();
                    return UserProfileDTO.builder()
                .nickname(userSessionEntity.getNickname())
                .introduction(userSessionEntity.getIntroduction())
                .allowInvite(userSessionEntity.getAllowInvite())
                .lastUpdate(userSessionEntity.getLastUpdate())
                .isActive(userSessionEntity.getIsActive())
                .build();
        }
        return null;
    }

    // 채팅방 생성 (타입 지정)
    @Transactional
    public ChatRoomDTO createChatRoom(String name, String roomType, String creatorNickname) {
        log.info("채팅방 생성: name={}, type={}, creator={}", name, roomType, creatorNickname);

        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(name)
                .name(name)
                .roomType(roomType)
                .creatorNickname(creatorNickname)
                .build();

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return convertToDTO(savedRoom);
    }

    // 채팅방 목록 조회 (참여한 방과 다른 방 구분)
    public ChatRoomListDTO getChatRoomList(String userId) {
        log.info("채팅방 목록 조회: userId={}", userId);

        // 오픈 채팅방 목록
        List<ChatRoom> openRooms = chatRoomRepository.findOpenChatRooms();

        // 개인 채팅방 목록 (참여자만)
        List<ChatRoom> privateRooms = chatRoomRepository.findPrivateChatRoomsByUserId(userId);

        // 참여한 방들 (오픈 + 개인)
        List<ChatRoom> participatedRooms = new ArrayList<>();
        participatedRooms.addAll(openRooms);
        participatedRooms.addAll(privateRooms);

        // 참여하지 않은 오픈 채팅방들
        List<ChatRoom> otherOpenRooms = openRooms.stream()
                .filter(room -> !isUserParticipating(room.getRoomId(), userId))
                .collect(Collectors.toList());

        return ChatRoomListDTO.builder()
                .participatedRooms(convertToChatRoomListDTO(participatedRooms, userId))
                .otherRooms(convertToChatRoomListDTO(otherOpenRooms, userId))
                .build();
    }

    // 사용자 초대
    @Transactional
    public InviteUserDTO inviteUserToRoom(String roomId, String targetNickname, String inviterNickname) {
        log.info("사용자 초대: roomId={}, target={}, inviter={}", roomId, targetNickname, inviterNickname);

        // 대상 사용자 확인
        Optional<UserSession> targetSession = userSessionRepository.findActiveSessionByNickname(targetNickname);
        if (targetSession.isEmpty()) {
            return InviteUserDTO.builder()
                    .success(false)
                    .message("존재하지 않는 사용자입니다.")
                    .build();
        }

        // 초대 허용 여부 확인
        UserSession targetUser = targetSession.get();
        if (!targetUser.getAllowInvite()) {
            return InviteUserDTO.builder()
                    .success(false)
                    .message("초대를 허용하지 않는 사용자입니다.")
                    .build();
        }

        // 채팅방 확인
        Optional<ChatRoom> room = chatRoomRepository.findById(roomId);
        if (room.isEmpty()) {
            return InviteUserDTO.builder()
                    .success(false)
                    .message("존재하지 않는 채팅방입니다.")
                    .build();
        }

        // 이미 참여 중인지 확인
        if (isUserParticipating(roomId, targetSession.get().getUserSession())) {
            return InviteUserDTO.builder()
                    .success(false)
                    .message("이미 참여 중인 사용자입니다.")
                    .build();
        }

        // 이전에 퇴장한 사용자인지 확인 (재입장 가능)
        Optional<UserParticipation> previousParticipation = userParticipationRepository
                .findByChatRoomIdAndUserIdOrderByEnterTimeDesc(roomId, targetSession.get().getUserSession());

        if (previousParticipation.isPresent()) {
            UserParticipation prev = previousParticipation.get();
            if (!prev.getIsActive() && prev.getLeaveTime() != null) {
                // 이전에 퇴장한 사용자이므로 재입장 처리
                prev.setIsActive(true);
                prev.setLeaveTime(null);
                prev.setEnterTime(LocalDateTime.now());
                userParticipationRepository.save(prev);

                return InviteUserDTO.builder()
                        .roomId(roomId)
                        .targetNickname(targetNickname)
                        .inviterNickname(inviterNickname)
                        .success(true)
                        .message("사용자가 재입장했습니다.")
                        .build();
            }
        }

        // 새로운 사용자 초대 (초대 정보로 참여 생성)
        Integer nextJoinOrder = userParticipationRepository.getNextJoinOrder(roomId);
        UserParticipation participation = UserParticipation.builder()
                .chatRoomId(roomId)
                .userId(targetSession.get().getUserSession())
                .nickname(targetNickname)
                .joinOrder(nextJoinOrder)
                .isInvited(true)
                .inviteTime(LocalDateTime.now())
                .build();

        userParticipationRepository.save(participation);

        return InviteUserDTO.builder()
                .roomId(roomId)
                .targetNickname(targetNickname)
                .inviterNickname(inviterNickname)
                .success(true)
                .message("사용자가 성공적으로 초대되었습니다.")
                .build();
    }

    // 사용자가 채팅방에 참여 중인지 확인
    private boolean isUserParticipating(String roomId, String userId) {
        Optional<UserParticipation> participation = userParticipationRepository
                .findActiveParticipationByRoomIdAndUserId(roomId, userId);
        return participation.isPresent();
    }

        // 3일 이상 메시지가 없는 채팅방의 모든 참여자 퇴장 처리 및 빈 채팅방 삭제
    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    @Transactional
    public void removeUsersFromInactiveRooms() {
        log.info("비활성 채팅방 사용자 자동 퇴장 처리 및 빈 채팅방 삭제 시작");
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(3);
        List<UserParticipation> inactiveParticipants = userParticipationRepository
                .findActiveParticipantsInInactiveRooms(cutoffTime);
        
        for (UserParticipation participation : inactiveParticipants) {
            participation.setIsActive(false);
            participation.setLeaveTime(LocalDateTime.now());
            userParticipationRepository.save(participation);
            log.info("사용자 자동 퇴장: roomId={}, userId={}", participation.getChatRoomId(), participation.getUserId());
        }
        
        log.info("비활성 채팅방 사용자 자동 퇴장 처리 완료: {}명", inactiveParticipants.size());
        
        // 참여자가 0인 채팅방 즉시 삭제
        deleteEmptyChatRooms();
    }

    // 30분 이상 업데이트되지 않은 세션 삭제
    @Scheduled(fixedRate = 1800000) // 30분마다 실행
    @Transactional
    public void removeInactiveSessions() {
        log.info("비활성 세션 자동 삭제 처리 시작");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(30);
        List<UserSession> inactiveSessions = userSessionRepository.findInactiveSessions(cutoffTime);

        for (UserSession session : inactiveSessions) {
            session.setIsActive(false);
            userSessionRepository.save(session);
            log.info("비활성 세션 삭제: nickname={}", session.getNickname());
        }

        log.info("비활성 세션 자동 삭제 처리 완료: {}개", inactiveSessions.size());
    }

        // 특정 빈 채팅방 즉시 삭제 (메시지 포함)
    @Transactional
    public void deleteEmptyChatRoom(String roomId) {
        log.info("빈 채팅방 즉시 삭제: roomId={}", roomId);
        
        Optional<ChatRoom> roomOpt = chatRoomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            ChatRoom room = roomOpt.get();
            
            // 메시지 삭제
            chatMessageRepository.deleteByChatRoomId(roomId);
            
            // 채팅방 삭제
            chatRoomRepository.delete(room);
            
            log.info("빈 채팅방 삭제 완료: roomId={}, name={}", roomId, room.getName());
        }
    }

    // 참여자가 0인 채팅방 즉시 삭제 (메시지 포함)
    @Transactional
    public void deleteEmptyChatRooms() {
        log.info("빈 채팅방 즉시 삭제 처리 시작");
        
        // 활성 상태인 모든 채팅방 조회
        List<ChatRoom> activeRooms = chatRoomRepository.findAllActiveRooms();
        int deletedCount = 0;
        
        for (ChatRoom room : activeRooms) {
            // 각 채팅방의 활성 참여자 수 확인
            Long participantCount = userParticipationRepository.countActiveParticipantsByRoomId(room.getRoomId());
            
            if (participantCount == 0) {
                // 참여자가 0인 경우 메시지와 채팅방 모두 삭제
                log.info("빈 채팅방 삭제: roomId={}, name={}", room.getRoomId(), room.getName());
                
                // 메시지 삭제
                chatMessageRepository.deleteByChatRoomId(room.getRoomId());
                
                // 채팅방 삭제
                chatRoomRepository.delete(room);
                
                deletedCount++;
            }
        }
        
        log.info("빈 채팅방 즉시 삭제 처리 완료: {}개", deletedCount);
    }

    // 채팅방 완전 삭제 (관리자용)
    @Transactional
    public boolean deleteChatRoom(String roomId) {
        log.info("채팅방 완전 삭제: roomId={}", roomId);
        
        // 참여자가 없는 방인지 확인
        Long participantCount = userParticipationRepository.countActiveParticipantsByRoomId(roomId);
        if (participantCount > 0) {
            log.warn("참여자가 있는 채팅방은 삭제할 수 없습니다: roomId={}, participantCount={}", roomId, participantCount);
            return false;
        }
        
        // 채팅방 비활성화
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(roomId);
        if (chatRoom.isPresent()) {
            ChatRoom room = chatRoom.get();
            room.setIsActive(false);
            chatRoomRepository.save(room);
            log.info("채팅방 비활성화 완료: roomId={}", roomId);
            return true;
        }
        
        return false;
    }

    // 세션 갱신
    @Transactional
    public UserSessionDTO refreshSession(String nickname, String userSession) {
        log.info("세션 갱신: nickname={}", nickname);

        // 기존 세션 확인
        Optional<UserSession> existingSession = userSessionRepository.findActiveSessionByUserSession(userSession);
        if (existingSession.isEmpty()) {
            return UserSessionDTO.builder()
                    .success(false)
                    .message("유효하지 않은 세션입니다.")
                    .build();
        }

        UserSession session = existingSession.get();
        if (!session.getNickname().equals(nickname)) {
            return UserSessionDTO.builder()
                    .success(false)
                    .message("닉네임과 세션이 일치하지 않습니다.")
                    .build();
        }

        // 새로운 UUID 생성
        String newUserSession = UUID.randomUUID().toString();
        session.setUserSession(newUserSession);
        session.setLastUpdate(LocalDateTime.now());

        userSessionRepository.save(session);

        return UserSessionDTO.builder()
                .nickname(nickname)
                .userSession(newUserSession)
                .success(true)
                .message("세션이 갱신되었습니다.")
                .build();
    }

    // DTO 변환 메서드들
    private ChatRoomDTO convertToDTO(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .name(chatRoom.getName())
                .regDate(chatRoom.getRegDate())
                .build();
    }

    private ChatDTO convertToChatDTO(ChatMessage message) {
        return ChatDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .userId(message.getUserId())
                .nickname(message.getNickname())
                .message(message.getMessage())
                .messageType(message.getMessageType())
                .regDate(message.getRegDate())
                .build();
    }

    private ChatRoomDetailDTO.ParticipantDTO convertToParticipantDTO(UserParticipation participation) {
        return ChatRoomDetailDTO.ParticipantDTO.builder()
                .nickname(participation.getNickname())
                .enterTime(participation.getEnterTime())
                .joinOrder(participation.getJoinOrder())
                .build();
    }

    private List<ChatRoomListDTO.ChatRoomDTO> convertToChatRoomListDTO(List<ChatRoom> rooms, String userId) {
        return rooms.stream()
                .map(room -> {
                    Long participantCount = userParticipationRepository
                            .countActiveParticipantsByRoomId(room.getRoomId());
                    boolean isParticipating = isUserParticipating(room.getRoomId(), userId);

                    return ChatRoomListDTO.ChatRoomDTO.builder()
                            .roomId(room.getRoomId())
                            .name(room.getName())
                            .roomType(room.getRoomType())
                            .creatorNickname(room.getCreatorNickname())
                            .regDate(room.getRegDate())
                            .participantCount(participantCount)
                            .isParticipating(isParticipating)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
