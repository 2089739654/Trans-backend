import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite';

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue({
      script: {
        defineModel: true,
        propsDestructure: true
      }
    }),
    Components({
      resolvers: [ElementPlusResolver()],//{ importStyle: 'css' }
      dts: 'src/components.d.ts',
    }),
    AutoImport({
      resolvers: [ElementPlusResolver()], // 自动导入 Element Plus 组件
      dts: true, // 生成类型声明文件
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),  // 核心别名配置
      'vue': 'vue/dist/vue.esm-bundler.js' // 明确指向完整版本
    }
  },
  build: {

   /** If you set esmExternals to true, this plugins assumes that 
     all external dependencies are ES modules */

   commonjsOptions: {
      esmExternals: true 
   },
  }
})
