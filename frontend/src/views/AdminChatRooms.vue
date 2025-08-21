<template>
  <div class="admin-chat-rooms-container">
    <div class="header-section">
      <h2>관리자 채팅방 관리</h2>
      <div class="user-info">
        <span><strong>관리자:</strong> {{ userStore.currentUser?.nickname }}</span>
        <span class="admin-badge">SuperAdmin</span>
      </div>
    </div>
    
    <el-card class="stats-card">
      <template #header>
        <span>채팅방 통계</span>
      </template>
      <div class="stats-grid">
        <div class="stat-item">
          <h3>{{ totalRooms }}</h3>
          <p>전체 채팅방</p>
        </div>
        <div class="stat-item">
          <h3>{{ activeRooms }}</h3>
          <p>활성 채팅방</p>
        </div>
        <div class="stat-item">
          <h3>{{ emptyRooms }}</h3>
          <p>빈 채팅방</p>
        </div>
        <div class="stat-item">
          <h3>{{ totalMessages }}</h3>
          <p>전체 메시지</p>
        </div>
      </div>
    </el-card>
    
    <el-card class="rooms-card">
      <template #header>
        <div class="card-header">
          <span>채팅방 목록</span>
          <div class="header-actions">
            <el-button type="success" @click="refreshRooms">
              새로고침
            </el-button>
            <el-button type="primary" @click="showCreateRoomDialog">
              새 채팅방 만들기
            </el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="chatStore.chatRooms.length === 0" class="empty-state">
        <el-empty description="채팅방이 없습니다." />
      </div>
      
      <div v-else class="rooms-list">
        <el-card 
          v-for="room in chatStore.chatRooms" 
          :key="room.roomId" 
          class="room-item"
          :class="{ 'empty-room': room.participantCount === 0 }"
        >
          <div class="room-info">
            <h3>{{ room.name }}</h3>
            <p>방 ID: {{ room.roomId }}</p>
            <p>생성일: {{ formatDate(room.regDate) }}</p>
            <p>마지막 메시지: {{ formatDateTime(room.lastMessageTime) }}</p>
            <p class="participant-count">
              참여자: {{ room.participantCount || 0 }}명
            </p>
          </div>
          <div class="room-actions">
            <el-button 
              type="primary" 
              size="small"
              @click="viewRoom(room.roomId)"
            >
              보기
            </el-button>
            <el-button 
              type="info" 
              size="small"
              @click="viewMessages(room.roomId)"
            >
              메시지
            </el-button>
            <el-button 
              v-if="room.participantCount === 0"
              type="danger" 
              size="small"
              @click="deleteRoom(room.roomId)"
            >
              삭제
            </el-button>
          </div>
        </el-card>
      </div>
    </el-card>
    
    <!-- 새 채팅방 생성 다이얼로그 -->
    <el-dialog v-model="createRoomDialog" title="새 채팅방 만들기" width="400px">
      <el-form :model="newRoomForm" :rules="roomRules" ref="newRoomFormRef" label-width="80px">
        <el-form-item label="방 이름" prop="name">
          <el-input v-model="newRoomForm.name" placeholder="채팅방 이름을 입력하세요" />
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
    
    <!-- 메시지 조회 다이얼로그 -->
    <el-dialog v-model="messagesDialog" title="채팅 메시지" width="800px">
      <div v-if="messagesLoading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else class="messages-list">
        <div 
          v-for="(message, index) in currentMessages" 
          :key="index" 
          class="message-item"
        >
          <div class="message-header">
            <span class="nickname">{{ message.nickname }}</span>
            <span class="message-type" :class="getMessageTypeClass(message.messageType)">
              {{ getMessageTypeText(message.messageType) }}
            </span>
            <span class="timestamp">{{ formatDateTime(message.regDate) }}</span>
          </div>
          <div class="message-content">
            {{ message.message || '(시스템 메시지)' }}
          </div>
        </div>
      </div>
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
const messagesDialog = ref(false)
const messagesLoading = ref(false)
const currentMessages = ref([])

const totalRooms = ref(0)
const activeRooms = ref(0)
const emptyRooms = ref(0)
const totalMessages = ref(0)

const newRoomForm = reactive({
  name: ''
})

