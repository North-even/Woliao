<template>
  <div class="edit-section">
    <el-form :model="pwdForm" :rules="pwdRules" ref="pwdFormRef" label-width="70px" style="max-width:400px;margin:auto;">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="pwdLoading" @click="changePassword">修改密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import api from '@/api/axios'
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