import { defineStore } from 'pinia'
import { ref, computed, watch,provide, reactive} from 'vue'
// import Ws from 'ws'; // 注意：这是Node.js库，浏览器环境需使用兼容方案

// utils.ts
import { useRouteStore } from './route';
import { storeToRefs } from 'pinia';
import { ElNotification } from 'element-plus'; 
// 创建一个独立的 store 实例
const routeStore = useRouteStore();
const { currentPath } = storeToRefs(routeStore);

// 获取当前路径
console.log('当前路径:', currentPath.value);
const segments = currentPath.value.split('/');
const teamId = segments[2]; // 索引2对应路径中的第三个部分
console.log('提取的teamId:', teamId);

export const useDocumentStore = defineStore('document', () => {

  const editingSentenceId = ref('');

  interface UserInfo {
    id: number;
    userAccount: string;
    userAvatar?: string | null;
    userName: string;
  }

  // 更新当前编辑的句子ID
  const setEditingSentenceId = (id: string | null) => {
    editingSentenceId.value = id;
  };
  

  // 文档内容
  const content = ref('')
  
  // WebSocket连接
  const socket = ref<WebSocket | null>(null)
  
  // 连接状态
  const connectionStatus = ref<'connecting' | 'connected' | 'disconnected'>('disconnected')
  
 // 定义接口
interface TextData {
  position: number
  transText: string
}

// 创建响应式对象并初始化
const text = reactive<TextData>({
  position: 0,       // 默认值
  transText: ''      // 默认值
})

const userList= reactive<UserInfo[]>([])
  // 计算属性：连接状态文本
  const connectionStatusText = computed(() => {
    switch(connectionStatus.value) {
      case 'connecting': return '连接中...'
      case 'connected': return '已连接'
      case 'disconnected': return '已断开'
    }
  })
  
  // 计算属性：连接状态样式
  const connectionStatusClass = computed(() => {
    switch(connectionStatus.value) {
      case 'connecting': return 'text-yellow-500'
      case 'connected': return 'text-green-500'
      case 'disconnected': return 'text-red-500'
    }
  })
  

  // 初始化WebSocket连接
  const initializeWebSocket = () => {
    if (socket.value) return
    console.log("socket不存在")
    try {
      connectionStatus.value = 'connecting'
      console.log('当前路径:', currentPath.value);
      const segments = currentPath.value.split('/');
      const teamId = segments[2]; // 索引2对应路径中的第三个部分
      console.log('提取的teamId:', teamId,segments);
      const token = localStorage.getItem('token');
      if (!token) {
        ElMessage.error('请先登录');
        return;
      }
      // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4
      socket.value = new WebSocket(`ws://26.143.62.131:8080/socket/ws?token=${token}&groupId=${teamId}`)
      socket.value.onopen = () => {
        connectionStatus.value = 'connected'
        console.log('WebSocket连接已建立，状态:', socket.value?.readyState)
      }
      //$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$44
      socket.value.onmessage = (event: any) => {
        console.log('收到消息:', event.data)
        const data = JSON.parse(event.data)
        console.log("onmessage后1：",data,data.type,data.position)
        if(data.type=='进入编辑') {
          console.log('gogo')
          handleEditEntry(data.user as UserInfo);
        }else{
        text.position = data.position
        text.transText = data.transText
        console.log('$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$',text)
        //backText()
        showInformation(data.user as UserInfo)
        }
      }
      
      socket.value.onclose = () => {
        connectionStatus.value = 'disconnected'
        socket.value = null
        console.log('WebSocket连接已关闭')
        
      // 尝试重连
        setTimeout(() => {
          initializeWebSocket()
        }, 3000)
      }
      
      socket.value.onerror = (error: any) => {
        console.error('WebSocket错误:', error)
      }
    } catch (error) {
      console.error('初始化WebSocket失败:', error)
      connectionStatus.value = 'disconnected'
    }
  }
  
  const showInformation  = (user: UserInfo) => {
      // 检查是否存在相同 ID 的用户
      //const exists = userList.some(item => item.id === user.id)
      console.log('你好')
      // 清空列表，仅保留最新用户
      userList.splice(0, userList.length)

      userList.push(user)
      console.log('新增用户:', user)

      console.log('用户列表',userList)
        // 使用Element Plus的Notification组件显示用户信息
    
    // ElNotification({
    //   title: '刚才编辑用户信息',
    //   message: `
    //     <div>用户ID: ${user.id}</div>
    //     <div>用户名: ${user.userName}</div>
    //     <div>账号: ${user.userAccount}</div>
    //   `,
    //   type: 'info',
    //   duration: 1000, // 1秒后自动关闭
    //   dangerouslyUseHTMLString: true // 允许使用HTML
    // });
  };

  // 处理"进入编辑"事件
  const handleEditEntry = (user: UserInfo) => {
      console.log('你好')
      // 清空列表，仅保留最新用户
      userList.splice(0, userList.length)
      userList.push(user)
      console.log('用户列表',userList)
    // 使用Element Plus的Notification组件显示用户信息
    ElNotification({
      title: '用户进入编辑状态',
      message: `
        <div>用户ID: ${user.id}</div>
        <div>用户名: ${user.userName}</div>
        <div>账号: ${user.userAccount}</div>
      `,
      type: 'info',
      duration: 2000, // 2秒后自动关闭
      dangerouslyUseHTMLString: true // 允许使用HTML
    });
  };
  

  // 设置文档内容
  const setContent = (newContent: string) => {
    content.value = newContent
  }
  
  const getTimestamp = () => Date.now(); // 毫秒级时间戳
  // 发送内容到服务器￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥￥
  const sendContent = (message: {
    id: string;
    transText: string;
    position: number;
    fileId: string;
    originalText: string;
  }) => {
    if (!socket.value || connectionStatus.value !== 'connected'){
      console.warn('WebSocket 未连接或状态异常:', socket.value?.readyState);
     return
    }
    const time1 = getTimestamp()
    console.log("要发送内容到服务器",message,time1);
    console.log('1313',socket.value)
    try {
      socket.value.send(JSON.stringify({
        fileId: message.fileId,
        transId: message.id,
        position: message.position,
        transText: message.transText,
        timestamp: time1,
        sourceText: message.originalText,
        // clientId: typingUsers.value
      }))
    } catch (error) {
      console.error('WebSocket 发送失败:', error);
    }
  }
  
  // 开始同步
  // const startSyncing = () => {
  //   if (isSyncing.value || !socket.value) return
  //   console.log("开始同步")
  //   isSyncing.value = true
    
  //   // 立即同步一次
  //   // sendContent()
    
  //   // 设置定时同步
  //   // syncTimer = setInterval(() => {
  //   //   sendContent()
  //   //   console.log("定时同步")
  //   // }, SYNC_INTERVAL)
  // }
  
  // 停止同步
  // const stopSyncing = () => {
  //   if (!isSyncing.value) return
  //   console.log("停止同步")
  //   isSyncing.value = false
  //   if (syncTimer) clearInterval(syncTimer)
  //   syncTimer = null
  // }
  
  // 发送用户正在输入状态
  // const sendUserTyping = (isTyping: boolean) => {
  //   if (!socket.value || connectionStatus.value !== 'connected') return
  //   console.log("发送用户正在输入状态")
  //   socket.value.send(JSON.stringify({
  //     type: 'typing',
  //     isTyping,
  //     clientId: typingUsers.value
  //   }))
  // }
  
  // 更新正在输入的用户列表
  // const updateTypingUsers = (user: string, isTyping: boolean) => {
  //   if (isTyping && !typingUsers.value.includes(user)) {
  //     typingUsers.value = [...typingUsers.value, user]
  //   } else if (!isTyping && typingUsers.value.includes(user)) {
  //     typingUsers.value = typingUsers.value.filter(u => u !== user)
  //   }
  // }
  
  // 添加编辑日志
  // const addEditLog = (user: string) => {
  //   const now = new Date()
  //   const timeString = now.toLocaleTimeString()
    
  //   editLogs.value = [
  //     ...editLogs.value,
  //     { user, time: timeString }
  //   ]
    
  //   // 限制日志数量为10条
  //   if (editLogs.value.length > 10) {
  //     editLogs.value = editLogs.value.slice(-10)
  //   }
  // }
  
  // 断开连接
  const disconnect = () => {
    // stopSyncing()
    if (socket.value) {
      socket.value.close()
      socket.value = null
    }
    connectionStatus.value = 'disconnected'
  }
  
  // 监听组件销毁
  // watch(() => connectionStatus.value, (newStatus: any) => {
  //   if (newStatus !== 'connected') {
  //     stopSyncing()
  //   }
  // })
 
  return {
    setEditingSentenceId,
    text,
    userList,
    connectionStatusText,
    connectionStatusClass,
    // isSyncing,
    initializeWebSocket,
    sendContent,
    setContent,
    // startSyncing,
    // sendUserTyping,
    disconnect
  }
})    