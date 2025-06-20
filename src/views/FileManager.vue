<script setup lang="ts">
import { ref, computed } from 'vue'
import axios from 'axios'
import { useRoute } from "vue-router";
const route = useRoute();
const id = route.params.fileId;
console.log('文件夹：',id)

interface FileWithMeta extends File {
  progress: number
  error?: string
}

interface Props {
  maxSize?: number // MB
  allowedTypes?: string[]
}

// 上传文件要求
const props = withDefaults(defineProps<Props>(), {
  maxSize: 10,
  allowedTypes: () => ['png','ppt','pdf','docx','jpg','txt']
})

const emit = defineEmits(['upload-success', 'upload-error'])

// 响应式状态
const dragActive = ref(false)
const files = ref<FileWithMeta[]>([])
const fileInput = ref<HTMLInputElement>()

// 计算属性
const allowedTypesString = computed(() => props.allowedTypes.join(', '))

// 文件处理方法
// 当文件被拖入可视区域时触发
const handleDragEnter = (e: DragEvent) => {
  e.preventDefault()
  dragActive.value = true
}
// 拖拽过程中持续触发
const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
}
// 当文件移出可视区域时触发
const handleDragLeave = (e: DragEvent) => {
  if ((e.currentTarget as HTMLElement).contains(e.relatedTarget as Node)) return
  dragActive.value = false
}
// 文件放置时触发
const handleDrop = (e: DragEvent) => {
  dragActive.value = false
  const droppedFiles = e.dataTransfer?.files
  console.log('文件放置',droppedFiles)
  if (droppedFiles) processFiles(Array.from(droppedFiles))
}

// 点击文件上传
const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileSelect = (e: Event) => {
  const input = e.target as HTMLInputElement
  console.log('input',input.files)
  if (input.files) processFiles(Array.from(input.files))
}

// 文件处理流程
const processFiles = (newFiles: File[]) => {
  console.log('处理前文件：',newFiles)
  // 1. 文件验证
  const validFiles = newFiles.filter(file => 
    validateFileType(file) && 
    validateFileSize(file)
  ).map(file => 
    Object.assign(
      new File([file], file.name, { type: file.type }), 
      { progress: 0, error: undefined }
    ) as FileWithMeta
  )
  // console.log('处理中文件：',validFiles)
  // 3. 状态更新
  files.value = [...files.value, ...validFiles]
  // 4. 触发上传
  console.log(files.value)
  console.log('处理后文件：',validFiles)
  uploadFiles(validFiles)
}

