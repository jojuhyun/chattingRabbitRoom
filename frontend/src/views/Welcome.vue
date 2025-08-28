<template>
  <div class="welcome-container">
    <div class="welcome-content">
      <h1 class="welcome-title">🐰 채팅토끼에 오신 것을 환영합니다!</h1>
      <p class="welcome-subtitle">새로운 채팅 경험을 시작해보세요</p>
      
      <div class="welcome-buttons">
        <router-link to="/nickname-register" class="welcome-btn primary">
          <span class="btn-icon">✨</span>
          <span class="btn-text">신규 닉네임 생성</span>
        </router-link>
        
        <router-link to="/nickname-login" class="welcome-btn secondary">
          <span class="btn-icon">🔑</span>
          <span class="btn-text">기존 닉네임 로그인</span>
        </router-link>
      </div>
      
      <div class="welcome-info">
        <h3>💡 서비스 안내</h3>
        <ul>
          <li>신규 사용자는 닉네임과 비밀번호를 설정하여 계정을 생성할 수 있습니다</li>
          <li>기존 사용자는 닉네임과 비밀번호로 로그인할 수 있습니다</li>
          <li>오픈 채팅방과 개인 채팅방을 자유롭게 이용할 수 있습니다</li>
          <li>다른 사용자와 실시간 채팅을 즐길 수 있습니다</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
// 컴포넌트 로직은 필요 없음
</script>

<style scoped>
.welcome-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.welcome-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
}

.welcome-title {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
}

.welcome-subtitle {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 40px;
}

.welcome-buttons {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 40px;
}

.welcome-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  padding: 20px 30px;
  border-radius: 15px;
  text-decoration: none;
  font-size: 1.1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.welcome-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.welcome-btn.secondary {
  background: white;
  color: #667eea;
  border-color: #667eea;
}

.welcome-btn.secondary:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.btn-icon {
  font-size: 1.5rem;
}

.btn-text {
  font-size: 1.1rem;
}

.welcome-info {
  text-align: left;
  background: #f8f9fa;
  padding: 25px;
  border-radius: 15px;
  border-left: 4px solid #667eea;
}

.welcome-info h3 {
  color: #333;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.welcome-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.welcome-info li {
  color: #666;
  margin-bottom: 10px;
  padding-left: 20px;
  position: relative;
  line-height: 1.6;
}

.welcome-info li:before {
  content: "•";
  color: #667eea;
  font-weight: bold;
  position: absolute;
  left: 0;
}

@media (max-width: 768px) {
  .welcome-content {
    padding: 30px 20px;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .welcome-buttons {
    gap: 15px;
  }
  
  .welcome-btn {
    padding: 18px 25px;
    font-size: 1rem;
  }
}
</style>
