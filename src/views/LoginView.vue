<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios';
//import type { FormInstance } from 'element-plus'; // 导入FormInstance类型


const formRef = ref<null>(null); // 显式声明ref的类型
const router = useRouter()

const loading = ref(false)

// 登录信息
const form = reactive<{
  userAccount: string;
  password: string;
}>({
  userAccount: '',
  password: ''
});

// 错误提示规则
const rules = reactive({
  userAccount: [
    { required: true, message: '用户名不能为空', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
})

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000
});
// 定义接口类型
interface LoginResponse {
  code: number;
  data: {
    createTime: string;
    id: string;
    token: string;
    updateTime: string;
    userAccount: string;
    userAvatar: string;
    userName: string;
    userPassword: string;
    userRole: string;
  };
  message: string;
}


// 登陆按钮
const handleLogin = async () => {
  await formRef.value?.validate((valid: boolean)=> {
    if (valid) {
      loading.value = true
      // 发送登录请求
      service.post<LoginResponse>('http://26.143.62.131:8080/admin/login', {
        userAccount: form.userAccount, // 根据实际表单字段调整
        userPassword: form.userPassword   // 根据实际表单字段调整
      })
      .then((response: axios.AxiosResponse<LoginResponse>) => {
        // 深拷贝响应数据并处理大整数
        //response.data = parseJSONWithBigInt(JSON.stringify(response.data));
        const { code, data, message} = response.data;
        const id = data.id;
        console.log('id',id);
        if (code === 200) { // 成功状态码
          // 提取并存储用户信息
          const { token, userName, userAccount } = data;
          localStorage.setItem('token', token);
          localStorage.setItem('user', JSON.stringify({
            userName,
            userAccount,
            id
          }));
          console.log('kkk',localStorage)
          // 设置axios全局请求头
          axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
          
          router.push('/projects');
          ElMessage.success('登录成功');
        } else {
          ElMessage.error(message || '登录失败，请重试');
          console.error('业务错误:', message);
        }
      })
      .catch((error: any) => {
        let errorMessage = '登录失败，请检查网络连接';
        if (error.response) {
          errorMessage = error.response.data.message || errorMessage;
        }
        ElMessage.error(errorMessage);
        console.error('请求错误:', error);
      })
      .finally(() => {
        loading.value = false;
      });
    }
    // 模拟API请求
    //   setTimeout(() => {
    //     localStorage.setItem('token', 'demo-token')
    //     router.push('/projects')
    //     ElMessage.success('登录成功')
    //     console.log(form)
    //     loading.value = false
    //   }, 1000)
  })
}
// 注册按钮
const handleRegister = ()=>{
    //alert('你点击了注册按钮')
    router.push('/register')
}
</script>

<template> 
    <div class="login-container">
        <el-card class="login-card">
            <h2 class="login-title">用户登录</h2>
            <el-form ref="formRef" :model="form" :rules="rules">
                <el-form-item prop="username">
                <el-input
                    v-model="form.userAccount"
                    placeholder="请输入用户名"
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

                <el-form-item>
                    <el-button 
                        type="primary" 
                        :loading="loading"
                        @click="handleLogin"
                        block
                    >
                        登录
                    </el-button>
                    <el-button 
                        type="primary" 
                        @click="handleRegister"
                        class="register"
                    >
                        注册
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<style scoped>
    .login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 100vh;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .login-card {
        width: 400px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .login-title {
        text-align: center;
        margin-bottom: 24px;
        color: #2d3748;
    }

    .el-form-item {
        display: flex;
        justify-content: space-between;
        gap: 16px; /* 按钮间距控制 */
    }

    .register {
        margin-left: auto; /* 将注册按钮推到最右侧 */
    }
</style>