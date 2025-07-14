<template>
  <div class="chat-window">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">
        <svg width="24" height="24" viewBox="0 0 24 24"><path d="M15 18l-6-6 6-6" stroke="#333" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
      </button>
      <h3>{{ chatPartnerName }}</h3>
    </div>
    <div class="chat-messages">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="messages.length === 0" class="empty">暂无消息</div>
      <div v-else>
        <div v-for="msg in messages" :key="msg.id" :class="['msg-item', msg.isSelf ? 'self' : '']">
          <div class="msg-nickname">{{ msg.nickname }}</div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-time">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
    </div>
    <div class="chat-input">
      <input type="text" v-model="inputMsg" placeholder="输入消息..." @keyup.enter="sendMsg" />
      <button @click="sendMsg">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api/axios';

const route = useRoute();
const router = useRouter();

const chatType = computed(() => route.params.type as string); // 'user' | 'group'
const partnerId = computed(() => route.params.id as string);

const loading = ref(true);
const messages = ref<any[]>([]);
const inputMsg = ref('');

// 获取当前用户信息（从本地缓存）
function getCurrentAccount() {
  try {
    const token = localStorage.getItem('accessToken');
    const accounts = JSON.parse(localStorage.getItem('accounts') || '[]');
    return accounts.find((a:any) => a.accessToken === token) || null;
  } catch { return null; }
}
const currentAccount = getCurrentAccount();

// 获取联系人/群昵称（优先本地缓存，找不到再请求API）
const chatPartnerName = ref('');
async function resolvePartnerName() {
  // 0. 优先从路由参数/跳转时的 session 直接获取 name
  if (route.query && route.query.name) {
    chatPartnerName.value = String(route.query.name);
    return;
  }
  // 1. 先从联系人缓存找
  try {
    const contacts = JSON.parse(localStorage.getItem('contacts') || '{}');
    if (chatType.value === 'user' && contacts.individuals) {
      const user = contacts.individuals.find((u:any) => String(u.id) === String(partnerId.value));
      if (user) { chatPartnerName.value = user.name; return; }
    }
    if (chatType.value === 'group' && contacts.groups) {
      const group = contacts.groups.find((g:any) => String(g.id) === String(partnerId.value));
      if (group) { chatPartnerName.value = group.name; return; }
    }
  } catch {}
  // 2. 再从会话缓存找
  try {
    const sessions = JSON.parse(localStorage.getItem('sessions') || '[]');
    // 兼容 partner_id、id
    const session = sessions.find((s:any) => String(s.partner_id || s.id) === String(partnerId.value));
    if (session && session.name) { chatPartnerName.value = session.name; return; }
  } catch {}
  // 3. 实在找不到，请求API
  try {
    if (chatType.value === 'user') {
      const res = await api.get(`/contacts`);
      const user = res.data.individuals.find((u:any) => String(u.id) === String(partnerId.value));
      if (user) { chatPartnerName.value = user.name; return; }
    } else if (chatType.value === 'group') {
      const res = await api.get(`/contacts`);
      const group = res.data.groups.find((g:any) => String(g.id) === String(partnerId.value));
      if (group) { chatPartnerName.value = group.name; return; }
    }
  } catch {}
  // 兜底
  chatPartnerName.value = `ID: ${partnerId.value}`;
}

// 获取历史消息
async function fetchMessages() {
  loading.value = true;
  try {
    let url = '';
    if (chatType.value === 'user') {
      url = `/messages/user/${partnerId.value}`;
    } else {
      url = `/messages/group/${partnerId.value}`;
    }
    const res = await api.get(url);
    // 假设后端返回 { id, senderId, nickname, content, timestamp }
    messages.value = res.data.map((msg:any) => ({
      ...msg,
      isSelf: currentAccount && String(msg.senderId) === String(currentAccount.phoneNumber || currentAccount.id),
    }));
  } catch {
    messages.value = [];
  } finally {
    loading.value = false;
  }
}

// 发送消息
async function sendMsg() {
  const content = inputMsg.value.trim();
  if (!content) return;
  let url = '';
  let payload:any = {};
  if (chatType.value === 'user') {
    url = `/messages/user/${partnerId.value}`;
    payload = { content };
  } else {
    url = `/messages/group/${partnerId.value}`;
    payload = { content };
  }
  try {
    const res = await api.post(url, payload);
    // 假设返回新消息对象
    messages.value.push({
      ...res.data,
      isSelf: true,
    });
    inputMsg.value = '';
  } catch {
    // 可加错误提示
  }
}

function goBack() {
  router.back();
}

function formatTime(ts:string) {
  if (!ts) return '';
  const date = new Date(ts);
  return date.toLocaleTimeString();
}

onMounted(() => {
  resolvePartnerName();
  fetchMessages();
});
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.chat-header {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
  position: relative;
}
.back-btn {
  background: none;
  border: none;
  cursor: pointer;
  margin-right: 1rem;
  padding: 0;
  display: flex;
  align-items: center;
}
.chat-header h3 {
  flex: 1;
  text-align: center;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}
.chat-messages {
  flex-grow: 1;
  padding: 1rem;
  overflow-y: auto;
  background: #f7f8fa;
}
.loading {
  text-align: center;
  color: #999;
  margin-top: 2rem;
}
.empty {
  text-align: center;
  color: #bbb;
  margin-top: 2rem;
}
.msg-item {
  margin-bottom: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
  max-width: 70%;
}
.msg-item.self {
  background: #e6f7ff;
  margin-left: auto;
}
.msg-nickname {
  font-size: 13px;
  color: #409eff;
  margin-bottom: 2px;
}
.msg-content {
  font-size: 15px;
  color: #222;
}
.msg-time {
  font-size: 12px;
  color: #aaa;
  text-align: right;
}
.chat-input {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #eee;
  background: #fff;
}
.chat-input input {
  flex-grow: 1;
  margin-right: 1rem;
  border-radius: 4px;
  border: 1px solid #eee;
  padding: 0.5rem;
  font-size: 15px;
}
.chat-input button {
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0 1.5rem;
  font-size: 15px;
  cursor: pointer;
}
</style> 