<template>
  <div class="admin-rooms">
    <div class="admin-header">
      <h1>관리자 - 채팅방 관리</h1>
      <div class="header-actions">
        <el-button @click="goBack" type="info" plain>
          <el-icon><ArrowLeft /></el-icon>
          뒤로 가기
        </el-button>
        <el-button @click="refreshRooms" type="primary" :loading="loading">
          <el-icon><Refresh /></el-icon>
          새로고침
        </el-button>
      </div>
    </div>

    <div class="admin-content" v-loading="loading">
      <!-- 채팅방 통계 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalRooms }}</div>
              <div class="stat-label">전체 채팅방</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.activeRooms }}</div>
              <div class="stat-label">활성 채팅방</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.emptyRooms }}</div>
              <div class="stat-label">빈 채팅방</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 채팅방 목록 -->
      <el-card class="rooms-card">
        <template #header>
          <div class="card-header">
            <span>채팅방 목록</span>
            <div class="header-filters">
              <el-select v-model="filterType" placeholder="타입별 필터" clearable>
                <el-option label="전체" value="" />
                <el-option label="오픈 채팅방" value="OPEN" />
                <el-option label="개인 채팅방" value="PRIVATE" />
              </el-select>
              <el-select v-model="filterStatus" placeholder="상태별 필터" clearable>
                <el-option label="전체" value="" />
                <el-option label="활성" value="active" />
                <el-option label="비활성" value="inactive" />
              </el-select>
            </div>
          </div>
        </template>

        <el-table 
          :data="filteredRooms" 
          style="width: 100%"
          v-loading="tableLoading"
        >
          <el-table-column prop="roomId" label="방 ID" width="120" />
          <el-table-column prop="name" label="방 이름" min-width="150" />
          <el-table-column prop="roomType" label="타입" width="100">
            <template #default="scope">
              <el-tag 
                :type="scope.row.roomType === 'OPEN' ? 'success' : 'warning'"
                size="small"
              >
                {{ getRoomTypeText(scope.row.roomType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="creatorNickname" label="생성자" width="120" />
          <el-table-column prop="participantCount" label="참여자 수" width="100" align="center" />
          <el-table-column prop="lastMessageTime" label="마지막 메시지" width="150">
            <template #default="scope">
              {{ formatDateTime(scope.row.lastMessageTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="regDate" label="생성일" width="120">
            <template #default="scope">
              {{ formatDate(scope.row.regDate) }}
            </template>
          </el-table-column>
          <el-table-column label="상태" width="100" align="center">
            <template #default="scope">
              <el-tag 
                :type="scope.row.isActive ? 'success' : 'danger'"
                size="small"
              >
                {{ scope.row.isActive ? '활성' : '비활성' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="작업" width="200" align="center">
            <template #default="scope">
              <el-button 
                size="small" 
                @click="viewRoom(scope.row)"
                type="primary"
                plain
              >
                보기
              </el-button>
              <el-button 
                size="small" 
                @click="deleteRoom(scope.row)"
                type="danger"
                :disabled="scope.row.participantCount > 0"
              >
                삭제
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 자동 정리 설정 -->
      <el-card class="cleanup-card">
        <template #header>
          <div class="card-header">
            <span>자동 정리 설정</span>
          </div>
        </template>
        
        <div class="cleanup-content">
          <p class="cleanup-info">
            <el-icon><InfoFilled /></el-icon>
            1일 이상 메시지가 없는 채팅방의 참여자를 자동으로 퇴장시키고, 
            참여자가 없는 채팅방은 관리자만 볼 수 있습니다.
          </p>
          
          <el-button 
            type="warning" 
            @click="runCleanup" 
            :loading="cleanupLoading"
          >
            수동 정리 실행
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Refresh, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const loading = ref(false)
const tableLoading = ref(false)
const cleanupLoading = ref(false)
const filterType = ref('')
const filterStatus = ref('')

const rooms = ref([])
const stats = ref({
  totalRooms: 0,
  activeRooms: 0,
  emptyRooms: 0
})

onMounted(async () => {
  // 관리자 권한 체크
  if (!userStore.checkSession() || !userStore.currentUser?.isSuperAdmin) {
    ElMessage.error('관리자 권한이 필요합니다.')
    router.push('/rooms')
    return
  }
  
  await loadAdminRooms()
})

const loadAdminRooms = async () => {
  loading.value = true
  tableLoading.value = true
  
  try {
    const result = await chatStore.getAdminRoomList(userStore.currentUser.userSession)
    if (result.success) {
      rooms.value = result.rooms
      updateStats()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('채팅방 목록을 불러올 수 없습니다.')
  } finally {
    loading.value = false
    tableLoading.value = false
  }
}

const updateStats = () => {
  stats.value.totalRooms = rooms.value.length
  stats.value.activeRooms = rooms.value.filter(room => room.isActive).length
  stats.value.emptyRooms = rooms.value.filter(room => room.participantCount === 0).length
}

const filteredRooms = computed(() => {
  let filtered = rooms.value
  
  if (filterType.value) {
    filtered = filtered.filter(room => room.roomType === filterType.value)
  }
  
  if (filterStatus.value === 'active') {
    filtered = filtered.filter(room => room.isActive)
  } else if (filterStatus.value === 'inactive') {
    filtered = filtered.filter(room => !room.isActive)
  }
  
  return filtered
})

const refreshRooms = async () => {
  await loadAdminRooms()
}

const viewRoom = (room) => {
  router.push(`/rooms/${room.roomId}`)
}

const deleteRoom = async (room) => {
  if (room.participantCount > 0) {
    ElMessage.warning('참여자가 있는 채팅방은 삭제할 수 없습니다.')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `정말로 채팅방 "${room.name}"을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`,
      '채팅방 삭제',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'warning',
        dangerouslyUseHTMLString: true,
      }
    )
    
    const result = await chatStore.deleteChatRoom(room.roomId, userStore.currentUser.userSession)
    if (result.success) {
      ElMessage.success('채팅방이 삭제되었습니다.')
      await loadAdminRooms()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('채팅방 삭제에 실패했습니다.')
    }
  }
}

const runCleanup = async () => {
  try {
    await ElMessageBox.confirm(
      '1일 이상 메시지가 없는 채팅방의 참여자를 자동으로 퇴장시키시겠습니까?',
      '자동 정리 실행',
      {
        confirmButtonText: '실행',
        cancelButtonText: '취소',
        type: 'warning',
      }
    )
    
    cleanupLoading.value = true
    
    const result = await chatStore.runCleanup(userStore.currentUser.userSession)
    if (result.success) {
      ElMessage.success(`정리가 완료되었습니다. ${result.cleanedCount}명의 사용자가 퇴장되었습니다.`)
      await loadAdminRooms()
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('자동 정리에 실패했습니다.')
    }
  } finally {
    cleanupLoading.value = false
  }
}

const getRoomTypeText = (type) => {
  if (type === 'OPEN') return '오픈'
  if (type === 'PRIVATE') return '개인'
  return '알 수 없음'
}

const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('ko-KR')
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR')
}

const goBack = () => {
  router.push('/rooms')
}
</script>

<style scoped>
.admin-rooms {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.admin-header h1 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.admin-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 8px;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 2.5em;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  color: #606266;
  font-size: 1.1em;
}

.rooms-card,
.cleanup-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.header-filters {
  display: flex;
  gap: 10px;
}

.cleanup-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cleanup-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  margin: 0;
}

.cleanup-info .el-icon {
  color: #409eff;
}

@media (max-width: 768px) {
  .admin-rooms {
    padding: 15px;
  }
  
  .admin-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .admin-header h1 {
    text-align: center;
  }
  
  .header-actions {
    justify-content: center;
  }
  
  .card-header {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }
  
  .header-filters {
    justify-content: center;
  }
}
</style>
