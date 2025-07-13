<template>
  <div class="accounts-page">
    <div class="page-header">
      <h2>账号管理</h2>
    </div>
    <div class="menu-list">
      <el-card class="menu-item" @click="showDialog('nickname')">
        <div class="menu-title">更改昵称</div>
        <div class="menu-desc">修改当前账号昵称</div>
      </el-card>
      <el-card class="menu-item" @click="showDialog('password')">
        <div class="menu-title">更改密码</div>
        <div class="menu-desc">修改当前账号密码</div>
      </el-card>
      <el-card class="menu-item" @click="showDialog('switch')">
        <div class="menu-title">账号切换</div>
        <div class="menu-desc">切换或退出3天内登录过的账号</div>
      </el-card>
    </div>
    <!-- 更改昵称弹窗 -->
    <el-dialog v-model="dialog.nickname" title="更改昵称" width="400px" @close="resetNickname">
      <el-form :model="nicknameForm" :rules="nicknameRules" ref="nicknameFormRef" label-width="70px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="nicknameForm.nickname" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.nickname = false">取消</el-button>
        <el-button type="primary" :loading="nicknameLoading" @click="updateNickname">保存</el-button>
      </template>
    </el-dialog>
    <!-- 更改密码弹窗 -->
    <el-dialog v-model="dialog.password" title="更改密码" width="400px" @close="resetPwd">
      <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="70px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.password = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="changePassword">修改</el-button>
      </template>
    </el-dialog>
    <!-- 账号切换弹窗 -->
    <el-dialog v-model="dialog.switch" title="账号切换" width="400px">
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
        <div class="logout-section">
          <el-button type="danger" @click="logout" style="width:100%;">退出当前账号</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import api from '@/api/axios'

const dialog = ref({ nickname: false, password: false, switch: false })
const showDialog = (type: 'nickname' | 'password' | 'switch') => {
  dialog.value[type] = true
}
// 昵称相关
const ACCOUNTS_KEY = 'accounts'
const CURRENT_KEY = 'accessToken'
const getAccounts = () => {
  try {
    return JSON.parse(localStorage.getItem(ACCOUNTS_KEY) || '[]')
  } catch { return [] }
}
const saveAccounts = (accounts: any[]) => {
  localStorage.setItem(ACCOUNTS_KEY, JSON.stringify(accounts))
}
const getCurrentAccount = () => {
  const token = localStorage.getItem(CURRENT_KEY)
  const accounts = getAccounts()
  return accounts.find((a:any) => a.accessToken === token) || null
}
const currentAccount = ref(getCurrentAccount())
const nicknameForm = ref({ nickname: currentAccount.value?.nickname || '' })
const nicknameFormRef = ref<FormInstance>()
const nicknameLoading = ref(false)
const nicknameRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '昵称长度2-16位', trigger: 'blur' }
  ]
}
const updateNickname = async () => {
  if (!nicknameFormRef.value) return
  await nicknameFormRef.value.validate()
  nicknameLoading.value = true
  try {
    await api.post('/user/update-nickname', { nickname: nicknameForm.value.nickname })
    // 同步本地accounts
    const accounts = getAccounts()
    const token = localStorage.getItem(CURRENT_KEY)
    const idx = accounts.findIndex((a:any) => a.accessToken === token)
    if (idx !== -1) {
      accounts[idx].nickname = nicknameForm.value.nickname
      saveAccounts(accounts)
      currentAccount.value = accounts[idx]
    }
    ElMessage.success('昵称修改成功')
    dialog.value.nickname = false
  } catch (e) {
    ElMessage.error('昵称修改失败')
  } finally {
    nicknameLoading.value = false
  }
}
const resetNickname = () => {
  nicknameForm.value.nickname = currentAccount.value?.nickname || ''
}
// 密码相关
const pwdForm = ref({ oldPassword: '', newPassword: '' })
const pwdFormRef = ref<FormInstance>()
const pwdLoading = ref(false)
const pwdRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}
const changePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await api.post('/user/change-password', { ...pwdForm.value })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    window.location.href = '/login'
  } catch (e: any) {
    if (e.response?.data) {
      ElMessage.error(e.response.data)
    } else {
      ElMessage.error('密码修改失败')
    }
  } finally {
    pwdLoading.value = false
  }
}
const resetPwd = () => {
  pwdForm.value.oldPassword = ''
  pwdForm.value.newPassword = ''
}
// 账号切换相关
const now = Date.now()
const THREE_DAYS = 3 * 24 * 60 * 60 * 1000
const validAccounts = computed(() => {
  return getAccounts().filter((a:any) => now - a.lastLogin < THREE_DAYS)
})
const switchAccount = (account: any) => {
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
  window.location.href = '/login'
}
onMounted(() => {
  // 清理过期账号
  const accounts = getAccounts().filter((a:any) => now - a.lastLogin < THREE_DAYS)
  saveAccounts(accounts)
  currentAccount.value = getCurrentAccount()
  nicknameForm.value.nickname = currentAccount.value?.nickname || ''
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
.menu-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 32px 16px;
}
.menu-item {
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.menu-item:hover {
  box-shadow: 0 4px 16px rgba(64,158,255,0.15);
}
.menu-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.menu-desc {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
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