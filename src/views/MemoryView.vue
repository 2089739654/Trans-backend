<script setup lang="ts">
import { ref } from "vue";
// import { computed } from 'vue'
// import { useRoute } from 'vue-router'
// const route = useRoute()
// const pageTitle = computed(() => route.meta.title || '默认标题')// 动态获取标题

//111111
// 引入递归组件（需确认组件文件路径）
// import TreeNode from '@/components/TreeNode.vue';
// const staticFiles = ref([
//     {
//       name: '记忆库1',
//       id: 'folder_'+ crypto.randomUUID(),
//       isFolder: true,
//       children: [
//           { id: 'file_'+ Math.random().toString(36).substr(2, 9),name: '记忆库1.1' },
//           { id: 'file_'+ Math.random().toString(36).substr(2, 9),name: '记忆库1.2' }
//       ]
//     },
//     {
//       name: '记忆库2',
//       id: 'folder_'+ crypto.randomUUID(),
//       isFolder: true,
//       children: [
//           { id: 'file_'+ Math.random().toString(36).substr(2, 9),name: '记忆库2.1' }
//       ]
//     }
// ])
// const currentPath = ref(['示例', '记忆库列表'])//根目录
// const activeId = ref(null) // 统一管理激活状态
// const handleNodeClick = (item) => {
//   // 每次点击新节点时重置激活状态
//   activeId.value = activeId.value === item.id ? null : item.id
//   console.log('点击节点:', item)
//   // 可通过item.id进行精确操作
// }

//   // 递归处理函数（独立封装）
//   const processChildren = (children) => {
//   return children.map(child => ({
//     id: child.id,
//     name: child.name,
//     isOpen: false, // 新增展开状态字段（默认折叠）
//     isFolder: child.isFolder || false, // 默认非文件夹
//     children: child.children ? processChildren(child.children) : [] // 递归关键点
//   }));
// };
//   // 父组件新增事件处理
//   const onNodeUpdate = (updatedNode) => {
//   staticFiles.value = staticFiles.value.map(item =>
//     updateTreeItem(item, updatedNode)
//   );
// };

// // 递归更新树节点（核心函数）
// const updateTreeItem = (node, target) => {
//   if (node.id === target.id) return target;
//   if (node.children) {
//     return {
//       ...node,
//       children: node.children.map(child =>
//         updateTreeItem(child, target)
//       )
//     };
//   }
//   return node;
// };

//222222222222
// 表格数据

import { type MemoryEntry } from "../types/memories";

