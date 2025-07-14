<template>
  <div class="chat-window">
    <div class="chat-header">
      <h3>{{ chatPartnerName }}</h3>
    </div>
    <div class="chat-messages">
      <p>与 {{ partnerId }} (类型: {{ chatType }}) 的聊天记录...</p>
    </div>
    <div class="chat-input">
      <input type="text" placeholder="输入消息..." />
      <button>发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

// 从URL动态获取聊天类型和对方ID
const chatType = computed(() => route.params.type as string);
const partnerId = computed(() => route.params.id as string);

// TODO: 将来我们会根据ID从状态库(Pinia)或API获取真实名称
const chatPartnerName = computed(() => {
  return `与 ${partnerId.value} 的对话`;
});

// 调试信息：在浏览器控制台打印出我们从URL获取到的参数
console.log(`正在打开聊天窗口，类型: ${chatType.value}, ID: ${partnerId.value}`);
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.chat-header {
  padding: 1rem;
  border-bottom: 1px solid #eee;
  text-align: center;
}
.chat-messages {
  flex-grow: 1;
  padding: 1rem;
  overflow-y: auto;
}
.chat-input {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #eee;
}
.chat-input input {
  flex-grow: 1;
  margin-right: 1rem;
}
</style> 