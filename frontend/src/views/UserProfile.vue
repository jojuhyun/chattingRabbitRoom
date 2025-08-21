<template>
  <div class="user-profile">
    <div class="profile-header">
      <h1>사용자 프로필</h1>
      <el-button @click="goBack" type="info" plain>
        <el-icon><ArrowLeft /></el-icon>
        뒤로 가기
      </el-button>
    </div>

    <div class="profile-content" v-loading="loading">
      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <span>프로필 정보</span>
          </div>
        </template>
        
        <el-form 
          ref="profileFormRef" 
          :model="profileForm" 
          :rules="profileRules" 
          label-width="120px"
          class="profile-form"
        >
          <el-form-item label="닉네임" prop="nickname">
            <el-input 
              v-model="profileForm.nickname" 
              disabled 
              placeholder="닉네임"
            />
          </el-form-item>
          
          <el-form-item label="자기소개" prop="introduction">
            <el-input 
              v-model="profileForm.introduction" 
              type="textarea" 
              :rows="4"
              placeholder="자기소개를 입력해주세요"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="채팅방 초대 허용" prop="allowInvite">
            <el-switch
              v-model="profileForm.allowInvite"
              active-text="허용"
              inactive-text="거부"
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
            <div class="form-help-text">
              다른 사용자가 나를 채팅방에 초대할 수 있도록 허용합니다.
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="updateProfile" 
              :loading="updating"
            >
              프로필 수정
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="danger-zone-card">
        <template #header>
          <div class="card-header danger">
            <span>위험 구역</span>
          </div>
        </template>
        
        <div class="danger-zone-content">
          <p class="warning-text">
            <el-icon><Warning /></el-icon>
            닉네임을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.
          </p>
          
          <el-button 
            type="danger" 
            @click="deleteNickname" 
            :loading="deleting"
          >
            닉네임 삭제
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const updating = ref(false)
const deleting = ref(false)
const profileFormRef = ref()

const profileForm = reactive({
  nickname: '',
  introduction: '',
  allowInvite: true
})

const profileRules = {
  introduction: [
    { max: 500, message: '자기소개는 500자 이하여야 합니다', trigger: 'blur' }
  ]
}

onMounted(async () => {
  // 세션 체크
  if (!userStore.checkSession()) {
    router.push('/nickname-register')
    return
  }
  
  await loadUserProfile()
})

const loadUserProfile = async () => {
  loading.value = true
  try {
    const result = await userStore.getUserProfile(userStore.currentUser.userSession)
    if (result.success) {
      profileForm.nickname = result.profile.nickname
      profileForm.introduction = result.profile.introduction || ''
      profileForm.allowInvite = result.profile.allowInvite !== undefined ? result.profile.allowInvite : true
    } else {
      ElMessage.error(result.message)
      router.push('/nickname-register')
    }
  } catch (error) {
    ElMessage.error('프로필 정보를 불러올 수 없습니다.')
    router.push('/nickname-register')
  } finally {
    loading.value = false
  }
}

const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    updating.value = true
    
    // 자기소개 업데이트
    const profileResult = await userStore.updateUserProfile(
      userStore.currentUser.userSession,
      profileForm.introduction
    )
    
    // 초대 허용 설정 업데이트
    const inviteResult = await userStore.updateAllowInvite(
      userStore.currentUser.userSession,
      profileForm.allowInvite
    )
    
    if (profileResult.success && inviteResult.success) {
      ElMessage.success('프로필이 수정되었습니다.')
      // 사용자 스토어 업데이트
      userStore.currentUser.introduction = profileForm.introduction
      userStore.currentUser.allowInvite = profileForm.allowInvite
    } else {
      ElMessage.error('프로필 수정에 실패했습니다.')
    }
  } catch (error) {
    ElMessage.error('프로필 수정에 실패했습니다.')
  } finally {
    updating.value = false
  }
}

const deleteNickname = async () => {
  try {
    await ElMessageBox.confirm(
      '정말로 닉네임을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없으며, 모든 데이터가 영구적으로 삭제됩니다.',
      '닉네임 삭제',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
        dangerouslyUseHTMLString: true,
      }
    )
    
    deleting.value = true
    
    const result = await userStore.deleteNickname(userStore.currentUser.userSession)
    if (result.success) {
      ElMessage.success('닉네임이 삭제되었습니다.')
      userStore.logout()
      router.push('/nickname-register')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('닉네임 삭제에 실패했습니다.')
    }
  } finally {
    deleting.value = false
  }
}

const goBack = () => {
  router.push('/rooms')
}
</script>

<style scoped>
.user-profile {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.profile-header h1 {
  margin: 0;
  color: #303133;
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card,
.danger-zone-card {
  border-radius: 8px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.card-header.danger {
  color: #f56c6c;
}

.profile-form {
  margin-top: 20px;
}

.danger-zone-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.warning-text {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f56c6c;
  font-weight: 500;
  margin: 0;
}

.warning-text .el-icon {
  font-size: 18px;
}

.form-help-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  line-height: 1.4;
}

.el-switch {
  margin-right: 10px;
}

@media (max-width: 768px) {
  .user-profile {
    padding: 15px;
  }
  
  .profile-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .profile-header h1 {
    text-align: center;
  }
}
</style>
