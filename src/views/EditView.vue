<script setup lang="ts">
import { ref } from "vue";
//哈哈哈哈哈哈哈哈哈哈哈哈十分
import { onMounted } from "vue";
import axios from "axios";

import { type OriginalText } from "../types/original_text";
import { type TextEntry } from "../types/original_text";

// 数据内容
const textText = ref<OriginalText>({
  title: "",
  fullText: [],
});

// 哈哈哈哈哈哈哈哈哈哈哈哈
import { watch } from "vue";
import { useRoute } from "vue-router";
const route = useRoute();
const fetchText = watch(
  () => route.params.fileId,
  async (newId: string) => {
    console.log(newId);
    try {
      // 根据ID调用API获取文件内容   http://26.143.62.131:8080/file/getTransText
      const response = await fetch(`/documents/${newId}.json`); //(`/api/docs/${newId}`);
      console.log(response)
      // 关键校验：状态码和内容类型
      if (!response.ok) console.log(`HTTP错误: ${response.status}`);
      const contentType = response.headers.get("Content-Type");
      if (!contentType?.includes("application/json")) {
        console.log("响应非 JSON 格式");
      }
      const jsonData = await response.json();
      // 使用ref包裹整个响应数据
      textText.value = {
        title: jsonData.title,
        fullText: jsonData.fullText.map((item: TextEntry) => ({
          originalText: item.originalText,
          baseText: item.baseText,
          finalText: item.finalText,
        })),
      };
      console.log("请求json数据:", textText.value);
      fullTranslation.value =
        textText.value?.fullText[0]?.finalText +
        textText.value?.fullText[1]?.finalText +
        textText.value?.fullText[2]?.finalText;
      sentencesLength.value = textText.value.fullText.length;
    } catch (error) {
      console.error("请求失败:", error);
    }
  },
  { immediate: true }
);

import { Download } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";

//44444
// 示例导出数据（整段文本）
const fullTranslation = ref(
  `这里是一段完整的翻译结果文本示例...Vue3的组合式API提供了更强大的逻辑封装能力，通过setup函数可以更好地组织代码。相较于Options API，Composition API在复杂组件开发中表现出更好的可维护性。同时，Vue3对TypeScript的支持也更加完善，类型推断能力显著提升...`
);
// 当前句子总数
const sentencesLength = ref(0);
//2222

// 请求数据
// const fetchText = async () => {
//     try {
//         // const response = await axios.get('/mock/originalText.json')
//         // textText.value = response.data.fullText//[0].originalText // 注意数据结构匹配
//         const response2 = await fetch('/mock/originalText.json');
//         // 关键校验：状态码和内容类型
//         if (!response2.ok) console.log(`HTTP错误: ${response2.status}`);
//         const contentType = response2.headers.get('Content-Type');
//         if (!contentType?.includes('application/json')) {
//         console.log('响应非 JSON 格式');
//         }
//         const jsonData = await response2.json();
//         // 使用ref包裹整个响应数据
//         textText.value = {
//         title: jsonData.title,
//         fullText: jsonData.fullText.map(item => ({
//             originalText: item.originalText,
//             baseText: item.baseText,
//             finalText: item.finalText
//         }))
//         };
//         console.log('请求json数据:',textText.value)
//         fullTranslation.value = textText.value?.fullText[0]?.finalText + textText.value?.fullText[1]?.finalText + textText.value?.fullText[2]?.finalText
//         sentencesLength.value = textText.value.fullText.length
//     } catch (error) {
//         console.error('请求失败:', error)
//     }
// }

//333333
// 示例数据
const systemPrompt = ref(
  `系统提示：建议注意专业术语一致性...当前项目术语库匹配率：92%`
);
import { nextTick } from "vue";

const inputText = ref("");
// 提交数据
const handleSubmit = async () => {
  console.log("提交内容:", inputText.value);
  if (!inputText.value.trim()) {
    console.log("内容不能为空");
    ElMessage.warning("内容不能为空");
    return;
  }
  try {//http://26.143.62.131:8080/file/saveTransText
    const response = await axios.post("/api/submit", {
      content: inputText.value.trim(),
    });
    await nextTick(); // 等待DOM更新
    console.log("提交成功，返回：",response);
    ElMessage.success("提交成功！");
    inputText.value = ""; // 清空输入框
  } catch (error) {
    console.log("提交失败: " + error);
  }
};
// 当前索引
const currentIndex = ref(0);
// 上下项按钮操作方法
const prevItem = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};
const nextItem = () => {
  if (currentIndex.value < sentencesLength.value - 1) {
    currentIndex.value++;
  }
};

