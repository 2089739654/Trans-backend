<!-- TreeNode.vue  -->
<template>
  <div 
  @contextmenu.stop="handleContextMenu"
  @click.stop="handleClick"
  class="tree-node"
  :class="{ 
    'active-node': item.id === activeId && !item.isFolder,
    'folder-node': item.isFolder
  }"
  >
    <!-- 文件夹图标动态切换 -->
    <el-icon >
      <FolderOpened v-if="item.isFolder && item.isOpen"/>
      <Folder v-else-if="item.isFolder"/>
      <Document v-else/>
    </el-icon>
    
    <!-- 名称展示 -->
    <span>{{ item.name }}</span>

    <!-- 子节点容器（根据展开状态显示） v-show="item.isFolder && item.isOpen" -->
    <div 
      :class="{ 'children-show': item.isFolder && item.isOpen }"
      class="children"
    >
      <TreeNode 
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :active-id="activeId"
        @node-click="emit('node-click', $event)"
        @update-node="emit('update-node', $event)"
        
      />
    </div>
  </div>
</template>
  
<script setup lang="ts">
  import eventBus from '@/event-bus';
  import { Folder, FolderOpened, Document } from '@element-plus/icons-vue';
  // 定义节点类型
  interface TreeNode {
    id: string;
    name: string;
    isFolder: boolean;
    isOpen?: boolean;
    children?: TreeNode[];
  }

  const props = defineProps<{
    item: TreeNode;
    //   type: Object,
    //   required: true
    // },
    activeId: String ,// 来自父组件的激活状态
  }>();

  const emit = defineEmits<{
    (e: 'node-click', node: TreeNode): void;
    (e: 'update-node', node: TreeNode): void;
    (e: 'node-contextmenu', event: MouseEvent, node: TreeNode): void;
  }>();

// 处理文件夹点击（展开/折叠）
// const handleFolderClick = (event: MouseEvent) => {
//   event.stopPropagation(); // 阻止冒泡到节点点击
//   console.log('文件夹')
//   if (props.item.isFolder) {
//     const updatedNode: TreeNode = {
//       ...props.item,
//       isOpen: !props.item.isOpen
//     };
    
//     emit('update-node', updatedNode);
//   }
// };

// // 处理节点点击（选中文件）
// const handleNodeClick = () => {
//   console.log('文件')
//   if (!props.item.isFolder) {
//     emit('node-click', props.item);
//   }
// };
// 处理节点点击（统一处理文件夹和文件点击）
const handleClick = (event: MouseEvent) => {
  if (props.item.isFolder) {
    // 文件夹点击：切换展开状态
    const updatedNode: TreeNode = {
      ...props.item,
      isOpen: !props.item.isOpen
    };
    emit('update-node', updatedNode);
  } else {
    // 文件点击：选中文件
    emit('node-click', props.item);
  }
};

// 处理右键菜单
const handleContextMenu = (event: MouseEvent) => {
  event.preventDefault();
  console.log(event)
  console.log('子组件右键事件触发，节点:', props.item);
  eventBus.emit('node-contextmenu', event, props.item);
  console.log('已触发 node-contextmenu 事件');
};
</script>
<style>
  .children {
    max-height: 0;
    opacity: 0;
    transition: all 0.3s ease;
    overflow: hidden;
  }
  .children-show {
    max-height: 1000px; /* 根据实际内容调整 */
    opacity: 1;
  }
</style>