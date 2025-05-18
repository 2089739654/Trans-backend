// import Vue from 'vue'
// import VueRouter from "vue-router";
import { createRouter, createWebHistory } from "vue-router";

// 1. 创建路由对应的组件（需先创建以下三个文件）
const ProjectsView = () => import("../views/ProjectsView.vue");
const MemoryView = () => import("../views/MemoryView.vue");
const TermsView = () => import("../views/TermsView.vue");
const EditView = () => import("../views/EditView.vue");
// const LoginView = () => import('@/views/TermsView.vue')

const routes = [
  {
    path: "/",
    redirect: "/projects", // 核心重定向配置
    //meta: { requiresAuth: true }
  },
  {
    path: "/projects",
    name: "Projects",
    component: ProjectsView,
    //meta: { title: '项目管理' }  // 可选元信息
    children: [
      {
        path: "content/:fileId",
        name: "content",
        component: EditView,
        props: true, // 启用props接收参数
      },
    ],
  },
  {
    path: "/memory",
    name: "Memory",
    component: MemoryView,
    // meta: { title: '记忆库' }
  },
  {
    path: "/terms",
    name: "Terms",
    component: TermsView,
    //meta: { title: '术语库' }
  },
];

// 3. 创建路由实例
export const router = createRouter({
  history: createWebHistory(),
  routes,
});
