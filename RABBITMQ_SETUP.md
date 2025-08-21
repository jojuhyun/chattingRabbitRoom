# 🐰 RabbitMQ 설치 및 설정 가이드

ChattingRabbit 프로젝트에서 RabbitMQ를 메시지 브로커로 사용하기 위한 설치 및 설정 가이드입니다.

## 📋 사전 요구사항

- **Windows 10/11** 또는 **Windows Server 2016+**
- **Erlang 24.0+** (RabbitMQ 실행을 위해 필요)
- **관리자 권한** (서비스 설치 및 설정을 위해)

## 🚀 1단계: Erlang 설치

### **Chocolatey 사용 (권장)**

```cmd
# Chocolatey가 설치되어 있지 않다면 먼저 설치
# https://chocolatey.org/install

# Erlang 설치
choco install erlang
```

### **수동 설치**

1. [Erlang 공식 사이트](https://www.erlang.org/downloads)에서 Windows 버전 다운로드
2. 설치 파일 실행 및 기본 설정으로 설치
3. 시스템 환경변수에 Erlang 경로 추가

### **설치 확인**

```cmd
erl -version
```

## 🐰 2단계: RabbitMQ 설치

### **Chocolatey 사용 (권장)**

```cmd
# RabbitMQ 설치
choco install rabbitmq

# 또는 특정 버전 설치
choco install rabbitmq --version=3.12.0
```

### **수동 설치**

1. [RabbitMQ 공식 사이트](https://www.rabbitmq.com/install-windows.html)에서 Windows 버전 다운로드
2. 설치 파일 실행 및 기본 설정으로 설치
3. 설치 완료 후 RabbitMQ 서비스 자동 시작

### **설치 확인**

```cmd
# 서비스 상태 확인
sc query RabbitMQ

# 또는
rabbitmqctl status
```

## ⚙️ 3단계: RabbitMQ 플러그인 활성화

### **관리 플러그인 활성화**

```cmd
# RabbitMQ 관리 플러그인 활성화
rabbitmq-plugins enable rabbitmq_management

# STOMP 플러그인 활성화 (WebSocket 통신을 위해 필수)
rabbitmq-plugins enable rabbitmq_stomp

# AMQP 1.0 플러그인 활성화 (선택사항)
rabbitmq-plugins enable rabbitmq_amqp1_0
```

### **플러그인 상태 확인**

```cmd
rabbitmq-plugins list
```

## 🔄 4단계: RabbitMQ 서비스 재시작

### **서비스 재시작**

```cmd
# 서비스 중지
net stop RabbitMQ

# 서비스 시작
net start RabbitMQ

# 또는 Windows 서비스 관리자에서 수동으로 재시작
```

### **재시작 후 확인**

```cmd
# 서비스 상태 확인
sc query RabbitMQ

# 플러그인 상태 확인
rabbitmq-plugins list
```

## 🌐 5단계: RabbitMQ 관리 콘솔 접속

### **웹 관리 콘솔**

- **URL**: http://localhost:15672
- **기본 사용자명**: `guest`
- **기본 비밀번호**: `guest`

### **관리 콘솔에서 확인할 항목**

1. **Overview**: 전체 시스템 상태 및 통계
2. **Connections**: 현재 연결된 클라이언트
3. **Channels**: 활성 채널
4. **Exchanges**: 메시지 교환소
5. **Queues**: 메시지 큐
6. **Admin**: 사용자 및 권한 관리

## 🔐 6단계: 보안 설정 (선택사항)

### **새 사용자 생성**

```cmd
# 새 사용자 생성
rabbitmqctl add_user admin your_password

# 관리자 권한 부여
rabbitmqctl set_user_tags admin administrator

# 모든 권한 부여
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

### **guest 계정 비활성화 (프로덕션 환경 권장)**

```cmd
# guest 계정 삭제
rabbitmqctl delete_user guest
```

## 📊 7단계: ChattingRabbit 전용 설정

### **가상 호스트 생성**

```cmd
# 채팅 전용 가상 호스트 생성
rabbitmqctl add_vhost /chatting

# 사용자에게 가상 호스트 권한 부여
rabbitmqctl set_permissions -p /chatting guest ".*" ".*" ".*"
```

### **큐 및 익스체인지 자동 생성 확인**

ChattingRabbit 애플리케이션 시작 시 자동으로 다음 항목들이 생성됩니다:

- **Exchange**: `chat.exchange` (Topic Exchange)
- **Queue**: `chat.queue`
- **Binding**: `chat.room.*` → `chat.queue`

## 🧪 8단계: 연결 테스트

### **애플리케이션 시작**

```cmd
cd backend
gradlew.bat bootRun
```

### **헬스체크 API 호출**

```bash
# RabbitMQ 상태 확인
curl http://localhost:8080/api/health/rabbitmq

# 전체 시스템 상태 확인
curl http://localhost:8080/api/health
```

### **예상 응답**

```json
{
  "success": true,
  "status": "UP",
  "message": "RabbitMQ 연결 정상",
  "timestamp": 1703123456789
}
```

## 🚨 문제 해결

### **일반적인 문제들**

#### **1. RabbitMQ 서비스 시작 실패**

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

#### **3. Erlang 경로 문제**

```cmd
# 환경변수 확인
echo %ERLANG_HOME%

# Erlang 경로가 없다면 추가
setx ERLANG_HOME "C:\Program Files\Erlang OTP\24.0"
setx PATH "%PATH%;%ERLANG_HOME%\bin"
```

#### **4. 권한 문제**

```cmd
# 관리자 권한으로 명령 프롬프트 실행
# 또는 서비스 계정 권한 확인
```

### **로그 확인 방법**

```cmd
# RabbitMQ 로그 실시간 확인
tail -f "C:\Users\[사용자명]\AppData\Roaming\RabbitMQ\log\rabbit@[호스트명].log"

# 또는 Windows 이벤트 뷰어에서 확인
```

## 📚 추가 리소스

### **공식 문서**

- [RabbitMQ Windows 설치 가이드](https://www.rabbitmq.com/install-windows.html)
- [RabbitMQ STOMP 플러그인](https://www.rabbitmq.com/stomp.html)
- [RabbitMQ 관리 가이드](https://www.rabbitmq.com/management.html)

### **유용한 명령어**

```cmd
# RabbitMQ 상태 확인
rabbitmqctl status

# 플러그인 목록
rabbitmq-plugins list

# 사용자 목록
rabbitmqctl list_users

# 가상 호스트 목록
rabbitmqctl list_vhosts

# 큐 목록
rabbitmqctl list_queues

# 익스체인지 목록
rabbitmqctl list_exchanges
```

## ✅ 설치 완료 체크리스트

- [ ] Erlang 설치 및 환경변수 설정
- [ ] RabbitMQ 설치 및 서비스 시작
- [ ] 관리 플러그인 활성화
- [ ] STOMP 플러그인 활성화
- [ ] 관리 콘솔 접속 확인 (http://localhost:15672)
- [ ] ChattingRabbit 애플리케이션 시작
- [ ] 헬스체크 API 응답 확인
- [ ] 채팅 메시지 전송 테스트

---

**RabbitMQ 설치가 완료되면 ChattingRabbit 프로젝트가 정상적으로 동작합니다!** 🐰✨
