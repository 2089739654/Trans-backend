// store/route.ts
import { defineStore } from 'pinia';
import { router } from '../router';

export const useRouteStore = defineStore('route', {
  state: () => ({
    currentPath: '' as string,
  }),
  actions: {
    updatePath(path: string) {
      this.currentPath = path;
    },
    init() {
      // 初始化当前路径
      this.updatePath(router.currentRoute.value.fullPath);
      
      // 监听路由变化
      router.afterEach((to) => {
        this.updatePath(to.fullPath);
      });
    },
  },
});