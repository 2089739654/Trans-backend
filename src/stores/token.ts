// stores/token.js
import { defineStore } from 'pinia';
import { watch } from 'vue';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    currentUser: null,
  }),
  
  getters: {
    token: (state) => {
      if (!state.currentUser) return null;
      return localStorage.getItem(`token_${state.currentUser}`);
    },
    
    isAuthenticated: (state) => !!state.currentUser && !!state.token,
  },
  
  actions: {
    login(username, token) {
      this.currentUser = username;
      localStorage.setItem(`token_${username}`, token);
    },
    
    logout() {
      if (this.currentUser) {
        localStorage.removeItem(`token_${this.currentUser}`);
        this.currentUser = null;
      }
    },
    
    // 从localStorage恢复状态
    init() {
      const tokenKey = Object.keys(localStorage).find(key => 
        key.startsWith('token_')
      );
      
      if (tokenKey) {
        const username = tokenKey.split('_')[1];
        this.currentUser = username;
        console.log('已恢复用户状态:', username);
      } else {
        this.currentUser = null;
      }
    },
    
    // 验证token有效性（可选增强功能）
    validateToken() {
      if (!this.currentUser) return false;
      
      const token = this.token;
      if (!token) return false;
      
      // 这里可以添加JWT验证逻辑
      // 例如检查token过期时间
      return true;
    }
  }
});

// 自动初始化函数
export function setupAuthStore() {
  const store = useAuthStore();
  
  // 立即初始化
  store.init();
  
  // 监听currentUser变化，确保token一致性
  watch(() => store.currentUser, (newUser, oldUser) => {
    if (oldUser && !newUser) {
      // 用户登出，清除旧token
      localStorage.removeItem(`token_${oldUser}`);
    }
  });
  
  return store;
}