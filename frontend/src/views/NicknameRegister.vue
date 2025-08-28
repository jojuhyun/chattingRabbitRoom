<template>
  <div class="nickname-register-container">
    <div class="register-card">
      <div class="header">
        <h1>🎯 닉네임 등록</h1>
        <p>채팅방에 참여하기 위해 닉네임을 등록해주세요</p>
      </div>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="0"
        class="register-form"
      >
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="닉네임을 입력하세요 (2-20자)"
            size="large"
            maxlength="20"
            show-word-limit
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="비밀번호를 입력하세요 (4-20자)"
            size="large"
            maxlength="20"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="introduction">
          <el-input
            v-model="form.introduction"
            type="textarea"
            :rows="3"
            placeholder="자신을 소개해주세요 (선택사항)"
            maxlength="200"
            show-word-limit
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            @click="registerNickname"
            :loading="loading"
            class="register-button"
          >
            닉네임 등록
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="info-section">
        <h3>📋 안내사항</h3>
        <ul>
          <li>닉네임은 모든 채팅방에서 공통으로 사용됩니다</li>
          <li>중복된 닉네임은 사용할 수 없습니다</li>
          <li>30분 동안 활동이 없으면 자동으로 세션이 만료됩니다</li>
          <li>세션은 10분마다 자동으로 갱신됩니다</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  nickname: '',
  password: '',
  introduction: ''
})

const rules = {
  nickname: [
    { required: true, message: '닉네임을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 20, message: '닉네임은 2-20자 사이여야 합니다', trigger: 'blur' },
    { pattern: /^[가-힣a-zA-Z0-9\s]+$/, message: '닉네임은 한글, 영문, 숫자, 공백만 사용 가능합니다', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '비밀번호를 입력해주세요', trigger: 'blur' },
    { min: 4, max: 20, message: '비밀번호는 4-20자 사이여야 합니다', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 이미 로그인된 경우 채팅방 목록으로 이동
  if (userStore.isLoggedIn) {
    router.push('/rooms')
  }
})

const registerNickname = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const result = await userStore.registerNickname(form.nickname, form.introduction, form.password)
    if (result.success) {
      ElMessage.success(result.message)
      router.push('/rooms')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('닉네임 등록에 실패했습니다.')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.nickname-register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  max-width: 500px;
  width: 100%;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 2.5em;
}

.header p {
  margin: 0;
  color: #666;
  font-size: 1.1em;
}

.register-form {
  margin-bottom: 30px;
}

.register-button {
  width: 100%;
  height: 50px;
  font-size: 1.1em;
  font-weight: bold;
}

.info-section {
  background: #f8f9fa;
  border-radius: 15px;
  padding: 20px;
}

.info-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.2em;
}

.info-section ul {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.6;
}

.info-section li {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .register-card {
    padding: 30px 20px;
  }
  
  .header h1 {
    font-size: 2em;
  }
}
</style>
