<template>
  <div class="main-layout">
    <div class="content-area">
      <router-view />
    </div>
    
    <div class="bottom-nav">
      <div
        class="nav-item"
        :class="{ active: $route.path === '/messages' }"
        @click="$router.push('/messages')"
      >
        <el-icon size="24">
          <ChatDotRound />
        </el-icon>
        <span>消息</span>
      </div>
      
      <div
        class="nav-item"
        :class="{ active: $route.path === '/contacts' }"
        @click="$router.push('/contacts')"
      >
        <el-icon size="24">
          <UserFilled />
        </el-icon>
        <span>联系人</span>
      </div>
      <div
        class="nav-item"
        :class="{ active: $route.path === '/accounts' }"
        @click="$router.push('/accounts')"
      >
        <el-icon size="24">
          <Setting />
        </el-icon>
        <span>账号管理</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useWebSocketStore } from '@/store/websocket'; // 导入 WebSocket store
import { ChatDotRound, UserFilled, Setting } from '@element-plus/icons-vue'

const webSocketStore = useWebSocketStore();

onMounted(() => {
  const token = localStorage.getItem('accessToken');
  if (token && !webSocketStore.isConnected) {
    webSocketStore.connect(token);
  }
});
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f5f5;
}

.content-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.bottom-nav {
  display: flex;
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 8px 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  margin: 0 8px;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  color: #409eff;
  background-color: #ecf5ff;
}

.nav-item span {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}
</style> 