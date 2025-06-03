<script setup lang="ts">
//哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈
import { useRouter } from "vue-router";
const router = useRouter();
import { ref, reactive, watch, onMounted, onBeforeUnmount } from "vue";
//import { debounce } from 'lodash';

// 组件中新增展开状态存储
//const expandedNodes = ref(new Set());

//import { type FileItem } from "../types/file_contents";
// 定义文件节点类型
interface FileItem {
  id: string;
  name: string;
  isFolder: boolean;
  isOpen?: boolean;
  children?: FileItem[];
  content?: string;
}
// 设置点击事件
const showSettings = () => {
  alert("你点击了上传文件按钮");
  router.push('/file-manager')
};

// 引入递归组件（需确认组件文件路径）
import TreeNode from "../components/TreeNode.vue";
const staticFiles = ref<FileItem[]>();
// 请求数据
// 递归处理函数（独立封装）
const processChildren = (children: FileItem["children"]): FileItem[] => {
  //console.log('children',children)
  return (children ?? []).map((child: FileItem) => ({
    ...child, // 保留原始属性
    isOpen: child.isOpen ?? false, //expandedNodes.value[child.id] ?? false, // 新增展开状态字段（默认折叠）
    isFolder: child.isFolder || false, // 默认非文件夹
    children: child.children ? processChildren(child.children) : [], // 递归关键点
  }));
};
//const isFetched = ref(false)
const fetchText = async () => {
  //if (isFetched.value) return // 添加缓存判断
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.error('请先登录');
      router.push('/login');
      return;
    }
    console.log('请求总督文件：',token);
    // const config = {
    //   headers: {
    //     'token': token
    //   }
    // };
    const response2 = await fetch(
      "http://26.143.62.131:8080/file/project/projects",
      {
        method: 'GET', // 指定请求方法
        headers: {
          'token': token || '', // 添加 token 头
          'Content-Type': 'application/json' // 如果需要
        }
      }
    );
    // 关键校验：状态码和内容类型
    if (!response2.ok) console.log(`HTTP错误: ${response2.status}`);
    const contentType = response2.headers.get("Content-Type");
    if (!contentType?.includes("application/json")) {
      console.log("响应非 JSON 格式");
    }
    const jsonData = await response2.json();
    console.log(jsonData)

    // 获取子文件
    const fetchChildren = async (projectId: string) => {
      try {
        const response = await fetch(
          `http://26.143.62.131:8080/file/selectFiles?projectId=${projectId}`,
          {
            method: 'GET',
            headers: {
              'token': token,
              'Content-Type': 'application/json'
            }
          }
        );
        
        if (!response.ok) {
          throw new Error(`获取子文件失败: ${response.status}`);
        }
        
        const jsonData = await response.json();
        console.log('文件：',jsonData.data)
        // 假设返回格式为 { data: [...] }
        const returnData = jsonData.data.map((child:any) => ({
          id: child.id,
          name: child.fileName,
          isFolder: false,
          children: [] // 初始为空
        }))
        console.log(returnData)
        return returnData;
      } catch (error) {
        console.error('获取子文件列表错误:', error);
        ElMessage.error('加载文件列表失败');
        return [];
      }
    };

    // 使用ref包裹整个响应数据
    staticFiles.value = await Promise.all(
      jsonData.data.map(async (item: FileItem) => ({
      id: item.id,
      name: item.name,
      isFolder: true,
      children: await fetchChildren(item.id),
    })));


    console.log("请求json数据:", staticFiles.value);
    //isFetched.value = true
  } catch (error) {
    console.error("请求失败:", error);
  }
};
// 组件处理数据时添加展开状态
const onNodeUpdate = (updatedNode: FileItem) => {
  console.log("接收到更新:", updatedNode);
  // if (updatedNode.isOpen) {
  //   expandedNodes.value.add(updatedNode.id);
  // } else {
  //   expandedNodes.value.delete(updatedNode.id);
  // }
  staticFiles.value = (staticFiles.value ?? []).map((item: FileItem) =>
    updateTreeItem(item, updatedNode)
  );
  console.log("更新后的:", staticFiles.value);
};

// 递归更新树节点（核心函数）
const updateTreeItem = (node: FileItem, target: FileItem): FileItem => {
  // console.log('当前节点:', node.id, '目标节点:', target.id);
  if (node.id === target.id) {
    // 创建新对象触发响应式更新
    return {
      ...node,
      isOpen: target.isOpen, // 直接使用新状态
      // 保留原有 children 的引用（避免重建整棵树）
      children: node.children,
      // children: node.children?.map(child => ({
      //   ...child,
      //   // 保持子节点原有状态（不强制继承父级展开状态）
      // }))
    };
  }
  if (node.children) {
    return {
      ...node,
      children: node.children.map((child) => updateTreeItem(child, target)),
    };
  }
  return node;
};

