<script setup lang="ts">
  const projectName = "辅助翻译工具";
  import { useRouter,useRoute } from 'vue-router'
  const router = useRouter()
  const route = useRoute()

  import UserCenter from '@/components/UserCenter.vue'
  import { ref } from 'vue'
  const isLogin = ref(true) // 临时登录状态

  const showLogin = () => {
    alert('你点击了登录按钮')
    router.push('/login')
  }

  const showRegister = () => {
    alert('你点击了注册按钮')
    router.push('/register')
  }
</script>

<template>
  <!-- 顶部导航栏 -->
  <nav v-if="route.path !='/login' && route.path !='/register' && route.path !='/file-manager' " class="header-nav">
    <!-- 左侧导航区 -->
    <div class="nav-left">
      <router-link to="/projects" class="nav-item" exact-active-class="active-link">项目管理</router-link>
      <router-link to="/memory" class="nav-item" exact-active-class="active-link">记忆库</router-link>
      <router-link to="/terms" class="nav-item" exact-active-class="active-link">术语库</router-link>
    </div>
    
    <!-- 中间项目名称 -->
    <div class="nav-center">
      {{ projectName }}
    </div>

      <!-- 右侧模块 -->
      <div class="nav-right">
        <UserCenter v-if="isLogin" />
        <div v-else class="auth-buttons">
          <el-button size="small" @click="showLogin">登录</el-button>
          <el-button type="primary" size="small" @click="showRegister">注册</el-button>
        </div>
      </div>

  </nav>

  <!-- 路由内容 -->
  <router-view :key="$route.fullPath" v-slot="{ Component }">
    <keep-alive>
      <transition name="fade" mode="out-in">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </transition>
    </keep-alive>
  </router-view>
</template>

<style lang="scss" scoped>
//路由
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/*1. 导航栏样式 */
.header-nav {
  position: fixed;
  top: 0;
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 2rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.nav-left {
  display: flex;
  gap: 2rem;
}

.nav-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-weight: bold;
}

.nav-right {
  margin-left: auto;
  display: flex;
  gap: 1rem;
}

.active-link {
  color: #42b983;
  border-bottom: 2px solid currentColor;
}

.nav-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  
  .auth-buttons {
    .el-button {
      padding: 8px 15px;
      border-radius: 4px;
    }
  }
}
</style>