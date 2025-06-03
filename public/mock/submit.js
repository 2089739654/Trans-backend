import Mock from 'mockjs'
//import { ref ,shallowRef } from 'vue'
import mockData from './originalText.json'// 加载数据文件

//const changeData = ref(shallowRef(mockData))

export default [
  {
    url: '/api/submit',
    method: 'post',
    response: ({ body }) => {
      console.log('[Mock] 接收数据:', body) // 添加日志输出
      // 修改第一条finalText
      mockData.fullText[0].finalText = body.content
      //changeData.value = mockData
      console.log('修改成功')
      try {
        return Mock.mock({
          code: 200,
          data: {
            id: '@guid',
            content: body.content,
            timestamp: +Mock.Random.date('T'),
            newdata: mockData,
          }
        })
      } catch (e) {
        console.error('[Mock Error]', e) // 捕获语法错误
        return { code: 500, message: 'Mock数据生成失败' }
      }
    }
  }
]