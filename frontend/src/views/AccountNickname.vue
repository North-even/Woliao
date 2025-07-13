<template>
  <div class="edit-section">
    <el-form :model="nicknameForm" :rules="nicknameRules" ref="nicknameFormRef" label-width="70px" style="max-width:400px;margin:auto;">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="nicknameForm.nickname" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="nicknameLoading" @click="updateNickname">保存昵称</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import api from '@/api/axios'

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
const currentAccount = getCurrentAccount()
const nicknameForm = ref({ nickname: currentAccount?.nickname || '' })
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
    }
    ElMessage.success('昵称修改成功')
  } catch (e) {
    ElMessage.error('昵称修改失败')
  } finally {
    nicknameLoading.value = false
  }
}
onMounted(() => {
  nicknameForm.value.nickname = getCurrentAccount()?.nickname || ''
})
</script>
<style scoped>
.edit-section {
  margin: 32px auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 32px 0 0 0;
  max-width: 500px;
}
</style> 