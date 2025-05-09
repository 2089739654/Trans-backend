import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import vueDevTools from 'vite-plugin-vue-devtools'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

//哈哈哈哈
import { viteMockServe } from 'vite-plugin-mock'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    // 哈哈哈哈哈哈哈哈哈哈哈哈
    viteMockServe({
      mockPath: './public/mock',  // Mock 文件存储目录
      localEnabled: true,  // 开发环境启用
      watchFiles: true,  // 开启文件监听
      logger: true         // 控制台显示请求日志
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  //哈哈哈哈哈哈哈
  // server: {
  //   proxy: {
  //     '/api': {  // 代理配置解决跨域
  //       target: 'http://localhost:5173',  // 本地服务地址
  //       changeOrigin: true,
  //       rewrite: (path) => path.replace(/^\/api/, '/api')  // 去除接口前缀
  //     }
  //   }
  // }
})
