package com.example.chattingrabbit.repository;

import com.example.chattingrabbit.entity.UserParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserParticipationRepository extends JpaRepository<UserParticipation, Long> {

    // 특정 채팅방의 활성 참여자 목록 조회 (참여 순서대로)
    @Query("SELECT up FROM UserParticipation up WHERE up.chatRoomId = :roomId AND up.isActive = true ORDER BY up.joinOrder ASC")
    List<UserParticipation> findActiveParticipantsByRoomId(@Param("roomId") String roomId);

    // 특정 사용자의 특정 채팅방 참여 정보 조회
    @Query("SELECT up FROM UserParticipation up WHERE up.chatRoomId = :roomId AND up.userId = :userId AND up.isActive = true")
    Optional<UserParticipation> findActiveParticipationByRoomIdAndUserId(@Param("roomId") String roomId,
            @Param("userId") String userId);

    // 특정 채팅방의 참여자 수 조회
    @Query("SELECT COUNT(up) FROM UserParticipation up WHERE up.chatRoomId = :roomId AND up.isActive = true")
    Long countActiveParticipantsByRoomId(@Param("roomId") String roomId);

    // 특정 사용자의 모든 채팅방 참여 정보 조회
    @Query("SELECT up FROM UserParticipation up WHERE up.userId = :userId AND up.isActive = true")
    List<UserParticipation> findActiveParticipationsByUserId(@Param("userId") String userId);

    // 1일 이상 메시지가 없는 채팅방의 활성 참여자 조회
    @Query("SELECT up FROM UserParticipation up WHERE up.chatRoomId IN " +
            "(SELECT cr.roomId FROM ChatRoom cr WHERE cr.lastMessageTime < :cutoffTime) " +
            "AND up.isActive = true")
    List<UserParticipation> findActiveParticipantsInInactiveRooms(@Param("cutoffTime") LocalDateTime cutoffTime);

    // 특정 채팅방의 다음 참여 순서 조회
    @Query("SELECT COALESCE(MAX(up.joinOrder), 0) + 1 FROM UserParticipation up WHERE up.chatRoomId = :roomId")
    Integer getNextJoinOrder(@Param("roomId") String roomId);

    // 특정 사용자의 특정 채팅방 참여 기록 조회 (최신순)
    @Query("SELECT up FROM UserParticipation up WHERE up.chatRoomId = :roomId AND up.userId = :userId ORDER BY up.enterTime DESC")
    Optional<UserParticipation> findByChatRoomIdAndUserIdOrderByEnterTimeDesc(@Param("roomId") String roomId,
            @Param("userId") String userId);
}
