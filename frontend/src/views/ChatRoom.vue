<template>
  <div class="chat-room-container">
    <!-- 로딩 상태 표시 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-content">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>채팅방을 불러오는 중...</p>
      </div>
    </div>
    
    <!-- 에러 상태 표시 -->
    <div v-else-if="error" class="error-overlay">
      <div class="error-content">
        <el-icon class="error-icon"><Warning /></el-icon>
        <h3>채팅방 로드 실패</h3>
        <p>{{ error }}</p>
        <el-button type="primary" @click="retryLoad">다시 시도</el-button>
        <el-button @click="goBack">채팅방 목록으로</el-button>
      </div>
    </div>
    
    <!-- 정상 채팅방 화면 -->
    <div v-else class="chat-content">
      <div class="header-section">
        <div class="room-info">
          <h2>{{ roomName || '채팅방' }}</h2>
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
            <div v-if="participants.length === 0" class="no-participants">
              참여자가 없습니다.
            </div>
          </div>
        </div>
        
        <!-- 채팅 영역 -->
        <div class="chat-main">
          <div class="messages-container" ref="messagesContainer">
            <div 
              v-for="(message, index) in messages" 
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
            <div v-if="messages.length === 0" class="no-messages">
              아직 메시지가 없습니다.
            </div>
          </div>
          
          <div class="input-section">
            <el-input
              v-model="newMessage"
              placeholder="메시지를 입력하세요..."
              @keyup.enter="sendMessage"
              :disabled="false"
            >
              <template #append>
                <el-button 
                  type="primary" 
                  @click="sendMessage"
                  :disabled="!newMessage.trim()"
                >
                  전송
                </el-button>
              </template>
            </el-input>
            <div v-if="!isConnected" class="connection-status">
              <el-tag type="warning" size="small">
                <el-icon><Warning /></el-icon>
                WebSocket 연결 중... (메시지는 정상 전송됩니다)
              </el-tag>
            </div>
          </div>
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
// SockJS import 방식 변경 (ES6 모듈)
import SockJS from 'sockjs-client'
import { Stomp } from '@stomp/stompjs'
import config from '../../env.config.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const roomName = ref('')
const messages = ref([])
const newMessage = ref('')
const participants = ref([])
const participantCount = ref(0)
const isConnected = ref(false)
const loading = ref(false)
const error = ref('')
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
  console.log('=== ChatRoom.onMounted 시작 ===')
  console.log('현재 route 정보:', {
    path: route.path,
    query: route.query,
    params: route.params
  })
  
  try {
    // roomId 유효성 검사
    if (!route.query.roomId) {
      console.error('roomId가 없습니다. 채팅방 목록으로 이동합니다.')
      ElMessage.error('채팅방 정보가 올바르지 않습니다.')
      router.push('/rooms')
      return
    }
    
    const currentRoomId = route.query.roomId
    console.log('채팅방 진입:', { currentRoomId, query: route.query })
    
    // userStore 초기화 확인
    if (!userStore.currentUser) {
      console.log('userStore 초기화 시도...')
      try {
        userStore.initialize()
        await new Promise(resolve => setTimeout(resolve, 1000)) // 1초 대기
      } catch (initError) {
        console.error('userStore 초기화 실패:', initError)
      }
    }
    
    // 세션 체크
    if (!userStore.checkSession()) {
      console.error('사용자 세션이 없습니다.')
      router.push('/nickname-register')
      return
    }
    
    if (!userStore.currentUser) {
      console.error('사용자 정보를 가져올 수 없습니다.')
      ElMessage.error('사용자 정보를 가져올 수 없습니다.')
      router.push('/nickname-register')
      return
    }
    
    console.log('사용자 정보 확인:', userStore.currentUser)
    
    // 채팅방 초기화
    await initializeChatRoom(currentRoomId)
    
  } catch (error) {
    console.error('ChatRoom onMounted 에러:', error)
    ElMessage.error('채팅방을 초기화할 수 없습니다.')
    // 에러 발생 시에도 loading 상태 해제
    loading.value = false
    router.push('/rooms')
  }
})

onUnmounted(() => {
  disconnect()
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
  }
})

