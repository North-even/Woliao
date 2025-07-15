<template>
  <div style="max-width: 500px; margin: 40px auto; padding: 24px;">
    <h2>消息列表</h2>
    <div v-for="session in sessions" :key="session.id" class="session-item" @click="goToChat(session)">
      <el-badge :is-dot="getUnreadDot(session)" class="unread-dot">
        <div style="font-weight: bold;">{{ session.name }}</div>
      </el-badge>
      <div style="color: #888; font-size: 13px;">{{ session.lastMessage }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api/axios';
import { useWebSocketStore } from '@/store/websocket';

const router = useRouter();

const sessions = ref<any[]>([]);

async function fetchSessions() {
  const res = await api.get('/api/sessions');
  sessions.value = res.data;
}

function getUnreadDot(session: any) {
  return session.unreadCount > 0;
}

async function goToChat(session: any) {
  // 进入会话时，调用已读API
  if (session.unreadCount > 0) {
    await api.post('/api/messages/mark-read', [session.partnerId]);
    session.unreadCount = 0;
  }
  if (session.chatType === 'SINGLE') {
    router.push(`/chat/${session.partnerId}`);
  } else if (session.chatType === 'GROUP') {
    router.push(`/group/${session.partnerId}`);
  }
}

const webSocketStore = useWebSocketStore();

watch(() => webSocketStore.newMessage, (newMessage) => {
  if (!newMessage) return;
  if (newMessage.type === 'MARK_AS_READ') {
    const session = sessions.value.find(s => s.partnerId === newMessage.partnerId && s.chatType === newMessage.chatType);
    if (session) session.unreadCount = 0;
  }
});

onMounted(() => {
  fetchSessions();
});
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
.unread-dot :deep(.el-badge__content.is-dot) {
  background-color: #f56c6c;
  border: 1px solid white;
  width: 10px;
  height: 10px;
  right: -8px;
  top: 2px;
}
</style> 