const currentPath = ref(["示例", "树形文件"]); //根目录
const activeId = ref<string | null>(null); // 统一管理激活状态
const handleNodeClick = (item: FileItem) => {
  // 每次点击新节点时重置激活状态
  activeId.value = activeId.value === item.id ? null : item.id;
  console.log("点击节点:", item);
  console.log("点击文件后：", staticFiles.value);
  router.push({
    name: "content",
    params: { fileId: item.id },
    //query: { t: Date.now() } // 防止相同路径缓存
  });
};
import { useRoute } from "vue-router";
const route = useRoute(); // 获取当前路由对象
// 新增路由监听同步状态
//console.log('你好',route.params.fileId)
watch(
  () => route.params.fileId,
  (newId: any, oldId: any) => {
    if (newId !== oldId) {
      // 仅在id实际变化时更新
      activeId.value = Array.isArray(newId) ? newId[0] || null : newId || null;
    }
  },
  { immediate: true }
);
onMounted(fetchText);

// 解决文件右键点击无响应
import eventBus from '@/event-bus';

onMounted(() => {
  // 监听事件总线事件
  eventBus.on('node-contextmenu', (event: MouseEvent, node: FileItem) => {
    handleNodeContextMenu(event, node);
  });
});

// import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElDivider } from 'element-plus';
// import { useContextMenu } from '@vueuse/core';
// 状态管理
//const visible = ref(true);
const contextMenuX = ref(0);
const contextMenuY = ref(0);
const contextMenuData = ref<FileItem | null>(null);
const contextMenuVisible = ref(false);
// 清理全局事件监听器
// onUnmounted(() => {
//   document.removeEventListener('click', handleGlobalClick);
//   document.removeEventListener('contextmenu', handleGlobalContextMenu);
// });
// 处理右键菜单
const handleNodeContextMenu = (event: MouseEvent, node: FileItem) => {
  event.preventDefault();
  // 记录右键菜单位置和数据
  contextMenuX.value = event.clientX;
  contextMenuY.value = event.clientY;
  contextMenuData.value = node;
  console.log('收到右键菜单事件:', node);
  // 显示右键菜单
  contextMenuVisible.value = true;
};

// 关闭右键菜单
const closeContextMenu = () => {
  contextMenuVisible.value = false;
};

// 新建文件相关状态
const createFileModalVisible = ref(false);
const newFileName = ref('新文件.txt');
const currentParentNode = ref<FileItem | null>(null);
// 打开新建文件模态框
const openCreateFileModal = (parentNode: FileItem) => {
  console.log('新文件')
  currentParentNode.value = parentNode;
  newFileName.value = '新文件.txt'; // 默认值
  createFileModalVisible.value = true;
  contextMenuVisible.value = false; // 关闭上下文菜单
};
// 确认创建文件
const confirmCreateFile = () => {
  if (!currentParentNode.value) return;
  
  const parentNode = currentParentNode.value;
  const fileName = newFileName.value.trim();
  
  if (!fileName) {
    // 可以添加错误提示
    return;
  }
  
  const newFile: FileItem = {
    id: `file_${Date.now()}`,
    name: fileName,
    isFolder: false,
    content: ''
  };
  
  staticFiles.value = updateTreeWithNewItem(
    staticFiles.value,
    parentNode.id,
    newFile
  );
  
  // 展开父文件夹
  if (!parentNode.isOpen) {
    const updatedParent: FileItem = {
      ...parentNode,
      isOpen: true
    };
    onNodeUpdate(updatedParent);
  }

  // 关闭模态框
  createFileModalVisible.value = false;
  currentParentNode.value = null;
};
// 新建文件
const handleAddFile = () => {
  const parentNode = contextMenuData.value;
  if (!parentNode || !parentNode.isFolder) return;
  console.log(parentNode.id)
  router.push({
    name: "file-manager",
    params: { fileId: parentNode.id },
    //query: { t: Date.now() } // 防止相同路径缓存
  });
  // openCreateFileModal(parentNode);
  // const newFile: FileItem = {
  //   id: `file_${Date.now()}`,
  //   name: '新文件.txt',
  //   isFolder: false,
  //   content: ''
  // };
  
  // staticFiles.value = updateTreeWithNewItem(
  //   staticFiles.value,
  //   parentNode.id,
  //   newFile
  // );
  
  // // 展开父文件夹
  // if (!parentNode.isOpen) {
  //   const updatedParent: FileItem = {
  //     ...parentNode,
  //     isOpen: true
  //   };
  //   onNodeUpdate(updatedParent);
  // }

  // // 确保 DOM 更新完成后再执行其他操作
  // nextTick(() => {
  //   closeContextMenu();
  // });
};

// 新建文件夹
// const handleAddFolder = () => {
//   const parentNode = contextMenuData.value;
//   if (!parentNode || !parentNode.isFolder) return;
  
