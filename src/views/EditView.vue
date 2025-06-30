<script setup lang="ts">
import { ref ,onMounted } from "vue";

interface memoryItem {
  sourceText: string
  translatedText: string
}

// 当前页的句子
const pageSentences = ref<{
  id: string
  originalText: string
  transText: string
  version: number
  position: number
  isEditing: boolean
  memoryLists: memoryItem[]
}[]>([])

import { watch , inject } from "vue";
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const id = route.params.fileId;
console.log('id:',id);

// 分页相关状态
const currentPage = ref(1)      // 当前页码
const pageSize = ref(10)        // 每页显示数量
const totalSentences = ref(0)   // 总句子数

const currentIndex = ref(0)     // 当前选中的句子索引
const inputText = ref('')       // 输入框内容

const editingIndex = ref(-1)    // 当前正在编辑的句子索引
const contentRef = ref(null)    // 内容区域引用

import axios from 'axios'
import { ElMessage } from "element-plus";


// 获取当前页数
const fetchTotalPages = async()=>{
  console.log("请求页数")
      // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    console.log('token:',token);
    // 如果没有 token，提示用户重新登录
    if (!token) {
      ElMessage.error('请先登录');
      router.push('/login');
      return;
    }
    // 设置请求头
    const config = {
      headers: {
        'token': token
      }
    };
    // 发送POST请求，包含JSON请求体
    const response = await axios.post(`http://26.143.62.131:8080/file/getTeamTransTextCount?fileId=${id}`, 
      null,
      config,
    )
    console.log("返回页数",response.data.data)
    totalSentences.value = response.data.data
}

// 获取当前页的句子
const fetchPageData = async (page: number = 1, size: number = 10) => {
  try {
    // 构建JSON格式请求体
    const requestBody = {
      fileId: id,
      currentPage: page,
      size: size
    }
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    console.log('token:',token);
    // 如果没有 token，提示用户重新登录
    if (!token) {
      ElMessage.error('请先登录');
      router.push('/login');
      return;
    }
    // 设置请求头
    const config = {
      headers: {
        'token': token
      }
    };
    // 发送POST请求，包含JSON请求体
    const response = await axios.post('http://26.143.62.131:8080/file/getTeamTransText', 
      requestBody,
      config,
    )
    console.log("返回",response.data)
    if (response.data.code != 200) throw new Error(`HTTP错误: ${response.status}`)
    
    const jsonData = await response.data.data
    console.log("hhhh",jsonData);
    
    // 更新当前页数据
    pageSentences.value = jsonData.map((item: any) => ({
      id: item.id,
      originalText: item.sourceText,
      transText: item.translatedText,
      version:item.version,
      position:item.position,
      isEditing: false,
      memoryLists: null,
    }))
    console.log("sssssimopnt:",pageSentences.value);
    // 如果当前页没有数据且不是第一页，尝试加载前一页
    if (pageSentences.value.length === 0 && page > 1) {
      currentPage.value = page - 1
      await fetchPageData(page - 1, size)
    } else {
      // 重置当前句子索引
      currentIndex.value = 0
      inputText.value = pageSentences.value[0]?.finalText || ''
    }
    
    console.log(`成功加载第${page}页数据，共${pageSentences.value.length}条`)
  } catch (error) {
    console.error("分页加载失败:", error)
  }
}

// 监听路由参数变化，重新加载数据
watch(
  () => route.params.fileId,
  async (newId: string) => {
    console.log('文件ID变更，重新加载数据:', newId)
    currentPage.value = 1 // 重置到第一页
    await fetchPageData()
  },
  { immediate: true }
)

// 监听页码变化，加载对应页数据
watch(currentPage, async (newPage:any) => {
  // 关闭当前编辑状态
  editingIndex.value = -1
  await fetchPageData(newPage, pageSize.value)
})

// 监听每页数量变化，重新加载数据
watch(pageSize, async (newSize:any) => {
  // 关闭当前编辑状态
  editingIndex.value = -1
  await fetchPageData(currentPage.value, newSize)
})


// 处理分页大小变化
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
}

// 处理页码变化
const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
}

// 获取全局索引（相对于整个文档）
const getGlobalIndex = (pageIndex: number) => {
  return (currentPage.value - 1) * pageSize.value + pageIndex
}

// 导出全文
const exportAllPages = async () => {
  try {
    let allContent = ''
    
    // 计算总页数（假设每页固定数量的句子）
    const sentencesPerPage = pageSize.value || 10;
    const totalPages = Math.ceil(pageSentences.value.length / sentencesPerPage);
    
    for (let page = 1; page <= totalPages; page++) {
      // 获取当前页的句子
      const startIdx = (page - 1) * sentencesPerPage;
      const endIdx = Math.min(startIdx + sentencesPerPage, pageSentences.value.length);
      const pageItems = pageSentences.value.slice(startIdx, endIdx);
      
      const pageContent = pageItems.map((item:any, index:any) => {
        const globalIndex = startIdx + index + 1;
        const original = item.originalText;
        const translation = item.transText || '待翻译';
        return `${globalIndex}. ${original}\n   → ${translation}`;
      }).join('\n\n');
      
      allContent += `\n\n===== 第${page}页 =====\n\n` + pageContent;
    }
    
    downloadFile(`translation_full.txt`, allContent.trim());
  } catch (error) {
    console.error("导出全文失败:", error);
  }
};