// 文件验证
const validateFileType = (file: File) => {
  // 获取组件允许的文件类型（如从props传入的allowedTypes）
  const allowedTypes = props.allowedTypes //|| ['png','ppt','pdf','docx','jpg','txt'];
  
  // 构建扩展名-MIME类型映射
  const MIME_MAP: Record<string, string[]> = {
    png: ['image/png'],
    jpg: ['image/jpeg'],
    pdf: ['application/pdf'],
    docx: ['application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
    ppt: ['application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'],
    txt:['text/plain'],
  };

  // 验证逻辑
  const extension = file.name.split('.').pop()?.toLowerCase() || '';
  const mimeType = file.type.toLowerCase();
  console.log(extension,mimeType)
  console.log(allowedTypes.includes(extension),MIME_MAP[extension]?.includes(mimeType))
  // 双重验证（网页2和网页5建议的扩展名+类型验证）
  return (
    allowedTypes.includes(extension) && 
    MIME_MAP[extension]?.includes(mimeType)
  );
};
// 创建完整的MIME类型映射表
// const mimeTypeMap: Record<string, string[]> = {
//   'png': ['image/png'],
//   'jpg': ['image/jpeg'],        
//   'ppt': ['application/vnd.ms-powerpoint'],
//   'pptx': ['application/vnd.openxmlformats-officedocument.presentationml.presentation'],
//   'pdf': ['application/pdf'],
//   'docx': ['application/vnd.openxmlformats-officedocument.wordprocessingml.document']
// }
const validateFileSize = (file: File) => {
    const maxSizeBytes = (props.maxSize || 10) * 1024 * 1024
  if (file.size > maxSizeBytes) {
    // 添加友好提示
    console.log(`文件大小超过限制：${(file.size/1024/1024).toFixed(1)}MB > ${props.maxSize}MB`)
    return false
  }
  console.log('大小正确')
  return true
}

// 文件上传
const uploadFiles = async (filesToUpload: FileWithMeta[]) => {
  const formData = new FormData()
  // 添加ID字段
  formData.append('projectId', id.toString()); // 确保ID是字符串类型
  // 添加文件
  filesToUpload.forEach(file => {
    formData.append('file', file)
    console.log('名字',file.name)
  })
  console.log('上传数据:',formData)
  // 调试用：打印FormData内容
  for (let pair of formData.entries()) {
    console.log(pair[0] + ': ' + pair[1]);
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

  try {
    const response = await axios.post('http://26.143.62.131:8080/file/upload', 
    formData, config,{
      headers: { 'Content-Type': 'multipart/form-data' },  
      onUploadProgress: progressEvent => {
        const progress = Math.round(
          (progressEvent.loaded * 100) / (progressEvent.total || 1)
        )
        filesToUpload.forEach(file => {
          file.progress = progress
        })
      }
    })
    console.log('返回结果：',response)
    
    emit('upload-success', response)
    // 设置延迟跳转
    console.log("你好")
    if(response.status==200){
      ElMessage.success("上传成功！");
    }
    router.push('/projects');
  } catch (error) {
    handleUploadError(error, filesToUpload)
    emit('upload-error', error)
  }
}

// 错误处理
const handleUploadError = (error: unknown, files: FileWithMeta[]) => {
  const errorMessage = axios.isAxiosError(error) 
    ? error.response?.data?.message || '上传失败'
    : '未知错误'
  
  files.forEach(file => {
    file.error = errorMessage
  })
}

// 辅助函数
const formatFileSize = (bytes: number) => {
  const units = ['B', 'KB', 'MB', 'GB']
  let size = bytes
  let unitIndex = 0

  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }

  return `${size.toFixed(1)}${units[unitIndex]}`
}

const removeFile = (index: number) => {
  files.value.splice(index, 1)
}

import { useRouter } from 'vue-router'
const router = useRouter()

// 取消按钮
const handleBack = () => {
    if (window.history.length > 1) {
    router.go(-1) // 正常返回
  } else {
    router.push('/projects') // 无历史记录时跳转首页（兜底逻辑）
  }
}
</script>

<template>
  <div class="temporary-container">
  <button class="back-btn" @click="handleBack">x 取消</button>

  <div class="upload-container">
    <!-- 拖拽区域 -->
    <div 
      class="drag-zone" 
      @dragenter.prevent="handleDragEnter"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop.prevent="handleDrop"
      :class="{ 'drag-active': dragActive }"
      @click="triggerFileInput"
    >
      <div class="upload-icon">
        <svg width="48" height="48" viewBox="0 0 24 24">
          <path fill="currentColor" d="M14 13v-2h-4v2H9l3 3l3-3M5 20h14v-2H5m14-6h-4v2h4v2h-4v2h-2v-2h-4v2H5v-2h4v-2H5v-2h4v-2h2v2h4v-2h2v2h4v2Z"/>
        </svg>
      </div>
      <p>拖拽文件至此或点击上传</p>
      <p class="file-types">支持格式：{{ allowedTypesString }}（最大{{ maxSize }}MB）</p>
    </div>

    <!-- 隐藏的input -->
    <input
      type="file"
      ref="fileInput"
      @change="handleFileSelect"
      :accept="allowedTypesString"
      hidden
      multiple
    />

    <!-- 文件列表 -->
    <div v-if="files.length" class="file-list">
      <div v-for="(file, index) in files" :key="file.name + index" class="file-item">
        <div class="file-info">
          <span class="filename">{{ file.name }}</span>
          <span class="filesize">{{ formatFileSize(file.size) }}</span>
        </div>
        <div class="upload-status">
          <span v-if="file.error" class="error">{{ file.error }}</span>
          <progress 
            v-else
            :value="file.progress" 
            max="100"
            :class="{ 'completed': file.progress === 100 }"
          ></progress>
        </div>
        <button @click="removeFile(index)" class="remove-btn">×</button>
      </div>
    </div>
  </div>
</div>
</template>

<style>
.temporary-container {
  position: fixed;
  left: 20%;
  top: 60px;
  width: 80%;
  height: calc(100vh - 60px); /* 占满导航栏下方全部空间 */
  display: flex;
  flex-direction: column;
  justify-content: center; /* 新增：内部内容垂直居中 */
  align-items: center; /* 可选：水平居中 */
  border-left: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
}
/* 固定在左上角 + 基础交互样式 */
.back-btn {
  position: absolute;
  left: 20px;
  top: 20px;
  padding: 8px 15px;
  border-radius: 4px;
  background: #f0f0f0;
  border: 1px solid #ddd;
  cursor: pointer;
  transition: background 0.3s;

  &:hover {
    background: #e0e0e0;
  }
}

.upload-container {
    max-width: 600px;
    margin: 2rem auto;
}

.drag-zone {
  border: 2px dashed #cccccc;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.drag-active {
  border-color: #2196f3;
  background-color: rgba(33, 150, 243, 0.1);
}

.upload-icon {
  margin-bottom: 1rem;
  color: #666;
}

.file-types {
  font-size: 0.9em;
  color: #666;
  margin-top: 0.5rem;
}

.file-list {
  margin-top: 1.5rem;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 0.8rem;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 0.5rem;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.filename {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.filesize {
  font-size: 0.8em;
  color: #666;
}

.upload-status {
  width: 120px;
  margin: 0 1rem;
}

progress {
  width: 100%;
  height: 8px;
  border-radius: 4px;
}

progress.completed {
  opacity: 0.5;
}

.error {
  color: #ff4444;
  font-size: 0.9em;
}

.remove-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 1.2em;
  cursor: pointer;
  padding: 0 0.5rem;
}

.remove-btn:hover {
  color: #ff4444;
}
</style>