//   const newFolder: FileItem = {
//     id: `folder_${Date.now()}`,
//     name: '新文件夹',
//     isFolder: true,
//     children: []
//   };
  
//   staticFiles.value = updateTreeWithNewItem(
//     staticFiles.value,
//     parentNode.id,
//     newFolder
//   );
  
//   // 展开父文件夹
//   if (!parentNode.isOpen) {
//     const updatedParent: FileItem = {
//       ...parentNode,
//       isOpen: true
//     };
//     onNodeUpdate(updatedParent);
//   }
// };

import { ElMessage } from "element-plus";
// 状态管理
const createRootFolderVisible = ref(false);
const newFolderName = ref('');

// 打开创建文件夹模态框
const openCreateRootFolderModal = () => {
  newFolderName.value = '';
  console.log('新建文件夹');
  createRootFolderVisible.value = true;
};

// 关闭模态框
// const handleCloseCreateFolder = (done: Function) => {
//   createRootFolderVisible.value = false;
//   done();
// };
import axios from 'axios';

// 创建根文件夹
const createRootFolder = async() => {
  const name = newFolderName.value.trim();
  if (!name) {
    ElMessage.warning('请输入文件夹名称');
    return;
  }
  // 创建新文件夹对象
  const newFolder = {
    id: `folder_${Date.now()}`,
    name,
    isFolder: true,
    isOpen: true,
    children: []
  };
  
  // 添加到静态文件列表
  staticFiles.value = [...staticFiles.value, newFolder];
  
  // 关闭模态框
  createRootFolderVisible.value = false;
  try {
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

    const response = await axios.post(`http://26.143.62.131:8080/file/project/insert?name=${name}`, 
      {},
      // {
      // name: name,
      // //parentId: parentId || null, // 根文件夹的 parentId 为 null
      // },
      config
    );
    // 提示成功
    console.log('返回：',response.data.data)
    newFolder.id = response.data.data;
    console.log('文件夹：',newFolder);
    ElMessage.success(`文件夹 "${name}" 创建成功`);
    return response.data;
  } catch (error) {
    console.log('创建文件夹失败:', error);
    // 处理 token 过期或无效的情况
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录状态已过期，请重新登录');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      router.push('/login');
    }
    throw error;
  }
};


// 重命名
const handleRename = () => {
  const node = contextMenuData.value;
  console.log('node:',node)
  if (!node) return;
  
  const newName = prompt('请输入新名称:', node.name);
  if (!newName || newName === node.name) return;
  
  const updatedNode: FileItem = {
    ...node,
    name: newName
  };
  
  staticFiles.value = updateTreeItem(
    staticFiles.value,
    updatedNode
  );
};

// 删除
const handleDelete = () => {
  const node = contextMenuData.value;
  if (!node) return;
  
  if (!confirm(`确定要删除 "${node.name}" 吗？`)) return;
  
  staticFiles.value = removeTreeNode(
    staticFiles.value,
    node.id
  );
  
  // 如果删除的是当前激活的文件，重置activeId
  if (activeId.value === node.id) {
    activeId.value = null;
  }
};

// 辅助函数：在树中添加新节点
const updateTreeWithNewItem = (nodes: FileItem[], parentId: string, newItem: FileItem): FileItem[] => {
  return nodes.map(node => {
    if (node.id === parentId) {
      return {
        ...node,
        children: [...(node.children || []), newItem]
      };
    }
    if (node.children) {
      return {
        ...node,
        children: updateTreeWithNewItem(node.children, parentId, newItem)
      };
    }
    return node;
  });
};

// 辅助函数：从树中移除节点
const removeTreeNode = (nodes: FileItem[], nodeId: string): FileItem[] => {
  return nodes.filter(node => {
    if (node.id === nodeId) {
      return false;
    }
    if (node.children) {
      return {
        ...node,
        children: removeTreeNode(node.children, nodeId)
      };
    }
    return true;
  });
};

// 点击其他区域关闭右键菜单
document.addEventListener('click', closeContextMenu);
// document.addEventListener('click', (event) => {
//   if (!contextMenuVisible.value) return;
  
//   // 检查点击是否发生在右键菜单外部
//   const contextMenuElement = document.querySelector('.context-menu');
//   if (contextMenuElement && !contextMenuElement.contains(event.target as Node)) {
//     closeContextMenu();
//   }
// });

// // 右键点击其他区域关闭右键菜单
// document.addEventListener('contextmenu', (event) => {
//   if (contextMenuVisible.value) {
//     event.preventDefault();
//     closeContextMenu();
//   }
// });
</script>

