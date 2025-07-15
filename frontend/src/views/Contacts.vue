<template>
  <div class="contacts-page">
    <div class="page-header" style="display: flex; align-items: center; justify-content: space-between;">
      <div>
        <h2>联系人</h2>
        <el-input
          v-model="searchText"
          placeholder="搜索联系人"
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
      </div>
      <div style="display: flex; gap: 8px;">
        <el-button type="primary" @click="showAddFriend = true">添加好友</el-button>
        <el-button type="success" @click="showCreateGroup = true">创建群聊</el-button>
      </div>
    </div>
    <!-- 添加好友弹窗 -->
    <el-dialog v-model="showAddFriend" title="添加好友" width="350px" @close="resetAddFriend">
      <el-form @submit.prevent>
        <el-form-item label="手机号">
          <el-input v-model="addFriendPhone" maxlength="11" placeholder="请输入对方手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddFriend" :loading="addFriendLoading">添加</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
    <!-- 创建群聊弹窗 -->
    <el-dialog v-model="showCreateGroup" title="创建群聊" width="400px" @close="resetCreateGroup">
      <el-form @submit.prevent>
        <el-form-item label="群聊名称">
          <el-input v-model="groupName" maxlength="20" placeholder="请输入群聊名称" />
        </el-form-item>
        <el-form-item label="选择成员">
          <el-select v-model="selectedMemberIds" multiple filterable placeholder="请选择成员" style="width: 100%;">
            <el-option v-for="f in friends" :key="f.id" :label="f.nickname" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handleCreateGroup" :loading="createGroupLoading">创建</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
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
import { ElMessage, ElDialog, ElButton, ElForm, ElFormItem, ElInput, ElSelect, ElOption } from 'element-plus';
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

const showAddFriend = ref(false);
const addFriendPhone = ref('');
const addFriendLoading = ref(false);
const showCreateGroup = ref(false);
const groupName = ref('');
const selectedMemberIds = ref<number[]>([]);
const createGroupLoading = ref(false);

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

function resetAddFriend() {
  addFriendPhone.value = '';
  addFriendLoading.value = false;
}

async function handleAddFriend() {
  if (!/^1[3-9]\d{9}$/.test(addFriendPhone.value)) {
    ElMessage.warning('请输入有效的手机号');
    return;
  }
  addFriendLoading.value = true;
  try {
    await api.post('/contacts/add-friend', null, { params: { phoneNumber: addFriendPhone.value } });
    ElMessage.success('好友请求已发送或已添加');
    showAddFriend.value = false;
    fetchContacts();
  } catch (e: any) {
    ElMessage.error(e?.response?.data || '添加失败');
  } finally {
    addFriendLoading.value = false;
  }
}

function resetCreateGroup() {
  groupName.value = '';
  selectedMemberIds.value = [];
  createGroupLoading.value = false;
}

async function handleCreateGroup() {
  if (!groupName.value.trim()) {
    ElMessage.warning('请输入群聊名称');
    return;
  }
  if (selectedMemberIds.value.length === 0) {
    ElMessage.warning('请选择至少一位成员');
    return;
  }
  createGroupLoading.value = true;
  try {
    await api.post('/group/create', selectedMemberIds.value, { params: { name: groupName.value } });
    ElMessage.success('群聊创建成功');
    showCreateGroup.value = false;
    fetchContacts();
  } catch (e: any) {
    ElMessage.error(e?.response?.data || '创建失败');
  } finally {
    createGroupLoading.value = false;
  }
}

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