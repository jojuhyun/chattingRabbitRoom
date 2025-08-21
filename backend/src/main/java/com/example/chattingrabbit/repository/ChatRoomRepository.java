package com.example.chattingrabbit.repository;

import com.example.chattingrabbit.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    
    // 활성 채팅방 목록 조회 (참여자가 있는 방만)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true AND cr.roomId IN " +
            "(SELECT up.chatRoomId FROM UserParticipation up WHERE up.isActive = true)")
    List<ChatRoom> findActiveRoomsWithParticipants();
    
    // 모든 활성 채팅방 목록 조회 (관리자용)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true ORDER BY cr.regDate DESC")
    List<ChatRoom> findAllActiveRooms();
    
    // 1일 이상 메시지가 없는 채팅방 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.lastMessageTime < :cutoffTime AND cr.isActive = true")
    List<ChatRoom> findInactiveRooms(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 참여자가 없는 채팅방 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true AND cr.roomId NOT IN " +
            "(SELECT up.chatRoomId FROM UserParticipation up WHERE up.isActive = true)")
    List<ChatRoom> findEmptyRooms();
    
    // 오픈 채팅방 목록 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true AND cr.roomType = 'OPEN' ORDER BY cr.regDate DESC")
    List<ChatRoom> findOpenChatRooms();
    
    // 개인 채팅방 목록 조회 (참여자만)
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true AND cr.roomType = 'PRIVATE' AND cr.roomId IN " +
            "(SELECT up.chatRoomId FROM UserParticipation up WHERE up.userId = :userId AND up.isActive = true)")
    List<ChatRoom> findPrivateChatRoomsByUserId(@Param("userId") String userId);
}
