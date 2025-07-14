<template>
  <div class="chat-window">
    <div class="chat-header">
      <h3>{{ chatPartnerName }}</h3>
    </div>
    <div class="chat-messages">
      <div v-for="(msg, index) in allMessages" :key="index" class="message">
         {{ msg.content }}
      </div>
    </div>
    <div class="chat-input">
      <el-input
        v-model="messageText"
        placeholder="输入消息..."
        @keyup.enter="handleSendMessage"
        clearable
      />
      <el-button type="primary" @click="handleSendMessage" :disabled="!webSocketStore.isConnected">发送</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useWebSocketStore } from '@/store/websocket';
import { ElMessage } from 'element-plus';
import api from '@/api/axios';

const route = useRoute();
const webSocketStore = useWebSocketStore();

const messageText = ref('');
const historyMessages = ref<any[]>([]);

const chatType = computed(() => route.params.type as string);
const partnerId = computed(() => Number(route.params.id));

const chatPartnerName = computed(() => {
  return `与 ${chatType.value === 'user' ? '用户' : '群组'} ${partnerId.value} 的对话`;
});

// 只筛选当前会话的实时消息
const realtimeMessages = computed(() =>
  webSocketStore.messages.filter(msg => {
    if (chatType.value === 'user') {
      return (
        (msg.type === 'SINGLE_CHAT' || msg.type === 'USER_CHAT') &&
        (msg.toUserId === partnerId.value || msg.fromUserId === partnerId.value)
      );
    } else {
      return (
        (msg.type === 'GROUP_CHAT') &&
        msg.toUserId === partnerId.value
      );
    }
  })
);

const allMessages = computed(() => [
  ...historyMessages.value,
  ...realtimeMessages.value
]);

const handleSendMessage = async () => {
  if (!messageText.value.trim()) {
    ElMessage.warning('不能发送空消息');
    return;
  }
  if (!webSocketStore.isConnected) {
    ElMessage.error('连接已断开，无法发送消息');
    return;
  }
  const payload = {
    type: chatType.value.toUpperCase() + '_CHAT',
    toUserId: partnerId.value,
    content: messageText.value
  };
  webSocketStore.sendMessage(payload);
  messageText.value = '';
};

const fetchHistory = async () => {
  try {
    // 修正：去掉/api前缀，避免baseURL重复
    const response = await api.get(`/messages/history/user/${partnerId.value}`);
    historyMessages.value = response.data;
  } catch (error) {
    historyMessages.value = [];
    ElMessage.error("获取历史消息失败");
  }
};

onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f5f5f5;
}
.chat-header {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  background-color: #fff;
  text-align: center;
  font-weight: 600;
}
.chat-messages {
  flex-grow: 1;
  padding: 1rem;
  overflow-y: auto;
}
.message {
  padding: 8px 12px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 8px;
  max-width: 80%;
}
.chat-input {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #e0e0e0;
  background-color: #fff;
}
.chat-input .el-input {
  margin-right: 1rem;
}
</style> 