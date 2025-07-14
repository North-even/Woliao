<template>
  <div style="max-width: 500px; margin: 40px auto; padding: 24px;">
    <h2>消息列表</h2>
    <div v-for="session in sessions" :key="session.id" class="session-item" @click="goToChat(session)">
      <div style="font-weight: bold;">{{ session.name }}</div>
      <div style="color: #888; font-size: 13px;">{{ session.lastMessage }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// mock 数据，后续可用API替换
const sessions = ref([
  { id: 2, type: 'single', name: '张三', lastMessage: '你好！' },
  { id: 1001, type: 'group', name: '前端交流群', lastMessage: '欢迎新同学' },
]);

function goToChat(session: any) {
  if (session.type === 'single') {
    router.push(`/chat/${session.id}`);
  } else if (session.type === 'group') {
    router.push(`/group/${session.id}`);
  }
}
</script>

<style scoped>
.session-item {
  border-bottom: 1px solid #eee;
  padding: 12px 0;
  cursor: pointer;
}
.session-item:hover {
  background: #f5f5f5;
}
</style> 