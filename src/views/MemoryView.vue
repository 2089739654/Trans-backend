<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
const router = useRouter();

import axios from 'axios'
import { ElMessage, ElMessageBox } from "element-plus";

//222222222222

// 表单数据类型
interface MemoryFormData {
  source: string;
  trans: string;
  fileId: string;
  fileName: string;
  createTime?: string;
  lastTime?: string;
  id?: number; // 编辑时需要的记录ID
}

// 从 localStorage 获取 token
const token = localStorage.getItem('token');
console.log('token:',token);
// 如果没有 token，提示用户重新登录
if (!token) {
  ElMessage.error('请先登录');
  router.push('/login');
}
// 设置请求头
const config = {
  headers: {
    'token': token
  }
};

const tableData = ref<MemoryFormData[]>([]);
// 请求数据
const fetchText = async () => {
  try {
    const response = await fetch("/mock/memorys.json")
    // const response = await axios.get("/mock/memorys.json",
    //   null,
    //   config
    // );
    console.log("请求数据：",response);
    // 关键校验：状态码和内容类型
    const contentType = response.headers.get("Content-Type");
    if (!contentType?.includes("application/json")) {
      console.log("响应非 JSON 格式");
    }
    const jsonData = await response.json();
    console.log('jsonData:',jsonData)
    // 使用ref包裹整个响应数据
    tableData.value = jsonData.map((item: MemoryFormData) => ({
      id: item.id,
      source: item.source,
      trans: item.trans,
      fileId: item.fileId,
      fileName: item.fileName,
      createTime: item.createTime,
      lastTime: item.lastTime,
    }));
    console.log("请求json数据:", tableData.value);
  } catch (error) {
    console.error("请求失败:", error);
  }
};

onMounted(fetchText);

// 删除按钮
const handleDelete = (row: any) => {
  ElMessageBox.confirm("确定删除记忆？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    tableData.value = tableData.value.filter(
      (item: MemoryFormData) => item.id !== row.id
    );
    ElMessage.success(`已删除该记忆`);
  });
};

// 生成时间方法
function formatYMD(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0"); // 补零处理
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
const currentDate = ref<Date>(new Date());
const formattedDate = formatYMD(currentDate.value);

// 默认表单数据
const defaultFormData : MemoryFormData = {
  source: "",
  trans: "",
  fileId:"",
  fileName:"",
  createTime: formattedDate,
  lastTime: formattedDate
};
// 表单数据初始化
const formData = ref<MemoryFormData>({
  source: "",
  trans: "",
  fileId: "暂无",
  fileName: "暂无",
  createTime: formattedDate,
  lastTime: formattedDate,
});
const dialogVisible = ref(false);
// 是否为编辑模式
// const isEditMode = computed(() => !!formData.value.id); 
const formRef = ref<any>(null);
// 表单验证规则
const rules = {
  source: [{ required: true, message: "源语言必填", trigger: "blur" }],
  trans: [{ required: true, message: "翻译结果必填", trigger: "blur" }],
};

// 编辑按钮
// const handleEdit = (row: FormData) => {
//   console.log("编辑行:", row);
//   // Object.assign(formData, row);
//   formData.value = { ...row }; 
//   dialogVisible.value = true;
//   //alert("编辑行");
// };

// 关闭弹窗(取消按钮)
const closeDialog = () => {
  dialogVisible.value = false;
  formData.value = {
    source: '',
    trans: '',
  }; // 重置表单数据
};
// 提交逻辑
const submitForm = async () => {
  const isValid = await formRef.value?.validate();
  if (isValid) {
    console.log()
    // 新增逻辑
    //await api.createTerm(requestData);
    ElMessage.success('术语新增成功');
    dialogVisible.value = false;
    formData.value = { ...defaultFormData }; // 重置表单
  };
};
// 新增按钮
const handleAdd = () => {
  dialogVisible.value = true;
};
// 批量删除按钮
const selectedRows = ref<any[]>([]);
// 多选事件处理
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows.map((row: any) => row.id);
};
// 批量删除
const handleBatchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning( "请先选择要删除的术语");
    return;
  }

  ElMessageBox.confirm("确定删除选中术语？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  }).then(() => {
    tableData.value = tableData.value.filter(
      (item:any) => !selectedRows.value.includes(item.id)
    );
    ElMessage.success(`已删除 ${selectedRows.value.length} 条术语`);
    selectedRows.value = []; // 清空选中状态
  });
};
//33333333333333333333333
import { computed, onMounted, onBeforeUnmount } from "vue";
import { Search } from "@element-plus/icons-vue";
// 示例可以查询的所有数据
const findData = ref([
  { id: 1, source: "Hypertension", trans: "高血压" },
  { id: 2, source: "Cloud Computing", trans: "云计算" },
  { id: 3, source: "Force Majeure", trans: "不可抗力" },
  { id: 4, source: "Artificial Intelligence", trans: "人工智能" },
  { id: 5, source: "Sustainability", trans: "持续可能性" },
  { id: 6, source: "Blockchain", trans: "区块链" },
  { id: 7, source: "Quantum Computing", trans: "量子计算" },
]);

// 搜索相关状态
const searchQuery = ref(""); //输入的搜索的句子
const loading = ref(false);
let scrollInterval: number | null | undefined = null;

