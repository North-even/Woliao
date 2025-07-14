<template>
  <div style="max-width: 600px; margin: 40px auto; border: 1px solid #eee; border-radius: 8px;">
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #eee;">
      <div style="font-weight: bold; font-size: 18px;">{{ chatTitle }}</div>
      <button @click="goToSettings">设置</button>
    </div>
    <div ref="msgListRef" style="height: 400px; overflow-y: auto; padding: 16px; background: #fafbfc;">
      <div v-for="(msg, idx) in messages" :key="idx" style="margin-bottom: 10px;">
        <span style="font-weight: bold; color: #555;">{{ msg.from }}</span>：
        <span>{{ msg.content }}</span>
      </div>
    </div>
    <div style="display: flex; padding: 12px 16px; border-top: 1px solid #eee;">
      <input v-model="input" @keyup.enter="sendMsg" placeholder="输入消息..." style="flex:1; margin-right: 8px; padding: 8px; border-radius: 4px; border: 1px solid #ccc;" />
      <button @click="sendMsg">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const input = ref('');
const messages = ref<any[]>([]);
const msgListRef = ref<HTMLElement|null>(null);

const isGroup = route.path.startsWith('/group/');
const chatId = route.params.userId || route.params.groupId;
const chatTitle = isGroup ? `群聊 ${chatId}` : `与用户 ${chatId}`;

// mock 历史消息，后续可用API替换
onMounted(() => {
  messages.value = [
    { from: isGroup ? '群友A' : '对方', content: '你好！' },
    { from: '我', content: '你好，有什么可以帮你？' },
  ];
  scrollToBottom();
});

function sendMsg() {
  if (!input.value.trim()) return;
  messages.value.push({ from: '我', content: input.value });
  input.value = '';
  scrollToBottom();
  // TODO: 通过 WebSocket 发送消息
}

function goToSettings() {
  if (isGroup) {
    router.push(`/group/${chatId}/settings`);
  } else {
    router.push(`/chat/${chatId}/settings`);
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgListRef.value) {
      msgListRef.value.scrollTop = msgListRef.value.scrollHeight;
    }
  });
}

watch(messages, scrollToBottom);
</script> 