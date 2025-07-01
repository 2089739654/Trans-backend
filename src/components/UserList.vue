<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="avatar-wrapper">
      <span class="pname">用户项目组</span>
      <el-icon class="el-icon-arrow-down el-dropdown__icon">
        <ArrowDown />
      </el-icon>
    </div>
    
    <!-- 下拉菜单 -->
    <template #dropdown>
      <el-dropdown-menu>
        <!-- 项目组列表 -->
        <el-dropdown-item 
          v-for="group in userGroups" 
          :key="group.id" 
          :command="group"
        >
          <span>{{ group.name }} ({{ group.userIds.length }}人)</span>
          <div class="group-actions">
            <el-button 
              type="text" 
              @click.stop="handleEdit(group)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button 
              type="text" 
              @click.stop="handleDelete(group)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
            <el-button 
              type="text" 
              @click.stop="handleAddUser(group)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </el-dropdown-item>
        
        <!-- 添加新项目组 -->
        <el-dropdown-item disabled>
          <div class="divider"></div>
        </el-dropdown-item>
        <el-dropdown-item command="add">
          <el-icon><Plus /></el-icon>
          <span>添加项目组</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  
  <!-- 自定义添加项目组模态框 -->
  <div 
    v-if="addModalVisible" 
    class="custom-modal"
    @click.self="addModalVisible = false"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h3>添加项目组</h3>
        <el-button 
          type="text" 
          @click="addModalVisible = false"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="modal-body">
        <el-form :model="addGroupForm" :rules="addFormRules" ref="addFormRef">
          <el-form-item label="项目组名称" prop="name">
            <el-input v-model="addGroupForm.name" placeholder="请输入项目组名称" />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="modal-footer">
        <el-button @click="addModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddGroup">确定</el-button>
      </div>
    </div>
  </div>
  
  <!-- 自定义编辑项目组模态框 -->
  <div 
    v-if="editModalVisible" 
    class="custom-modal"
    @click.self="editModalVisible = false"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h3>编辑项目组</h3>
        <el-button 
          type="text" 
          @click="editModalVisible = false"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="modal-body">
        <el-form :model="editGroupForm" :rules="editFormRules" ref="editFormRef">
          <el-form-item label="项目组名称" prop="name">
            <el-input v-model="editGroupForm.name" placeholder="请输入项目组名称" />
          </el-form-item>
          <el-form-item label="项目组创建时间">
                <div class="form-display-field">
                  {{ formatDate(editGroupForm.createTime) }}
                </div>
          </el-form-item>
          <el-form-item label="创建者ID">
            <div class="form-display-field">
              {{ editGroupForm.creatorId }}
            </div>
          </el-form-item>
          
          <el-form-item label="项目组成员">
            <el-tag 
              v-for="user in editGroupForm.users" 
              :key="user.id" 
              closable 
              @close="removeUser(user.id)"
            >
              {{ user.name+" ("+user.id+")" }}
            </el-tag>
          </el-form-item>
        </el-form>
      </div>
      
      <div class="modal-footer">
        <el-button @click="editModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEditGroup">确定</el-button>
      </div>
    </div>
  </div>

  <!-- 自定义添加用户到项目组模态框 -->
  <div 
    v-if="userAddModalVisible" 
    class="custom-modal"
    @click.self="userAddModalVisible = false"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h3>添加用户到 "{{ currentEditingGroup.name || '项目组' }}"</h3>
        <el-button 
          type="text" 
          @click="userAddModalVisible = false"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="modal-body">
        <el-form :model="userAddForm" :rules="userAddFormRules" ref="userAddFormRef">
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="userAddForm.userId" placeholder="请输入用户ID" />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="modal-footer">
        <el-button @click="userAddModalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddUser">确定</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown, 
  Edit, 
  Delete, 
  Plus,
  Close,
} from '@element-plus/icons-vue'

// 在script setup中添加
const formatDate = (timestamp: number) => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleString(); // 格式化为本地日期时间格式
};

const router = useRouter()

// 用户项目组数据 - 添加了userIds属性
const userGroups = ref([
  { id: 'group1', name: '前端开发组', creatorId:'',createTime:'',userIds: ['user1', 'user2'] },
  { id: 'group2', name: '后端开发组',creatorId:'',createTime:'', userIds: ['user3', 'user4'] },
  { id: 'group3', name: '测试组', userIds: ['user5'] },
  { id: 'group4', name: '产品组',creatorId:'', createTime:'',userIds: [] }
])

// 当前正在编辑的项目组
const currentEditingGroup = ref({ id: '', name: '', users: [] })

