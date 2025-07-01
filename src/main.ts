import "./assets/main.css";

import ElementPlus from "element-plus"; //全局引入
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import 'element-plus/dist/index.css'; // 全局样式
import { createApp} from "vue";
import App from "./App.vue";
import { router } from "./router";
// ElMessage
import "./assets/message-override.css";
import { useUserStore } from './stores/user'

import axios from "axios";
// import { createPinia } from 'pinia'
// const pinia = createPinia()

// 哈哈哈哈哈哈哈哈哈哈哈

// 哈哈哈哈哈哈哈

const app = createApp(App);

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component as any) // 使用 DefineComponent 类型断言
}
// const routeStore = useUserStore(pinia);
// routeStore.init();

app.use(ElementPlus);
app.use(router);
// app.use(pinia)
app.mount("#app");

app.config.globalProperties.axios = axios; // 通过 this.axios 调用

// Object.keys(Icons).forEach((key) => {
//   app.component(key, Icons[key]);
// });
