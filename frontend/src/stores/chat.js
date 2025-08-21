import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const chatRooms = ref([])
  const currentRoom = ref(null)
  const isConnected = ref(false)

  // 채팅방 목록 조회 (참여한 방과 다른 방으로 분리)
  const fetchChatRoomList = async (userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms?userSession=${userSession}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, roomList: result.roomList }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 목록 조회 오류:', error)
      return { success: false, message: '채팅방 목록을 불러올 수 없습니다.' }
    }
  }

  // 채팅방 생성 (타입 포함)
  const createChatRoomWithType = async (roomName, roomType, creatorNickname) => {
    try {
      const response = await fetch('http://localhost:8080/api/chat/rooms', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: roomName,
          roomType: roomType,
          creatorNickname: creatorNickname
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, room: result.room }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 생성 오류:', error)
      return { success: false, message: '채팅방 생성에 실패했습니다.' }
    }
  }

  // 채팅방 상세 정보 조회
  const getChatRoomDetail = async (roomId, userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms/${roomId}?userSession=${userSession}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, roomDetail: result.roomDetail }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 상세 정보 조회 오류:', error)
      return { success: false, message: '채팅방 정보를 불러올 수 없습니다.' }
    }
  }

  // 채팅방 참여
  const joinChatRoom = async (roomId, userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms/${roomId}/join`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession: userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '채팅방에 입장했습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 입장 오류:', error)
      return { success: false, message: '채팅방 입장에 실패했습니다.' }
    }
  }

  // 채팅방 떠나기
  const leaveChatRoom = async (roomId, userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms/${roomId}/leave`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession: userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '채팅방을 떠났습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 퇴장 오류:', error)
      return { success: false, message: '채팅방 퇴장에 실패했습니다.' }
    }
  }

  // 채팅 메시지 조회 (참여 시점 이후)
  const getChatMessages = async (roomId, userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms/${roomId}/messages?userSession=${userSession}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, messages: result.messages }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅 메시지 조회 오류:', error)
      return { success: false, message: '채팅 메시지를 불러올 수 없습니다.' }
    }
  }

  // 채팅 메시지 전송
  const sendChatMessage = async (messageData) => {
    try {
      const response = await fetch('http://localhost:8080/api/chat/messages', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(messageData),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '메시지가 전송되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('메시지 전송 오류:', error)
      return { success: false, message: '메시지 전송에 실패했습니다.' }
    }
  }

  // 사용자 초대
  const inviteUserToRoom = async (roomId, nickname) => {
    try {
      const response = await fetch(`http://localhost:8080/api/chat/rooms/${roomId}/invite`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nickname: nickname
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '사용자를 초대했습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('사용자 초대 오류:', error)
      return { success: false, message: '사용자 초대에 실패했습니다.' }
    }
  }

  // 관리자용 채팅방 목록 조회
  const getAdminRoomList = async (userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/admin/rooms?userSession=${userSession}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, rooms: result.rooms }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('관리자 채팅방 목록 조회 오류:', error)
      return { success: false, message: '관리자 채팅방 목록을 불러올 수 없습니다.' }
    }
  }

  // 채팅방 삭제 (관리자용)
  const deleteChatRoom = async (roomId, userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/admin/rooms/${roomId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession: userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '채팅방이 삭제되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('채팅방 삭제 오류:', error)
      return { success: false, message: '채팅방 삭제에 실패했습니다.' }
    }
  }

  // 자동 정리 실행 (관리자용)
  const runCleanup = async (userSession) => {
    try {
      const response = await fetch('http://localhost:8080/api/admin/cleanup', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession: userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, cleanedCount: result.cleanedCount }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('자동 정리 실행 오류:', error)
      return { success: false, message: '자동 정리 실행에 실패했습니다.' }
    }
  }

  // 연결 상태 설정
  const setConnectionStatus = (status) => {
    isConnected.value = status
  }

  // 현재 방 설정
  const setCurrentRoom = (room) => {
    currentRoom.value = room
  }

  return {
    chatRooms,
    currentRoom,
    isConnected,
    fetchChatRoomList,
    createChatRoomWithType,
    getChatRoomDetail,
    joinChatRoom,
    leaveChatRoom,
    getChatMessages,
    sendChatMessage,
    inviteUserToRoom,
    getAdminRoomList,
    deleteChatRoom,
    runCleanup,
    setConnectionStatus,
    setCurrentRoom
  }
})
