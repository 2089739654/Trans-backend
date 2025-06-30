<script setup lang="ts">
  const projectName = "辅助翻译工具";
  import { useRoute } from 'vue-router'

  const route = useRoute()

  import UserCenter from '@/components/UserCenter.vue'
  import UserList from '@/components/UserList.vue'

</script>

<template>
  <!-- 顶部导航栏 -->
  <nav v-if="route.path !='/login' && route.path !='/register' && route.path !='/file-manager' " class="header-nav">
    <UserList/>

    <!-- 中间项目名称 -->
    <div class="nav-center">
      {{ projectName }}
    </div>

    <!-- 右侧模块 -->
    <div class="nav-right">
      <UserCenter/>
    </div>

  </nav>

  <!-- 临时内容 -->
  <div v-if="route.path == '/projects'" class="temporary-container">
    请选择用户项目组
  </div>
  
  <!-- 路由内容 -->
  <router-view :key="$route.fullPath" v-slot="{ Component }">
    <keep-alive>
      <transition name="fade" mode="out-in">
        <component :is="Component" />
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