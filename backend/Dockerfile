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