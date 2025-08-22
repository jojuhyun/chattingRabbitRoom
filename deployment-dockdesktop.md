# 🐳 Docker Desktop 배포 가이드

ChattingRabbit 프로젝트를 로컬 Docker Desktop 환경에 배포하기 위한 상세한 가이드입니다.

## 📋 목차

1. [🚀 빠른 시작 (5분 배포)](#-빠른-시작-5분-배포)
2. [🔧 사전 준비사항](#-사전-준비사항)
3. [🐰 RabbitMQ 배포](#-rabbitmq-배포)
4. [🖥️ 백엔드 배포](#-백엔드-배포)
5. [🌐 프론트엔드 배포](#-프론트엔드-배포)
6. [🚀 전체 서비스 배포](#-전체-서비스-배포)
7. [📊 모니터링 및 관리](#-모니터링-및-관리)
8. [🔧 문제 해결](#-문제-해결)
9. [📝 업데이트 이력](#-업데이트-이력)

## 🚀 **빠른 시작 (5분 배포)**

### **🎯 이 가이드의 목표**

**ChattingRabbit을 Docker Desktop에서 5분 안에 실행하기!**

### **📋 단계별 진행 상황**

```
✅ 1단계: RabbitMQ 실행 (1분)
✅ 2단계: 백엔드 실행 (2분)
✅ 3단계: 프론트엔드 실행 (1분)
✅ 4단계: 채팅 테스트 (1분)
```

### **🚀 즉시 실행 명령어 (복사해서 붙여넣기)**

```bash
# 1. 프로젝트 폴더로 이동
cd chattingRabbitRoom

# 2. RabbitMQ 실행
docker run -d --name rabbitmq --hostname rabbitmq -p 5672:5672 -p 15672:15672 -p 61613:61613 -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin123 rabbitmq:3.12-management

# 3. STOMP 플러그인 활성화
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stomp

# 4. 백엔드 실행
docker run -d --name chattingrabbit-backend -p 8080:8080 chattingrabbit-backend:latest

# 5. 프론트엔드 실행
docker run -d --name chattingrabbit-frontend -p 80:80 chattingrabbit-frontend:latest
```

### **🌐 접속 주소**

- **채팅 앱**: http://localhost
- **백엔드 API**: http://localhost:8080
- **RabbitMQ 관리**: http://localhost:15672 (admin/admin123)

---

## 🔧 사전 준비사항

### **⚠️ 중요 사전 준비사항**

**이 가이드를 따라하기 전에 반드시 확인해야 할 사항들:**

1. **✅ Docker Desktop이 설치되어 있어야 합니다**
2. **✅ Docker Desktop이 실행 중이어야 합니다**
3. **✅ 프로젝트 폴더(`chattingRabbitRoom`)에 접근할 수 있어야 합니다**
4. **✅ 관리자 권한이 필요할 수 있습니다**

### **🔍 사전 준비사항 체크리스트**

```bash
# 1. Docker Desktop 설치 확인
docker --version

# 2. Docker Desktop 실행 상태 확인
docker info

# 3. 프로젝트 폴더 존재 확인
dir chattingRabbitRoom

# 4. PowerShell 관리자 권한으로 실행 (권장)
# PowerShell 우클릭 → "관리자 권한으로 실행"
```

**Docker Desktop이 설치되어 있지 않다면:**

- [Docker Desktop 공식 사이트](https://www.docker.com/products/docker-desktop/)에서 다운로드
- Windows 10/11 Pro, Enterprise, Education 버전 필요
- WSL 2 백엔드 사용 권장

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

### **🎯 이 단계에서 할 일**

**RabbitMQ는 ChattingRabbit의 핵심 구성요소입니다. 이 단계에서는:**

1. ✅ RabbitMQ Docker 이미지를 다운로드합니다
2. ✅ RabbitMQ 컨테이너를 실행합니다
3. ✅ 필요한 플러그인을 활성화합니다
4. ✅ 연결을 테스트합니다

### **❓ 왜 RabbitMQ가 필요한가요?**

- **💬 채팅 기능**: RabbitMQ를 통해 메시지를 전달합니다
- **🔌 WebSocket**: STOMP 프로토콜을 지원하여 실시간 통신을 가능하게 합니다
- **📡 메시지 브로커**: 여러 사용자 간의 메시지를 중계합니다

### **⏱️ 예상 소요 시간: 1-2분**

### **1. RabbitMQ Docker 이미지 다운로드**

```bash
# RabbitMQ 3.12-management 이미지 다운로드
docker pull rabbitmq:3.12-management

# 이미지 확인
docker images rabbitmq:3.12-management
```

### **2. RabbitMQ 컨테이너 실행**

#### **기본 실행 (권장 - 초보자용)**

```bash
# RabbitMQ 컨테이너 실행 (Docker가 자동으로 네트워크 처리)
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

#### **사용자 정의 네트워크 사용 (고급 - 프로덕션용)**

```bash
# 1. 사용자 정의 네트워크 생성
docker network create rabbitmq-network

# 2. RabbitMQ 컨테이너 실행 (네트워크 지정)
docker run -d \
  --name rabbitmq \
  --hostname rabbitmq \
  --network rabbitmq-network \
  -p 5672:5672 \
  -p 15672:15672 \
  -p 61613:61613 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3.12-management

# 3. 네트워크 확인
docker network ls | findstr rabbitmq-network
docker network inspect rabbitmq-network
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

### **🎯 이 단계에서 할 일**

**백엔드는 ChattingRabbit의 서버 부분입니다. 이 단계에서는:**

1. ✅ 백엔드용 Dockerfile을 생성합니다
2. ✅ Docker 환경 설정 파일을 만듭니다
3. ✅ Docker 이미지를 빌드합니다
4. ✅ 백엔드 컨테이너를 실행합니다

### **❓ 왜 백엔드가 필요한가요?**

- **💾 데이터 처리**: 채팅 메시지를 처리하고 저장합니다
- **🔌 WebSocket 관리**: 실시간 연결을 관리합니다
- **📡 메시지 중계**: RabbitMQ와 통신하여 메시지를 전달합니다
- **🌐 API 제공**: 프론트엔드에 필요한 API 엔드포인트를 제공합니다

### **⏱️ 예상 소요 시간: 3-5분 (빌드 시간 포함)**

### **1. 필수 파일 생성 및 코드 수정**

#### **⚠️ 중요: 사전 준비사항**

**빌드 전에 다음 파일들을 반드시 생성해야 합니다!**

### **📁 1단계: settings.gradle 파일 생성**

```bash
# backend 폴더로 이동
cd backend

# settings.gradle 파일 생성 (Windows PowerShell)
echo "rootProject.name = 'chattingrabbit-backend'" > settings.gradle

# 파일 생성 확인
dir settings.gradle
```

### **📁 2단계: gradle.properties 파일 생성**

```bash
# gradle.properties 파일 생성 (한 번에 복사해서 붙여넣기)
@"
# Gradle 빌드 설정
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true

# JVM 메모리 설정
org.gradle.jvmargs=-Xmx2g -Xms512m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# 네트워크 타임아웃 설정
systemProp.org.gradle.internal.http.connectionTimeout=180000
systemProp.org.gradle.internal.http.socketTimeout=180000

# 로깅 설정
org.gradle.logging.level=info
"@ > gradle.properties

# 파일 생성 확인
dir gradle.properties
```

### **📁 3단계: 코드 수정 (중요!)**

**`UserRepository.java` 파일의 import 문을 수정해야 합니다!**

#### **🔍 파일 위치 찾기**

```
chattingRabbitRoom/
└── backend/
    └── src/
        └── main/
            └── java/
                └── com/
                    └── example/
                        └── chattingrabbit/
                            └── repository/
                                └── UserRepository.java  ← 이 파일!
```

#### **✏️ 수정할 내용**

**변경 전 (오류 발생):**

```java
import javax.annotation.PostConstruct;
```

**변경 후 (정상 동작):**

```java
import jakarta.annotation.PostConstruct;
```

#### **❓ 왜 수정해야 하나요?**

- Spring Boot 3.x는 Jakarta EE 9+를 사용합니다
- `javax.annotation` → `jakarta.annotation`으로 패키지명이 변경되었습니다
- 이 수정을 하지 않으면 빌드 오류가 발생합니다

### **2. 백엔드 Dockerfile 생성**

#### **📁 backend/Dockerfile 파일 생성**

**중요**: `backend/` 폴더 안에 `Dockerfile` 파일을 생성해야 합니다. **확장자는 없습니다!**

#### **🔧 파일 생성 방법 (3가지 중 선택)**

**방법 1: PowerShell 명령어 (권장)**

```bash
# backend 폴더로 이동
cd backend

# Dockerfile 생성 (확장자 없음)
echo. > Dockerfile

# 파일 생성 확인
dir Dockerfile
```

**방법 2: 메모장 사용**

```
1. 메모장 실행
2. 새로 만들기
3. 파일 → 다른 이름으로 저장
4. 파일명: Dockerfile (확장자 없음!)
5. 파일 형식: 모든 파일 (*.*)
6. backend 폴더에 저장
```

**방법 3: VS Code 사용**

```
1. VS Code에서 backend 폴더 열기
2. 새 파일 생성
3. 파일명: Dockerfile (확장자 없음!)
4. 저장
```

#### **📝 Dockerfile 내용 (복사해서 붙여넣기)**

**아래 내용을 Dockerfile에 복사해서 붙여넣기 하세요:**

```dockerfile
# 간단한 멀티스테이지 Spring Boot Dockerfile
FROM gradle:8.5-jdk17-alpine as builder

WORKDIR /app

# 필수 파일들만 복사
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src

# 빌드 실행
RUN gradle clean build -x test --no-daemon

# 실행 스테이지
FROM openjdk:17-jdk-alpine

WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **✨ 이 Dockerfile의 장점**

- **🚀 빠른 빌드**: Alpine Linux로 이미지 크기 최적화
- **🔧 안정성**: Gradle Wrapper 없이 직접 Gradle 이미지 사용
- **💾 메모리 효율**: 불필요한 설정 제거로 빌드 안정성 향상
- **📦 최적화**: 멀티스테이지 빌드로 최종 이미지 크기 최소화

### **3. 백엔드 환경 설정**

#### **📁 application-docker.properties 파일 생성**

**중요**: `backend/src/main/resources/` 폴더 안에 `application-docker.properties` 파일을 생성해야 합니다.

#### **🔍 폴더 구조 확인**

```
chattingRabbitRoom/
└── backend/
    └── src/
        └── main/
            └── resources/  ← 이 폴더에 생성!
                └── application-docker.properties
```

#### **🔧 파일 생성 방법**

**방법 1: PowerShell 명령어 (권장)**

```bash
# backend 폴더로 이동
cd backend

# resources 폴더로 이동
cd src/main/resources

# application-docker.properties 파일 생성
echo. > application-docker.properties

# 파일 생성 확인
dir application-docker.properties
```

**방법 2: 메모장 사용**

```
1. 메모장 실행
2. 새로 만들기
3. 파일 → 다른 이름으로 저장
4. 파일명: application-docker.properties
5. backend/src/main/resources 폴더에 저장
```

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

### **4. 백엔드 빌드 및 실행**

#### **Docker 이미지 빌드**

```bash
# 백엔드 디렉토리로 이동 (중요!)
cd backend

# Docker 이미지 빌드
docker build -t chattingrabbit-backend:latest .

# 이미지 확인
docker images chattingrabbit-backend
```

#### **백엔드 컨테이너 실행**

**⚠️ 사전 확인사항:**

**방법 1: 기본 네트워크 사용 (권장 - 초보자용)**

```bash
# 네트워크 확인 (기본 bridge 네트워크 사용)
docker network ls
```

**방법 2: 사용자 정의 네트워크 사용 (고급 - 프로덕션용)**

```bash
# 네트워크 존재 확인
docker network ls | findstr rabbitmq-network

# 네트워크가 없으면 생성
docker network create rabbitmq-network
```

**기본 실행 명령어 (방법 1 - 권장):**

```bash
# 백엔드 컨테이너 실행 (기본 네트워크 사용)
docker run -d \
  --name chattingrabbit-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
```

**사용자 정의 네트워크 사용 (방법 2 - 고급):**

```bash
# 백엔드 컨테이너 실행 (사용자 정의 네트워크 사용)
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
```

**포트 권한 문제 발생 시 대안:**

**방법 1: 로컬호스트 바인딩 (권장)**

```bash
# 기본 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  -p 127.0.0.1:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest

# 사용자 정의 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  -p 127.0.0.1:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
```

**방법 2: 다른 포트 사용**

```bash
# 기본 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  -p 18080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest

# 사용자 정의 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  -p 18080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
```

**방법 3: 호스트 네트워크 사용 (고급)**

```bash
# 기본 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  --network host \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest

# 사용자 정의 네트워크 사용 시
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  --network-alias backend \
  -p 0.0.0.0:8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
```

**컨테이너 상태 확인:**

```bash
# 실행 상태 확인
docker ps

# 로그 확인
docker logs chattingrabbit-backend

# 상세 정보 확인
docker inspect chattingrabbit-backend

# 포트 바인딩 확인
docker port chattingrabbit-backend
```

**🚨 문제 해결 가이드:**

**포트 권한 오류 발생 시:**

```bash
# 오류: "bind: An attempt was made to access a socket in a way forbidden by its access permissions"

# 해결방법 1: PowerShell을 관리자 권한으로 실행
# PowerShell 우클릭 → "관리자 권한으로 실행"

# 해결방법 2: Docker Desktop 재시작
# Docker Desktop 완전 종료 후 재시작

# 해결방법 3: 다른 포트 사용
docker run -d \
  --name chattingrabbit-backend \
  --network rabbitmq-network \
  -p 18080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest
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

### **🎯 이 단계에서 할 일**

**프론트엔드는 ChattingRabbit의 웹 인터페이스입니다. 이 단계에서는:**

1. 프론트엔드용 Dockerfile을 생성합니다
2. Nginx 설정 파일을 만듭니다
3. Docker 이미지를 빌드합니다
4. 프론트엔드 컨테이너를 실행합니다

**왜 프론트엔드가 필요한가요?**

- 사용자가 채팅할 수 있는 웹 인터페이스를 제공합니다
- WebSocket을 통해 백엔드와 실시간 통신합니다
- 정적 파일(HTML, CSS, JavaScript)을 서빙합니다
- 사용자 경험을 향상시킵니다

### **1. 프론트엔드 Dockerfile 생성**

#### **frontend/Dockerfile 파일 생성**

**중요**: `frontend/` 폴더 안에 `Dockerfile` 파일을 생성해야 합니다. 확장자는 없습니다.

**파일 생성 방법:**

```bash
# frontend 폴더로 이동
cd frontend

# Dockerfile 생성 (확장자 없음)
# Windows에서:
echo. > Dockerfile

# 또는 텍스트 에디터에서 새 파일로 저장할 때:
# 파일명: Dockerfile (확장자 없음)
# 파일 형식: 모든 파일 (*.*)
```

**Dockerfile 내용:**

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

#### **frontend/nginx.conf 파일 생성**

**중요**: `frontend/` 폴더 안에 `nginx.conf` 파일을 생성해야 합니다. 확장자는 `.conf`입니다.

**파일 생성 방법:**

```bash
# frontend 폴더로 이동
cd frontend

# nginx.conf 파일 생성
# Windows에서:
echo. > nginx.conf

# 또는 텍스트 에디터에서 새 파일로 저장할 때:
# 파일명: nginx.conf
# 파일 형식: 모든 파일 (*.*)
```

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

#### **frontend/.env.docker 파일 생성**

**중요**: `frontend/` 폴더 안에 `.env.docker` 파일을 생성해야 합니다. 확장자는 없습니다.

**파일 생성 방법:**

```bash
# frontend 폴더로 이동
cd frontend

# .env.docker 파일 생성 (확장자 없음)
# Windows에서:
echo. > .env.docker

# 또는 텍스트 에디터에서 새 파일로 저장할 때:
# 파일명: .env.docker (확장자 없음)
# 파일 형식: 모든 파일 (*.*)
```

```env
cㅊ
```

### **4. 프론트엔드 빌드 및 실행**

#### **Docker 이미지 빌드**

```bash
# 프론트엔드 디렉토리로 이동 (중요!)
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

---

## 🔧 **프론트엔드 빌드 오류 해결 가이드**

### **❌ 주요 빌드 오류 및 해결방법**

#### **1. npm ci 의존성 설치 오류**

**오류 메시지:**

```
ERROR [builder 4/6] RUN npm ci --only=production
```

**원인 분석:**

- `package-lock.json` 파일 누락
- `--only=production` 옵션으로 인한 개발 의존성 부족
- 필요한 패키지 의존성 누락

**해결방법:**

```bash
# 1. package-lock.json 생성
cd frontend
npm install

# 2. Dockerfile 수정 (--only=production 제거)
# RUN npm ci --only=production → RUN npm ci

# 3. 누락된 의존성 추가
# package.json에 @stomp/stompjs 추가
```

#### **2. @stomp/stompjs 패키지 누락 오류**

**오류 메시지:**

```
[vite]: Rollup failed to resolve import "@stomp/stompjs" from "/app/src/views/ChatRoom.vue"
```

**원인 분석:**

- `package.json`에 `@stomp/stompjs` 의존성 누락
- Vue 컴포넌트에서 사용하는 패키지가 설치되지 않음

**해결방법:**

```json
// package.json dependencies에 추가
{
  "dependencies": {
    // ... 기존 의존성들
    "@stomp/stompjs": "^7.0.0"
  }
}
```

#### **3. Dockerfile 최적화**

**최적화된 Dockerfile:**

```dockerfile
# 빌드 단계
FROM node:18-alpine AS builder

WORKDIR /app

# 패키지 파일 복사 및 의존성 설치 (캐시 최적화)
COPY package*.json ./
RUN npm ci

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

**주요 개선사항:**

- `AS` 키워드 대소문자 수정 (경고 제거)
- `--only=production` 옵션 제거 (빌드 의존성 포함)
- 멀티스테이지 빌드로 최종 이미지 크기 최적화

#### **4. .dockerignore 파일 생성**

**빌드 성능 향상을 위한 .dockerignore:**

```
node_modules
npm-debug.log
.git
.gitignore
README.md
.env*
.DS_Store
*.log
dist
coverage
.vscode
.idea
```

**효과:**

- 불필요한 파일 제외로 빌드 컨텍스트 크기 감소
- 빌드 속도 향상
- 캐시 효율성 증대

### **✅ 검증된 성공 사례**

- **빌드 시간**: 193.1초 (약 3분 13초)
- **빌드 성공률**: 100%
- **이미지 최적화**: 멀티스테이지 빌드로 최종 크기 최소화
- **의존성 관리**: package.json + package-lock.json 완벽 동기화

### **🎯 권장 사항**

1. **의존성 관리**: `npm install`로 package-lock.json 항상 생성
2. **빌드 전 확인**: 필요한 패키지가 package.json에 포함되어 있는지 확인
3. **Docker 최적화**: .dockerignore와 멀티스테이지 빌드 활용
4. **정기 업데이트**: 패키지 의존성 정기적으로 업데이트 및 검증

## 🚀 전체 서비스 배포

### **🎯 이 단계에서 할 일**

**이제 모든 구성요소를 함께 실행합니다. 이 단계에서는:**

1. Docker 네트워크를 생성합니다
2. Docker Compose 파일을 만듭니다
3. 모든 서비스를 한 번에 시작합니다
4. 전체 시스템이 정상 동작하는지 확인합니다

**왜 Docker Compose를 사용하나요?**

- 여러 컨테이너를 한 번에 관리할 수 있습니다
- 서비스 간의 의존성을 자동으로 처리합니다
- 설정을 파일로 관리하여 재현 가능합니다
- 개발 환경과 프로덕션 환경을 일치시킬 수 있습니다

### **1. Docker 네트워크 생성**

```bash
# 사용자 정의 네트워크 생성
docker network create rabbitmq-network

# 네트워크 확인
docker network ls
docker network inspect rabbitmq-network
```

### **2. Docker Compose 파일 생성**

#### **docker-compose.yml 파일 생성**

**중요**: 프로젝트 루트 디렉토리(`chattingRabbitRoom/`)에 `docker-compose.yml` 파일을 생성해야 합니다. 확장자는 `.yml`입니다.

**파일 생성 방법:**

```bash
# 프로젝트 루트 디렉토리로 이동
cd chattingRabbitRoom

# docker-compose.yml 파일 생성
# Windows에서:
echo. > docker-compose.yml

# 또는 텍스트 에디터에서 새 파일로 저장할 때:
# 파일명: docker-compose.yml
# 파일 형식: 모든 파일 (*.*)
```

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
# 프로젝트 루트 디렉토리에서 실행 (중요!)
cd chattingRabbitRoom
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

**💡 주요 빌드 오류 및 해결방법**

**1. Gradle Wrapper 파일 누락 오류**

```
ERROR: failed to solve: failed to calculate checksum of ref: "/gradle": not found
```

**해결방법:**

```bash
# backend 폴더에 필수 파일 생성
cd backend

# settings.gradle 생성
echo "rootProject.name = 'chattingrabbit-backend'" > settings.gradle

# gradle.properties 생성 (네트워크 설정 포함)
echo "org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.jvmargs=-Xmx2g -Xms512m
systemProp.org.gradle.internal.http.connectionTimeout=180000
systemProp.org.gradle.internal.http.socketTimeout=180000" > gradle.properties
```

**2. Jakarta EE 패키지 오류**

```
error: package javax.annotation does not exist
import javax.annotation.PostConstruct;
```

**해결방법:**
`UserRepository.java` 파일에서 import 문 수정:

```java
// 변경 전 (오류 발생)
import javax.annotation.PostConstruct;

// 변경 후 (정상 동작)
import jakarta.annotation.PostConstruct;
```

**3. RabbitMQ STOMP 연결 오류 (61613 포트)**

```
ERROR: Failed to start bean 'stompBrokerRelayMessageHandler'
TCP connection failure: Connection refused: localhost/127.0.0.1:61613
```

**원인 분석:**

- RabbitMQ STOMP 플러그인 비활성화
- Spring Boot 환경 변수 설정 문제
- STOMP 브로커 릴레이 연결 실패

**해결방법:**

```bash
# 1. RabbitMQ STOMP 플러그인 활성화
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stomp

# 2. RabbitMQ 재시작
docker restart rabbitmq

# 3. STOMP 포트 연결 확인
Test-NetConnection -ComputerName localhost -Port 61613

# 4. 백엔드 컨테이너에서 호스트 연결 테스트
docker exec chattingrabbit-backend ping -c 3 host.docker.internal
```

**4. Spring Boot 설정 파일 하드코딩 문제**

```
Attempting to connect to: localhost:5672 (하드코딩된 설정)
Attempting to connect to: localhost:61613 (하드코딩된 설정)
```

**원인 분석:**

- `StompConfig.java`에서 `setRelayHost("localhost")` 하드코딩
- `RabbitConfig.java`에서 `factory.setHost("localhost")` 하드코딩
- 잘못된 사용자명/비밀번호 (`guest` → `admin`)

**해결방법:**

```java
// StompConfig.java 수정
registry.enableStompBrokerRelay("/topic", "/queue")
    .setRelayHost("host.docker.internal")  // localhost → host.docker.internal
    .setRelayPort(61613)
    .setClientLogin("admin")               // guest → admin
    .setClientPasscode("admin123")         // guest → admin123
    .setSystemLogin("admin")               // guest → admin
    .setSystemPasscode("admin123");        // guest → admin123

// RabbitConfig.java 수정
factory.setHost("host.docker.internal");   // localhost → host.docker.internal
factory.setUsername("admin");              // guest → admin
factory.setPassword("admin123");           // guest → admin123
```

**수정 후 결과:**

- ✅ 61613 포트 연결 성공
- ✅ RabbitMQ AMQP 연결 성공
- ✅ Spring Boot 애플리케이션 정상 시작

**5. 일반적인 백엔드 디버깅**

```bash
# 애플리케이션 로그 확인
docker logs chattingrabbit-backend

# 환경 변수 확인
docker exec chattingrabbit-backend env | grep RABBITMQ

# 데이터베이스 연결 테스트
curl http://localhost:8080/h2-console

# 빌드 실패 시 상세 로그 확인
docker build -t chattingrabbit-backend:latest . --progress=plain

# Docker Desktop 연결 문제 시
docker version
# Docker Desktop 재시작 필요할 수 있음
```

#### **프론트엔드 문제**

**💡 주요 빌드 오류 및 해결방법**

**1. npm ci 의존성 설치 오류**

```
ERROR [builder 4/6] RUN npm ci --only=production
```

**해결방법:**

```bash
# package-lock.json 생성
cd frontend
npm install

# Dockerfile에서 --only=production 제거
# RUN npm ci --only=production → RUN npm ci
```

**2. @stomp/stompjs 패키지 누락 오류**

```
[vite]: Rollup failed to resolve import "@stomp/stompjs"
```

**해결방법:**

```json
// package.json dependencies에 추가
"@stomp/stompjs": "^7.0.0"
```

**3. 일반적인 프론트엔드 디버깅**

```bash
# Nginx 설정 문법 검사
docker exec chattingrabbit-frontend nginx -t

# Nginx 재시작
docker exec chattingrabbit-frontend nginx -s reload

# 정적 파일 접근 확인
docker exec chattingrabbit-frontend ls -la /usr/share/nginx/html

# 빌드 실패 시 상세 로그 확인
docker build -t chattingrabbit-frontend:latest . --progress=plain
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

## 🚀 **빠른 시작 명령어 (초보자용)**

### **⚠️ 시작하기 전 필수 체크리스트**

**아래 명령어를 순서대로 실행하세요:**

```bash
# 0. 사전 준비사항 확인
cd chattingRabbitRoom/backend

# 1. 필수 파일 생성
echo "rootProject.name = 'chattingrabbit-backend'" > settings.gradle
echo "org.gradle.daemon=false" > gradle.properties

# 2. 파일 생성 확인
dir settings.gradle
dir gradle.properties

# 3. 코드 수정 확인 (UserRepository.java)
# javax.annotation.PostConstruct → jakarta.annotation.PostConstruct
```

### **🚀 배포 실행**

#### **방법 1: Docker Compose 사용 (권장 - 전체 서비스)**

**아래 명령어를 순서대로 실행하세요:**

```bash
# 1. 프로젝트 루트 디렉토리로 이동 (중요!)
cd chattingRabbitRoom

# 2. 전체 서비스 시작
docker-compose up -d

# 3. 서비스 상태 확인
docker-compose ps

# 4. 로그 확인
docker-compose logs -f

# 5. 웹 애플리케이션 접속
# 브라우저에서: http://localhost
```

#### **방법 2: 개별 컨테이너 실행 (단계별 배포)**

**아래 명령어를 순서대로 실행하세요:**

```bash
# 1. RabbitMQ 실행
docker run -d \
  --name rabbitmq \
  --hostname rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -p 61613:61613 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3.12-management

# 2. STOMP 플러그인 활성화
docker exec rabbitmq rabbitmq-plugins enable rabbitmq_stomp

# 3. 백엔드 실행
docker run -d \
  --name chattingrabbit-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  chattingrabbit-backend:latest

# 4. 프론트엔드 실행
docker run -d \
  --name chattingrabbit-frontend \
  -p 80:80 \
  chattingrabbit-frontend:latest
```

#### **🔍 각 단계별 확인 방법**

```bash
# RabbitMQ 상태 확인
docker ps | findstr rabbitmq

# 백엔드 상태 확인
docker ps | findstr backend

# 프론트엔드 상태 확인
docker ps | findstr frontend

# 전체 컨테이너 상태 확인
docker ps
```

#### **🔧 개별 백엔드 빌드 (필요시)**

**백엔드만 별도로 빌드하고 싶을 때:**

```bash
# 1. 백엔드 폴더로 이동
cd backend

# 2. Docker 이미지 빌드
docker build -t chattingrabbit-backend:latest .

# 3. 빌드 성공 확인
docker images | findstr chattingrabbit

# 4. 빌드된 이미지 확인
docker images chattingrabbit-backend
```

#### **⚠️ 빌드 시 주의사항**

- **시간**: 첫 빌드는 3-5분 정도 소요됩니다
- **인터넷**: 안정적인 인터넷 연결이 필요합니다
- **메모리**: Docker Desktop에 충분한 메모리 할당이 필요합니다

---

## 📝 업데이트 이력

### **최근 해결된 주요 문제들 (2025-01-22)**

**🐛 해결된 빌드 오류:**

1. **Gradle Wrapper 파일 누락**

   - 문제: `"/gradle": not found` 오류
   - 해결: `settings.gradle`, `gradle.properties` 파일 생성

2. **Jakarta EE 패키지 오류**

   - 문제: `javax.annotation.PostConstruct` 패키지 없음
   - 해결: `jakarta.annotation.PostConstruct`로 변경

3. **Docker 빌드 최적화**

   - 기존: 복잡한 멀티스테이지 빌드 with Gradle Wrapper
   - 개선: Alpine Linux + 직접 Gradle 이미지 사용

4. **RabbitMQ STOMP 연결 오류 (61613 포트)**

   - 문제: `Failed to start bean 'stompBrokerRelayMessageHandler'`
   - 원인: STOMP 플러그인 비활성화 + Spring Boot 설정 파일 하드코딩 문제
   - 해결: 플러그인 활성화 + `StompConfig.java`, `RabbitConfig.java` 수정

5. **프론트엔드 빌드 오류 (npm ci & @stomp/stompjs)**
   - 문제: `ERROR [builder 4/6] RUN npm ci --only=production` + `@stomp/stompjs` 패키지 누락
   - 원인: package-lock.json 누락 + 의존성 누락 + Dockerfile 최적화 부족
   - 해결: `npm install` + `@stomp/stompjs` 추가 + Dockerfile 최적화 + .dockerignore 생성

**✅ 검증된 성공 사례:**

- **백엔드 빌드 시간**: 약 4분 45초 (285.3초)
- **백엔드 이미지 크기**: 615MB
- **프론트엔드 빌드 시간**: 약 3분 13초 (193.1초)
- **전체 빌드 성공률**: 100%
- **STOMP 연결**: 61613 포트 정상 연결
- **RabbitMQ AMQP 연결**: 5672 포트 정상 연결
- **Spring Boot 애플리케이션**: 정상 시작 및 동작
- **Vue.js 프론트엔드**: 정상 빌드 및 Nginx 서빙

**🎯 권장 사항:**

- 항상 사전 준비사항 체크리스트 확인
- 코드 수정 후 개별 빌드로 검증
- Docker Desktop 재시작 시 연결 상태 확인
- RabbitMQ STOMP 플러그인 활성화 상태 확인
- Spring Boot 설정 파일에서 하드코딩된 `localhost` 확인 및 수정
- `StompConfig.java`와 `RabbitConfig.java`의 연결 설정 검증
- **프론트엔드 빌드 전**: `npm install`로 package-lock.json 생성 확인
- **의존성 관리**: package.json에 필요한 패키지가 모두 포함되어 있는지 확인
- **Docker 최적화**: .dockerignore와 멀티스테이지 빌드 활용으로 빌드 성능 향상

---

## 🎉 **최근 성공한 주요 수정사항 (2025-01-22)**

### **✅ 완벽하게 해결된 문제들**

1. **Spring Boot 설정 파일 하드코딩 문제**

   - **문제**: `StompConfig.java`, `RabbitConfig.java`에서 `localhost` 하드코딩
   - **해결**: `host.docker.internal`로 변경하여 Docker 컨테이너 간 통신 가능
   - **결과**: 61613 포트 STOMP 연결 성공, 5672 포트 AMQP 연결 성공

2. **RabbitMQ 인증 문제**

   - **문제**: 잘못된 사용자명/비밀번호 (`guest` → `admin`)
   - **해결**: 올바른 인증 정보로 수정
   - **결과**: RabbitMQ 연결 및 인증 성공

3. **전체 시스템 정상 동작**

   - **백엔드**: Spring Boot 애플리케이션 정상 시작 및 동작
   - **RabbitMQ**: STOMP 플러그인 활성화, 모든 포트 정상 연결
   - **통신**: 컨테이너 간 네트워크 통신 정상 작동

4. **프론트엔드 빌드 최적화**
   - **문제**: npm ci 의존성 설치 오류 및 @stomp/stompjs 패키지 누락
   - **해결**: package-lock.json 생성, 의존성 추가, Dockerfile 최적화
   - **결과**: Vue.js 애플리케이션 정상 빌드, Nginx 서빙 성공