const initializeChatRoom = async (currentRoomId) => {
  console.log('=== ChatRoom.initializeChatRoom 시작 ===')
  console.log('입력 파라미터:', { 
    currentRoomId, 
    userSession: userStore.currentUser?.userSession,
    currentRoute: route.path,
    router: router
  })
  
  if (!currentRoomId) {
    console.error('currentRoomId가 없습니다.')
    error.value = '채팅방 ID가 올바르지 않습니다.'
    return
  }
  
  if (!userStore.currentUser?.userSession) {
    console.error('userSession이 없습니다.')
    error.value = '사용자 세션이 올바르지 않습니다.'
    return
  }
  
  loading.value = true
  error.value = '' // 에러 초기화
  
  try {
    console.log('채팅방 초기화 시작:', { currentRoomId, userSession: userStore.currentUser.userSession })
    
    // 채팅방 정보 및 참여자 목록 가져오기
    const roomResult = await chatStore.getChatRoomDetail(currentRoomId, userStore.currentUser.userSession)
    console.log('채팅방 상세 정보 조회 결과:', roomResult)
    
    if (roomResult && roomResult.success) {
      console.log('채팅방 상세 정보:', roomResult.roomDetail)
      roomName.value = roomResult.roomDetail.name || '채팅방'
      participants.value = roomResult.roomDetail.participants || []
      participantCount.value = participants.value.length
      
      console.log('WebSocket 연결 시도...')
      // WebSocket 연결
      connectWebSocket()
      
      console.log('채팅 메시지 로드 시도...')
      // 채팅 메시지 로드
      await loadChatMessages()
      
      // WebSocket 연결 후 입장 메시지 전송
      setTimeout(() => {
        if (isConnected.value && stompClient) {
          try {
            const enterMessage = {
              chatRoomId: currentRoomId,
              userId: userStore.currentUser.userSession,
              nickname: userStore.currentUser.nickname,
              message: `${userStore.currentUser.nickname}님이 입장했습니다.`,
              messageType: 'ENTER'
            }
            const destination = `/app/chat.enter.${currentRoomId}`
            console.log('입장 메시지 전송:', destination)
            stompClient.send(destination, {}, JSON.stringify(enterMessage))
          } catch (error) {
            console.error('입장 메시지 전송 실패:', error)
          }
        }
      }, 1000)
      
      console.log('채팅방 초기화 완료')
      // 채팅방 초기화 완료 후 loading 상태 해제
      loading.value = false
    } else {
      console.error('채팅방 상세 정보 조회 실패:', roomResult?.message || '알 수 없는 오류')
      error.value = `채팅방 정보를 불러올 수 없습니다: ${roomResult?.message || '알 수 없는 오류'}`
      ElMessage.error(roomResult?.message || '채팅방 정보를 불러올 수 없습니다.')
      loading.value = false
    }
  } catch (err) {
    console.error('채팅방 초기화 중 예외 발생:', err)
    console.error('에러 스택:', err.stack)
    error.value = `채팅방 초기화에 실패했습니다: ${err.message}`
    ElMessage.error('채팅방 초기화에 실패했습니다.')
    loading.value = false
  }
}

