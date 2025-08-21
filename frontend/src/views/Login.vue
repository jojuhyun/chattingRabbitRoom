<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>로그인</h2>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="아이디" prop="userId">
          <el-input v-model="loginForm.userId" placeholder="아이디를 입력하세요" />
        </el-form-item>
        
        <el-form-item label="비밀번호" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="비밀번호를 입력하세요" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            로그인
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button @click="goToRegister" style="width: 100%">
            회원가입
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="superadmin-info">
        <h4>SuperAdmin 정보</h4>
        <p><strong>아이디:</strong> superadmin</p>
        <p><strong>비밀번호:</strong> whwngusdlqslek.</p>
        <p><em>SuperAdmin만 모든 채팅방에 방송 메시지를 보낼 수 있습니다.</em></p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  userId: '',
  password: ''
})

const rules = {
  userId: [
    { required: true, message: '아이디를 입력해주세요', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const result = await userStore.login(loginForm.userId, loginForm.password)
    
    if (result.success) {
      ElMessage.success('로그인 성공!')
      router.push('/rooms')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('로그인 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
}

.login-card {
  width: 400px;
}

.superadmin-info {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9ff;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.superadmin-info h4 {
  margin: 0 0 10px 0;
  color: #409eff;
}

.superadmin-info p {
  margin: 5px 0;
  font-size: 14px;
}

.superadmin-info em {
  color: #666;
  font-size: 12px;
}
</style>
