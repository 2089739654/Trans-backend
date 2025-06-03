<script setup lang="ts">
    import { ref, reactive, computed } from 'vue';
    import { ElMessage } from 'element-plus';
    import axios from 'axios';

    // 模拟用户信息
    const userInfo = reactive({
        avatar: 'https://example.com/avatar.jpg',
        username: 'user123',
        nickname: '翻译小能手',
        userId: 123
    });

    // 编辑状态
    const editMode = ref(false);
    const tempNickname = ref(userInfo.nickname);

    // 模拟翻译记录
    const translationRecords = ref([
    {
        id: 1,
        originalText: 'Hello, world!',
        translatedText: '世界，你好！',
        createTime: 1689753600000
    },
    {
        id: 2,
        originalText: 'Good morning',
        translatedText: '早上好',
        createTime: 1689840000000
    }
    ]);

    const searchKeyword = ref('');

    // 过滤后的记录
    const filteredRecords = computed(() => {
    return translationRecords.value.filter(record => 
        record.originalText.includes(searchKeyword.value) || 
        record.translatedText.includes(searchKeyword.value)
    );
    });

    // 上传头像配置
    const uploadUrl = 'https://api.example.com/upload-avatar';

    const beforeAvatarUpload = (file: File) => {
    const isJpg = file.type === 'image/jpeg';
    const isPng = file.type === 'image/png';
    const isLt2M = file.size / 1024 / 1024 < 2;

    if (!isJpg && !isPng) {
        ElMessage.error('只能上传JPG/PNG格式图片');
        return false;
    }
    if (!isLt2M) {
        ElMessage.error('图片大小不能超过2MB');
        return false;
    }
    return true;
    };

    const handleAvatarSuccess = (response: any, file: File) => {
    userInfo.avatar = URL.createObjectURL(file);
    editMode.value = false;
    ElMessage.success('头像更新成功');
    };

    // 保存用户信息
    const saveUserInfo = () => {
    // 模拟保存请求
    axios.post('/api/user/update', {
        userId: userInfo.userId,
        nickname: tempNickname.value
    }).then(() => {
        userInfo.nickname = tempNickname.value;
        editMode.value = false;
        ElMessage.success('资料保存成功');
    }).catch(() => {
        ElMessage.error('保存失败，请重试');
    });
    };

    // 删除翻译记录
    const deleteRecord = (recordId: number) => {
    translationRecords.value = translationRecords.value.filter(
        record => record.id !== recordId
    );
    ElMessage.success('记录已删除');
    };

    // 时间格式化
    const formatTime = (row: any, column: any) => {
    return new Date(row.createTime).toLocaleString();
    };
</script>

<template>
  <div class="personal-center" style="margin-top: 60px;">
    <!-- 个人信息区域 -->
    <el-card class="info-card">
      <div class="header">
        <el-avatar
          size="large"
          :src="userInfo.avatar"
          class="user-avatar"
          @click="handleEditAvatar"
        />
        <div class="user-info">
          <h3 class="username">{{ userInfo.username }}</h3>
          <p class="nickname">昵称：{{ userInfo.nickname }}</p>
          <el-button
            type="primary"
            size="small"
            @click="editMode = true"
          >编辑资料</el-button>
        </div>
      </div>

      <!-- 编辑状态 -->
      <div v-if="editMode" class="edit-form">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar v-if="userInfo.avatar" :size="large" :src="userInfo.avatar" />
          <i v-else class="el-icon-plus avatar-uploader-icon" />
        </el-upload>

        <el-input
          v-model="tempNickname"
          label="昵称"
          placeholder="请输入新昵称"
          class="nickname-input"
        />

        <el-button
          type="primary"
          size="small"
          @click="saveUserInfo"
        >保存</el-button>
        <el-button
          size="small"
          @click="editMode = false"
        >取消</el-button>
      </div>
    </el-card>

    <!-- 翻译记录区域 -->
    <el-card class="record-card" style="margin-top: 24px;">
      <div class="record-header">
        <h3>翻译记录</h3>
        <el-input
          v-model="searchKeyword"
          placeholder="搜索原文/译文"
          prefix-icon="el-icon-search"
          class="search-input"
        />
      </div>

      <el-table
        :data="filteredRecords"
        stripe
        border
        size="small"
        class="record-table"
      >
        <el-table-column
          prop="originalText"
          label="原文"
          width="300"
        />
        <el-table-column
          prop="translatedText"
          label="译文"
          width="400"
        />
        <el-table-column
          prop="createTime"
          label="时间"
          width="180"
          :formatter="formatTime"
        />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              size="mini"
              type="danger"
              @click="deleteRecord(scope.row.id)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.personal-center {
  padding: 24px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.info-card, .record-card {
  padding: 24px;
}

.header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.user-avatar {
  width: 100px;
  height: 100px;
}

.user-info {
  flex-grow: 1;
}

.edit-form {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.nickname-input {
  margin-right: 16px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-input {
  width: 300px;
}

.record-table {
  margin-top: 16px;
}
</style>