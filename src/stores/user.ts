interface UserInfo {
  userId: string | number;
  userName: string;
  accountName: string;
}

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 定义用户状态
  const userInfo = ref<UserInfo | null>(null)

  // 获取用户信息的计算属性
  const getUserInfo = computed(() => userInfo.value)

  // 设置用户信息的方法
  const setUserInfo = (user: UserInfo) => {
    userInfo.value = user
  }

  // 清除用户信息的方法
  const clearUserInfo = () => {
    userInfo.value = null
  }

  return {
    userInfo,
    getUserInfo,
    setUserInfo,
    clearUserInfo
  }
})