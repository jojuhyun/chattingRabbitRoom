package com.example.chattingrabbit.service;

import org.springframework.stereotype.Service;

import com.example.chattingrabbit.dto.UserDTO;
import com.example.chattingrabbit.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final UserRepository userRepository;

    public UserDTO registerUser(String userId, String nickname, String password) {
        log.info("사용자 등록: userId={}, nickname={}", userId, nickname);
        return userRepository.createUser(userId, nickname, password);
    }

    public UserDTO loginUser(String userId, String password) {
        log.info("사용자 로그인: userId={}", userId);
        return userRepository.authenticateUser(userId, password);
    }

    public void logoutUser(String userId) {
        log.info("사용자 로그아웃: userId={}", userId);
        userRepository.removeUser(userId);
    }

    public boolean checkUserIdAvailability(String userId) {
        return userRepository.isUserIdUnique(userId);
    }

    public boolean checkNicknameAvailability(String nickname) {
        return userRepository.isNicknameUnique(nickname);
    }

    public boolean isSuperAdmin(String userId) {
        return userRepository.isSuperAdmin(userId);
    }

    public UserDTO getUserById(String userId) {
        return userRepository.getUserById(userId);
    }
}
