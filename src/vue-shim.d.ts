// src/vue-shim.d.ts
declare module 'vue' {
  export * from 'vue/dist/vue';
  import { createApp } from 'vue/dist/vue';
  //export { createApp };
  import type { App, Component } from 'vue'
  export * from 'vue/dist/vue'
  export { createApp, Component ,ref, watch, onMounted, onBeforeUnmount,reactive, computed,nextTick}
  import { DefineProps, DefineEmits } from 'vue'
  
  export const defineProps: DefineProps
  export const defineEmits: DefineEmits
  export const defineModel: any
  export const withDefaults: any
}