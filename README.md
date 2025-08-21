# 🐰 ChattingRabbit - 실시간 채팅 시스템

ChattingRabbit은 Spring Boot와 Vue.js를 기반으로 한 현대적인 실시간 채팅 시스템입니다. **RabbitMQ를 메시지 브로커로 사용**하여 안전하고 효율적인 채팅 환경을 제공합니다.

## ✨ 주요 기능

### 🔐 사용자 관리 시스템

- **닉네임 등록**: 필수 닉네임 등록으로 채팅 참여
- **자기소개**: 사용자 프로필에 자기소개 추가/수정 가능
- **초대 허용 설정**: 채팅방 초대 허용/거부 설정 가능
- **세션 관리**: UUID 기반 세션으로 10분마다 자동 갱신
- **자동 로그아웃**: 30분 비활성 시 자동 로그아웃

### 💬 채팅방 시스템

- **채팅방 타입**: 오픈 채팅방(모든 사용자) vs 개인 채팅방(참여자만)
- **참여자 관리**: 참여자 수 표시, 참여 순서별 목록
- **초대 시스템**: 다른 사용자를 채팅방에 초대 가능
- **자동 정리**: 3일 이상 메시지 없는 방의 참여자 자동 퇴장
- **즉시 삭제**: 참여자가 0인 채팅방과 메시지 즉시 삭제

### 👁️ 메시지 가시성 제어

- **참여한 채팅방**: 떠나기 버튼을 누르기 전까지 모든 메시지 조회 가능
- **재입장 시**: 떠나기 전까지의 모든 메시지를 다시 볼 수 있음
- **관리자 권한**: 모든 채팅방의 전체 메시지 조회 가능

### 🛡️ 관리자 기능

- **채팅방 관리**: 모든 채팅방 목록 조회 및 삭제
- **통계 대시보드**: 전체/활성/빈 채팅방 수 표시
- **수동 정리**: 비활성 채팅방 수동 정리 실행

### 🎨 사용자 인터페이스

- **반응형 디자인**: 모바일과 데스크톱 모두 지원
- **직관적 UI**: Element Plus 컴포넌트로 현대적인 디자인
- **사용자 경험**: 툴팁, 확인 다이얼로그 등 사용자 친화적 요소

## 🏗️ 기술적 아키텍처

### **백엔드 (Spring Boot 3.2.0)**

- **Java 17**: 최신 LTS 버전 사용
- **Spring Boot**: 웹 애플리케이션 프레임워크
- **Spring WebSocket**: 실시간 통신 지원
- **Spring Data JPA**: 데이터 영속성 관리
- **H2 Database**: 인메모리 데이터베이스
- **RabbitMQ**: 메시지 브로커 (필수)
- **STOMP**: 메시징 프로토콜
- **Lombok**: 보일러플레이트 코드 제거
- **Spring Scheduling**: 자동 정리 작업

### **프론트엔드 (Vue.js 3.4.0)**

- **Vue 3**: Composition API 기반
- **Vue Router 4**: 클라이언트 사이드 라우팅
- **Pinia**: 상태 관리 라이브러리
- **Element Plus**: UI 컴포넌트 라이브러리
- **Vite**: 빠른 빌드 도구
- **SockJS**: WebSocket 폴백 지원
- **@stomp/stompjs**: STOMP 클라이언트

## 🚀 실행 방법

### 1. 사전 요구사항

- **Java 17** 이상
- **Node.js 18** 이상
- **npm** 또는 **yarn**
- **RabbitMQ 3.8+** (필수)

### 2. RabbitMQ 설치 및 설정

#### **Windows에서 RabbitMQ 설치**

```bash
# Chocolatey 사용 (권장)
choco install erlang
choco install rabbitmq

# 또는 수동 설치
# https://www.rabbitmq.com/install-windows.html
```

#### **RabbitMQ 서비스 시작**

```bash
# 서비스 시작
net start RabbitMQ

# 또는 수동 시작
rabbitmq-server
```

#### **STOMP 플러그인 활성화**

```bash
# RabbitMQ 관리 플러그인 활성화
rabbitmq-plugins enable rabbitmq_management

# STOMP 플러그인 활성화
rabbitmq-plugins enable rabbitmq_stomp

# RabbitMQ 재시작
net stop RabbitMQ
net start RabbitMQ
```

