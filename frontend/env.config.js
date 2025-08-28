// Frontend 환경변수 설정
const config = {
  // 개발 환경
  development: {
    API_BASE_URL: 'http://localhost:8080',
    WS_BASE_URL: 'http://localhost:8080',
    FRONTEND_PORT: 3000
  },
  
  // Docker 환경
  docker: {
    API_BASE_URL: 'http://localhost',
    WS_BASE_URL: 'http://localhost',
    FRONTEND_PORT: 80
  },
  
  // 프로덕션 환경
  production: {
    API_BASE_URL: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
    WS_BASE_URL: process.env.VITE_WS_BASE_URL || 'http://localhost:8080',
    FRONTEND_PORT: process.env.VITE_FRONTEND_PORT || 80
  }
};

// 환경 감지 및 설정 반환
const getCurrentConfig = () => {
  // Docker 환경 감지
  const isDockerEnvironment = () => {
    // 1. 메타 태그로 Docker 환경 확인 (우선순위 높음)
    if (typeof document !== 'undefined' && document.querySelector('meta[name="docker-env"]')?.content === 'true') {
      return true;
    }
    
    // 2. 포트 80에서 실행 중인지 확인
    if (typeof window !== 'undefined' && window.location.port === '80') {
      return true;
    }
    
    // 3. 기본 HTTP 포트(포트 없음)에서 실행 중인지 확인
    if (typeof window !== 'undefined' && window.location.port === '') {
      return true;
    }
    
    // 4. 환경변수로 Docker 환경 확인
    if (process.env.VITE_DOCKER_ENV === 'true') {
      return true;
    }
    
    // 5. URL 패턴으로 Docker 환경 확인
    if (typeof window !== 'undefined' && window.location.hostname === 'localhost' && 
        (window.location.port === '80' || window.location.port === '')) {
      return true;
    }
    
    return false;
  };
  
  // Docker 환경인지 확인
  if (isDockerEnvironment()) {
    console.log('Docker 환경으로 감지됨');
    return config.docker;
  }
  
  // 개발 환경
  console.log('개발 환경으로 감지됨');
  return config.development;
};

// 즉시 실행하여 설정 반환
const currentConfig = getCurrentConfig();
console.log('현재 환경 설정:', currentConfig);

export default currentConfig;
