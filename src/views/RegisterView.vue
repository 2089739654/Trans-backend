<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

//import { registerAPI } from '@/api/auth'

const router = useRouter()
const formRef = ref<any>()
const loading = ref(false)

// 注册表单
const form = reactive({
  userAccount:'',
  userName: '',
  userPassword: '',
  confirmPassword: ''
})

// 错误提示规则
const rules = reactive({
  userAccount: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 4, max: 16, message: '长度4-16个字符', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 4, max: 16, message: '长度4-16个字符', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { 
      pattern: /^(?=.*[A-Za-z])(?=.*\d).{8,}$/,
      message: '需包含字母和数字且至少8位'
    }
  ],
  confirmPassword: [
    {
      validator: (rule: any, value: string,callback: any) => {
        if (value !== form.userPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur'
    }
  ]
})

// 创建axios实例
const service = axios.create({
  baseURL: 'http://26.143.62.131:8080',
  timeout: 5000
})

// 注册接口返回类型
interface RegisterResponse {
  code: number
  data: {
    createTime: string
    id: number
    token: string
    updateTime: string
    userAccount: string
    userAvatar: string
    userName: string
    userPassword: string
    userRole: string
  }
  message: string
}

// 注册
const handleRegister = async () => {
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        // 准备请求数据
        const requestData = {
          userAccount: form.userAccount,
          userPassword: form.userPassword,
          userName: form.userName
        }
        // 发送注册请求
        const response = await service.post<RegisterResponse>('http://26.143.62.131:8080/admin/register', requestData)
        // 处理响应
        const { code, data, message } = response.data
        if (code === 200) {
          ElMessage.success(message || '注册成功')
          router.push('/login')
        } else {
          ElMessage.error(message || '注册失败')
        }
      } catch (error: any) {
        console.error('注册错误:', error)
        ElMessage.error(error.response?.data?.message || '注册请求失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 已有账号,立即登录(跳转至登录界面)
const goToLogin = () => router.push('/login')
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules"
        @keyup.enter="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.userName"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="form.userAccount"
            placeholder="请输入账户名"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.userPassword"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading"
            @click="handleRegister"
            block
          >
            立即注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          <span>已有账号？
            <el-link type="primary" @click="goToLogin">立即登录</el-link>
          </span>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #4f46e5 0%, #a855f7 100%);
}

.register-card {
  width: 450px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.register-title {
  text-align: center;
  margin-bottom: 24px;
  color: #2d3748;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #718096;
}
</style>