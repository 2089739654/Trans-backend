<!-- components/UserCenter.vue -->
<template>
  <el-dropdown trigger="click">
    <!-- 头像展示 -->
    <div class="avatar-wrapper">
      <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
      <span class="username">测试用户</span>
    </div>
    
    <!-- 下拉菜单 -->
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="goProfile">
          <el-icon><User /></el-icon>个人中心
        </el-dropdown-item>
        <el-dropdown-item divided @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
    import { User, SwitchButton } from '@element-plus/icons-vue'
    import { useRouter } from 'vue-router'
    const router = useRouter()
    // 个人中心
    const goProfile = () => router.push('/personal')
    const handleLogout = () => {
    //   ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    //     confirmButtonText: '确定',
    //     cancelButtonText: '取消',
    //     type: 'warning'
    //   }).then(() => {
        //userStore.logout()
        router.replace('/login')
    //   })
    }

</script>

<style scoped>
/* 头像容器 */
.avatar-wrapper {
  display: flex;
  align-items: center;
  padding: 0 15px;
  height: 60px;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

/* 用户名样式 */
.username {
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

/* 深度修改下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  min-width: 120px;
  padding: 8px 20px;
  
  .el-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}

/* 分隔线样式 */
:deep(.el-dropdown-menu__item--divided) {
  margin-top: 6px;
  border-top: 1px solid var(--el-border-color-light);
}
</style>