<template>
  
  <!-- 2.左侧文件管理 -->
  <div class="file-manager1">
    <!-- 顶部路径导航 -->
    <div class="path-bar">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item v-for="(item, index) in currentPath" :key="index">
          {{ item }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 文件树 -->
    <div class="file-tree" @contextmenu.prevent>
      <TreeNode
        v-for="item in staticFiles"
        :key="item.id"
        :item="item"
        :active-id="activeId"
        @update-node="onNodeUpdate"
        @node-click="handleNodeClick" 
        @node-contextmenu="handleNodeContextMenu" />
    </div>

    <!-- 底部添加文件夹按钮 -->
    <div class="add-folder-button">
      <el-button type="primary" @click="openCreateRootFolderModal">
        <el-icon><Plus /></el-icon>添加文件夹
      </el-button>
    </div>

    <!-- 底部设置按钮 -->
    <!-- <div class="settings-footer">
      <el-button type="primary" @click="showSettings">
        <el-icon><Setting /></el-icon>上传文件
      </el-button>
    </div> -->
  </div>

  <!-- 创建根文件夹模态框 -->
  <div 
    v-if="createRootFolderVisible" 
    class="custom-modal-overlay"
    @click.self="createRootFolderVisible = false"
  >
    <div class="custom-modal">
      <div class="modal-header">
        <h3>创建文件夹</h3>
        <button @click="createRootFolderVisible = false">×</button>
      </div>
      <div class="modal-body">
        <input
          v-model="newFolderName"
          placeholder="请输入文件夹名称"
          class="folder-input"
        />
      </div>
      <div class="modal-footer">
        <button @click="createRootFolderVisible = false">取消</button>
        <button @click="createRootFolder">确定</button>
      </div>
    </div>
  </div>

  <!-- 右键菜单 -->
  <div
    v-show="contextMenuVisible"
    class="context-menu"
    :style="{
      left: `${contextMenuX}px`,
      top: `${contextMenuY}px`
    }"
  >
    <ul>
      <li v-if="contextMenuData?.isFolder" @click="handleAddFile">
        <i class="fa fa-file-o mr-2"></i>新建文件
      </li>
      <li v-if="contextMenuData" @click="handleRename">
        <i class="fa fa-pencil mr-2"></i>重命名
      </li>
      <li v-if="contextMenuData" @click="handleDelete">
        <i class="fa fa-trash-o mr-2"></i>删除
      </li>
    </ul>
  </div>

   <!-- 创建文件模态框 -->
  <div v-show="createFileModalVisible" class="modal-overlay" @click.self="createFileModalVisible = false">
    <div class="modal-content">
      <h3>新建文件</h3>
      <div class="modal-body">
        <div class="form-group">
          <label>文件名:</label>
          <input 
            type="text" 
            v-model="newFileName" 
            class="form-control"
            placeholder="请输入文件名"
          />
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-cancel" @click="createFileModalVisible = false">取消</button>
        <button class="btn btn-primary" @click="confirmCreateFile">创建</button>
      </div>
    </div>
  </div>

  <!-- 临时内容 -->
  <div v-if="route.path == '/projects'" class="temporary-container">
    请打开文件
  </div>
  <!-- 路由内容 -->
  <router-view :key="$route.fullPath" v-slot="{ Component }">
    <keep-alive>
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </keep-alive>
  </router-view>
</template>

<style>
/* 自定义模态框样式 */
.custom-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.custom-modal {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 300px;
  max-width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.modal-header button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
}

.modal-body {
  padding: 15px;
}

.folder-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 15px;
  border-top: 1px solid #eee;
}

.modal-footer button {
  margin-left: 10px;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-footer button:first-child {
  background-color: #f5f7fa;
  color: #606266;
}

.modal-footer button:last-child {
  background-color: #409eff;
  color: white;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 300px;
  max-width: 90%;
}

.modal-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.modal-body {
  padding: 15px;
}

.modal-footer {
  padding: 15px;
  text-align: right;
  border-top: 1px solid #eee;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 8px;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-cancel {
  background-color: #f5f7fa;
  color: #606266;
}
/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 6px 0;
  z-index: 1000;
  min-width: 120px;
  display: block; /* 确保菜单是可见的 */
}

/* 如果使用v-if控制显示，确保没有其他样式覆盖 */
.context-menu[style*="display: none"] {
  display: none !important;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
/*2. 主页面左侧栏*/
.file-manager1 {
  position: fixed;
  width: 20%;
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
    /* 添加展开动画 */
    .children {
      overflow: hidden;
      transition: all 0.3s ease-in-out;
      max-height: 0;
    }

    .children-show {
      max-height: 1000px; /* 根据实际内容调整 */
    }
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

/*无数据显示*/
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

/* 滚动条美化*/
.fulltext-scroll::-webkit-scrollbar {
  width: 8px;
  background: #f1f1f1;
}

.fulltext-scroll::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
  &:hover {
    background: #a8a8a8;
  }
}
</style>
