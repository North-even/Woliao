<template>
  <div class="chat-window">
    <div class="chat-header">
      <h3>{{ chatPartnerName }}</h3>
    </div>
    <div class="chat-messages" ref="messagesContainer">
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
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { useWebSocketStore } from '@/store/websocket';
import { ElMessage } from 'element-plus';
import api from '@/api/axios'; // 确保导入了axios实例

const route = useRoute();
const webSocketStore = useWebSocketStore();

const messageText = ref('');
const historyMessages = ref<any[]>([]);
const realtimeMessagesList = ref<any[]>([]); // 单独存储实时消息
const messagesContainer = ref<HTMLElement | null>(null);

// 从URL动态获取聊天类型和对方ID
const chatType = computed(() => route.params.type as string);
const partnerId = computed(() => Number(route.params.id));

// 合并历史消息和实时消息用于渲染
const allMessages = computed(() => [...historyMessages.value, ...realtimeMessagesList.value]);

const chatPartnerName = computed(() => `与 ${chatType.value === 'user' ? '用户' : '群组'} ${partnerId.value} 的对话`);

// 获取历史记录的方法
const fetchHistory = async () => {
  try {
    const historyUrl = chatType.value === 'user'
      ? `/messages/history/user/${partnerId.value}`
      : `/messages/history/group/${partnerId.value}`;

    const response = await api.get(historyUrl);
    historyMessages.value = response.data;
  } catch (error) {
    console.error("获取历史消息失败:", error);
    ElMessage.error("获取历史消息失败");
  }
};

// 【核心修正】监听来自WebSocket的新消息
watch(() => webSocketStore.newMessage, (newMessage) => {
  if (!newMessage) return;

  const currentChatType = chatType.value;
  const currentPartnerId = partnerId.value;

  // 判断收到的消息是否属于当前聊天窗口
  let isCurrentChatMessage = false;
  if (currentChatType === 'user' && (newMessage.type === 'USER_CHAT')) {
    // 单聊：发送方或接收方是当前聊天对象
    if (newMessage.fromUserId === currentPartnerId || newMessage.toUserId === currentPartnerId) {
      isCurrentChatMessage = true;
    }
  } else if (currentChatType === 'group' && newMessage.type === 'GROUP_CHAT') {
    // 群聊：接收方ID(即群ID)是当前群聊ID
    if (newMessage.toUserId === currentPartnerId) {
      isCurrentChatMessage = true;
    }
  }

  if (isCurrentChatMessage) {
    realtimeMessagesList.value.push(newMessage);
    // 滚动到底部
    nextTick(() => {
        if(messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
    });
  }
}, { deep: true });

onMounted(() => {
  realtimeMessagesList.value = []; // 每次进入新窗口时，清空实时消息列表
  fetchHistory();
});

const handleSendMessage = () => {
  if (!messageText.value.trim()) return;

  const payload = {
    type: chatType.value.toUpperCase() + '_CHAT',
    toUserId: partnerId.value,
    content: messageText.value
  };

  webSocketStore.sendMessage(payload);
  messageText.value = '';
};
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
.el-input {
  margin-right: 1rem;
}
</style> 