const connectWebSocket = () => {
  try {
    // SockJS 유효성 검사
    if (typeof SockJS !== 'function') {
      console.error('SockJS가 제대로 로드되지 않았습니다:', SockJS)
      ElMessage.warning('WebSocket 연결을 초기화할 수 없습니다. HTTP API를 통해 메시지를 전송합니다.')
      return
    }
    
    const currentRoomId = route.query.roomId
    if (!currentRoomId) {
      console.error('roomId가 없어 WebSocket 연결을 할 수 없습니다.')
      return
    }
    
    console.log('WebSocket 연결 시도...')
    const socket = new SockJS(`${config.WS_BASE_URL}/stomp`)
    stompClient = Stomp.over(socket)
    
    // STOMP 연결 설정
    const connectHeaders = {
      'heart-beat': '10000,10000'
    }
  
    stompClient.connect(connectHeaders, (frame) => {
      console.log('WebSocket 연결 성공:', frame)
      isConnected.value = true
      ElMessage.success('실시간 채팅이 활성화되었습니다.')
      
      // 채팅방 구독 - 올바른 destination 경로 사용
      const chatTopic = `/topic/chat.${currentRoomId}`
      console.log('채팅방 구독:', chatTopic)
      stompClient.subscribe(chatTopic, (message) => {
        try {
          const chatMessage = JSON.parse(message.body)
          console.log('실시간 메시지 수신:', chatMessage)
          messages.value.push(chatMessage)
          scrollToBottom()
        } catch (error) {
          console.error('메시지 파싱 오류:', error)
        }
      })
      
      // 참여자 목록 업데이트 구독
      const participantsTopic = `/topic/participants.${currentRoomId}`
      console.log('참여자 목록 구독:', participantsTopic)
      stompClient.subscribe(participantsTopic, (message) => {
        try {
          const participantUpdate = JSON.parse(message.body)
          console.log('참여자 목록 업데이트:', participantUpdate)
          participants.value = participantUpdate.participants
          participantCount.value = participants.value.length
        } catch (error) {
          console.error('참여자 목록 파싱 오류:', error)
        }
      })
      
      // 연결 성공 후 자동 재연결 타이머 정리
      if (reconnectTimer) {
        clearTimeout(reconnectTimer)
        reconnectTimer = null
      }
      
    }, (error) => {
      console.error('WebSocket 연결 실패:', error)
      isConnected.value = false
      ElMessage.warning('실시간 채팅 연결에 실패했습니다. HTTP API를 통해 메시지를 전송합니다.')
      
      // 재연결 시도 (5초 후)
      if (!reconnectTimer) {
        reconnectTimer = setTimeout(() => {
          console.log('WebSocket 재연결 시도...')
          reconnectTimer = null
          if (stompClient) {
            connectWebSocket()
          }
        }, 5000)
      }
    })
    
    // 연결 타임아웃 설정 (10초)
    setTimeout(() => {
      if (!isConnected.value && stompClient) {
        console.log('WebSocket 연결 타임아웃')
        ElMessage.warning('WebSocket 연결이 시간 초과되었습니다. HTTP API를 통해 메시지를 전송합니다.')
        disconnect()
      }
    }, 10000)
    
  } catch (error) {
    console.error('WebSocket 연결 초기화 실패:', error)
    ElMessage.warning('WebSocket 연결을 초기화할 수 없습니다. HTTP API를 통해 메시지를 전송합니다.')
  }
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
    const currentRoomId = route.query.roomId
    if (!currentRoomId) {
      console.error('roomId가 없어 메시지를 로드할 수 없습니다.')
      return
    }
    
    if (!userStore.currentUser?.userSession) {
      console.error('userSession이 없어 메시지를 로드할 수 없습니다.')
      return
    }
    
    console.log('메시지 로드 시도:', { currentRoomId, userSession: userStore.currentUser.userSession })
    const result = await chatStore.getChatMessages(currentRoomId, userStore.currentUser.userSession)
    console.log('메시지 로드 결과:', result)
    
    if (result && result.success) {
      messages.value = result.messages || []
      console.log('로드된 메시지 수:', messages.value.length)
      await nextTick()
      scrollToBottom()
    } else {
      console.error('메시지 로드 실패:', result?.message || '알 수 없는 오류')
    }
  } catch (error) {
    console.error('메시지 로드 중 예외 발생:', error)
    console.error('에러 스택:', error.stack)
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return
  
  try {
    const currentRoomId = route.query.roomId
    if (!currentRoomId) {
      console.error('roomId가 없어 메시지를 전송할 수 없습니다.')
      return
    }
    
    const messageData = {
      chatRoomId: currentRoomId,
      userId: userStore.currentUser.userSession,
      nickname: userStore.currentUser.nickname,
      message: newMessage.value.trim(),
      messageType: 'TEXT'
    }
    
    console.log('메시지 전송 시도:', messageData)
    
    // WebSocket 연결이 활성화된 경우 실시간 전송 시도
    if (isConnected.value && stompClient) {
      try {
        const destination = `/app/chat.message.${currentRoomId}`
        console.log('WebSocket 메시지 전송:', destination)
        stompClient.send(destination, {}, JSON.stringify(messageData))
        
        // 메시지 입력란 초기화
        newMessage.value = ''
        console.log('WebSocket 메시지 전송 성공')
        return
      } catch (wsError) {
        console.error('WebSocket 메시지 전송 실패, HTTP API로 대체:', wsError)
      }
    }
    
    // WebSocket 전송 실패 시 HTTP API로 대체 전송
    console.log('HTTP API를 통한 메시지 전송 시도')
    const result = await chatStore.sendChatMessage(messageData)
    if (result.success) {
      newMessage.value = ''
      console.log('HTTP API 메시지 전송 성공')
    } else {
      console.error('HTTP API 메시지 전송 실패:', result.message)
      ElMessage.error(result.message)
    }
  } catch (error) {
    console.error('메시지 전송 중 예외 발생:', error)
    ElMessage.error('메시지 전송에 실패했습니다.')
  }
}

