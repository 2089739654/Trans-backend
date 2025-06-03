<script>
    // import { useRouter } from 'vue-router'
    // const router = useRouter()
</script>

<template>
    <nav class="header-nav">
        <!-- 左侧导航区 -->
        <div class="nav-left">
          <router-link to="/projects" class="nav-item">项目管理</router-link>
          <router-link to="/memory" class="nav-item">记忆库</router-link>
          <router-link to="/terms" class="nav-item">术语库</router-link>
        </div>
        
        <!-- 中间项目名称 -->
        <div class="nav-center">
          {{ projectName }}
        </div>
    
        <!-- 右侧登录注册 -->
        <div class="nav-right">
          <button @click="showLogin">登录</button>
          <button @click="showRegister">注册</button>
        </div>
      </nav>
</template>

<style>
.header-nav {
    position: fixed;
    top: 0;
    width: 100%;
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 2rem;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
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
</style>