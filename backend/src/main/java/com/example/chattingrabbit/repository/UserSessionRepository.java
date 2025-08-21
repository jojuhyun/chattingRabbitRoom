package com.example.chattingrabbit.repository;

import com.example.chattingrabbit.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    // 닉네임으로 활성 세션 조회
    @Query("SELECT us FROM UserSession us WHERE us.nickname = :nickname AND us.isActive = true")
    Optional<UserSession> findActiveSessionByNickname(@Param("nickname") String nickname);
    
    // userSession으로 활성 세션 조회
    @Query("SELECT us FROM UserSession us WHERE us.userSession = :userSession AND us.isActive = true")
    Optional<UserSession> findActiveSessionByUserSession(@Param("userSession") String userSession);
    
    // 30분 이상 업데이트되지 않은 세션 조회
    @Query("SELECT us FROM UserSession us WHERE us.lastUpdate < :cutoffTime AND us.isActive = true")
    List<UserSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 모든 활성 세션 조회
    @Query("SELECT us FROM UserSession us WHERE us.isActive = true ORDER BY us.lastUpdate DESC")
    List<UserSession> findAllActiveSessions();
    
    // 사용자 소개 수정
    @Query("UPDATE UserSession us SET us.introduction = :introduction WHERE us.nickname = :nickname")
    void updateIntroduction(@Param("nickname") String nickname, @Param("introduction") String introduction);
}
