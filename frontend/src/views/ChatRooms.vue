<template>
  <div class="chat-rooms-container">
    <div class="header-section">
      <h2>채팅방 목록</h2>
      <div class="user-info">
        <span><strong>사용자:</strong> {{ userStore.currentUser?.nickname }}</span>
        <span v-if="isSuperAdmin" class="superadmin-badge">SuperAdmin</span>
        <el-button 
          v-if="isSuperAdmin"
          type="warning" 
          size="small"
          @click="goToAdmin"
        >
          관리자 페이지
        </el-button>
        <el-button 
          type="info" 
          size="small"
          @click="goToUserProfile"
        >
          사용자 관리
        </el-button>
      </div>
    </div>
    
    <el-card class="rooms-card">
      <template #header>
        <div class="card-header">
          <span>채팅방 목록</span>
          <div class="header-actions">
            <el-button type="primary" @click="showCreateRoomDialog">
              새 채팅방 만들기
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="!roomList.participatedRooms?.length && !roomList.otherRooms?.length" class="empty-state">
        <el-empty description="채팅방이 없습니다. 첫 번째 채팅방을 만들어보세요!" />
      </div>
      
      <div v-else class="rooms-sections">
        <!-- 참여한 채팅방 -->
        <div v-if="roomList.participatedRooms?.length" class="room-section">
          <h3>🏠 참여한 채팅방</h3>
          <div class="rooms-list">
            <el-card 
              v-for="room in roomList.participatedRooms" 
              :key="room.roomId" 
              class="room-item"
              :class="{ 'empty-room': room.participantCount === 0 }"
            >
              <div class="room-info">
                <div class="room-header">
                  <h4>{{ room.name }}</h4>
                  <span class="room-type" :class="getRoomTypeClass(room.roomType)">
                    {{ getRoomTypeText(room.roomType) }}
                  </span>
                </div>
                <p>방 ID: {{ room.roomId }}</p>
                <p>생성자: {{ room.creatorNickname }}</p>
                <p>생성일: {{ formatDate(room.regDate) }}</p>
                <p class="participant-count">
                  참여자: {{ room.participantCount || 0 }}명
                </p>
              </div>
              <div class="room-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="joinRoom(room.roomId)"
                >
                  입장하기
                </el-button>
                <el-button 
                  type="warning" 
                  size="small"
                  @click="leaveRoom(room.roomId)"
                >
                  채팅방 떠나기
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 참여하지 않은 오픈 채팅방 -->
        <div v-if="roomList.otherRooms?.length" class="room-section">
          <h3>🌐 참여하지 않은 오픈 채팅방</h3>
          <div class="rooms-list">
            <el-card 
              v-for="room in roomList.otherRooms" 
              :key="room.roomId" 
              class="room-item"
            >
              <div class="room-info">
                <div class="room-header">
                  <h4>{{ room.name }}</h4>
                  <span class="room-type open">오픈</span>
                </div>
                <p>방 ID: {{ room.roomId }}</p>
                <p>생성자: {{ room.creatorNickname }}</p>
                <p>생성일: {{ formatDate(room.regDate) }}</p>
                <p class="participant-count">
                  참여자: {{ room.participantCount || 0 }}명
                </p>
              </div>
              <div class="room-actions">
                <el-button 
                  type="primary" 
                  size="small"
                  @click="joinRoom(room.roomId)"
                >
                  참여하기
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 새 채팅방 생성 다이얼로그 -->
    <el-dialog v-model="createRoomDialog" title="새 채팅방 만들기" width="500px">
      <el-form :model="newRoomForm" :rules="roomRules" ref="newRoomFormRef" label-width="100px">
        <el-form-item label="방 이름" prop="name">
          <el-input v-model="newRoomForm.name" placeholder="채팅방 이름을 입력하세요" />
        </el-form-item>
        <el-form-item label="채팅방 타입" prop="roomType">
          <el-radio-group v-model="newRoomForm.roomType">
            <el-radio label="OPEN">오픈 채팅방</el-radio>
            <el-radio label="PRIVATE">개인 채팅방</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createRoomDialog = false">취소</el-button>
          <el-button type="primary" @click="createRoom" :loading="creating">
            생성
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const loading = ref(false)
const creating = ref(false)
const createRoomDialog = ref(false)
const newRoomFormRef = ref()

