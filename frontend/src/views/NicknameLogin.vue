<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-header">
        <h1 class="login-title">🔑 닉네임 로그인</h1>
        <p class="login-subtitle">기존 닉네임과 비밀번호로 로그인하세요</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="nickname">
          <el-input
            v-model="loginForm.nickname"
            placeholder="닉네임을 입력하세요"
            size="large"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="비밀번호를 입력하세요"
            size="large"
            prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            로그인
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="login-link">
          계정이 없으신가요? 
          <router-link to="/nickname-register" class="link-text">
            닉네임 생성하기
          </router-link>
        </p>
        <p class="login-link">
          <router-link to="/" class="link-text">
            ← 첫 화면으로 돌아가기
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  nickname: '',
  password: ''
})

const loginRules = {
  nickname: [
    { required: true, message: '닉네임을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 20, message: '닉네임은 2~20자 사이여야 합니다', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 4, max: 20, message: '비밀번호는 4~20자 사이여야 합니다', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await userStore.loginNickname(loginForm.nickname, loginForm.password)
    
    if (result.success) {
      ElMessage.success('로그인이 성공했습니다!')
      router.push('/rooms')
    } else {
      ElMessage.error(result.message || '로그인에 실패했습니다.')
    }
  } catch (error) {
    console.error('로그인 오류:', error)
    ElMessage.error('로그인 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-content {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 450px;
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 10px;
  font-weight: 700;
}

.login-subtitle {
  color: #666;
  font-size: 1rem;
}

.login-form {
  margin-bottom: 30px;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
}

.login-footer {
  text-align: center;
}

.login-link {
  margin: 10px 0;
  color: #666;
}

.link-text {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.link-text:hover {
  color: #764ba2;
  text-decoration: underline;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  height: 50px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .login-content {
    padding: 30px 20px;
  }
  
  .login-title {
    font-size: 1.8rem;
  }
}
</style>
