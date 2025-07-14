<template>
  <div class="contacts-page">
    <div class="page-header">
      <h2>联系人</h2>
      <el-input
        v-model="searchText"
        placeholder="搜索联系人"
        :prefix-icon="Search"
        clearable
        class="search-input"
      />
    </div>

    <div class="contacts-content">
      <el-collapse v-model="activeCollapseNames" class="contacts-collapse">
        <el-collapse-item name="groups">
          <template #title>
            <div class="collapse-title">
              <el-icon><UserFilled /></el-icon>
              <span>群聊</span>
              <span class="count-badge">{{ filteredGroups.length }}</span>
            </div>
          </template>
          <div v-if="filteredGroups.length > 0" class="contact-list">
            <div
              v-for="group in filteredGroups"
              :key="group.id"
              class="contact-item"
              @click="openGroupChat(group)"
            >
              <el-avatar :size="40" :src="group.avatarUrl || `https://via.placeholder.com/40/67c23a/ffffff?text=${group.groupName.charAt(0)}`" />
              <span class="contact-name">{{ group.groupName }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无群聊" />
        </el-collapse-item>

        <el-collapse-item name="friends">
          <template #title>
            <div class="collapse-title">
              <el-icon><User /></el-icon>
              <span>好友</span>
              <span class="count-badge">{{ filteredFriends.length }}</span>
            </div>
          </template>
          <div v-if="filteredFriends.length > 0" class="contact-list">
            <div
              v-for="friend in filteredFriends"
              :key="friend.id"
              class="contact-item"
              @click="openUserChat(friend)"
            >
              <el-badge :is-dot="friend.isOnline" class="online-status-badge">
                <el-avatar :size="40" :src="friend.avatarUrl || `https://via.placeholder.com/40/409eff/ffffff?text=${friend.nickname.charAt(0)}`" />
              </el-badge>
              <span class="contact-name">{{ friend.nickname }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无好友" />
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Search, UserFilled, User } from '@element-plus/icons-vue';
import api from '@/api/axios';

interface Friend {
  id: number;
  nickname: string;
  avatarUrl: string;
  isOnline: boolean; // 新增字段
}

interface Group {
  id: number;
  groupName: string;
  avatarUrl: string;
}

const router = useRouter();
const friends = ref<Friend[]>([]);
const groups = ref<Group[]>([]);
const activeCollapseNames = ref(['friends', 'groups']); // 默认展开所有
const searchText = ref('');

// 新增：按在线状态和字母排序好友
const sortedFriends = computed(() => 
  [...friends.value].sort((a, b) => {
    if (a.isOnline && !b.isOnline) return -1;
    if (!a.isOnline && b.isOnline) return 1;
    return a.nickname.localeCompare(b.nickname, 'zh-Hans-CN');
  })
);

const sortedGroups = computed(() => 
  [...groups.value].sort((a, b) => a.groupName.localeCompare(b.groupName, 'zh-Hans-CN'))
);

// 新增：根据搜索文本过滤列表
const filteredFriends = computed(() =>
  sortedFriends.value.filter(friend => friend.nickname.toLowerCase().includes(searchText.value.toLowerCase()))
);

const filteredGroups = computed(() =>
  sortedGroups.value.filter(group => group.groupName.toLowerCase().includes(searchText.value.toLowerCase()))
);

const fetchContacts = async () => {
  try {
    const response = await api.get('/contacts');
    friends.value = response.data.friends;
    groups.value = response.data.groups;
  } catch (error) {
    ElMessage.error('获取联系人列表失败');
    console.error(error);
  }
};

const openUserChat = (friend: Friend) => router.push(`/chat/user/${friend.id}`);
const openGroupChat = (group: Group) => router.push(`/chat/group/${group.id}`);

onMounted(() => {
  fetchContacts();
});
</script>

<style scoped>
.contacts-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.page-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.page-header h2 {
  margin: 0 0 16px 0;
  font-size: 24px;
  font-weight: 600;
}
.search-input {
  width: 100%;
}
.contacts-content {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}
.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
}
.count-badge {
  background-color: #e4e7ed;
  color: #909399;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: normal;
}
.contact-list {
  padding: 0 8px;
}
.contact-item {
  display: flex;
  align-items: center;
  padding: 10px 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-radius: 6px;
}
.contact-item:hover {
  background-color: #f5f7fa;
}
.contact-name {
  margin-left: 12px;
  font-size: 16px;
  color: #303133;
}
.online-status-badge :deep(.el-badge__content.is-dot) {
  background-color: #67c23a; /* 在线状态显示为绿色 */
  border: 1px solid white;
  width: 10px;
  height: 10px;
}
.el-collapse {
  border: none;
}
:deep(.el-collapse-item__header) {
  border: none;
}
:deep(.el-collapse-item__wrap) {
  border: none;
}
:deep(.el-collapse-item__content) {
  padding-bottom: 10px;
}
</style> 