// console.log(1,textText.value.value)
// 导出功能
const exportTranslation = () => {
  const blob = new Blob([fullTranslation.value], { type: "text/plain" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `translation_${new Date().toISOString().slice(0, 10)}.txt`;
  link.click();
  ElMessage.success("导出成功");
};

onMounted(fetchText);
//console.log(2,textText.value.value)
</script>

<template>
  <!-- 3.中间编辑区 -->
  <div class="editor-container1">
    <!-- 操作按钮 -->
    <div class="controls">
      <button @click="prevItem">上一项</button>
      第{{ currentIndex + 1 }}句
      <button @click="nextItem">下一项</button>
    </div>
    <!-- 原文展示区 -->
    <div class="section original-text">
      <h3>原文内容</h3>
      <div class="content-box">
        {{ textText?.fullText[currentIndex]?.originalText }}
      </div>
    </div>

    <!-- 初译结果 -->
    <div class="section translation-result">
      <h3>初译结果</h3>
      <div class="content-box">
        {{ textText?.fullText[currentIndex]?.baseText }}
      </div>
    </div>

    <!-- 提示词 -->
    <div class="section prompt-area">
      <h3>提示建议</h3>
      <div class="content-box">{{ systemPrompt }}</div>
    </div>

    <!-- 输入与提交 -->
    <div class="input-group">
      <el-input
        v-model="inputText"
        placeholder="请输入修改内容"
        class="input-field"
        :rows="4"
        clearable />
      <el-button type="primary" @click="handleSubmit" class="submit-btn"
        >提交修改</el-button
      >
    </div>
  </div>

  <!-- 4.右侧结果全文展示 -->
  <div class="result-container1">
    <!-- 原文整段滚动区域 -->
    <div class="fulltext-scroll">
      <!-- <pre class="translation-content"> -->
      原文
      <div
        class="translation-content"
        v-for="(item, index) in textText?.fullText"
        :key="index">
        <span :class="{ 'highlighted-text': index == currentIndex }">
          {{ index + 1 }},{{ item.originalText }}
        </span>
      </div>
      <!-- </pre> -->
      <!-- <pre class="translation-content">
            1,{{ textText?.fullText[0]?.finalText }}
            2,{{ textText?.fullText[1]?.finalText }}
            3,{{ textText?.fullText[2]?.finalText }}
        </pre> -->
    </div>
    <!-- 译文整段滚动区域 -->
    <div class="fulltext-scroll">
      译文
      <!-- <pre class="translation-content"> -->
      <div
        class="translation-content"
        v-for="(item, index) in textText?.fullText"
        :key="index">
        <span :class="{ 'highlighted-text': index == currentIndex }">
          {{ index + 1 }},{{ item.finalText }}
        </span>
      </div>
    </div>
    <!-- 底部固定导出按钮 -->
    <div class="export-footer">
      <el-button type="primary" @click="exportTranslation">
        <el-icon><Download /></el-icon>导出全文
      </el-button>
    </div>
  </div>
</template>

<style>
/*3. 中间编辑区*/
.editor-container1 {
  position: fixed;
  left: 20%;
  top: 60px;
  width: 50%;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  border-left: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
  padding: 16px;

  .controls button {
    margin: 0 10px;
    padding: 8px 16px;
    background: #409eff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }

  .controls button:disabled {
    background: #c0c4cc;
    cursor: not-allowed;
  }
  .section {
    flex: 1;
    margin-bottom: 12px;
    border: 1px solid #ebeef5;
    border-radius: 4px;

    h3 {
      padding: 8px 12px;
      background: #f5f7fa;
      margin: 0;
      border-bottom: 1px solid #e4e7ed;
    }

    .content-box {
      text-align: left;
      padding: 12px;
      height: calc(100% - 42px);
      overflow-y: auto;
    }
  }

  .input-group {
    display: flex;
    gap: 12px;
    margin-top: auto;
    padding: 12px 0;

    .input-field {
      flex: 1;
    }

    .submit-btn {
      width: 120px;
    }
  }
}

/*4. 右侧结果全文展示*/
.result-container1 {
  position: fixed;
  left: 70%;
  top: 60px;
  width: 30%;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  border-left: 1px solid #e0e0e0;

  .fulltext-scroll {
    flex: 1;
    padding: 16px;
    overflow-y: auto;

    .translation-content {
      white-space: pre-wrap;
      line-height: 1.6;
      text-align: left;
      font-family: "Segoe UI", sans-serif;
      background: linear-gradient(180deg, #fff 0%, #f8f9fa 100%);
      padding: 12px;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      .highlighted-text {
        background-color: rgb(232, 232, 116); /* 高亮颜色 */
        padding: 2px 4px; /* 增加文字可读性 */
        border-radius: 3px; /* 圆角效果 */
      }
    }
  }

  .export-footer {
    padding: 16px;
    border-top: 1px solid #e0e0e0;
    background: #fff;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.03);
  }
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