#### **RabbitMQ 관리 콘솔 접속**

- URL: http://localhost:15672
- 사용자명: `guest`
- 비밀번호: `guest`

### 3. 프로젝트 클론

```bash
git clone <repository-url>
cd ChattingRabbit
```

### 4. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

또는 Windows에서:

```cmd
cd backend
gradlew.bat bootRun
```

### 5. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

### 6. 배치 파일 사용 (Windows)

```cmd
# 전체 서비스 시작
start-all.bat

# 백엔드만 시작
start-backend.bat

# 프론트엔드만 시작
start-frontend.bat
```

### 7. 브라우저 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **H2 콘솔**: http://localhost:8080/h2-console
- **RabbitMQ 관리**: http://localhost:15672

## 🔧 RabbitMQ 서비스 관리

### **서비스 상태 확인**

```cmd
# Windows 서비스 상태 확인
sc query RabbitMQ

# RabbitMQ 상태 확인
rabbitmqctl status

# 플러그인 목록 확인
rabbitmq-plugins list
```

### **서비스 시작/중지**

```cmd
# 서비스 시작
net start RabbitMQ

# 서비스 중지
net stop RabbitMQ

# 또는 Windows 서비스 관리자에서 수동으로 제어
```

### **자동 시작 설정**

```cmd
# 서비스 자동 시작 설정
sc config RabbitMQ start= auto

# 서비스 수동 시작 설정
sc config RabbitMQ start= demand
```

### **포트 확인**

```cmd
# 사용 중인 포트 확인
netstat -an | findstr "5672\|15672\|61613"

# RabbitMQ 기본 포트
# 5672: AMQP 프로토콜
# 15672: 관리 콘솔
# 61613: STOMP 플러그인
```

## 📡 API 엔드포인트

### **사용자 관리** (`/api/users`)

- `POST /register` - 닉네임 등록
- `POST /refresh-session` - 세션 갱신
- `GET /profile` - 프로필 조회
- `PUT /update-profile` - 프로필 수정
- `PUT /update-allow-invite` - 초대 허용 설정 수정
- `DELETE /delete-nickname` - 닉네임 삭제

### **채팅방 관리** (`/api/chat`)

- `GET /rooms` - 채팅방 목록 조회
- `POST /rooms` - 채팅방 생성
- `GET /rooms/{roomId}` - 채팅방 상세 정보
- `POST /rooms/{roomId}/join` - 채팅방 참여
- `POST /rooms/{roomId}/leave` - 채팅방 떠나기
- `GET /rooms/{roomId}/messages` - 메시지 조회
- `POST /messages` - 메시지 전송
- `POST /rooms/{roomId}/invite` - 사용자 초대

### **관리자 기능** (`/api/admin`)

- `GET /rooms` - 모든 채팅방 조회
- `GET /rooms/{roomId}/messages` - 전체 메시지 조회
- `DELETE /rooms/{roomId}` - 채팅방 삭제
- `POST /cleanup` - 자동 정리 실행

### **시스템 상태** (`/api/health`)

- `GET /health` - 전체 시스템 상태 확인
- `GET /health/rabbitmq` - RabbitMQ 연결 상태 확인

### **WebSocket** (`/stomp`)

- `/topic/chat.{roomId}` - 채팅 메시지 구독
- `/topic/participants.{roomId}` - 참여자 목록 업데이트
- `/app/chat.message` - 메시지 전송

## 🐰 RabbitMQ 메시지 흐름

### **메시지 전송 과정**

1. 클라이언트 → WebSocket → StompChatController
2. StompChatController → RabbitMQMessageService
3. RabbitMQMessageService → RabbitMQ Exchange
4. RabbitMQ Exchange → Queue (라우팅 키 기반)
5. RabbitMQMessageListener → WebSocket → 클라이언트

### **RabbitMQ 구성 요소**

- **Exchange**: `chat.exchange` (Topic Exchange)
- **Queue**: `chat.queue`
- **Routing Key**: `chat.room.{roomId}`
- **STOMP Port**: 61613

### **메시지 타입**

- **ENTER**: 채팅방 입장
- **MESSAGE**: 일반 메시지
- **BROADCAST**: 방송 메시지
- **LEAVE**: 채팅방 퇴장

