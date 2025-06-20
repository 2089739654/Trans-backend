<script setup lang="ts">
import { ref ,onMounted } from "vue";
import {computed} from "vue";

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
  isEditing: boolean
  memoryLists: memoryItem[]
}[]>([])


// 哈哈哈哈哈哈哈哈哈哈哈哈
import { watch } from "vue";
import { useRoute, useRouter } from "vue-router";
const route = useRoute();
const router = useRouter();
const id = route.params.fileId;
console.log('id:',id);

// 分页相关状态
const currentPage = ref(1)      // 当前页码
const pageSize = ref(10)        // 每页显示数量
const totalSentences = ref(0)   // 总句子数
const totalPages = computed(() => Math.ceil(totalSentences.value / pageSize.value)) // 总页数

const currentIndex = ref(0)     // 当前选中的句子索引
const inputText = ref('')       // 输入框内容

const editingIndex = ref(-1)    // 当前正在编辑的句子索引
const tempTranslation = ref('') // 临时翻译内容
const contentRef = ref(null)    // 内容区域引用

import axios from 'axios'
import { ElMessage } from "element-plus";

// 获取当前页数
const fetchTotalPages = async()=>{
  console.log("请求页数")
      // 从 localStorage 获取 token
    const token = localStorage.getItem('token');
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
    const response = await axios.post(`http://26.143.62.131:8080/file/getTransTextCount?fileId=${id}`, 
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
    const token = localStorage.getItem('token');
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
    const response = await axios.post('http://26.143.62.131:8080/file/getTransText', 
      requestBody,
      config,
    )
    console.log("返回",response.data)
    if (response.data.code != 200) throw new Error(`HTTP错误: ${response.status}`)
    
    const jsonData = await response.data.data
    console.log("hhhh",jsonData);
    // 更新总句子数
    //totalSentences.value = jsonData.fullText.length
    
    // 更新当前页数据
    pageSentences.value = jsonData.map((item: any) => ({
      id: item.id,
      originalText: item.sourceText,
      transText: item.translatedText,
      version:item.version,
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

// 开始编辑句子
const startEdit = (index: number) => {
  editingIndex.value = index
  tempTranslation.value = pageSentences.value[index].transText || ''
  
  // 滚动到编辑的句子位置
  nextTick(() => {
    const elements = document.querySelectorAll('.translation-pair')
    if (elements[index]) {
      elements[index].scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  })
}

// 临时保存翻译
const saveTranslation = async (index: number) => {
  if (!tempTranslation.value.trim()) return

  const sentence = pageSentences.value[index]
  console.log('替换的：',tempTranslation.value,'替换的：',sentence)
  sentence.isEditing = 'success'
  sentence.transText = tempTranslation.value
  setTimeout(() => {
    editingIndex.value = -1
    sentence.isEditing = false
  }, 1000)
  // 从 localStorage 获取 token
  // const token = localStorage.getItem('token');
  // console.log('token:',token);
  // // 如果没有 token，提示用户重新登录
  // if (!token) {
  //   ElMessage.error('请先登录');
  //   router.push('/login');
  //   return;
  // }
  // // 设置请求头
  // const config = {
  //   headers: {
  //     'token': token
  //   }
  // };

  // try {
  //   // 构建请求体
  //   const requestBody = {
  //     fileId: id, // 文件ID
  //     list:[
  //       {
  //         id: sentence.id,
  //         sourceText: sentence.originalText,
  //         translatedText: tempTranslation.value
  //       }
  //     ]
  //   }
    
  //   // 显示保存中状态
  //   sentence.isEditing = 'saving'
    
  //   // 发送请求到后端
  //   const response = await axios.post(`http://26.143.62.131:8080/file/saveTransText`, 
  //     requestBody,
  //     config
  //   )
    
  //   console.log("返回结果：",response)

  //   // 更新翻译内容
  //   sentence.transText = tempTranslation.value
    
  //   console.log(`已成功保存第${getGlobalIndex(index) + 1}句的翻译`)
    
  //   // 显示保存成功提示
  //   sentence.isEditing = 'success'
  //   setTimeout(() => {
  //     editingIndex.value = -1
  //     sentence.isEditing = false
  //   }, 1000)
    
  // } catch (error) {
  //   console.error("保存翻译失败:", error)
    
  //   // 显示保存失败提示
  //   sentence.isEditing = 'error'
  //   setTimeout(() => {
  //     sentence.isEditing = true // 恢复编辑状态
  //   }, 2000)
  // }
}

// 取消编辑
const cancelEdit = () => {
  editingIndex.value = -1
}

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
      
      const pageContent = pageItems.map((item, index) => {
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
  const token = localStorage.getItem('token');
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

// 机械翻译
const machineTranslation = async (index: number) => {
  //if (!tempTranslation.value.trim()) return

  const sentence = pageSentences.value[index]
  console.log('机械替换的:',sentence)
  sentence.isEditing = 'success'

  //从 localStorage 获取 token
  const token = localStorage.getItem('token');
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
    const requestBody = { 
      sourceText: sentence.originalText,
      fileId: id
    }
    
    // 显示保存中状态
    // sentence.isEditing = 'saving'
    console.log("机器",requestBody)
    // 发送请求到后端
    const response = await axios.post(`http://26.143.62.131:8080/file/translateText`, 
      requestBody,
      config
    )
    
    console.log("机器返回结果：",response)

    // 更新翻译内容
    sentence.transText = response.data.data
    
    console.log(`已成功展示第${getGlobalIndex(index) + 1}句的翻译`)
    
    console.log(pageSentences.value)
    // // 显示保存成功提示
    // sentence.isEditing = 'success'
    // setTimeout(() => {
    //   editingIndex.value = -1
    //   sentence.isEditing = false
    // }, 1000)
    
  } catch (error) {
    console.error("请求翻译失败:", error)
    
    // 显示保存失败提示
    sentence.isEditing = 'error'
    setTimeout(() => {
      sentence.isEditing = true // 恢复编辑状态
    }, 2000)
  }
}

// 回退翻译记录
const backTranslation = async (index: number) => {
  const sentence = pageSentences.value[index]
  console.log('回退翻译记录:',sentence)
  sentence.isEditing = 'success'

  //从 localStorage 获取 token
  const token = localStorage.getItem('token');
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
    const requestBody = { 
      transId: sentence.id,
      version: sentence.version,
    }
    
    // 显示保存中状态
    // sentence.isEditing = 'saving'
    console.log("回退",requestBody)
    // 发送请求到后端
    const response = await axios.post(`http://26.143.62.131:8080/file/getTranslationHistory`, 
      requestBody,
      config
    )
    
    console.log("版本返回结果：",response)

    // 更新翻译内容
    sentence.transText = response.data.data.translatedText
    sentence.version = response.data.data.version
    
    console.log(`已成功回退第${getGlobalIndex(index) + 1}句的翻译`)
    
    console.log(pageSentences.value)
  } catch (error) {
    console.error("回退翻译失败:", error)
    
    // 显示保存失败提示
    sentence.isEditing = 'error'
    setTimeout(() => {
      sentence.isEditing = true // 恢复编辑状态
    }, 2000)
  }
}

// 记忆列表
//const memoryList = ref<memoryItem[]>([])
// 记忆库查询
const memorySelect = async (index: number) => {
  const sentence = pageSentences.value[index]
  console.log('记忆库替换的:',sentence)

  //从 localStorage 获取 token
  const token = localStorage.getItem('token');
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
    const requestBody = { 
      text: sentence.originalText,
      transId: sentence.id
    }
    
    // 显示保存中状态
    // sentence.isEditing = 'saving'
    console.log("记忆",requestBody)
    // 发送请求到后端
    const response = await axios.post(`http://26.143.62.131:8080/file/searchTransPairs`, 
      requestBody,
      config
    )
    
    console.log("记忆返回结果：",response)
    // 记忆列表
    sentence.memoryList = response.data.data.map((item:any)=>({
      sourceText: item.sourceText,
      translatedText: item.translatedText,
    }))

    console.log("记忆列表:",sentence.memoryList)

    if(sentence.memoryList == null){
      ElMessage.success(`查询为空！`);
    }

    if(response.data.code==200 && sentence.memoryList != null){
      ElMessage.success(`已成功展示第${getGlobalIndex(index) + 1}句的记忆库查询结果`);
    }

    console.log(`已成功展示第${getGlobalIndex(index) + 1}句的记忆库查询结果`)
    
    console.log(pageSentences.value)
  } catch (error) {
    console.error("请求翻译失败:", error)
    
    // 显示保存失败提示
    sentence.isEditing = 'error'
    setTimeout(() => {
      sentence.isEditing = true // 恢复编辑状态
    }, 2000)
  }
}

onMounted(async () => {
  await fetchTotalPages()
  await fetchPageData()
})
//console.log(2,textText.value.value)  machineTranslation
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
        :key="index"
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
            <!-- <span class="sentence-number">{{ getGlobalIndex(index) + 1 }}.</span> -->
            <span class="sentence-label" style="font-weight: bold;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;翻译</span>
            
            <!-- 编辑状态下显示提交按钮 -->
            <div v-if="editingIndex === index" class="edit-controls">
              <el-button 
                type="primary" 
                size="small" 
                @click="saveTranslation(index)">
                确定
              </el-button>
              <el-button 
                size="small" 
                @click="cancelEdit()">
                取消
              </el-button>
            </div>
            
            <!-- 非编辑状态下显示编辑按钮 -->
            <div v-else class="edit-controls">
              <el-button 
                type="text" 
                size="small" 
                @click="startEdit(index)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button 
                size="small" 
                @click="backTranslation(index)">
                回退版本
              </el-button>
              
              <el-button 
                size="small" 
                @click="machineTranslation(index)">
                机器翻译
              </el-button>
            </div>
          </div>
          
          <!-- 编辑状态下显示文本框 -->
          <div v-if="editingIndex === index" class="sentence-editor">
            <el-input
              v-model="tempTranslation"
              type="textarea"
              :rows="3"
              auto-size
              placeholder="请输入翻译内容"
              @blur="saveTranslation(index)"
            ></el-input>
          </div>
          
          <!-- 非编辑状态下显示翻译内容 -->
          <div v-else class="sentence-content">
            {{ item.transText || '待翻译' }}
          </div>
        </div>

        <!-- 记忆库查询 -->
        <div class="original-sentence">
          <div class="sentence-header">
            <!-- <span class="sentence-number">{{ getGlobalIndex(index) + 1 }}.</span> -->
            <span class="sentence-label" style="font-weight: bold;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;记忆库查询</span>
            <!-- 查询按钮 -->
            <div  class="edit-controls">
              <el-button 
                type="primary" 
                size="small" 
                @click="memorySelect(index)">
                记忆库查询
              </el-button>
            </div>
          </div>

          <div class="memory-list-container">
            <div v-if="item.memoryList && item.memoryList.length > 0" 
                v-for="(item2, index) in item.memoryList" 
                :key="index" 
                class="memory-item">
              <div class="label-row">
                <span class="index-number">{{ index + 1 }} .</span>
                <span class="source-label">原文</span>
              </div>
              <p class="source-text">{{ item2.sourceText }}</p>
              
              <div class="label-row">
                <span class="translation-label">译文</span>
              </div>
              <p class="translated-text">{{ item2.translatedText }}</p>
            </div>
          
            <!-- 当列表为空时显示提示 -->
            <div v-else class="empty-state">
              <p>空空如也/(ㄒoㄒ)/~~</p>
            </div>
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