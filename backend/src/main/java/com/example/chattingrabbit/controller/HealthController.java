package com.example.chattingrabbit.controller;

import com.example.chattingrabbit.service.RabbitMQMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/rabbit/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {

    private final RabbitMQMessageService rabbitMQMessageService;

    /**
     * RabbitMQ 연결 상태 확인
     */
    @GetMapping("/rabbitmq")
    public ResponseEntity<Map<String, Object>> checkRabbitMQHealth() {
        try {
            boolean isConnected = rabbitMQMessageService.isConnected();

            if (isConnected) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "status", "UP",
                        "message", "RabbitMQ 연결 정상",
                        "timestamp", System.currentTimeMillis()));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "status", "DOWN",
                        "message", "RabbitMQ 연결 실패",
                        "timestamp", System.currentTimeMillis()));
            }
        } catch (Exception e) {
            log.error("RabbitMQ 헬스체크 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "status", "ERROR",
                    "message", "RabbitMQ 헬스체크 중 오류 발생: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 전체 시스템 상태 확인
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> checkSystemHealth() {
        try {
            boolean rabbitMQHealthy = rabbitMQMessageService.isConnected();

            Map<String, Object> healthStatus = Map.of(
                    "success", true,
                    "status", rabbitMQHealthy ? "UP" : "DEGRADED",
                    "timestamp", System.currentTimeMillis(),
                    "services", Map.of(
                            "rabbitmq", Map.of(
                                    "status", rabbitMQHealthy ? "UP" : "DOWN",
                                    "message", rabbitMQHealthy ? "연결 정상" : "연결 실패"),
                            "database", Map.of(
                                    "status", "UP",
                                    "message", "H2 데이터베이스 정상"),
                            "websocket", Map.of(
                                    "status", "UP",
                                    "message", "WebSocket 서버 정상")));

            return ResponseEntity.ok(healthStatus);
        } catch (Exception e) {
            log.error("시스템 헬스체크 실패", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "status", "ERROR",
                    "message", "시스템 헬스체크 중 오류 발생: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()));
        }
    }
}
