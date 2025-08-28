import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import Welcome from '../views/Welcome.vue'
import NicknameRegister from '../views/NicknameRegister.vue'
import NicknameLogin from '../views/NicknameLogin.vue'
import ChatRooms from '../views/ChatRooms.vue'
import ChatRoom from '../views/ChatRoom.vue'
import UserProfile from '../views/UserProfile.vue'
import AdminRooms from '../views/AdminRooms.vue'

const routes = [
  {
    path: '/',
    name: 'Welcome',
    component: Welcome,
    meta: { requiresAuth: false }
  },
  {
    path: '/nickname-register',
    name: 'NicknameRegister',
    component: NicknameRegister,
    meta: { requiresAuth: false }
  },
  {
    path: '/nickname-login',
    name: 'NicknameLogin',
    component: NicknameLogin,
    meta: { requiresAuth: false }
  },
  {
    path: '/rooms',
    name: 'ChatRooms',
    component: ChatRooms,
    meta: { requiresAuth: true }
  },
  {
    path: '/room',
    name: 'ChatRoom',
    component: ChatRoom,
    meta: { requiresAuth: true }
  },
  {
    path: '/user-profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/rooms',
    name: 'AdminRooms',
    component: AdminRooms,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 네비게이션 가드
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  
  // 세션 체크
  if (to.meta.requiresAuth && !userStore.checkSession()) {
    next('/nickname-register')
    return
  }
  
  // 관리자 권한 체크
  if (to.meta.requiresAdmin && !userStore.currentUser?.isSuperAdmin) {
    next('/rooms')
    return
  }
  
  // 이미 로그인된 사용자가 인증 페이지에 접근하는 경우
  if ((to.name === 'NicknameRegister' || to.name === 'NicknameLogin') && userStore.checkSession()) {
    next('/rooms')
    return
  }
  
  next()
})

export default router
