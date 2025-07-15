<template>
  <div style="max-width: 600px; margin: 40px auto; border: 1px solid #eee; border-radius: 8px;">
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #eee;">
      <div style="font-weight: bold; font-size: 18px;">{{ chatTitle }}</div>
      <el-button type="text" @click="showSettings = true">
        <el-icon><i-ep-setting /></el-icon>
      </el-button>
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
    <!-- 设置弹窗 -->
    <el-dialog v-model="showSettings" :title="isGroup ? '群聊管理' : '好友管理'" width="350px" @close="resetDialog">
      <template v-if="!isGroup">
        <div style="margin-bottom: 16px;">好友ID：{{ chatId }}</div>
        <el-button type="danger" @click="handleRemoveFriend" :loading="loading">删除好友</el-button>
      </template>
      <template v-else>
        <div style="margin-bottom: 16px;">群聊ID：{{ chatId }}</div>
        <el-button type="danger" @click="handleLeaveGroup" :loading="loading">退出群聊</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElButton, ElDialog, ElIcon } from 'element-plus';
import { Setting as IepSetting } from '@element-plus/icons-vue';
import axios from '@/api/axios';
import { useWebSocketStore } from '@/store/websocket';

const route = useRoute();
const input = ref('');
const messages = ref<any[]>([]);
const msgListRef = ref<HTMLElement|null>(null);
const showSettings = ref(false);
const loading = ref(false);

const isGroup = route.path.startsWith('/group/');
const chatId = route.params.userId || route.params.groupId;
const chatTitle = isGroup ? `群聊 ${chatId}` : `与用户 ${chatId}`;

const webSocketStore = useWebSocketStore();

onMounted(() => {
  messages.value = [
    { from: isGroup ? '群友A' : '对方', content: '你好！' },
    { from: '我', content: '你好，有什么可以帮你？' },
  ];
  scrollToBottom();
  // 标记已读
  const chatType = isGroup ? 'GROUP' : 'SINGLE';
  axios.post(`/api/sessions/${chatType}/${chatId}/read`).catch(err => console.error('标记已读失败', err));
  webSocketStore.clearUnreadCount(Number(chatId), chatType);
});

function sendMsg() {
  if (!input.value.trim()) return;
  messages.value.push({ from: '我', content: input.value });
  input.value = '';
  scrollToBottom();
  // TODO: 通过 WebSocket 发送消息
}

function resetDialog() {
  loading.value = false;
}

async function handleRemoveFriend() {
  loading.value = true;
  try {
    await axios.post('/api/contacts/remove-friend', null, { params: { friendId: chatId } });
    ElMessage.success('已删除好友');
    showSettings.value = false;
    // 可选：跳转回联系人页
  } catch (e) {
    ElMessage.error('删除失败');
  } finally {
    loading.value = false;
  }
}

async function handleLeaveGroup() {
  loading.value = true;
  try {
    await axios.post('/api/group/leave', null, { params: { groupId: chatId } });
    ElMessage.success('已退出群聊');
    showSettings.value = false;
    // 可选：跳转回联系人页
  } catch (e) {
    ElMessage.error('退出失败');
  } finally {
    loading.value = false;
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

<style scoped>
.el-dialog__footer {
  text-align: right;
}
</style> 