// 下载文件辅助函数
const downloadFile = (filename: string, content: string) => {
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

// 批量提交
const uploadTranslation = async () => {
  //const sentence = pageSentences.value[index]
  //console.log('替换的：',tempTranslation.value,'替换的：',sentence)
  console.log('批量提交')
  // 从 localStorage 获取 token
  const token = localStorage.getItem('token')
  console.log('token:',token);
  // 如果没有 token，提示用户重新登录
  if (!token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  // 设置请求头
  const config = {
    headers: {
      'token': token
    }
  };

  try {
    // 构建请求体
    const uploadedTranslation = ref([]);
    console.log('fason11g:',pageSentences.value)
    uploadedTranslation.value = pageSentences.value.map((item: any) => ({
      id: item.id,
      sourceText: item.originalText,
      translatedText: item.transText
    }))

    const requestBody = {
      fileId: id, // 文件ID
      list: uploadedTranslation.value
    }
    console.log('fasong:',requestBody)
    
    // 发送请求到后端
    const response = await axios.post(`http://26.143.62.131:8080/file/saveTransText`, 
      requestBody,
      config
    )
    console.log(response.data)
    if(response.data.code == 200){
      ElMessage.success("提交成功！")
    }
    // 重新请求数据
    await fetchPageData();
    console.log("返回结果：",response)   
  } catch (error) {
    console.error("保存翻译失败:", error)
  }
}

onMounted(async () => {
  await fetchTotalPages()
  await fetchPageData()
})

import { onUnmounted, computed } from 'vue'
import { useDocumentStore } from '../stores/document.ts'

const documentStore = useDocumentStore()

// 使用store中的状态
const { 
  initializeWebSocket,
  disconnect,
  text,
  userList,
  //backText
} = documentStore;

// const documentContent = computed({
//   get: () => documentStore.content,
//   set: (value: string) => documentStore.setContent(value)
// })

// 1. 连接状态文本（如："连接中..."、"已连接"、"已断开"）
const connectionStatusText = computed(() => documentStore.connectionStatusText)

// const text = computed(() => documentStore.text)
// 监听对象属性变化
watch(
  () => [text.position, text.transText], // 正确访问：text.position
  ([newPosition, newTransText]) => {
    console.log('属性变化:', newPosition, newTransText)
    if (newPosition !== undefined) {
      pageSentences.value[newPosition-1].transText = newTransText
    }
  }
  // 不需要 deep: true，因为我们监听的是具体属性，而不是整个对象
)

// 监听用户列表变化
watch(
  () => userList,
  (newList, oldList) => {
    console.log('用户列表55555变化:', newList)
    // 可以在这里执行其他操作，比如更新页面
  },
  { deep: true } // 必须设置为 true，才能监听数组内部元素的变化
)

// 2. 连接状态的 CSS 类（如："text-yellow-500"、"text-green-500"、"text-red-500"）
//const connectionStatusClass = computed(() => documentStore.connectionStatusClass)

// 3. 文档最后更新时间
//const lastUpdated = computed(() => documentStore.lastUpdated)

// 4. 正在编辑的用户列表
//const typingUsers = computed(() => documentStore.typingUsers)

// 5. 编辑日志
//const editLogs = computed(() => documentStore.editLogs)

// 开始编辑某个句子
// const startEditing = (sentenceId: string) => {
//   // 如果已经在编辑同一个句子，则忽略
//   if (editingSentenceId.value === sentenceId) return;
  
//   // 停止之前的输入状态
//   if (editingSentenceId.value) {
//     sendUserTyping(false);
//   }
  
//   // 设置新的编辑句子
//   setEditingSentenceId(sentenceId);
  
//   // 发送正在输入状态
//   sendUserTyping(true);
// };

let typingTimer: NodeJS.Timeout | null = null;
//const TYPING_INTERVAL = 3000; // 3秒

const handleInput = (sentenceId: string) => {
  // 清除之前的定时器
  // if (typingTimer) clearTimeout(typingTimer);
  
  // 发送正在输入状态
  // if (editingSentenceId.value) {
  //   sendUserTyping(true);
  // }
  
  // 设置新的定时器，3秒后发送停止输入状态
  // typingTimer = setTimeout(() => {
  //   if (editingSentenceId.value == sentenceId) {
  //     sendUserTyping(false);
  //   }
  //   typingTimer = null;
  // }, TYPING_INTERVAL);

  // 找到当前编辑的句子
  const currentSentence = pageSentences.value.find(item => item.id === sentenceId);
  console.log('currentSentence',currentSentence)
  if (currentSentence) {
    // 发送消息到服务端
    const message = {
      id: currentSentence.id,
      transText: currentSentence.transText,
      position: currentSentence.position,
      fileId: id,
      originalText: currentSentence.originalText
    };
    console.log('message45',message,pageSentences.value[currentSentence.position-1].transText)
    documentStore.sendContent(message);
    pageSentences.value[currentSentence.position-1].transText = currentSentence.transText
    console.log("结束后：",pageSentences.value[currentSentence.position-1])
  }
};

console.log()

onMounted(() => {
  initializeWebSocket();
});

onUnmounted(() => {
  disconnect();
  if (typingTimer) clearTimeout(typingTimer);
});

</script>

<template>
  <!-- 3.中间编辑区 -->
  <div class="editor-container1">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <!-- <div class="title">文档翻译</div> -->
      
      <div class="pagination-controls">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 30]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalSentences"
        ></el-pagination>
      </div>
      
      <div class="export-btn">
        <el-button type="primary" @click="uploadTranslation">
          <el-icon><Check /></el-icon>批量提交
        </el-button>
        <el-button type="primary" @click="exportAllPages">
          <el-icon><Download /></el-icon>导出全文
        </el-button>
      </div>
    </div>
    
    <!-- 翻译内容区域 -->
    <div class="translation-content" ref="contentRef">
      <div 
        v-for="(item, index) in pageSentences" 
        :key="item.id"
        class="translation-pair"
        :class="{ 'active-pair': editingIndex === index }">
        
        <!-- 原文句子 -->
        <div class="original-sentence">
          <div class="sentence-header">
            <span class="sentence-number">{{ getGlobalIndex(index) + 1 }}.</span>
            <span class="sentence-label" style="font-weight: bold;">原文</span>
          </div>
          <div class="sentence-content">
            {{ item.originalText }}
          </div>
        </div>
        <!-- 翻译句子 -->
        <div class="translation-sentence">
          <div class="sentence-header">
            <span class="sentence-label" style="font-weight: bold;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;翻译</span>
            <div>连接状态: <span>{{ connectionStatusText }}</span></div>
          </div>
          
           <h3>用户列表</h3>
            <ul>
              <!-- 循环渲染用户列表 -->
              <li v-for="user in userList" :key="user.id">
                {{ user.id }} - {{ user.userName }}
              </li>
            </ul>
          <!-- 编辑状态下显示文本框 -->
          <div class="sentence-editor">
            <el-input
              v-model="item.transText"
              type="textarea"
              :rows="3"
              auto-size
              placeholder="请输入翻译内容"
              @input="handleInput(item.id)"
            ></el-input>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style>
/*3. 中间编辑区*/
.editor-container1 {
  position: fixed;
  left: 20%;
  top: 60px;
  width: 80%;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
}

/* 顶部导航栏样式 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.pagination-controls {
  flex: 1;
  display: flex;
  justify-content: center;
  margin: 0 16px;
}

.export-btn {
  min-width: 120px;
}

/* 翻译内容区域样式 */
.translation-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.translation-pair {
  margin-bottom: 20px;
  border-radius: 6px;
  padding: 12px;
  transition: all 0.3s;
  border-bottom: 1px solid #3a42a5; /* 添加底部边框 */
}

/* 最后一项不显示分割线 */
.translation-pair:last-child {
  border-bottom: none;
}

.active-pair {
  background-color: #f5f7fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
}

.original-sentence, .translation-sentence {
  margin-bottom: 12px;
}

.sentence-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.sentence-number {
  font-weight: bold;
  color: #1890ff;
  margin-right: 8px;
}

.sentence-label {
  font-weight: 500;
  color: #606266;
  margin-right: 12px;
}

.edit-controls {
  margin-left: auto;
}

.sentence-content {
  padding: 12px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  line-height: 1.6;
  min-height: 40px;
}

.sentence-editor {
  padding: 0 12px;
}

/* memoryList.css */

.memory-list-container {
  padding: 16px;
}

.memory-item {
  margin-bottom: 24px;
  padding: 16px;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.index-number {
  font-weight: bold;
  color: #165DFF; /* 蓝色 */
  margin-right: 8px;
}

.source-label, .translation-label {
  font-weight: bold;
  color: #333; /* 黑色 */
  margin: 0 8px 4px 0;
}

.source-text, .translated-text {
  margin: 4px 0 12px;
  line-height: 1.5;
}

/* 自定义滚动条样式 */
.translation-content::-webkit-scrollbar {
  width: 6px;
}

.translation-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.translation-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.translation-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

</style>