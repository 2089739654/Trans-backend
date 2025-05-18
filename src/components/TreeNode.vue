<!-- TreeNode.vue  -->
<template>
  <div 
  class="tree-node"
  :class="{ 
    'active-node': item.id === activeId && !item.isFolder,
    'folder-node': item.isFolder
  }"
  >
    <!-- 文件夹图标动态切换 -->
    <el-icon @click.stop="handleClick">
      <FolderOpened v-if="item.isFolder && item.isOpen"/>
      <Folder v-else-if="item.isFolder"/>
      <Document v-else/>
    </el-icon>
    
    <!-- 名称展示 -->
    <span @click="handleClick">{{ item.name }}</span>

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
  
<script setup>
  // <div 
  //   class="tree-node" 
  //   :class="{ 
  //     'active-node': item.id === activeId && !item.isFolder,
  //     'folder-node': item.isFolder
  //   }"
  //   @click.stop="handleClick" >
  //   <!-- 图标与名称 -->
  //   <el-icon v-if="item.isFolder"><Folder /></el-icon>
  //   <el-icon v-else><Document /></el-icon>
  //   <span>{{ item.name }}</span>

  //   <!-- 递归渲染子节点 @node-click="emitClick"-->
  //   <div v-if="item.children" class="children">
  //     <TreeNode 
  //       v-for="child in item.children" 
  //       :key="child.id" 
  //       :item="child"
  //       :active-id="activeId"
  //       @node-click="emit('node-click', $event)"
  //     />
  //   </div>
  // </div>
  import { ref } from 'vue'
  const props = defineProps({
    item: { // 接收当前节点数据
      type: Object,
      required: true
    },
    activeId: String,// 来自父组件的激活状态
  });

  // 节点自治激活状态
  const localActive = ref(false)



  const handleClick = () => {
    if (props.item.isFolder) {
      // 直接基于props.item创建新状态（无需深拷贝）
      const newItem = { 
        ...props.item, 
        isOpen: !props.item.isOpen 
      };
      //console.log('点击后状态:', newItem.isOpen);
      emit('update-node', newItem); // 通知父组件更新状态
      //return
    } else {
      console.log('我是孩子');
      if (props.activeId !== props.item.id) {
        localActive.value = !localActive.value
        emit('node-click', props.item)
      }
      // const newItem = { 
      //   ...props.item, 
      //   isOpen: true 
      // };
      // console.log('点击后状态:', newItem.isOpen);
      // emit('update-node', newItem); 
    }
  }

  const emit = defineEmits(['node-click', 'update-node'])
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