// 过滤后的查询到的数据
const filteredData = computed(() => {
  return findData.value.filter(
    (item:any) =>
      item.source.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.trans.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

// 自动滚动控制
const startAutoScroll = () => {
  const tableBody = document.querySelector(".el-table__body-wrapper");
  if (!tableBody) return;

  let scrollTop = 0;
  scrollInterval = setInterval(() => {
    if (scrollTop >= tableBody.scrollHeight - tableBody.clientHeight) {
      scrollTop = 0;
    } else {
      scrollTop += 50;
    }
    tableBody.scrollTop = scrollTop;
  }, 1000);
};
const stopAutoScroll = () => {
  if (scrollInterval) clearInterval(scrollInterval);
};

// 生命周期控制
onMounted(() => {
  startAutoScroll();
});
onBeforeUnmount(() => {
  stopAutoScroll();
});

// 搜索处理（待扩展为API请求）
const handleSearch = () => {
  loading.value = true;
  setTimeout(() => {
    loading.value = false;
  }, 500);
};
// 详情处理
const handleDetail = (row: any) => {
  alert(row.id);
  console.log("处理详情：", row);
};
// 行样式处理
const tableRowClassName = (rowIndex: number) => {
  return rowIndex % 2 === 0 ? "even-row" : "odd-row";
};


// 设置点击事件
const showSettings = () => {
  alert("你点击了数据上传按钮");
  router.push('/file-manager')
};

</script>

<template>

  <!-- 3.中间编辑区 -->
  <div class="editor-container">
    <!-- 顶部工具栏 -->
    <div class="table-toolbar">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>
    <el-table
      :data="tableData"
      height="100%"
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa' }"
      @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="55" />
      <el-table-column prop="source" label="源文本" min-width="150" />
      <el-table-column prop="trans" label="翻译结果" min-width="150" />
      <el-table-column prop="fileId" label="文件ID" min-width="100" />
      <el-table-column prop="fileName" label="文件名称" min-width="100" />
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column prop="lastTime" label="最后修改时间" width="120" />
      <el-table-column label="操作" width="85">
        <template #default="{ row }">
          <!-- <el-button size="small" @click="handleEdit(row)">编辑</el-button> -->
          <el-button size="small" type="danger" @click="handleDelete(row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 4.右侧结果容器 -->
  <div class="result-container">
    <!-- 搜索输入区域 -->
    <div class="search-header">
      <el-input
        v-model="searchQuery"
        placeholder="输入术语搜索"
        @keyup.enter="handleSearch"
        clearable>
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 虚拟滚动表格 -->
    <el-table
      :data="filteredData"
      height="100%"
      style="width: 100%"
      :row-class-name="tableRowClassName"
      v-loading="loading"
      @mouseenter="stopAutoScroll"
      @mouseleave="startAutoScroll">
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column prop="source" label="源术语" min-width="200" />
      <el-table-column prop="trans" label="翻译结果" min-width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDetail(row)"
            >详情</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <div class="settings-footer">
      <el-button type="primary" @click="showSettings">
        <el-icon><Setting /></el-icon>数据上传
      </el-button>
    </div>
  </div>

  <!-- 新增弹窗 -->
  <el-dialog v-model="dialogVisible" title="新增记忆" width="30%">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="源语言" prop="source">
        <el-input v-model="formData.source" placeholder="请输入英文术语" />
      </el-form-item>
      <el-form-item label="翻译结果" prop="trans">
        <el-input v-model="formData.trans" placeholder="请输入翻译结果" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="closeDialog">取消</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </template>
  </el-dialog>
</template>

<style>

/*3. 中间编辑区*/
.editor-container {
  position: fixed;
  left: 0;
  top: 60px;
  width: 70%;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
  .table-toolbar {
    margin-bottom: 16px;
    padding: 8px;
    background: #f5f7fa;
    border-radius: 4px;
    display: flex;
    justify-content: flex-end; /* 子元素右对齐 */
    gap: 12px; /* 按钮间距 */
  }
  /* 新增表格滚动样式 */
  .el-table {
    flex: 1;
    overflow-y: auto;

    &::before {
      /* 移除默认的header边框 */
      height: 0;
    }

    .el-table__body-wrapper {
      scroll-behavior: smooth;
      &::-webkit-scrollbar {
        width: 6px;
      }
    }
  }
}

/*4. 右侧结果容器*/
.result-container {
  position: fixed;
  left: 70%;
  top: 60px;
  width: 30%;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-left: 1px solid #e0e0e0;
  padding: 12px;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);

  /* 搜索头区域 */
  .search-header {
    margin-bottom: 16px;

    .el-input-group__append {
      padding: 0 12px;
    }
  }

  /* 表格样式优化 */
  .el-table {
    flex: 1;
    background: transparent;

    &::before {
      height: 0; /* 移除默认边框 */
    }

    .el-table__body-wrapper {
      scroll-behavior: smooth;
      &::-webkit-scrollbar {
        width: 6px;
        background: #f1f1f1;
      }
      &::-webkit-scrollbar-thumb {
        background: #c1c1c1;
        border-radius: 4px;
      }
    }

    /* 斑马纹样式 */
    .even-row {
      background: #fafafa;
    }
    .odd-row {
      background: #fff;
    }
  }

  .settings-footer {
    padding: 12px;
    border-top: 1px solid #ebeef5;
    text-align: center;
  }
}
</style>