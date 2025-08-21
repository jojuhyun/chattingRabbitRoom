# 🚀 ChattingRabbit 배포 계획서

ChattingRabbit 프로젝트의 백엔드와 프론트엔드를 서버에 배포하기 위한 상세한 가이드입니다.

## 📋 목차

1. [백엔드 배포 방법](#-백엔드-배포-방법)
2. [프론트엔드 배포 방법](#-프론트엔드-배포-방법)
3. [프로덕션 환경 설정](#-프로덕션-환경-설정)
4. [배포 스크립트](#-배포-스크립트)
5. [배포 시 주의사항](#-배포-시-주의사항)
6. [권장 배포 방식](#-권장-배포-방식)

## 🖥️ 백엔드 배포 방법

### **1. JAR 파일 배포 (권장)**

#### **빌드 및 배포 과정**

```bash
# 백엔드 빌드
cd backend
./gradlew clean build -x test

# 생성된 JAR 파일 확인
ls build/libs/
# chattingrabbit-0.0.1-SNAPSHOT.jar

# 서버에 JAR 파일 업로드 후 실행
java -jar chattingrabbit-0.0.1-SNAPSN.jar
```

#### **프로덕션 환경 설정**

```bash
# 환경별 프로파일 설정
java -jar -Dspring.profiles.active=prod chattingrabbit-0.0.1-SNAPSN.jar

# JVM 옵션 최적화
java -Xms512m -Xmx2g -XX:+UseG1GC \
     -Dspring.profiles.active=prod \
     -jar chattingrabbit-0.0.1-SNAPSN.jar
```

#### **systemd 서비스 등록 (Linux)**

```ini
# /etc/systemd/system/chattingrabbit.service
[Unit]
Description=ChattingRabbit Backend
After=network.target rabbitmq-server.service

[Service]
Type=simple
User=chattingrabbit
WorkingDirectory=/opt/chattingrabbit
ExecStart=/usr/bin/java -Xms512m -Xmx2g -XX:+UseG1GC -Dspring.profiles.active=prod -jar chattingrabbit-0.0.1-SNAPSN.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**서비스 관리 명령어:**

```bash
# 서비스 시작
sudo systemctl start chattingrabbit

# 서비스 상태 확인
sudo systemctl status chattingrabbit

# 서비스 자동 시작 설정
sudo systemctl enable chattingrabbit

# 서비스 재시작
sudo systemctl restart chattingrabbit
```

### **2. Docker 컨테이너 배포**

#### **Dockerfile 생성**

```dockerfile
# backend/Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# JAR 파일 복사
COPY build/libs/chattingrabbit-0.0.1-SNAPSN.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD curl -f http://localhost:8080/api/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Xms512m", "-Xmx2g", "-XX:+UseG1GC", "-jar", "app.jar"]
```

#### **Docker Compose 설정**

```yaml
# docker-compose.yml
version: "3.8"

services:
  rabbitmq:
    image: rabbitmq:3.12-management
    ports:
      - "5672:5672"
      - "15672:15672"
      - "61613:61613"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=secure_password
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_USERNAME=admin
      - SPRING_RABBITMQ_PASSWORD=secure_password
    depends_on:
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

volumes:
  rabbitmq_data:
```

**Docker 명령어:**

```bash
# 컨테이너 빌드 및 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f backend

# 컨테이너 중지
docker-compose down

# 컨테이너 재시작
docker-compose restart backend
```

## 🌐 프론트엔드 배포 방법

### **1. 정적 파일 배포 (권장)**

#### **빌드 과정**

```bash
# 프론트엔드 빌드
cd frontend
npm run build

# 생성된 dist 폴더 확인
ls dist/
# index.html, assets/ 등
```

#### **Nginx 설정**

```nginx
# /etc/nginx/sites-available/chattingrabbit
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/chattingrabbit/frontend/dist;
    index index.html;

    # gzip 압축
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;

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
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 프록시
    location /stomp/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Nginx 설정 적용:**

```bash
# 설정 파일 활성화
sudo ln -s /etc/nginx/sites-available/chattingrabbit /etc/nginx/sites-enabled/

# 설정 문법 검사
sudo nginx -t

# Nginx 재시작
sudo systemctl restart nginx

# 상태 확인
sudo systemctl status nginx
```

### **2. Docker 컨테이너 배포**

#### **Dockerfile 생성**

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine as build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### **Nginx 설정 파일**

```nginx
# frontend/nginx.conf
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
    }
}
```

## 🔧 프로덕션 환경 설정

### **1. 환경별 프로파일 설정**

#### **application-prod.properties**

```properties
# backend/src/main/resources/application-prod.properties

# 서버 설정
server.port=8080

# 데이터베이스 설정 (PostgreSQL 권장)
spring.datasource.url=jdbc:postgresql://localhost:5432/chattingrabbit
spring.datasource.username=chattingrabbit_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# RabbitMQ 설정
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=secure_password
spring.rabbitmq.virtual-host=/

# 로깅 설정
logging.level.com.example.chattingrabbit=INFO
logging.level.org.springframework.web=WARN
logging.level.org.springframework.amqp=WARN

# 보안 설정
spring.h2.console.enabled=false
```

### **2. 환경 변수 설정**

```bash
# /etc/environment 또는 .env 파일
export SPRING_PROFILES_ACTIVE=prod
export SPRING_RABBITMQ_USERNAME=admin
export SPRING_RABBITMQ_PASSWORD=secure_password
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chattingrabbit
export SPRING_DATASOURCE_USERNAME=chattingrabbit_user
export SPRING_DATASOURCE_PASSWORD=secure_password
```

**환경 변수 적용:**

```bash
# 환경 변수 로드
source /etc/environment

# 또는 .env 파일 사용
export $(cat .env | xargs)
```

## 📦 배포 스크립트

### **1. 전체 배포 스크립트**

```bash
#!/bin/bash
# deploy.sh

set -e

echo "🚀 ChattingRabbit 배포 시작..."

# 1. 백엔드 빌드
echo "📦 백엔드 빌드 중..."
cd backend
./gradlew clean build -x test
cd ..

# 2. 프론트엔드 빌드
echo "🌐 프론트엔드 빌드 중..."
cd frontend
npm ci
npm run build
cd ..

# 3. 서버에 파일 업로드
echo "📤 서버에 파일 업로드 중..."
scp backend/build/libs/chattingrabbit-0.0.1-SNAPSN.jar user@server:/opt/chattingrabbit/
scp -r frontend/dist/* user@server:/var/www/chattingrabbit/frontend/

# 4. 서버에서 서비스 재시작
echo "🔄 서비스 재시작 중..."
ssh user@server << 'EOF'
    sudo systemctl restart chattingrabbit
    sudo nginx -s reload
    echo "✅ 배포 완료!"
EOF

echo "🎉 배포가 성공적으로 완료되었습니다!"
```

**스크립트 실행 권한 설정:**

```bash
chmod +x deploy.sh
./deploy.sh
```

### **2. Docker 배포 스크립트**

```bash
#!/bin/bash
# deploy-docker.sh

set -e

echo "🐳 Docker 배포 시작..."

# 1. 이미지 빌드
echo "📦 Docker 이미지 빌드 중..."
docker-compose build

# 2. 기존 컨테이너 중지 및 제거
echo "🛑 기존 컨테이너 중지 중..."
docker-compose down

# 3. 새 컨테이너 시작
echo "🚀 새 컨테이너 시작 중..."
docker-compose up -d

# 4. 헬스체크
echo "🏥 헬스체크 중..."
sleep 30
curl -f http://localhost:8080/api/health || exit 1

echo "🎉 Docker 배포가 성공적으로 완료되었습니다!"
```

**스크립트 실행 권한 설정:**

```bash
chmod +x deploy-docker.sh
./deploy-docker.sh
```

### **3. Windows 배포 스크립트**

```batch
@echo off
REM deploy.bat

echo 🚀 ChattingRabbit 배포 시작...

REM 1. 백엔드 빌드
echo 📦 백엔드 빌드 중...
cd backend
gradlew.bat clean build -x test
cd ..

REM 2. 프론트엔드 빌드
echo 🌐 프론트엔드 빌드 중...
cd frontend
npm ci
npm run build
cd ..

REM 3. 배포 완료 메시지
echo 🎉 빌드가 완료되었습니다!
echo 📤 서버에 파일을 업로드하고 서비스를 재시작하세요.
pause
```

## 🚨 배포 시 주의사항

### **1. 보안**

- ✅ **기본 계정 변경**: RabbitMQ guest 계정 변경
- ✅ **방화벽 설정**: 필요한 포트만 개방
- ✅ **SSL/TLS 적용**: HTTPS 적용 권장
- ✅ **환경 변수 관리**: 민감한 정보는 환경 변수로

**보안 체크리스트:**

```bash
# 방화벽 설정 확인
sudo ufw status

# SSL 인증서 확인
openssl s_client -connect your-domain.com:443

# 포트 스캔 확인
nmap -p 80,443,8080,5672,15672,61613 your-domain.com
```

### **2. 성능**

- ✅ **JVM 튜닝**: 메모리 및 GC 설정 최적화
- ✅ **Nginx 캐싱**: 정적 파일 캐싱 설정
- ✅ **데이터베이스 인덱스**: 성능 최적화
- ✅ **로드 밸런싱**: 트래픽 분산 고려

**성능 모니터링:**

```bash
# JVM 메모리 사용량 확인
jstat -gc <pid>

# Nginx 성능 확인
nginx -V

# 데이터베이스 성능 확인
pg_stat_statements (PostgreSQL)
```

### **3. 모니터링**

- ✅ **로그 관리**: 로그 로테이션 및 백업
- ✅ **메트릭 수집**: Prometheus + Grafana 고려
- ✅ **알림 설정**: 장애 상황 알림
- ✅ **백업 전략**: 데이터 및 설정 백업

**모니터링 설정:**

```bash
# 로그 로테이션 설정
sudo logrotate -f /etc/logrotate.d/chattingrabbit

# 시스템 리소스 모니터링
htop
iotop
nethogs
```

## 🏆 권장 배포 방식

### **소규모 환경 (사용자 100명 이하)**

- **백엔드**: JAR 파일 + systemd 서비스
- **프론트엔드**: Nginx 정적 파일 서빙
- **데이터베이스**: PostgreSQL
- **메시지 브로커**: RabbitMQ
- **웹 서버**: Nginx

**장점:**

- 간단한 설정 및 관리
- 낮은 리소스 사용량
- 빠른 배포 및 복구

**단점:**

- 수평 확장 제한
- 단일 장애점 존재

### **중간 규모 환경 (사용자 100-1000명)**

- **백엔드**: Docker + Docker Compose
- **프론트엔드**: Nginx 정적 파일 서빙
- **데이터베이스**: PostgreSQL (마스터-슬레이브)
- **메시지 브로커**: RabbitMQ 클러스터
- **로드 밸런서**: HAProxy

**장점:**

- 컨테이너 기반 배포
- 부분적 고가용성
- 관리 용이성

**단점:**

- 복잡한 설정
- 리소스 오버헤드

### **대규모 환경 (사용자 1000명 이상)**

- **백엔드**: Docker + Kubernetes
- **프론트엔드**: CDN + 정적 파일 서빙
- **데이터베이스**: PostgreSQL 클러스터
- **메시지 브로커**: RabbitMQ 클러스터
- **로드 밸런서**: HAProxy 또는 Nginx Plus
- **모니터링**: Prometheus + Grafana + AlertManager

**장점:**

- 완전한 고가용성
- 자동 확장 및 복구
- 엔터프라이즈급 안정성

**단점:**

- 높은 복잡성
- 전문 지식 요구
- 높은 리소스 사용량

## 📊 배포 체크리스트

### **배포 전 확인사항**

- [ ] **코드 품질**: 테스트 통과, 코드 리뷰 완료
- [ ] **환경 설정**: 프로덕션 환경 변수 설정
- [ ] **데이터베이스**: 마이그레이션 스크립트 준비
- [ ] **백업**: 기존 데이터 백업
- [ ] **모니터링**: 로그 및 메트릭 수집 준비

### **배포 중 확인사항**

- [ ] **빌드 성공**: 백엔드 JAR, 프론트엔드 dist 생성
- [ ] **파일 업로드**: 서버에 파일 전송 완료
- [ ] **서비스 시작**: 백엔드 애플리케이션 실행
- [ ] **웹 서버**: Nginx 설정 적용 및 재시작
- [ ] **연결 테스트**: API 및 WebSocket 연결 확인

### **배포 후 확인사항**

- [ ] **헬스체크**: `/api/health` 엔드포인트 응답 확인
- [ ] **기능 테스트**: 채팅 기능 정상 동작 확인
- [ ] **성능 모니터링**: 응답 시간 및 리소스 사용량 확인
- [ ] **로그 확인**: 오류 로그 및 접근 로그 확인
- [ ] **백업**: 배포 완료 후 데이터 백업

## 🔄 롤백 전략

### **1. JAR 파일 배포 시 롤백**

```bash
# 이전 버전 JAR 파일로 복구
sudo systemctl stop chattingrabbit
cd /opt/chattingrabbit
mv chattingrabbit-0.0.1-SNAPSN.jar chattingrabbit-0.0.1-SNAPSN.jar.new
mv chattingrabbit-0.0.1-SNAPSN.jar.backup chattingrabbit-0.0.1-SNAPSN.jar
sudo systemctl start chattingrabbit
```

### **2. Docker 배포 시 롤백**

```bash
# 이전 이미지로 롤백
docker-compose down
docker tag chattingrabbit_backend:previous chattingrabbit_backend:latest
docker-compose up -d
```

### **3. 프론트엔드 롤백**

```bash
# 이전 버전으로 복구
cd /var/www/chattingrabbit/frontend
rm -rf dist
cp -r dist.backup dist
sudo nginx -s reload
```

## 📚 추가 리소스

### **공식 문서**

- [Spring Boot 배포 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [Nginx 설정 가이드](https://nginx.org/en/docs/)
- [Docker 공식 문서](https://docs.docker.com/)
- [RabbitMQ 운영 가이드](https://www.rabbitmq.com/operations.html)

### **유용한 도구**

- **모니터링**: Prometheus, Grafana, ELK Stack
- **로드 밸런싱**: HAProxy, Nginx Plus, AWS ALB
- **CI/CD**: Jenkins, GitLab CI, GitHub Actions
- **인프라**: Terraform, Ansible, Docker Swarm

---

**이 배포 계획서를 따라 ChattingRabbit을 안전하고 효율적으로 배포하세요!** 🚀✨
