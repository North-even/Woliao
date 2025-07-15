import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import axios from '@/api/axios';
import { reactive } from 'vue';

export const useWebSocketStore = defineStore('websocket', () => {
  const socket = ref<WebSocket | null>(null);
  const isConnected = ref(false);
  const messages = ref<any[]>([]); // 用于存储接收到的消息
  const newMessage = ref<any | null>(null); // 新增一个ref来专门存放最新收到的消息
  let heartbeatTimer: any = null;

  // VVVV 在这里添加新方法 VVVV
  const clearMessages = () => {
    messages.value = [];
  };

  const setMessages = (historyMessages: any[]) => {
    messages.value = historyMessages;
  };
  // ^^^^ 在这里添加新方法 ^^^^

  // 未读消息Map，key为会话ID（单聊为对方ID，群聊为群ID），value为未读数
  const unreadMap = reactive<{ [key: string]: number }>({});

  // 拉取未读消息并统计
  const fetchUnreadMessages = async () => {
    const res = await axios.get('/api/messages/unread');
    // 清空
    Object.keys(unreadMap).forEach(k => delete unreadMap[k]);
    // 统计
    for (const msg of res.data) {
      const key = msg.type === 'GROUP_CHAT' ? `group_${msg.toUserId}` : `single_${msg.fromUserId}`;
      unreadMap[key] = (unreadMap[key] || 0) + 1;
    }
  };

  // 清除某会话未读
  const clearUnread = async (sessionType: string, sessionId: number) => {
    const key = sessionType === 'group' ? `group_${sessionId}` : `single_${sessionId}`;
    // 拉取未读消息ID
    const res = await axios.get('/api/messages/unread');
    const ids = res.data
      .filter((msg: any) =>
        (sessionType === 'group' && msg.type === 'GROUP_CHAT' && msg.toUserId === sessionId) ||
        (sessionType === 'single' && msg.type === 'SINGLE_CHAT' && msg.fromUserId === sessionId)
      )
      .map((msg: any) => msg.id);
    if (ids.length > 0) {
      await axios.post('/api/messages/mark-read', ids);
    }
    unreadMap[key] = 0;
  };

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
      // 自动心跳
      heartbeatTimer = setInterval(() => {
        if (isConnected.value && socket.value) {
          socket.value.send(JSON.stringify({ type: 'ping' }));
        }
      }, 10000); // 10秒心跳
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
    };

    ws.onclose = () => {
      isConnected.value = false;
      socket.value = null;
      if (heartbeatTimer) clearInterval(heartbeatTimer);
      heartbeatTimer = null;
      console.log('WebSocket disconnected.');
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
    if (heartbeatTimer) clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  };

  return {
    isConnected,
    messages,
    newMessage, // 暴露 newMessage
    clearMessages, // 暴露新方法
    setMessages,   // 暴露新方法
    unreadMap,
    fetchUnreadMessages,
    clearUnread,
    connect,
    sendMessage,
    disconnect,
  };
}); 