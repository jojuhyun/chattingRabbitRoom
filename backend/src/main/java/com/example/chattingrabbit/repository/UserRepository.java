package com.example.chattingrabbit.repository;

import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.example.chattingrabbit.dto.UserDTO;

@Repository
public class UserRepository {

    private Map<String, UserDTO> userMap;
    private Map<String, String> nicknameToUserIdMap;
    private static final String SUPER_ADMIN_PASSWORD = "whwngusdlqslek.";

    @PostConstruct
    private void init() {
        userMap = new ConcurrentHashMap<>();
        nicknameToUserIdMap = new ConcurrentHashMap<>();

        // superadmin 계정 생성
        UserDTO superAdmin = UserDTO.builder()
                .userId("superadmin")
                .nickname("superadmin")
                .password(SUPER_ADMIN_PASSWORD)
                .isSuperAdmin(true)
                .joinTime(LocalDateTime.now())
                .lastAccessTime(LocalDateTime.now())
                .build();

        userMap.put("superadmin", superAdmin);
        nicknameToUserIdMap.put("superadmin", "superadmin");
    }

    public UserDTO createUser(String userId, String nickname, String password) {
        // 중복 검사
        if (userMap.containsKey(userId)) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        if (nicknameToUserIdMap.containsKey(nickname)) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        UserDTO user = UserDTO.builder()
                .userId(userId)
                .nickname(nickname)
                .password(password)
                .isSuperAdmin(false)
                .joinTime(LocalDateTime.now())
                .lastAccessTime(LocalDateTime.now())
                .build();

        userMap.put(userId, user);
        nicknameToUserIdMap.put(nickname, userId);

        return user;
    }

    public UserDTO authenticateUser(String userId, String password) {
        UserDTO user = userMap.get(userId);
        if (user != null && user.getPassword().equals(password)) {
            user.setLastAccessTime(LocalDateTime.now());
            return user;
        }
        return null;
    }

    public UserDTO getUserById(String userId) {
        return userMap.get(userId);
    }

    public UserDTO getUserByNickname(String nickname) {
        String userId = nicknameToUserIdMap.get(nickname);
        return userId != null ? userMap.get(userId) : null;
    }

    public List<UserDTO> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    public void removeUser(String userId) {
        UserDTO user = userMap.get(userId);
        if (user != null) {
            nicknameToUserIdMap.remove(user.getNickname());
            userMap.remove(userId);
        }
    }

    public boolean isUserIdUnique(String userId) {
        return !userMap.containsKey(userId);
    }

    public boolean isNicknameUnique(String nickname) {
        return !nicknameToUserIdMap.containsKey(nickname);
    }

    public boolean isSuperAdmin(String userId) {
        UserDTO user = userMap.get(userId);
        return user != null && user.isSuperAdmin();
    }

    public String getSuperAdminPassword() {
        return SUPER_ADMIN_PASSWORD;
    }
}