## 🗄️ 데이터베이스 스키마

### **ChatMessage** (채팅 메시지)

- `id`: 메시지 ID (PK)
- `chatRoomId`: 채팅방 ID
- `userId`: 사용자 ID
- `nickname`: 사용자 닉네임
- `message`: 메시지 내용
- `messageType`: 메시지 타입 (TEXT, ENTER, LEAVE)
- `regDate`: 등록 시간

### **ChatRoom** (채팅방)

- `roomId`: 방 ID (PK)
- `name`: 방 이름
- `roomType`: 방 타입 (OPEN, PRIVATE)
- `creatorNickname`: 생성자 닉네임
- `regDate`: 생성 시간
- `lastMessageTime`: 마지막 메시지 시간
- `isActive`: 활성 상태

### **UserParticipation** (사용자 참여)

- `id`: 참여 ID (PK)
- `chatRoomId`: 채팅방 ID
- `userId`: 사용자 ID
- `nickname`: 사용자 닉네임
- `enterTime`: 입장 시간
- `leaveTime`: 퇴장 시간
- `isActive`: 활성 상태
- `joinOrder`: 참여 순서
- `isInvited`: 초대 여부
- `inviteTime`: 초대 시간

### **UserSession** (사용자 세션)

- `id`: 세션 ID (PK)
- `nickname`: 닉네임 (Unique)
- `userSession`: UUID 세션 (Unique)
- `introduction`: 자기소개
- `allowInvite`: 초대 허용 여부
- `lastUpdate`: 마지막 업데이트 시간
- `isActive`: 활성 상태

## 🎯 핵심 비즈니스 로직

### **메시지 가시성 제어**

- 사용자는 참여한 채팅방에서 떠나기 전까지 모든 메시지 조회 가능
- 재입장 시 떠나기 전까지의 모든 메시지를 다시 볼 수 있음
- 관리자는 모든 메시지에 접근 가능

### **자동 관리 시스템**

- 3일 이상 메시지가 없는 채팅방의 참여자 자동 퇴장
- 참여자가 0인 채팅방과 메시지 즉시 삭제
- 30분 이상 비활성인 세션 자동 삭제
- 참여자가 없는 채팅방은 일반 사용자에게 숨김

### **권한 제어**

- 일반 사용자: 참여한 채팅방만 접근 (떠나기 전까지 모든 메시지 조회 가능)
- 관리자: 모든 채팅방 접근 및 관리
- 초대 시스템: 개인 채팅방에만 적용
- 재입장: 떠나기 버튼을 통해 퇴장한 경우에만 가능

## 🖥️ 화면 구성

### 1. **닉네임 등록** (`/nickname-register`)

- 닉네임 입력 및 자기소개 작성
- 중복 닉네임 검증
- UUID 기반 세션 생성

### 2. **채팅방 목록** (`/rooms`)

- 참여한 채팅방과 참여하지 않은 오픈 채팅방 분리 표시
- 채팅방 타입별 구분 (오픈/개인)
- 참여자 수 및 방 생성자 정보

### 3. **채팅방** (`/rooms/:roomId`)

- 실시간 메시지 송수신 (RabbitMQ 기반)
- 참여자 목록 및 참여 순서 표시
- 사용자 초대 기능
- 채팅방 떠나기 기능 (떠나기 전까지의 모든 메시지 보존)

### 4. **사용자 프로필** (`/user-profile`)

- 자기소개 수정
- 초대 허용 설정 변경
- 닉네임 삭제 (계정 탈퇴)
- 프로필 정보 관리

### 5. **관리자 대시보드** (`/admin/rooms`)

- 전체 채팅방 통계
- 채팅방 관리 및 삭제
- 수동 정리 실행

## 🔧 개발 환경 설정

### **백엔드 개발**

```bash
cd backend
./gradlew bootRun
```

### **프론트엔드 개발**

```bash
cd frontend
npm install
npm run dev
```

### **데이터베이스 확인**

- H2 콘솔: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- 사용자명: `sa`
- 비밀번호: (비어있음)

### **RabbitMQ 확인**

- 관리 콘솔: http://localhost:15672
- 사용자명: `guest`
- 비밀번호: `guest`
- STOMP 포트: 61613

## 🚨 문제 해결

### **RabbitMQ 관련 문제**

#### **1. 서비스 시작 실패**