const newRoomForm = reactive({
  name: '',
  roomType: 'OPEN'
})

const roomRules = {
  name: [
    { required: true, message: '채팅방 이름을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 50, message: '채팅방 이름은 2-50자 사이여야 합니다', trigger: 'blur' }
  ],
  roomType: [
    { required: true, message: '채팅방 타입을 선택해주세요', trigger: 'change' }
  ]
}

const isSuperAdmin = computed(() => {
  return userStore.currentUser?.isSuperAdmin || false
})

const roomList = ref({
  participatedRooms: [],
  otherRooms: []
})

onMounted(async () => {
  // 세션 체크
  if (!userStore.checkSession()) {
    router.push('/nickname-register')
    return
  }
  
  await loadChatRooms()
})

const loadChatRooms = async () => {
  loading.value = true
  try {
    const result = await chatStore.fetchChatRoomList(userStore.currentUser.userSession)
    if (result.success) {
      roomList.value = result.roomList
    } else {
      ElMessage.error(result.message)
    }
  } finally {
    loading.value = false
  }
}

const showCreateRoomDialog = () => {
  createRoomDialog.value = true
  newRoomForm.name = ''
  newRoomForm.roomType = 'OPEN'
}

const createRoom = async () => {
  if (!newRoomFormRef.value) return
  
  try {
    await newRoomFormRef.value.validate()
    creating.value = true
    
    const result = await chatStore.createChatRoomWithType(
      newRoomForm.name, 
      newRoomForm.roomType, 
      userStore.currentUser.nickname
    )
    if (result.success) {
      ElMessage.success('채팅방이 생성되었습니다.')
      createRoomDialog.value = false
      await loadChatRooms()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('채팅방 생성에 실패했습니다.')
  } finally {
    creating.value = false
  }
}

const joinRoom = (roomId) => {
  router.push(`/rooms/${roomId}`)
}

const leaveRoom = async (roomId) => {
  try {
    await ElMessageBox.confirm(
      '정말로 이 채팅방을 떠나시겠습니까?\n\n떠나기 전까지의 모든 메시지는 보존되며, 재입장 시 다시 볼 수 있습니다.',
      '채팅방 떠나기',
      {
        confirmButtonText: '떠나기',
        cancelButtonText: '취소',
        type: 'warning',
        dangerouslyUseHTMLString: true,
      }
    )
    
    // 채팅방 떠나기 처리 (백엔드에서 참여 정보 비활성화)
    const result = await chatStore.leaveChatRoom(roomId)
    if (result.success) {
      ElMessage.success('채팅방을 떠났습니다. 재입장 시 이전 메시지를 모두 볼 수 있습니다.')
      await loadChatRooms() // 목록 새로고침
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('채팅방 떠나기에 실패했습니다.')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const getRoomTypeClass = (type) => {
  if (type === 'OPEN') return 'open'
  if (type === 'PRIVATE') return 'private'
  return ''
}

const getRoomTypeText = (type) => {
  if (type === 'OPEN') return '오픈'
  if (type === 'PRIVATE') return '개인'
  return '알 수 없음'
}

const goToAdmin = () => {
  router.push('/admin/rooms')
}

const goToUserProfile = () => {
  router.push('/user-profile')
}
</script>

<style scoped>
.chat-rooms-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.superadmin-badge {
  background-color: #e6a23c;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.rooms-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.loading {
  padding: 20px;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.rooms-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.room-section {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.room-section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #303133;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.rooms-list {
  display: grid;
  gap: 15px;
}

.room-item {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
}

.room-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.room-item.empty-room {
  opacity: 0.6;
  background-color: #f5f5f5;
  border-color: #eee;
}

.room-info {
  padding: 15px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.room-header h4 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.room-type {
  background-color: #e1f3d8;
  color: #67c23a;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.room-type.open {
  background-color: #e1f3d8;
  color: #67c23a;
}

.room-type.private {
  background-color: #fde2e2;
  color: #f56c6c;
}

.room-info p {
  margin: 5px 0;
  color: #606266;
  font-size: 14px;
}

.participant-count {
  font-weight: bold;
  color: #409eff;
}

.room-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
  padding: 0 15px 15px;
  border-top: 1px solid #eee;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .header-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .room-actions {
    flex-direction: column;
  }
}
</style>
