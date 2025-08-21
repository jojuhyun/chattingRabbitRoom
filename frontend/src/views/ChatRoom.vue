<template>
  <div class="chat-room-container">
    <div class="header-section">
      <div class="room-info">
        <h2>{{ roomName }}</h2>
        <p class="participant-count">참여자: {{ participantCount }}명</p>
      </div>
      <div class="user-info">
        <span><strong>사용자:</strong> {{ userStore.currentUser?.nickname }}</span>
        <el-button type="warning" size="small" @click="leaveRoom">
          채팅방 나가기
        </el-button>
      </div>
    </div>
    
    <div class="chat-layout">
      <!-- 참여자 목록 -->
      <div class="participants-sidebar">
        <h3>👥 참여자 목록</h3>
        <div class="participants-list">
          <div 
            v-for="participant in participants" 
            :key="participant.nickname"
            class="participant-item"
          >
            <span class="participant-nickname">{{ participant.nickname }}</span>
            <span class="join-time">{{ formatTime(participant.enterTime) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 채팅 영역 -->
      <div class="chat-main">
        <div class="messages-container" ref="messagesContainer">
          <div 
            v-for="(message, index) in chatStore.messages" 
            :key="index" 
            class="message-item"
            :class="{ 'own-message': message.userId === userStore.currentUser?.userSession }"
          >
            <div class="message-header">
              <span class="nickname">{{ message.nickname }}</span>
              <span class="message-type" :class="getMessageTypeClass(message.messageType)">
                {{ getMessageTypeText(message.messageType) }}
              </span>
              <span class="timestamp">{{ formatTime(message.regDate) }}</span>
            </div>
            <div class="message-content">
              {{ message.message || '(시스템 메시지)' }}
            </div>
          </div>
        </div>
        
        <div class="input-section">
          <el-input
            v-model="messageInput"
            placeholder="메시지를 입력하세요..."
            @keyup.enter="sendMessage"
            :disabled="!isConnected"
          >
            <template #append>
              <el-button 
                type="primary" 
                @click="sendMessage"
                :disabled="!isConnected || !messageInput.trim()"
              >
                전송
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { ElMessage, ElMessageBox, ElTooltip } from 'element-plus'
import { SockJS } from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const roomId = route.params.roomId
const roomName = ref('')
const messages = ref([])
const newMessage = ref('')
const participants = ref([])
const participantCount = ref(0)
const isConnected = ref(false)
const loading = ref(false)
const showInviteDialog = ref(false)
const inviteFormRef = ref()
const inviteForm = reactive({
  nickname: ''
})

const inviteRules = {
  nickname: [
    { required: true, message: '초대할 사용자의 닉네임을 입력해주세요', trigger: 'blur' }
  ]
}

let stompClient = null
let reconnectTimer = null

const isSuperAdmin = computed(() => {
  return userStore.currentUser?.isSuperAdmin || false
})

onMounted(async () => {
  // 세션 체크
  if (!userStore.checkSession()) {
    router.push('/nickname-register')
    return
  }
  
  await initializeChatRoom()
})

onUnmounted(() => {
  disconnect()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})

const initializeChatRoom = async () => {
  loading.value = true
  try {
    // 채팅방 정보 및 참여자 목록 가져오기
    const roomResult = await chatStore.getChatRoomDetail(roomId, userStore.currentUser.userSession)
    if (roomResult.success) {
      roomName.value = roomResult.roomDetail.name
      participants.value = roomResult.roomDetail.participants
      participantCount.value = participants.value.length
      
      // 채팅방 참여 처리
      const joinResult = await chatStore.joinChatRoom(roomId, userStore.currentUser.userSession)
      if (joinResult.success) {
        // WebSocket 연결
        connectWebSocket()
        
        // 채팅 메시지 로드 (참여 시점 이후의 메시지만)
        await loadChatMessages()
      } else {
        ElMessage.error(joinResult.message)
        router.push('/rooms')
      }
    } else {
      ElMessage.error(roomResult.message)
      router.push('/rooms')
    }
  } catch (error) {
    ElMessage.error('채팅방 초기화에 실패했습니다.')
    router.push('/rooms')
  } finally {
    loading.value = false
  }
}

const connectWebSocket = () => {
  const socket = new SockJS('http://localhost:8080/stomp')
  stompClient = Stomp.over(socket)
  
  stompClient.connect({}, (frame) => {
    console.log('Connected: ' + frame)
    isConnected.value = true
    
    // 채팅방 구독
    stompClient.subscribe(`/topic/chat/${roomId}`, (message) => {
      const chatMessage = JSON.parse(message.body)
      messages.value.push(chatMessage)
      scrollToBottom()
    })
    
    // 참여자 목록 업데이트 구독
    stompClient.subscribe(`/topic/participants/${roomId}`, (message) => {
      const participantUpdate = JSON.parse(message.body)
      participants.value = participantUpdate.participants
      participantCount.value = participants.value.length
    })
  }, (error) => {
    console.error('WebSocket 연결 실패:', error)
    isConnected.value = false
    // 재연결 시도
    reconnectTimer = setTimeout(() => {
      if (stompClient) {
        connectWebSocket()
      }
    }, 5000)
  })
}

const disconnect = () => {
  if (stompClient) {
    stompClient.disconnect()
    stompClient = null
  }
  isConnected.value = false
}

const loadChatMessages = async () => {
  try {
    const result = await chatStore.getChatMessages(roomId, userStore.currentUser.userSession)
    if (result.success) {
      messages.value = result.messages
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('메시지 로드 실패:', error)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !isConnected.value) return
  
  try {
    const messageData = {
      chatRoomId: roomId,
      userId: userStore.currentUser.userSession,
      nickname: userStore.currentUser.nickname,
      message: newMessage.value.trim(),
      messageType: 'TEXT'
    }
    
    const result = await chatStore.sendChatMessage(messageData)
    if (result.success) {
      newMessage.value = ''
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('메시지 전송에 실패했습니다.')
  }
}

const scrollToBottom = () => {
  const chatContainer = document.querySelector('.chat-messages')
  if (chatContainer) {
    chatContainer.scrollTop = chatContainer.scrollHeight
  }
}

const leaveRoom = async () => {
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
    
    const result = await chatStore.leaveChatRoom(roomId)
    if (result.success) {
      ElMessage.success('채팅방을 떠났습니다. 재입장 시 이전 메시지를 모두 볼 수 있습니다.')
      router.push('/rooms')
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('채팅방 떠나기에 실패했습니다.')
    }
  }
}

const openInviteDialog = () => {
  inviteForm.nickname = ''
  inviteFormRef.value = inviteForm
}

const inviteUser = async () => {
  if (!inviteFormRef.value) return
  
  try {
    await inviteFormRef.value.validate()
    
    const result = await chatStore.inviteUserToRoom(roomId, inviteForm.nickname)
    if (result.success) {
      ElMessage.success('사용자를 초대했습니다.')
      showInviteDialog.value = false
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('사용자 초대에 실패했습니다.')
  }
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleTimeString('ko-KR', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (date.toDateString() === today.toDateString()) {
    return '오늘'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '어제'
  } else {
    return date.toLocaleDateString('ko-KR')
  }
}

const goBack = () => {
  router.push('/rooms')
}
</script>

<style scoped>
.chat-room-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.room-info h2 {
  margin: 0 0 5px 0;
  color: #333;
}

.participant-count {
  margin: 0;
  color: #409eff;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.chat-layout {
  display: flex;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.participants-sidebar {
  width: 250px;
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  height: fit-content;
}

.participants-sidebar h3 {
  margin: 0 0 15px 0;
  color: #333;
  text-align: center;
}

.participants-list {
  max-height: 400px;
  overflow-y: auto;
}

.participant-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.participant-nickname {
  font-weight: bold;
  color: #333;
}

.join-time {
  font-size: 12px;
  color: #666;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  max-height: 500px;
}

.message-item {
  margin-bottom: 15px;
  padding: 15px;
  border-radius: 10px;
  background: #f8f9fa;
  border-left: 4px solid #409eff;
}

.message-item.own-message {
  background: #e3f2fd;
  border-left-color: #2196f3;
  margin-left: 20px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.nickname {
  font-weight: bold;
  color: #409eff;
}

.message-type {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  color: white;
  font-weight: bold;
}

.message-type.enter {
  background-color: #4caf50;
}

.message-type.leave {
  background-color: #f44336;
}

.message-type.broadcast {
  background-color: #ff9800;
}

.message-type.message {
  background-color: #9e9e9e;
}

.timestamp {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.message-content {
  color: #333;
  line-height: 1.4;
  word-break: break-word;
}

.input-section {
  padding: 20px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.input-section .el-input {
  width: 100%;
}

.input-section .el-button {
  height: 40px;
}

@media (max-width: 768px) {
  .chat-layout {
    flex-direction: column;
  }
  
  .participants-sidebar {
    width: 100%;
    order: 2;
  }
  
  .chat-main {
    order: 1;
  }
  
  .header-section {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }
  
  .user-info {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
