<template>
  <div class="contacts-page">
    <div class="page-header">
      <h2>联系人</h2>
    </div>
    
    <div class="contacts-content">
      <el-collapse v-model="activeNames" accordion>
        <!-- 群聊 -->
        <el-collapse-item name="groups">
          <template #title>
            <div class="collapse-title">
              <el-icon><UserFilled /></el-icon>
              <span>群聊</span>
              <span class="count">({{ contacts.groups.length }})</span>
            </div>
          </template>
          
          <div class="contact-list">
            <div
              v-for="group in sortedGroups"
              :key="group.id"
              class="contact-item"
              @click="openGroupChat(group)"
            >
              <el-avatar :size="40" :src="`https://via.placeholder.com/40/67c23a/ffffff?text=${group.name.charAt(0)}`" />
              <span class="contact-name">{{ group.name }}</span>
            </div>
          </div>
        </el-collapse-item>
        
        <!-- 个人 -->
        <el-collapse-item name="individuals">
          <template #title>
            <div class="collapse-title">
              <el-icon><User /></el-icon>
              <span>个人</span>
              <span class="count">({{ contacts.individuals.length }})</span>
            </div>
          </template>
          
          <div class="contact-list">
            <div
              v-for="individual in sortedIndividuals"
              :key="individual.id"
              class="contact-item"
              @click="openUserChat(individual)"
            >
              <el-avatar :size="40" :src="individual.avatar" />
              <span class="contact-name">{{ individual.name }}</span>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, User } from '@element-plus/icons-vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'

interface Contact {
  id: number
  name: string
  avatar?: string
}

interface ContactsData {
  groups: Contact[]
  individuals: Contact[]
}

const contacts = ref<ContactsData>({ groups: [], individuals: [] })
const activeNames = ref<string[]>([])
const loading = ref(false)

const sortedGroups = computed(() => {
  return [...contacts.value.groups].sort((a, b) => a.name.localeCompare(b.name))
})

const sortedIndividuals = computed(() => {
  return [...contacts.value.individuals].sort((a, b) => a.name.localeCompare(b.name))
})

const fetchContacts = async () => {
  try {
    loading.value = true
    const response = await api.get('/contacts')
    contacts.value = response.data
  } catch (error) {
    ElMessage.error('获取联系人列表失败')
  } finally {
    loading.value = false
  }
}

const router = useRouter()

const openUserChat = (user: any) => {
  router.push(`/chat/user/${user.id}`)
}

const openGroupChat = (group: any) => {
  router.push(`/chat/group/${group.id}`)
}

onMounted(() => {
  fetchContacts()
})
</script>

<style scoped>
.contacts-page {
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

.contacts-content {
  flex: 1;
  overflow-y: auto;
}

.collapse-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.count {
  color: #909399;
  font-weight: normal;
  font-size: 14px;
}

.contact-list {
  padding: 8px 0;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.3s;
  cursor: pointer;
}

.contact-item:hover {
  background-color: #f5f7fa;
}

.contact-name {
  margin-left: 12px;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
}

:deep(.el-collapse-item__header) {
  background-color: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
  padding: 16px;
}

:deep(.el-collapse-item__content) {
  padding: 0;
}

:deep(.el-collapse-item__wrap) {
  border: none;
}
</style> 