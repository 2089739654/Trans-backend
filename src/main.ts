import "./assets/main.css";

import ElementPlus from "element-plus"; //全局引入
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import 'element-plus/dist/index.css'; // 全局样式
import { createApp} from "vue";
import App from "./App.vue";
import { router } from "./router";
import { useRouteStore } from './stores/route';
import { setupAuthStore } from './stores/token'
// ElMessage
import "./assets/message-override.css";
import axios from "axios";
// import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

import { createPinia } from 'pinia'
const pinia = createPinia()

// pinia.use(piniaPluginPersistedstate)

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component as any) // 使用 DefineComponent 类型断言
}

app.use(ElementPlus);
app.use(router);
app.use(pinia)

// 初始化路由状态
const routeStore = useRouteStore(pinia);
routeStore.init();
setupAuthStore();
app.mount("#app");

app.config.globalProperties.axios = axios; // 通过 this.axios 调用