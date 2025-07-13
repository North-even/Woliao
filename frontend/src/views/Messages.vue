<template>
  <div class="messages-page">
    <div class="page-header">
      <h2>消息</h2>
    </div>
    
    <div class="sessions-list">
      <div
        v-for="session in sortedSessions"
        :key="session.id"
        class="session-item"
        :class="{ pinned: session.isPinned }"
      >
        <div class="session-avatar">
          <el-avatar :size="48" :src="`https://via.placeholder.com/48/409eff/ffffff?text=${session.name.charAt(0)}`" />
          <div v-if="session.unreadCount > 0" class="unread-badge">
            {{ session.unreadCount }}
          </div>
        </div>
        
        <div class="session-content">
          <div class="session-header">
            <span class="session-name">{{ session.name }}</span>
            <span class="session-time">{{ formatTime(session.timestamp) }}</span>
          </div>
          <div class="session-message">{{ session.lastMessage }}</div>
        </div>
        
        <div v-if="session.isPinned" class="pin-indicator">
          <el-icon><Star /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star } from '@element-plus/icons-vue'
import api from '@/api/axios'

interface Session {
  id: number
  name: string
  lastMessage: string
  timestamp: string
  isPinned: boolean
  unreadCount: number
}

const sessions = ref<Session[]>([])
const loading = ref(false)

const sortedSessions = computed(() => {
  return [...sessions.value].sort((a, b) => {
    // 首先按置顶状态排序
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    
    // 然后按时间戳降序排序
    return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
  })
})

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString()
  }
}

const fetchSessions = async () => {
  try {
    loading.value = true
    const response = await api.get('/sessions')
    sessions.value = response.data
  } catch (error) {
    ElMessage.error('获取会话列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.messages-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  position: relative;
}

.session-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.session-item.pinned {
  border-left: 4px solid #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
}

.session-avatar {
  position: relative;
  margin-right: 16px;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-name {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.session-time {
  color: #909399;
  font-size: 12px;
}

.session-message {
  color: #606266;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pin-indicator {
  margin-left: 12px;
  color: #409eff;
}
</style> 