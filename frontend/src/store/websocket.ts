import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';

export const useWebSocketStore = defineStore('websocket', () => {
  const socket = ref<WebSocket | null>(null);
  const isConnected = ref(false);
  const messages = ref<any[]>([]); // 用于存储接收到的消息
  const newMessage = ref<any | null>(null); // 新增一个ref来专门存放最新收到的消息

  // VVVV 在这里添加新方法 VVVV
  const clearMessages = () => {
    messages.value = [];
  };

  const setMessages = (historyMessages: any[]) => {
    messages.value = historyMessages;
  };
  // ^^^^ 在这里添加新方法 ^^^^

  // 动作：建立连接
  const connect = (token: string) => {
    if (isConnected.value || !token) {
      console.log('WebSocket is already connected or token is missing.');
      return;
    }

    const wsUrl = `ws://localhost:8080/ws?token=${token}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
      isConnected.value = true;
      socket.value = ws;
      console.log('WebSocket connected successfully!');
      ElMessage.success('长连接已建立');
    };

    ws.onmessage = (event) => {
      console.log('Received message:', event.data);
      const messageData = JSON.parse(event.data);

      if (messageData.type === 'pong') {
        console.log('Heartbeat pong received.');
        return; // 不把心跳消息计入聊天记录
      }

      // 将新消息存入newMessage，而不是直接push到messages数组
      newMessage.value = messageData;
    };

    ws.onerror = (error) => {
      console.error('WebSocket Error:', error);
      ElMessage.error('长连接发生错误');
    };

    ws.onclose = () => {
      isConnected.value = false;
      socket.value = null;
      console.log('WebSocket disconnected.');
      ElMessage.warning('长连接已断开');
    };
  };

  // 动作：发送消息
  const sendMessage = (payload: object) => {
    if (!isConnected.value || !socket.value) {
      ElMessage.error('连接已断开，无法发送消息');
      return;
    }
    socket.value.send(JSON.stringify(payload));
  };

  // 动作：断开连接
  const disconnect = () => {
    if (socket.value) {
      socket.value.close();
    }
  };

  return {
    isConnected,
    messages,
    newMessage, // 暴露 newMessage
    clearMessages, // 暴露新方法
    setMessages,   // 暴露新方法
    connect,
    sendMessage,
    disconnect,
  };
}); 