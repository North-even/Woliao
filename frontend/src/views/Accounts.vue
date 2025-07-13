<template>
  <div class="accounts-page">
    <div class="page-header">
      <h2>账号管理</h2>
    </div>
    <div class="accounts-list">
      <div
        v-for="account in validAccounts"
        :key="account.phoneNumber"
        class="account-item"
        :class="{ active: account.phoneNumber === currentAccount?.phoneNumber }"
        @click="switchAccount(account)"
      >
        <el-avatar :size="40" :src="account.avatarUrl || ''">
          {{ account.nickname?.charAt(0) || account.phoneNumber.slice(-2) }}
        </el-avatar>
        <div class="account-info">
          <div class="nickname">{{ account.nickname }}</div>
          <div class="phone">{{ account.phoneNumber }}</div>
        </div>
        <el-tag v-if="account.phoneNumber === currentAccount?.phoneNumber" type="success">当前</el-tag>
      </div>
    </div>
    <div class="logout-section">
      <el-button type="danger" @click="logout" style="width:100%;">退出当前账号</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

interface AccountInfo {
  phoneNumber: string
  nickname: string
  avatarUrl?: string
  accessToken: string
  refreshToken: string
  lastLogin: number // 时间戳
}

const ACCOUNTS_KEY = 'accounts'
const CURRENT_KEY = 'accessToken'

const getAccounts = (): AccountInfo[] => {
  const arr = localStorage.getItem(ACCOUNTS_KEY)
  if (!arr) return []
  try {
    return JSON.parse(arr)
  } catch {
    return []
  }
}

const saveAccounts = (accounts: AccountInfo[]) => {
  localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts))
}

const getCurrentAccount = (): AccountInfo | null => {
  const token = localStorage.getItem(CURRENT_KEY)
  const accounts = getAccounts()
  return accounts.find(a => a.accessToken === token) || null
}

const now = Date.now()
const THREE_DAYS = 3 * 24 * 60 * 60 * 1000

const validAccounts = computed(() => {
  return getAccounts().filter(a => now - a.lastLogin < THREE_DAYS)
})

const currentAccount = ref<AccountInfo | null>(getCurrentAccount())

const switchAccount = (account: AccountInfo) => {
  if (account.accessToken === localStorage.getItem(CURRENT_KEY)) return
  localStorage.setItem(CURRENT_KEY, account.accessToken)
  localStorage.setItem('refreshToken', account.refreshToken)
  ElMessage.success('切换成功')
  window.location.reload()
}

const logout = () => {
  localStorage.removeItem(CURRENT_KEY)
  localStorage.removeItem('refreshToken')
  ElMessage.success('已退出登录')
  router.push('/login')
}

onMounted(() => {
  // 清理过期账号
  const accounts = getAccounts().filter(a => now - a.lastLogin < THREE_DAYS)
  saveAccounts(accounts)
  currentAccount.value = getCurrentAccount()
})
</script>

<style scoped>
.accounts-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.page-header {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
  text-align: center;
}
.accounts-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.account-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background-color 0.3s;
  cursor: pointer;
  margin-bottom: 12px;
  border: 1px solid #f0f0f0;
}
.account-item.active {
  background: #f0f9ff;
  border-color: #409eff;
}
.account-info {
  margin-left: 16px;
  flex: 1;
}
.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.phone {
  color: #909399;
  font-size: 13px;
}
.logout-section {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}
</style> 