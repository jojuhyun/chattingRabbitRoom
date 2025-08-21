<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>회원가입</h2>
      </template>
      
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="80px">
        <el-form-item label="아이디" prop="userId">
          <el-input 
            v-model="registerForm.userId" 
            placeholder="아이디를 입력하세요"
            @blur="checkUserId"
          />
          <div v-if="userIdStatus" class="status-message" :class="userIdStatus.type">
            {{ userIdStatus.message }}
          </div>
        </el-form-item>
        
        <el-form-item label="닉네임" prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            placeholder="닉네임을 입력하세요"
            @blur="checkNickname"
          />
          <div v-if="nicknameStatus" class="status-message" :class="nicknameStatus.type">
            {{ nicknameStatus.message }}
          </div>
        </el-form-item>
        
        <el-form-item label="비밀번호" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="비밀번호를 입력하세요" 
          />
        </el-form-item>
        
        <el-form-item label="비밀번호 확인" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="비밀번호를 다시 입력하세요" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            회원가입
          </el-button>
        </el-form-item>
        
        <el-form-item>
          <el-button @click="goToLogin" style="width: 100%">
            로그인으로 돌아가기
          </el-button>
        </el-form-item>
      </el-form>
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
const registerFormRef = ref()
const loading = ref(false)
const userIdStatus = ref(null)
const nicknameStatus = ref(null)

const registerForm = reactive({
  userId: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('비밀번호가 일치하지 않습니다'))
  } else {
    callback()
  }
}

const rules = {
  userId: [
    { required: true, message: '아이디를 입력해주세요', trigger: 'blur' },
    { min: 3, max: 20, message: '아이디는 3-20자 사이여야 합니다', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '닉네임을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 20, message: '닉네임은 2-20자 사이여야 합니다', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 6, message: '비밀번호는 최소 6자 이상이어야 합니다', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '비밀번호 확인을 입력해주세요', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const checkUserId = async () => {
  if (!registerForm.userId || registerForm.userId.length < 3) return
  
  try {
    const available = await userStore.checkUserIdAvailability(registerForm.userId)
    if (available) {
      userIdStatus.value = { type: 'success', message: '사용 가능한 아이디입니다.' }
    } else {
      userIdStatus.value = { type: 'error', message: '이미 사용 중인 아이디입니다.' }
    }
  } catch (error) {
    userIdStatus.value = { type: 'error', message: '아이디 확인 중 오류가 발생했습니다.' }
  }
}

const checkNickname = async () => {
  if (!registerForm.nickname || registerForm.nickname.length < 2) return
  
  try {
    const available = await userStore.checkNicknameAvailability(registerForm.nickname)
    if (available) {
      nicknameStatus.value = { type: 'success', message: '사용 가능한 닉네임입니다.' }
    } else {
      nicknameStatus.value = { type: 'error', message: '이미 사용 중인 닉네임입니다.' }
    }
  } catch (error) {
    nicknameStatus.value = { type: 'error', message: '닉네임 확인 중 오류가 발생했습니다.' }
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    
    if (userIdStatus.value?.type === 'error' || nicknameStatus.value?.type === 'error') {
      ElMessage.error('사용할 수 없는 아이디 또는 닉네임이 있습니다.')
      return
    }
    
    loading.value = true
    
    const result = await userStore.register(
      registerForm.userId, 
      registerForm.nickname, 
      registerForm.password
    )
    
    if (result.success) {
      ElMessage.success('회원가입 성공!')
      router.push('/rooms')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('회원가입 중 오류가 발생했습니다.')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
}

.register-card {
  width: 400px;
}

.status-message {
  margin-top: 5px;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-message.success {
  background-color: #f0f9ff;
  color: #409eff;
  border: 1px solid #409eff;
}

.status-message.error {
  background-color: #fef0f0;
  color: #f56c6c;
  border: 1px solid #f56c6c;
}
</style>
