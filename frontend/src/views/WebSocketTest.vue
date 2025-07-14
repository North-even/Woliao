<template>
  <div style="max-width: 500px; margin: 40px auto; padding: 24px; border: 1px solid #eee; border-radius: 8px;">
    <h2>WebSocket 测试</h2>
    <div style="margin-bottom: 12px;">
      <label>Token: <input v-model="token" style="width: 320px;" /></label>
    </div>
    <div style="margin-bottom: 12px;">
      <button @click="connect" :disabled="connected">连接</button>
      <button @click="disconnect" :disabled="!connected">断开</button>
    </div>
    <div style="margin-bottom: 12px;">
      <strong>连接状态：</strong>
      <span :style="{color: connected ? 'green' : 'red'}">{{ connected ? '已连接' : '未连接' }}</span>
    </div>
    <div style="margin-bottom: 12px;">
      <strong>收到的消息：</strong>
      <div style="background: #f8f8f8; min-height: 60px; padding: 8px; border-radius: 4px;">
        <div v-for="(msg, idx) in messages" :key="idx">{{ msg }}</div>
      </div>
    </div>
    <div v-if="error" style="color: red; margin-bottom: 12px;">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue';

const token = ref('');
const ws = ref<WebSocket|null>(null);
const connected = ref(false);
const messages = ref<string[]>([]);
const error = ref('');
let heartbeatTimer: number|null = null;

function connect() {
  if (!token.value) {
    error.value = '请填写 token';
    return;
  }
  error.value = '';
  const url = `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host.replace(/:\d+$/, '')}:8080/ws?token=${encodeURIComponent(token.value)}`;
  ws.value = new WebSocket(url);
  ws.value.onopen = () => {
    connected.value = true;
    messages.value.push('WebSocket 已连接');
    startHeartbeat();
  };
  ws.value.onclose = (e) => {
    connected.value = false;
    messages.value.push('WebSocket 已断开');
    stopHeartbeat();
  };
  ws.value.onerror = (e) => {
    error.value = 'WebSocket 连接出错';
  };
  ws.value.onmessage = (e) => {
    messages.value.push(e.data);
  };
}

function disconnect() {
  if (ws.value) {
    ws.value.close();
    ws.value = null;
  }
  connected.value = false;
  stopHeartbeat();
}

function startHeartbeat() {
  stopHeartbeat();
  heartbeatTimer = window.setInterval(() => {
    if (ws.value && connected.value) {
      ws.value.send(JSON.stringify({ type: 'ping' }));
    }
  }, 30000);
}
function stopHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

onUnmounted(() => {
  disconnect();
});
</script> 