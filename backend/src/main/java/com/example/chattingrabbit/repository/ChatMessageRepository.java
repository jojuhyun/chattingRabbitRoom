package com.example.chattingrabbit.repository;

import com.example.chattingrabbit.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 채팅방의 메시지 조회 (입장 시간 이후부터)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoomId = :roomId AND cm.regDate >= :enterTime ORDER BY cm.regDate ASC")
    List<ChatMessage> findMessagesByRoomIdAndEnterTime(@Param("roomId") String roomId,
            @Param("enterTime") LocalDateTime enterTime);

    // 특정 채팅방의 모든 메시지 조회 (관리자용)
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoomId = :roomId ORDER BY cm.regDate ASC")
    List<ChatMessage> findAllMessagesByRoomId(@Param("roomId") String roomId);

    // 특정 채팅방의 마지막 메시지 시간 조회
    @Query("SELECT MAX(cm.regDate) FROM ChatMessage cm WHERE cm.chatRoomId = :roomId")
    LocalDateTime findLastMessageTimeByRoomId(@Param("roomId") String roomId);

    // 특정 채팅방의 메시지 개수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoomId = :roomId")
    Long countMessagesByRoomId(@Param("roomId") String roomId);
    
    // 특정 채팅방의 모든 메시지 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage cm WHERE cm.chatRoomId = :roomId")
    void deleteByChatRoomId(@Param("roomId") String roomId);
}