```cmd
# 이벤트 뷰어에서 오류 로그 확인
eventvwr.msc

# RabbitMQ 로그 확인
# 기본 위치: C:\Users\[사용자명]\AppData\Roaming\RabbitMQ\log
```

#### **2. 포트 충돌**

```cmd
# 사용 중인 포트 확인
netstat -an | findstr "5672\|15672\|61613"

# 충돌하는 프로세스 종료
taskkill /PID [프로세스ID] /F
```

#### **3. 연결 실패**

```cmd
# RabbitMQ 상태 확인
rabbitmqctl status

# 플러그인 상태 확인
rabbitmq-plugins list

# 서비스 재시작
net stop RabbitMQ
net start RabbitMQ
```

### **애플리케이션 관련 문제**

#### **1. 헬스체크 API로 상태 확인**

```bash
# RabbitMQ 상태 확인
curl http://localhost:8080/api/health/rabbitmq

# 전체 시스템 상태 확인
curl http://localhost:8080/api/health
```

#### **2. 로그 확인**

```cmd
# 애플리케이션 로그 확인
# 기본 위치: logs/chattingrabbit.log
```

## 📝 프로젝트 구조

```
ChattingRabbit/
├── backend/                          # Spring Boot 백엔드
│   ├── src/main/java/
│   │   └── com/example/chattingrabbit/
│   │       ├── config/              # 설정 클래스
│   │       │   ├── RabbitConfig.java        # RabbitMQ 설정
│   │       │   ├── StompConfig.java         # STOMP 설정
│   │       │   └── RabbitMQMessageListener.java # 메시지 리스너
│   │       ├── controller/          # REST API 컨트롤러
│   │       ├── dto/                 # 데이터 전송 객체
│   │       ├── entity/              # JPA 엔티티
│   │       ├── repository/          # 데이터 접근 계층
│   │       ├── service/             # 비즈니스 로직
│   │       │   └── RabbitMQMessageService.java # RabbitMQ 메시지 서비스
│   │       └── ChattingRabbitApplication.java
│   ├── src/main/resources/
│   │   └── application.properties   # 애플리케이션 설정
│   └── build.gradle                 # Gradle 빌드 설정
├── frontend/                         # Vue.js 프론트엔드
│   ├── src/
│   │   ├── components/              # Vue 컴포넌트
│   │   ├── router/                  # 라우터 설정
│   │   ├── stores/                  # Pinia 상태 관리
│   │   ├── views/                   # 페이지 뷰
│   │   ├── App.vue                  # 메인 앱 컴포넌트
│   │   └── main.js                  # 앱 진입점
│   ├── package.json                 # npm 의존성
│   └── vite.config.js               # Vite 설정
├── start-all.bat                     # 전체 서비스 시작 (Windows)
├── start-backend.bat                 # 백엔드 시작 (Windows)
├── start-frontend.bat                # 프론트엔드 시작 (Windows)
├── README.md                         # 프로젝트 문서
└── RABBITMQ_SETUP.md                # RabbitMQ 상세 설치 가이드
```

## 🚨 주의사항

### **보안**

- 프로덕션 환경에서는 H2 대신 PostgreSQL, MySQL 등 사용 권장
- CORS 설정을 실제 도메인으로 제한
- API 키나 민감한 정보는 환경 변수로 관리
- RabbitMQ 기본 계정(guest) 변경 권장

### **성능**

- 대용량 메시지 처리를 위한 페이징 구현 고려
- Redis를 활용한 세션 관리 고려
- 메시지 압축 및 최적화 고려
- RabbitMQ 클러스터링 고려

### **확장성**

- 마이크로서비스 아키텍처로 분리 고려
- 메시지 큐 시스템 도입 고려
- 로드 밸런싱 및 클러스터링 고려

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 문의

프로젝트에 대한 문의사항이나 버그 리포트는 이슈를 통해 제출해 주세요.

## 📚 추가 문서

- **[RABBITMQ_SETUP.md](RABBITMQ_SETUP.md)**: RabbitMQ 상세 설치 및 설정 가이드
- **[README.md](README.md)**: 프로젝트 개요 및 기본 사용법

---

**ChattingRabbit** - RabbitMQ 기반 안전하고 효율적인 실시간 채팅 시스템 🐰✨
