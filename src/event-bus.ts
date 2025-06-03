// 创建 event-bus.ts
import { ref, reactive, watch } from 'vue';

const eventBus = reactive({
  events: {},
  on(event: string, callback: Function) {
    if (!this.events[event]) {
      this.events[event] = [];
    }
    this.events[event].push(callback);
  },
  emit(event: string, ...args: any[]) {
    if (this.events[event]) {
      this.events[event].forEach((callback: Function) => {
        callback(...args);
      });
    }
  },
});

export default eventBus;