const scrollToBottom = () => {
  const chatContainer = document.querySelector('.messages-container')
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
    
    const currentRoomId = route.query.roomId
    if (!currentRoomId) {
      console.error('roomId가 없어 채팅방을 떠날 수 없습니다.')
      return
    }
    
    // WebSocket을 통한 퇴장 메시지 전송
    if (isConnected.value && stompClient) {
      try {
        const leaveMessage = {
          chatRoomId: currentRoomId,
          userId: userStore.currentUser.userSession,
          nickname: userStore.currentUser.nickname,
          message: `${userStore.currentUser.nickname}님이 퇴장했습니다.`,
          messageType: 'LEAVE'
        }
        const destination = `/app/chat.leave.${currentRoomId}`
        console.log('퇴장 메시지 전송:', destination)
        stompClient.send(destination, {}, JSON.stringify(leaveMessage))
      } catch (error) {
        console.error('퇴장 메시지 전송 실패:', error)
      }
    }
    
    const result = await chatStore.leaveChatRoom(currentRoomId)
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
    
    const currentRoomId = route.query.roomId
    if (!currentRoomId) {
      console.error('roomId가 없어 사용자를 초대할 수 없습니다.')
      return
    }
    
    const result = await chatStore.inviteUserToRoom(currentRoomId, inviteForm.nickname)
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

// 에러 처리 및 재시도 함수들
const retryLoad = async () => {
  console.log('채팅방 재로드 시도...')
  error.value = ''
  loading.value = true
  const currentRoomId = route.query.roomId
  if (currentRoomId) {
    try {
      await initializeChatRoom(currentRoomId)
    } catch (err) {
      console.error('채팅방 재로드 실패:', err)
      loading.value = false
    }
  } else {
    loading.value = false
  }
}

const goBack = () => {
  console.log('채팅방 목록으로 이동...')
  router.push('/rooms')
}

const clearError = () => {
  error.value = ''
  loading.value = false
}

// 메시지 타입별 CSS 클래스 반환
const getMessageTypeClass = (messageType) => {
  switch (messageType) {
    case 'ENTER': return 'enter'
    case 'LEAVE': return 'leave'
    case 'BROADCAST': return 'broadcast'
    case 'TEXT': return 'message'
    default: return 'message'
  }
}

// 메시지 타입별 텍스트 반환
const getMessageTypeText = (messageType) => {
  switch (messageType) {
    case 'ENTER': return '입장'
    case 'LEAVE': return '퇴장'
    case 'BROADCAST': return '공지'
    case 'TEXT': return '메시지'
    default: return '메시지'
  }
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

.connection-status {
  margin-top: 10px;
  text-align: center;
}

/* 로딩 및 에러 오버레이 스타일 */
.loading-overlay,
.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-content,
.error-content {
  text-align: center;
  padding: 30px;
  border-radius: 10px;
  background-color: #fff;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.loading-content .is-loading {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 15px;
}

.error-content .error-icon {
  font-size: 60px;
  color: #f56c6c;
  margin-bottom: 15px;
}

.error-content h3 {
  color: #f56c6c;
  margin-bottom: 10px;
}

.error-content p {
  color: #606266;
  margin-bottom: 25px;
  font-size: 14px;
}

.error-content .el-button {
  margin-right: 10px;
}

.no-participants,
.no-messages {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
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
