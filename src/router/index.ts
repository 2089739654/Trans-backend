// import Vue from 'vue'
// import VueRouter from "vue-router";
import { createRouter, createWebHistory } from "vue-router";

// 1. 创建路由对应的组件（需先创建以下三个文件）
const ProjectsView = () => import("../views/ProjectsView.vue");
const EditView = () => import("../views/EditView.vue");
const LoginView = () => import('../views/LoginView.vue')
const RegisterView = () => import('@/views/RegisterView.vue')
const FileManagerView= () => import('@/views/FileManager.vue')
const routes = [
      {
          path: '/',
          redirect: '/login', // 核心重定向配置
      },
      {
          path: '/login',
          component: LoginView, 
      },
      {
          path: '/register',
          component: RegisterView, 
      },
      {
          path: "/projects/:teamId",
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
            {
                path: "file-manager/:fileId",
                name: "file-manager",
                component: FileManagerView,
                props: true, // 启用props接收参数
            },
          ],
      },
];

// 3. 创建路由实例
export const router = createRouter({
  history: createWebHistory(),
  routes,
});