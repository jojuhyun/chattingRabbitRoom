# 🐳 Docker Desktop 배포 가이드

ChattingRabbit 프로젝트를 로컬 Docker Desktop 환경에 배포하기 위한 상세한 가이드입니다.

## 📋 목차

1. [사전 준비사항](#-사전-준비사항)
2. [RabbitMQ 배포](#-rabbitmq-배포)
3. [백엔드 배포](#-백엔드-배포)
4. [프론트엔드 배포](#-프론트엔드-배포)
5. [전체 서비스 배포](#-전체-서비스-배포)
6. [모니터링 및 관리](#-모니터링-및-관리)
7. [문제 해결](#-문제-해결)

## 🔧 사전 준비사항

### **1. Docker Desktop 설치 및 설정**

#### **Docker Desktop 다운로드 및 설치**

```bash
# Docker Desktop 공식 사이트에서 다운로드
# https://www.docker.com/products/docker-desktop/

# Windows 10/11 Pro, Enterprise, Education
# WSL 2 백엔드 사용 권장
```

#### **Docker Desktop 설정 확인**

```bash
# Docker 버전 확인
docker --version
docker-compose --version

# Docker Desktop 상태 확인
docker info

# Docker Hub 로그인 (선택사항)
docker login
```

#### **시스템 요구사항**

- **Windows**: Windows 10 64-bit: Pro, Enterprise, Education (Build 16299 이상)
- **메모리**: 최소 4GB RAM (권장 8GB 이상)
- **디스크**: 최소 20GB 여유 공간
- **WSL 2**: Windows 10 버전 2004 이상에서 권장

### **2. 프로젝트 구조 확인**

```bash
chattingRabbitRoom/
├── backend/
│   ├── build.gradle
│   ├── Dockerfile
│   └── src/
├── frontend/
│   ├── package.json
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
├── .env
└── README.md
```

## 🐰 RabbitMQ 배포

### **1. RabbitMQ Docker 이미지 다운로드**

```bash
# RabbitMQ 3.12-management 이미지 다운로드
docker pull rabbitmq:3.12-management

# 이미지 확인
docker images rabbitmq:3.12-management
```

### **2. RabbitMQ 컨테이너 실행**

#### **기본 실행**

```bash
# RabbitMQ 컨테이너 실행
docker run -d \
  --name rabbitmq \
  --hostname rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -p 61613:61613 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3.12-management

# 컨테이너 상태 확인
docker ps
docker logs rabbitmq
```

#### **볼륨 마운트를 통한 데이터 영속성**

```bash
# 데이터 디렉토리 생성
mkdir -p ./rabbitmq-data

# 볼륨 마운트로 실행
docker run -d \
  --name rabbitmq \
  --hostname rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -p 61613:61613 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  -v ./rabbitmq-data:/var/lib/rabbitmq \
  rabbitmq:3.12-management
```

### **3. RabbitMQ 플러그인 활성화**

```bash
# STOMP 플러그인 활성화
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stomp

# 관리 플러그인 확인
docker exec rabbitmq rabbitmq-plugins list

# RabbitMQ 재시작
docker restart rabbitmq
```

### **4. RabbitMQ 연결 테스트**

```bash
# 컨테이너 내부에서 연결 테스트
docker exec -it rabbitmq bash

# RabbitMQ 상태 확인
rabbitmqctl status

# 사용자 목록 확인
rabbitmqctl list_users

# 큐 목록 확인
rabbitmqctl list_queues

# 컨테이너에서 나가기
exit
```

### **5. RabbitMQ 관리 콘솔 접속**

- **URL**: http://localhost:15672
- **사용자명**: admin
- **비밀번호**: admin123

#### **관리 콘솔에서 확인할 항목**

- **Overview**: 전체 시스템 상태
- **Connections**: 연결된 클라이언트
- **Channels**: 활성 채널
- **Exchanges**: 교환소 목록
- **Queues**: 큐 목록
- **Admin**: 사용자 및 권한 관리

## 🖥️ 백엔드 배포

### **1. 백엔드 Dockerfile 생성**

#### **backend/Dockerfile**

```dockerfile
# 멀티스테이지 빌드
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Gradle 래퍼 및 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew clean build -x test --no-daemon

# 실행 단계
FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/api/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-XX:+UseG1GC", "-jar", "app.jar"]
```

### **2. 백엔드 환경 설정**

#### **backend/src/main/resources/application-docker.properties**

```properties
# Docker 환경 전용 설정
server.port=8080

# H2 데이터베이스 (개발용)
spring.datasource.url=jdbc:h2:mem:chattingrabbit
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 콘솔 활성화
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# RabbitMQ 설정
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin123
spring.rabbitmq.virtual-host=/

# 로깅 설정
logging.level.com.example.chattingrabbit=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.amqp=DEBUG

# WebSocket 설정
spring.websocket.max-text-message-size=8192
spring.websocket.max-binary-message-size=8192
```

### **3. 백엔드 빌드 및 실행**

#### **Docker 이미지 빌드**

```bash
# 백엔드 디렉토리로 이동
cd backend

# Docker 이미지 빌드
docker build -t chattingrabbit-backend:latest .

# 이미지 확인
docker images chattingrabbit-backend
```

#### **백엔드 컨테이너 실행**

```bash
# 백엔드 컨테이너 실행
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest

# 컨테이너 상태 확인
docker ps
docker logs chattingrabbit-backend
```

### **4. 백엔드 연결 테스트**

```bash
# 헬스체크
curl http://localhost:8080/api/health

# H2 콘솔 접속
# http://localhost:8080/h2-console

# WebSocket 연결 테스트
# http://localhost:8080/ws
```

## 🌐 프론트엔드 배포

### **1. 프론트엔드 Dockerfile 생성**

#### **frontend/Dockerfile**

```dockerfile
# 빌드 단계
FROM node:18-alpine as builder

WORKDIR /app

# 패키지 파일 복사 및 의존성 설치
COPY package*.json ./
RUN npm ci --only=production

# 소스 코드 복사 및 빌드
COPY . .
RUN npm run build

# 실행 단계
FROM nginx:alpine

# 빌드된 파일 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
```

### **2. Nginx 설정 파일 생성**

#### **frontend/nginx.conf**

```nginx
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 로그 설정
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    # gzip 압축
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;

    # 업스트림 서버 정의
    upstream backend {
        server chattingrabbit-backend:8080;
    }

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        # 정적 파일 캐싱
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # SPA 라우팅 지원
        location / {
            try_files $uri $uri/ /index.html;
        }

        # API 프록시
        location /api/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # WebSocket 프록시
        location /stomp/ {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # H2 콘솔 프록시
        location /h2-console/ {
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### **3. 프론트엔드 환경 설정**

#### **frontend/.env.docker**

```env
# Docker 환경 전용 환경 변수
VITE_API_BASE_URL=http://localhost/api
VITE_WS_BASE_URL=ws://localhost/stomp
VITE_APP_TITLE=ChattingRabbit (Docker)
```

### **4. 프론트엔드 빌드 및 실행**

#### **Docker 이미지 빌드**

```bash
# 프론트엔드 디렉토리로 이동
cd frontend

# Docker 이미지 빌드
docker build -t chattingrabbit-frontend:latest .

# 이미지 확인
docker images chattingrabbit-frontend
```

#### **프론트엔드 컨테이너 실행**

```bash
# 프론트엔드 컨테이너 실행
docker run -d \
  --name chattingrabbit-frontend \
  --network rabbitmq-network \
  -p 80:80 \
  chattingrabbit-frontend:latest

# 컨테이너 상태 확인
docker ps
docker logs chattingrabbit-frontend
```

### **5. 프론트엔드 연결 테스트**

```bash
# 웹 애플리케이션 접속
# http://localhost

# 정적 파일 로딩 확인
curl -I http://localhost/

# API 프록시 테스트
curl http://localhost/api/health
```

## 🚀 전체 서비스 배포

### **1. Docker 네트워크 생성**

```bash
# 사용자 정의 네트워크 생성
docker network create rabbitmq-network

# 네트워크 확인
docker network ls
docker network inspect rabbitmq-network
```

### **2. Docker Compose 파일 생성**

#### **docker-compose.yml**

```yaml
version: "3.8"

services:
  # RabbitMQ 서비스
  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: rabbitmq
    hostname: rabbitmq
    ports:
      - "5672:5672" # AMQP 프로토콜
      - "15672:15672" # 관리 콘솔
      - "61613:61613" # STOMP 프로토콜
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=admin123
      - RABBITMQ_DEFAULT_VHOST=/
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
      - ./rabbitmq-logs:/var/log/rabbitmq
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped

  # 백엔드 서비스
  backend:
    build: ./backend
    container_name: chattingrabbit-backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_USERNAME=admin
      - SPRING_RABBITMQ_PASSWORD=admin123
    depends_on:
      rabbitmq:
        condition: service_healthy
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped

  # 프론트엔드 서비스
  frontend:
    build: ./frontend
    container_name: chattingrabbit-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - rabbitmq-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s
    restart: unless-stopped

volumes:
  rabbitmq_data:
    driver: local

networks:
  rabbitmq-network:
    driver: bridge
```

### **3. 전체 서비스 실행**

#### **서비스 시작**

```bash
# 프로젝트 루트 디렉토리에서 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

#### **서비스 중지**

```bash
# 서비스 중지
docker-compose down

# 볼륨까지 제거
docker-compose down -v

# 이미지까지 제거
docker-compose down --rmi all
```

### **4. 서비스 상태 확인**

```bash
# 모든 컨테이너 상태 확인
docker ps -a

# 서비스별 로그 확인
docker-compose logs rabbitmq
docker-compose logs backend
docker-compose logs frontend

# 네트워크 연결 확인
docker network inspect rabbitmq-network
```

## 📊 모니터링 및 관리

### **1. Docker Desktop 대시보드**

#### **Containers 탭**

- 컨테이너 상태 및 리소스 사용량
- 로그 실시간 확인
- 컨테이너 시작/중지/재시작

#### **Images 탭**

- 이미지 크기 및 레이어 정보
- 이미지 삭제 및 정리

#### **Volumes 탭**

- 데이터 영속성 확인
- 볼륨 백업 및 복원

### **2. 명령어 기반 모니터링**

#### **리소스 사용량 확인**

```bash
# 컨테이너별 리소스 사용량
docker stats

# 특정 컨테이너 리소스 사용량
docker stats rabbitmq chattingrabbit-backend chattingrabbit-frontend

# 디스크 사용량 확인
docker system df
```

#### **로그 모니터링**

```bash
# 실시간 로그 확인
docker-compose logs -f --tail=100

# 특정 서비스 로그
docker-compose logs -f backend

# 로그 레벨별 필터링
docker-compose logs backend | grep ERROR
```

### **3. 헬스체크 및 상태 모니터링**

#### **서비스 상태 확인**

```bash
# 헬스체크 엔드포인트
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/rabbitmq

# RabbitMQ 상태 확인
curl -u admin:admin123 http://localhost:15672/api/overview
```

#### **연결 상태 확인**

```bash
# 포트 리스닝 확인
netstat -an | findstr "80\|8080\|5672\|15672\|61613"

# Docker 포트 매핑 확인
docker port rabbitmq
docker port chattingrabbit-backend
docker port chattingrabbit-frontend
```

## 🔧 문제 해결

### **1. 일반적인 문제 및 해결방법**

#### **포트 충돌 문제**

```bash
# 포트 사용 중인 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /PID <PID> /F

# 또는 다른 포트 사용
# docker-compose.yml에서 포트 변경
```

#### **메모리 부족 문제**

```bash
# Docker Desktop 메모리 설정 증가
# Docker Desktop > Settings > Resources > Memory: 4GB 이상

# 컨테이너 메모리 제한 설정
docker run -m 1g chattingrabbit-backend:latest
```

#### **네트워크 연결 문제**

```bash
# 네트워크 재생성
docker-compose down
docker network prune
docker-compose up -d

# 컨테이너 간 통신 테스트
docker exec chattingrabbit-backend ping rabbitmq
```

### **2. 서비스별 문제 해결**

#### **RabbitMQ 문제**

```bash
# RabbitMQ 서비스 재시작
docker restart rabbitmq

# 플러그인 상태 확인
docker exec rabbitmq rabbitmq-plugins list

# 사용자 권한 확인
docker exec rabbitmq rabbitmqctl list_users
docker exec rabbitmq rabbitmqctl list_user_permissions admin
```

#### **백엔드 문제**

```bash
# 애플리케이션 로그 확인
docker logs chattingrabbit-backend

# 환경 변수 확인
docker exec chattingrabbit-backend env | grep SPRING

# 데이터베이스 연결 테스트
curl http://localhost:8080/h2-console
```

#### **프론트엔드 문제**

```bash
# Nginx 설정 문법 검사
docker exec chattingrabbit-frontend nginx -t

# Nginx 재시작
docker exec chattingrabbit-frontend nginx -s reload

# 정적 파일 접근 확인
docker exec chattingrabbit-frontend ls -la /usr/share/nginx/html
```

### **3. 디버깅 도구**

#### **컨테이너 내부 접속**

```bash
# 백엔드 컨테이너 접속
docker exec -it chattingrabbit-backend bash

# 프론트엔드 컨테이너 접속
docker exec -it chattingrabbit-frontend sh

# RabbitMQ 컨테이너 접속
docker exec -it rabbitmq bash
```

#### **네트워크 디버깅**

```bash
# 네트워크 연결 테스트
docker exec chattingrabbit-backend curl -v http://rabbitmq:15672

# DNS 확인
docker exec chattingrabbit-backend nslookup rabbitmq
```

## 📚 추가 리소스

### **Docker Desktop 관련**

- [Docker Desktop 공식 문서](https://docs.docker.com/desktop/)
- [Docker Compose 가이드](https://docs.docker.com/compose/)
- [Docker 네트워킹](https://docs.docker.com/network/)

### **RabbitMQ 관련**

- [RabbitMQ Docker 가이드](https://www.rabbitmq.com/download.html)
- [RabbitMQ STOMP 플러그인](https://www.rabbitmq.com/stomp.html)
- [RabbitMQ 관리 API](https://www.rabbitmq.com/management.html)

### **Spring Boot 관련**

- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [Spring Boot 환경 설정](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)

### **Vue.js 관련**

- [Vue.js Docker 배포](https://vuejs.org/guide/scaling-up/deployment.html)
- [Nginx 설정 가이드](https://nginx.org/en/docs/)

---

**이 가이드를 따라 Docker Desktop에서 ChattingRabbit을 성공적으로 배포하세요!** 🐳✨

### **빠른 시작 명령어**

```bash
# 1. 프로젝트 루트 디렉토리로 이동
cd chattingRabbitRoom

# 2. 전체 서비스 시작
docker-compose up -d

# 3. 서비스 상태 확인
docker-compose ps

# 4. 로그 확인
docker-compose logs -f

# 5. 웹 애플리케이션 접속
# http://localhost
```