const roomRules = {
  name: [
    { required: true, message: '채팅방 이름을 입력해주세요', trigger: 'blur' },
    { min: 2, max: 50, message: '채팅방 이름은 2-50자 사이여야 합니다', trigger: 'blur' }
  ]
}

onMounted(async () => {
  if (!userStore.currentUser?.isSuperAdmin) {
    ElMessage.error('관리자 권한이 필요합니다.')
    router.push('/rooms')
    return
  }
  
  await loadAdminData()
})

const loadAdminData = async () => {
  loading.value = true
  try {
    await chatStore.fetchAllChatRooms()
    calculateStats()
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  totalRooms.value = chatStore.chatRooms.length
  activeRooms.value = chatStore.chatRooms.filter(room => (room.participantCount || 0) > 0).length
  emptyRooms.value = chatStore.chatRooms.filter(room => (room.participantCount || 0) === 0).length
  // totalMessages는 실제 구현에서 계산
  totalMessages.value = 0
}

const refreshRooms = async () => {
  await loadAdminData()
  ElMessage.success('새로고침 완료')
}

const showCreateRoomDialog = () => {
  createRoomDialog.value = true
  newRoomForm.name = ''
}

const createRoom = async () => {
  if (!newRoomFormRef.value) return
  
  try {
    await newRoomFormRef.value.validate()
    creating.value = true
    
    const result = await chatStore.createChatRoom(newRoomForm.name)
    if (result.success) {
      ElMessage.success('채팅방이 생성되었습니다.')
      createRoomDialog.value = false
      await loadAdminData()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('채팅방 생성에 실패했습니다.')
  } finally {
    creating.value = false
  }
}

const viewRoom = (roomId) => {
  router.push(`/rooms/${roomId}`)
}

const viewMessages = async (roomId) => {
  messagesDialog.value = true
  messagesLoading.value = true
  
  try {
    const result = await chatStore.fetchAllChatMessages(roomId)
    if (result.success) {
      currentMessages.value = result.messages
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('메시지를 불러올 수 없습니다.')
  } finally {
    messagesLoading.value = false
  }
}

const deleteRoom = async (roomId) => {
  try {
    await ElMessageBox.confirm(
      '정말로 이 채팅방을 삭제하시겠습니까?',
      '채팅방 삭제',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )
    
    const result = await chatStore.deleteChatRoom(roomId)
    if (result.success) {
      ElMessage.success(result.message)
      await loadAdminData()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('채팅방 삭제에 실패했습니다.')
    }
  }
}

const getMessageTypeClass = (messageType) => {
  switch (messageType) {
    case 'ENTER': return 'enter'
    case 'LEAVE': return 'leave'
    case 'BROADCAST': return 'broadcast'
    default: return 'message'
  }
}

const getMessageTypeText = (messageType) => {
  switch (messageType) {
    case 'ENTER': return '입장'
    case 'LEAVE': return '퇴장'
    case 'BROADCAST': return '방송'
    default: return '메시지'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('ko-KR')
}

const formatDateTime = (dateString) => {
  if (!dateString) return '없음'
  return new Date(dateString).toLocaleString('ko-KR')
}
</script>

<style scoped>
.admin-chat-rooms-container {
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

.admin-badge {
  background-color: #e6a23c;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.stat-item h3 {
  margin: 0 0 10px 0;
  font-size: 2em;
  color: #409eff;
}

.stat-item p {
  margin: 0;
  color: #606266;
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

.rooms-list {
  display: grid;
  gap: 15px;
}

.room-item {
  transition: all 0.3s ease;
}

.room-item.empty-room {
  opacity: 0.6;
  background-color: #f5f5f5;
}

.room-info h3 {
  margin: 0 0 10px 0;
  color: #303133;
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
}

.messages-list {
  max-height: 400px;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 8px;
  background-color: #f5f5f5;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.nickname {
  font-weight: bold;
  color: #409eff;
}

.message-type {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  color: white;
}

.message-type.enter {
  background-color: #67c23a;
}

.message-type.leave {
  background-color: #f56c6c;
}

.message-type.broadcast {
  background-color: #e6a23c;
}

.message-type.message {
  background-color: #909399;
}

.timestamp {
  font-size: 11px;
  color: #999;
  margin-left: auto;
}

.message-content {
  word-break: break-word;
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
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
