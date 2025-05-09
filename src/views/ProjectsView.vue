<script setup>    
  import {ref} from 'vue';
  import { watch } from 'vue'
  //哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈
  import { useRouter } from 'vue-router'
  const router = useRouter()
  import { onMounted } from 'vue'
  //import { debounce } from 'lodash';

  // 组件中新增展开状态存储
  //const expandedNodes = ref(new Set());

  // 设置点击事件
  const showSettings = () => {
      alert('你点击了系统设置按钮')
  }

  // 引入递归组件（需确认组件文件路径）
  import TreeNode from '@/components/TreeNode.vue'; 
  const staticFiles = ref()
  // 请求数据
  // 递归处理函数（独立封装）
  const processChildren = (children) => {
    //console.log('children',children)
    return children.map(child => ({
      ...child, // 保留原始属性
      isOpen: child.isOpen ?? false,//expandedNodes.value[child.id] ?? false, // 新增展开状态字段（默认折叠）
      isFolder: child.isFolder || false, // 默认非文件夹
      children: child.children ? processChildren(child.children) : [] // 递归关键点
    }));
  };
  //const isFetched = ref(false)
  const fetchText = async () => {
    //if (isFetched.value) return // 添加缓存判断
      try {
        const response2 = await fetch('/mock/contents.json');
        // 关键校验：状态码和内容类型
        if (!response2.ok) console.log(`HTTP错误: ${response2.status}`);
          const contentType = response2.headers.get('Content-Type');
        if (!contentType?.includes('application/json')) {
          console.log('响应非 JSON 格式');
        }
        const jsonData = await response2.json();
        // 使用ref包裹整个响应数据
        staticFiles.value = jsonData.map(item => ({
          id: item.id,
          name: item.name,
          isFolder: item.isFolder || false,
          children: item.children ? processChildren(item.children) : []
        }));
        console.log('请求json数据:',staticFiles.value)
        //isFetched.value = true
      } catch (error) {
          console.error('请求失败:', error)
      }
  }
  // 组件处理数据时添加展开状态
  const onNodeUpdate = (updatedNode) => {
    console.log('接收到更新:', updatedNode);
    // if (updatedNode.isOpen) {
    //   expandedNodes.value.add(updatedNode.id);
    // } else {
    //   expandedNodes.value.delete(updatedNode.id);
    // }
    staticFiles.value = staticFiles.value.map(item => 
      updateTreeItem(item, updatedNode)
    );
    console.log('更新后的:',staticFiles.value)
  };

  // 递归更新树节点（核心函数）
  const updateTreeItem = (node, target) => {
    // console.log('当前节点:', node.id, '目标节点:', target.id);
    if (node.id === target.id){
      // 创建新对象触发响应式更新
      return {
        ...node,
        isOpen: target.isOpen, // 直接使用新状态
        // 保留原有 children 的引用（避免重建整棵树）
        children: node.children 
        // children: node.children?.map(child => ({
        //   ...child,
        //   // 保持子节点原有状态（不强制继承父级展开状态）
        // }))
      };
    }
    if (node.children) {
      return {
        ...node,
        children: node.children.map(child => 
          updateTreeItem(child, target)
        )
      };
    }
    return node;
  };


  const currentPath = ref(['示例', '树形文件'])//根目录
  const activeId = ref(null) // 统一管理激活状态
  const handleNodeClick = (item) => {
    // 每次点击新节点时重置激活状态
    activeId.value = activeId.value === item.id ? null : item.id
    console.log('点击节点:', item)
    console.log('点击文件后：',staticFiles.value)
    router.push({ 
      name: 'content',
      params: { fileId: item.id },
      //query: { t: Date.now() } // 防止相同路径缓存
    })
    
  }
  import { useRoute } from 'vue-router'
  const route = useRoute() // 获取当前路由对象
  // 新增路由监听同步状态
  //console.log('你好',route.params.fileId)
  watch(
    () => route,
    (newId, oldId) => {
      if (newId !== oldId) { // 仅在id实际变化时更新
        activeId.value = newId || null
      }
    },
    { immediate: true }
  )
  onMounted(fetchText)
  // const isFetched = ref(false);
  // onMounted(() => {
  //   if (!isFetched.value) {
  //     fetchText();
  //     isFetched.value = true;
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
    
    <!-- 文件树示例 -->
    <div class="file-tree" >
      <TreeNode 
        v-for="item in staticFiles" 
        :key="item.id" 
        :item="item"
        :active-id="activeId"
        @update-node="onNodeUpdate"
        @node-click="handleNodeClick"
      />
    </div>

    <!-- 底部设置按钮 -->
    <div class="settings-footer">
        <el-button type="primary" @click="showSettings">
        <el-icon><Setting /></el-icon>系统设置
        </el-button>
    </div>
    
  </div>
  <!-- 临时内容 -->
  <div v-if="route.path == '/projects'" class="temporary-container">请打开文件</div>
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
      height: calc(100vh - 60px);  /* 占满导航栏下方全部空间 */
      display: flex;
      flex-direction: column;
      justify-content: center;     /* 新增：内部内容垂直居中 */
      align-items: center;         /* 可选：水平居中 */
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
    &:hover { background: #a8a8a8; }
  }
</style>