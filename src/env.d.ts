// src/env.d.ts
/// <reference types="pinia" />

// 如果你使用了Vue 3的组合式API
import { ComponentCustomProperties } from 'vue'
import { Store } from 'pinia'

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $store: Store
  }
}