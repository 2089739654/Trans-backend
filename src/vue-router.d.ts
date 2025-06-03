// src/vue-router.d.ts
declare module 'vue-router' {
  import { 
    useRouter,
    useRoute,
    Router, 
    RouteLocationNormalized, 
    RouteLocationRaw, 
    createRouter, 
    createWebHistory 
  } from 'vue-router'
  
  export * from 'vue-router'
  export { useRouter,useRoute, Router, RouteLocationNormalized, RouteLocationRaw, createRouter, createWebHistory }
}