/// <reference types="vite/client" />
declare module "*.vue" {
  import Vue from "vue"
  import type { DefineComponent } from "vue";
  const component: DefineComponent<{}, {}, any>;
  //export default Vue;
  export default component;
}
// 声明 Element Plus 和图标库
declare module "element-plus";
declare module "@element-plus/icons-vue";

// 声明 Vue 模块类型[1,4](@ref)
declare module 'vue' {
  import { CompatVue } from '@vue/runtime-dom';
  const Vue: CompatVue;
  // export default Vue;
  export * from '@vue/runtime-dom';
}

// 声明 axios 模块
declare module "axios" {
  interface AxiosResponse<T = any> {
    code: number;
    data: T;
    message: string;
  }
  export interface AxiosInstance {
    <T = any>(config: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  }
}
// 扩展 Vite 环境变量类型（可选）
interface ImportMetaEnv {
  readonly VITE_API_BASE: string;
}