const tableData = ref<MemoryEntry[]>([]);
// 请求数据
const fetchText = async () => {
  try {
    const response2 = await fetch("/mock/memorys.json");
    // 关键校验：状态码和内容类型
    if (!response2.ok) console.log(`HTTP错误: ${response2.status}`);
    const contentType = response2.headers.get("Content-Type");
    if (!contentType?.includes("application/json")) {
      console.log("响应非 JSON 格式");
    }
    const jsonData = await response2.json();
    // 使用ref包裹整个响应数据
    tableData.value = jsonData.map((item: MemoryEntry) => ({
      id: item.id,
      source: item.source,
      trans: item.trans,
      createTime: item.createTime,
      lastTime: item.lastTime,
      status: item.status,
    }));
    console.log("请求json数据:", tableData.value);
  } catch (error) {
    console.error("请求失败:", error);
  }
};
onMounted(fetchText);
// 编辑按钮
const handleEdit = (row: FormData) => {
  console.log("编辑行:", row);
  Object.assign(formData, row);
  dialogVisible.value = true;
  //alert("编辑行");
};
// 删除按钮
const handleDelete = (row: any) => {
  tableData.value = tableData.value.filter(
    (item: MemoryEntry) => item.id !== row.id
  );
};
import { reactive } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
// 生成时间方法
function formatYMD(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0"); // 补零处理
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
const currentDate = ref<Date>(new Date());
const formattedDate = formatYMD(currentDate.value);
// 表单数据初始化
const formData = reactive<MemoryEntry>({
  source: "",
  trans: "",
  createTime: formattedDate,
  lastTime: formattedDate,
  status: "待审核",
});
const dialogVisible = ref(false);
const formRef = ref<any>(null);
// 表单验证规则
const rules = {
  source: [{ required: true, message: "源术语必填", trigger: "blur" }],
  trans: [{ required: true, message: "翻译结果必填", trigger: "blur" }],
};
// 提交逻辑
const submitForm = async () => {
  await formRef.value.validate();
  const newId = tableData.value.length + 1;
  tableData.value.unshift({
    id: newId,
    ...formData,
    status: "待审核", // 新增记录默认状态
  });
  dialogVisible.value = false;
  ElMessage.success({
    message: "新增成功",
    customClass: "custom-message", // 注入自定义类名
    duration: 3000,
    offset: 60,
  });
  formRef.value.resetFields(); // 重置表单
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
    ElMessage.warning({
      message: "请先选择要删除的术语",
      customClass: "custom-message", // 注入自定义类名
      duration: 3000,
      offset: 60,
    });
    return;
  }

  ElMessageBox.confirm("确定删除选中术语？", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
    customClass: "custom-messagebox", // 自定义类名
  }).then(() => {
    tableData.value = tableData.value.filter(
      (item) => !selectedRows.value.includes(item.id)
    );
    ElMessage.success({
      message: `已删除 ${selectedRows.value.length} 条术语`,
      customClass: "custom-message", // 注入自定义类名
      duration: 3000,
      offset: 60,
    });
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
    (item) =>
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

// 搜索处理（可扩展为API请求）
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
import { useRouter } from "vue-router";
const router = useRouter();

// 设置点击事件
const showSettings = () => {
  alert("你点击了数据上传按钮");
  router.push('/file-manager')
};
// <div class="file-manager">
//   <!-- 顶部路径导航 -->
//   <div class="path-bar">
//       <el-breadcrumb separator=">">
//       <el-breadcrumb-item v-for="(item, index) in currentPath" :key="index">
//           {{ item }}
//       </el-breadcrumb-item>
//       </el-breadcrumb>
//   </div>

//   <!-- 静态文件树示例 -->
//   <div class="file-tree">
//       <TreeNode
//         v-for="item in staticFiles"
//         :key="item.name"
//         :item="item"
//         :active-id="activeId"
//         @update-node="onNodeUpdate"
//         @node-click="handleNodeClick"
//       />
//     </div>

//   <!-- 底部设置按钮 -->
//   <div class="settings-footer">
//       <el-button type="primary" @click="showSettings">
//       <el-icon><Setting /></el-icon>系统设置
//       </el-button>
//   </div>
// </div>
</script>

<template>
  <!-- 2.左侧文件管理 -->

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
      <el-table-column type="index" label="序号" width="80" />
      <el-table-column prop="source" label="源语言" min-width="150" />
      <el-table-column prop="trans" label="翻译结果" min-width="150" />
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column prop="lastTime" label="最后修改时间" width="120" />
      <el-table-column prop="status" label="语言状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === '已审核' ? 'success' : 'warning'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
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
  <el-dialog v-model="dialogVisible" title="新增术语" width="30%">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="源术语" prop="source">
        <el-input v-model="formData.source" placeholder="请输入英文术语" />
      </el-form-item>
      <el-form-item label="翻译结果" prop="trans">
        <el-input v-model="formData.trans" placeholder="请输入翻译结果" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </template>
  </el-dialog>
</template>

<style>
/*2. 主页面左侧栏*/
.file-manager {
  position: fixed;
  width: 10%;
  top: 60px;
  height: calc(100vh - 60px);
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;

  .path-bar {
    padding: 12px;
    border-bottom: 1px solid #ebeef5;
    background: #f5f7fa;
  }

  .file-tree {
    text-align: left; /* 全局左对齐 */
    flex: 1;
    padding: 8px 2px;
    overflow-y: auto;
    /* 全局样式或组件scoped样式 */
    /* 激活节点样式 */
    .tree-node.active-node {
      background: #6db5e4;
      box-shadow: inset 14px 0 0 #7fbad6;
    }

    /* 文件夹节点禁用交互 */

    /* 常规节点交互 */
    .tree-node:not(.folder-node) {
      cursor: pointer;
      transition: background 0.3s;

      &:hover {
        background: #e07a7a;
      }
    }
    .children {
      margin-left: 24px;
      border-left: 1px dashed #e5e7eb;

      /* 末级节点缩进优化 */
      &:last-child {
        margin-left: 28px;
      }
    }
    /* 这里 */
  }

  .settings-footer {
    padding: 12px;
    border-top: 1px solid #ebeef5;
    text-align: center;
  }
}

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
