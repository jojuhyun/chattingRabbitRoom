@echo off
echo ChattingRabbit 프로젝트 시작 중...
echo.
echo 1. 백엔드 시작...
start "Backend" cmd /k "cd backend && gradlew.bat bootRun"
echo.
echo 2. 프론트엔드 시작...
start "Frontend" cmd /k "cd frontend && npm run dev"
echo.
echo 모든 서비스가 시작되었습니다.
echo 백엔드: http://localhost:8080
echo 프론트엔드: http://localhost:3000
echo.
pause