// 添加项目组模态框
const addModalVisible = ref(false)
const addGroupForm = reactive({
  name: '',
})
const addFormRules = reactive({
  name: [
    { required: true, message: '请输入项目组名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
})
const addFormRef = ref()

// 编辑项目组模态框
const editModalVisible = ref(false)
const editGroupForm = reactive({
  id: '',
  name: '',
  createTime:'',
  creatorId:'',
  users: []
})
const editFormRules = reactive({
  name: [
    { required: true, message: '请输入项目组名称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
})
const editFormRef = ref()

// 添加用户到项目组模态框
const userAddModalVisible = ref(false)
const userAddForm = reactive({
  userId: '',
})
const userAddFormRules = reactive({
  userId: [
    { required: true, message: '请输入用户ID', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
})
const userAddFormRef = ref()

// 生命周期钩子：初始化时获取用户项目组
onMounted(() => {
  fetchUserGroups()
})
import axios from 'axios'
import { useAuthStore } from '@/stores/token';

const authStore = useAuthStore();
// 获取用户项目组数据
const fetchUserGroups = async () => {
  try {
    // 模拟API请求
    const token = authStore.token
    // if (!token) {
    //   ElMessage.error('请先登录');
    //   router.push('/login');
    //   return;
    // }
    console.log('请求总督文件151253：',token);
    const response = await fetch(
      'http://26.143.62.131:8080/admin/getGroupList',
      {
        method: 'GET', // 指定请求方法
        headers: {
          'token': token || '', // 添加 token 头
        }
      }
    );

    const data = await response.json()
    console.log("获取项目组数据:", data.data)
    // 转换数据格式
    const transformedGroups = data.data.map(item => ({
      id: item.id,
      name: item.name,
      creatorId: item.creatorId,
      createTime: item.createTime,
      userIds: [] // 初始化空的用户ID数组，后续可以通过其他API填充
    }));
    // 更新到ref对象中
    userGroups.value = transformedGroups;
    } catch (error) {
      console.error('获取用户项目组失败:', error)
      ElMessage.error('获取项目组列表失败')
    }
}

// 处理菜单项点击
const handleCommand = (command: any) => {
  if (command === 'add') {
    // 打开添加项目组模态框
    addModalVisible.value = true
    console.log("添加项目组")
  } else {
    // 切换到项目组
    console.log('切换到项目组:', command)
    router.push({
      name: "Projects",
      params: { teamId: command.id }
    })
  }
}

// 处理编辑项目组
const handleEdit = async (group: any) => {
  // 保存当前编辑的项目组引用
  currentEditingGroup.value = group
  
  // 填充编辑表单
  editGroupForm.id = group.id
  editGroupForm.name = group.name
  editGroupForm.createTime = group.createTime
  editGroupForm.creatorId = group.creatorId
  const token = authStore.token
  if (!token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  const response = await fetch(
    `http://26.143.62.131:8080/admin/getUserByGroupId?groupId=${group.id}`,
    {
      method: 'GET', // 指定请求方法
      headers: {
        'token': token || '', // 添加 token 头
      }
    }
  );
  const data = await response.json()
  console.log("获取用户组数据:", data.data)
  // 转换用户数据格式
  const transformUserList = (apiUserList) => {
    return apiUserList.map(user => ({
      id: user.id,          // 用户ID
      name: user.userName,  // 用户名称
      // 可以根据需要添加更多字段
    }));
  };
  if(data.code==200){
    // 转换用户数据格式
    const transformedUsers = transformUserList(data.data);
    editGroupForm.users = transformedUsers; 
  }
  
  console.log('展示：',editGroupForm);
  // 打开编辑模态框
  editModalVisible.value = true
  console.log("点击编辑：", editModalVisible.value)
}

// 处理删除项目组
const handleDelete = (group: any) => {
  ElMessageBox.confirm(
    `确定要删除项目组 "${group.name}" 吗？这将删除所有关联的项目和文件。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 模拟API请求
      const response = await fetch(`/api/user/groups/${group.id}`, {
        method: 'DELETE'
      })
      
      const data = await response.json()
      if (data.code === 0) {
        // 从本地数据中删除
        userGroups.value = userGroups.value.filter(g => g.id !== group.id)
        ElMessage.success('项目组删除成功')
      } else {
        ElMessage.error(data.message || '删除失败')
      }
    } catch (error) {
      console.error('删除项目组失败:', error)
      ElMessage.error('删除项目组失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 处理添加用户到项目组
const handleAddUser = (group: any) => {
  console.log("+",group)
  // 保存当前编辑的项目组引用
  currentEditingGroup.value.name = group.name
  currentEditingGroup.value.id = group.id
  console.log('添加用户：',currentEditingGroup.value)
  // 重置表单
  userAddForm.userId = ''
  
  // 打开添加用户模态框
  userAddModalVisible.value = true
  console.log("点击添加用户：", userAddModalVisible.value)
}

// 从项目组中移除用户
const removeUser = async (userId: string) => {
  console.log("移除成员：",userId,editGroupForm)
  const updatedGroup = {
    groupId:editGroupForm.id,
    userId: userId
  }
  console.log('提交的',updatedGroup);
  const token = authStore.token
  if (!token) {
    ElMessage.error('请先登录');
    router.push('/login');
    return;
  }
  // 设置请求头
  const config = {
    headers: {
      token: token,
    },
  };
  // 模拟API请求
  const response = await axios.post(
    `http://26.143.62.131:8080/admin/deleteUserFromGroup`, 
    updatedGroup, 
    config
  )
  console.log("删除用户返回：",response.data)
  if(response.data.code==200){
    ElMessage.success('成员移除成功');
    // 重新加载项目组数据
    await handleEdit(currentEditingGroup.value);
  }else {
    ElMessage.error(response.data.message || '移除成员失败');
  }
}

// 提交添加项目组
const submitAddGroup = () => {
  addFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      console.log('添加表单验证失败')
      return
    }
    
    try {
      // 准备数据
      // const newGroup = {
      //   id: `group${Date.now()}`,
      //   name: addGroupForm.name,
      //   userIds: [] // 新项目组初始用户列表为空
      // }
      const groupName = addGroupForm.name
      const token = localStorage.getItem("token")
      if (!token) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }
      // 模拟API请求
      const response = await fetch(`http://26.143.62.131:8080/admin/createGroup?groupName=${groupName}`, {
        method: 'POST',
        headers: { token },
      })
      
      const data = await response.json()
      console.log('datass',data)
      if (data.code === 200) {
        // 添加到本地数据
        //userGroups.value.push(data.data)
        addModalVisible.value = false
        resetAddForm()
        ElMessage.success('项目组添加成功')
      } else {
        ElMessage.error(data.message || '添加失败')
      }
    } catch (error) {
      console.error('添加项目组失败:', error)
      ElMessage.error('添加项目组失败')
    }
  })
}

// 提交编辑项目组
const submitEditGroup = () => {
  editFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      console.log('编辑表单验证失败')
      return
    }
    
    try {
      // 准备数据
      const updatedGroup = {
        id: editGroupForm.id,
        name: editGroupForm.name,
        userIds: editGroupForm.userIds
      }
      
      // 模拟API请求
      const response = await fetch(`/api/user/groups/${editGroupForm.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedGroup)
      })
      
      const data = await response.json()
      if (data.code === 0) {
        // 更新本地数据
        const index = userGroups.value.findIndex(g => g.id === editGroupForm.id)
        if (index !== -1) {
          userGroups.value[index] = data.data
        }
        
        editModalVisible.value = false
        resetEditForm()
        ElMessage.success('项目组更新成功')
      } else {
        ElMessage.error(data.message || '更新失败')
      }
    } catch (error) {
      console.error('更新项目组失败:', error)
      ElMessage.error('更新项目组失败')
    }
  })
}

// 提交添加用户到项目组
const submitAddUser = () => {
  userAddFormRef.value?.validate(async (valid: boolean) => {
    if (!valid) {
      console.log('添加用户表单验证失败')
      return
    }
    console.log('添加用户：',currentEditingGroup.value)
    // 检查用户是否已存在于项目组中
    // if (currentEditingGroup.value.userIds.includes(userAddForm.userId)) {
    //   ElMessage.warning('该用户已存在于项目组中')
    //   return
    // }
    
    // try {
      // 准备数据
      const updatedGroup = {
        groupId:currentEditingGroup.value.id,
        userId: userAddForm.userId
      }
      console.log('提交的',updatedGroup);
      const token = authStore.token
      if (!token) {
        ElMessage.error('请先登录');
        router.push('/login');
        return;
      }
      // 设置请求头
      const config = {
        headers: {
          token: token,
        },
      };
      // 模拟API请求
      const response = await axios.post(
        `http://26.143.62.131:8080/admin/addUserToGroup`, 
        updatedGroup, 
        config
      )
      console.log("添加用户返回：",response.data)
      if (response.data.code === 200) {
        // 更新本地数据
        // const index = userGroups.value.findIndex(g => g.id === currentEditingGroup.value.id)
        // if (index !== -1) {
        //   userGroups.value[index] = updatedGroup
        // }
        
        userAddModalVisible.value = false
        userAddForm.userId = ''
        ElMessage.success('用户添加成功')
      } else {
        ElMessage.error(response.data.message || '添加失败')
      }
    // } catch (error) {
    //   console.error('添加用户失败:', error)
    //   ElMessage.error('添加用户失败')
    // }
  })
}

// 重置添加表单
const resetAddForm = () => {
  addGroupForm.name = ''
  addFormRef.value?.resetFields()
}

// 重置编辑表单
const resetEditForm = () => {
  editGroupForm.id = ''
  editGroupForm.name = ''
  editGroupForm.userIds = []
  editFormRef.value?.resetFields()
}
</script>

<style scoped>
.pname {
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.group-actions {
  float: right;
  display: flex;
  opacity: 0;
  transition: opacity 0.2s;
}

.el-dropdown-menu__item:hover .group-actions {
  opacity: 1;
}

/* 深度修改下拉菜单样式 */
:deep(.el-dropdown-menu__item) {
  min-width: 220px;
  padding: 8px 20px;
  
  .el-icon {
    margin-right: 8px;
    font-size: 16px;
  }
}

/* 分隔线样式 */
:deep(.el-dropdown-menu__item--divided) {
  margin-top: 6px;
  border-top: 1px solid var(--el-border-color-light);
}

/* 自定义模态框样式 */
.custom-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  width: 400px;
  max-width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--el-border-color-light);
}

.modal-footer button {
  margin-left: 10px;
}

/* 项目组成员标签样式 */
:deep(.el-tag) {
  margin-right: 5px;
  margin-bottom: 5px;
}
</style>