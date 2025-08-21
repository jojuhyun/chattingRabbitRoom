import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const currentUser = ref(null)
  const sessionInterval = ref(null)

  const isLoggedIn = computed(() => {
    return currentUser.value !== null
  })

  const isSuperAdmin = computed(() => {
    return currentUser.value?.isSuperAdmin || false
  })

  // 세션 체크
  const checkSession = () => {
    if (!currentUser.value) {
      const storedUser = sessionStorage.getItem('userSession')
      if (storedUser) {
        try {
          const userData = JSON.parse(storedUser)
          currentUser.value = userData
          startSessionRefresh()
          return true
        } catch (error) {
          console.error('세션 데이터 파싱 오류:', error)
          logout()
          return false
        }
      }
      return false
    }
    return true
  }

  // 닉네임 등록
  const registerNickname = async (nickname, introduction = '') => {
    try {
      const response = await fetch('http://localhost:8080/api/users/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nickname,
          introduction
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        currentUser.value = {
          nickname: result.userSession.nickname,
          userSession: result.userSession.userSession,
          introduction: result.userSession.introduction,
          isSuperAdmin: result.userSession.isSuperAdmin || false
        }
        
        // 세션 스토리지에 저장
        sessionStorage.setItem('userSession', JSON.stringify(currentUser.value))
        
        // 세션 갱신 시작
        startSessionRefresh()
        
        return { success: true, message: '닉네임이 등록되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('닉네임 등록 오류:', error)
      return { success: false, message: '닉네임 등록에 실패했습니다.' }
    }
  }

  // 세션 갱신
  const refreshSession = async () => {
    if (!currentUser.value) return { success: false, message: '사용자 정보가 없습니다.' }
    
    try {
      const response = await fetch('http://localhost:8080/api/users/refresh-session', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          nickname: currentUser.value.nickname,
          userSession: currentUser.value.userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        // 새로운 세션 정보로 업데이트
        currentUser.value.userSession = result.userSession.userSession
        currentUser.value.introduction = result.userSession.introduction
        
        // 세션 스토리지 업데이트
        sessionStorage.setItem('userSession', JSON.stringify(currentUser.value))
        
        return { success: true, message: '세션이 갱신되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('세션 갱신 오류:', error)
      return { success: false, message: '세션 갱신에 실패했습니다.' }
    }
  }

  // 사용자 프로필 조회
  const getUserProfile = async (userSession) => {
    try {
      const response = await fetch(`http://localhost:8080/api/users/profile?userSession=${userSession}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, profile: result.profile }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('프로필 조회 오류:', error)
      return { success: false, message: '프로필 정보를 불러올 수 없습니다.' }
    }
  }

  // 사용자 프로필 수정
  const updateUserProfile = async (userSession, introduction) => {
    try {
      const response = await fetch('http://localhost:8080/api/users/update-profile', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession,
          introduction
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '프로필이 수정되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('프로필 수정 오류:', error)
      return { success: false, message: '프로필 수정에 실패했습니다.' }
    }
  }

  // 사용자 초대 허용 설정 수정
  const updateAllowInvite = async (userSession, allowInvite) => {
    try {
      const response = await fetch('http://localhost:8080/api/users/update-allow-invite', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession,
          allowInvite
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '초대 허용 설정이 업데이트되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('초대 허용 설정 업데이트 오류:', error)
      return { success: false, message: '초대 허용 설정 업데이트에 실패했습니다.' }
    }
  }

  // 닉네임 삭제
  const deleteNickname = async (userSession) => {
    try {
      const response = await fetch('http://localhost:8080/api/users/delete-nickname', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          userSession
        }),
      })

      const result = await response.json()
      
      if (result.success) {
        return { success: true, message: '닉네임이 삭제되었습니다.' }
      } else {
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('닉네임 삭제 오류:', error)
      return { success: false, message: '닉네임 삭제에 실패했습니다.' }
    }
  }

  // 세션 갱신 시작
  const startSessionRefresh = () => {
    // 기존 인터벌 정리
    if (sessionInterval.value) {
      clearInterval(sessionInterval.value)
    }
    
    // 10분마다 세션 갱신
    sessionInterval.value = setInterval(async () => {
      const result = await refreshSession()
      if (!result.success) {
        ElMessage.error('세션 갱신에 실패했습니다. 다시 로그인해주세요.')
        logout()
        clearInterval(sessionInterval.value)
      }
    }, 600000) // 10분
  }

  // 로그아웃
  const logout = () => {
    currentUser.value = null
    sessionStorage.removeItem('userSession')
    
    if (sessionInterval.value) {
      clearInterval(sessionInterval.value)
      sessionInterval.value = null
    }
  }

  // 스토어 초기화 시 세션 체크
  const initialize = () => {
    checkSession()
  }

  return {
    currentUser,
    isLoggedIn,
    isSuperAdmin,
    checkSession,
    registerNickname,
    refreshSession,
    getUserProfile,
    updateUserProfile,
    updateAllowInvite,
    deleteNickname,
    startSessionRefresh,
    logout,
    initialize
  }
})
