<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>注册新账号</h2>
        <p>请填写信息完成注册</p>
      </div>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="registerForm.phoneNumber"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item prop="verificationCode">
          <div class="captcha-container">
            <el-input
              v-model="registerForm.verificationCode"
              placeholder="请输入验证码"
              prefix-icon="Key"
              size="large"
              style="flex: 1; margin-right: 10px;"
            />
            <div class="captcha-image-container">
              <img
                v-if="captchaImage"
                :src="`data:image/png;base64,${captchaImage}`"
                alt="验证码"
                class="captcha-image"
                @click="refreshCaptcha"
              />
              <el-button
                v-else
                type="primary"
                size="large"
                @click="refreshCaptcha"
              >
                获取验证码
              </el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="goToLogin">已有账号？去登录</el-link>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import api from '@/api/axios'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaId = ref('')

const registerForm = reactive({
  phoneNumber: '',
  nickname: '',
  password: '',
  verificationCode: '',
  captchaId: ''
})

const registerRules: FormRules = {
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 16, message: '昵称长度2-16位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  try {
    const response = await api.get('/captcha/generate')
    const { captchaId: newCaptchaId, imageBase64 } = response.data
    captchaId.value = newCaptchaId
    captchaImage.value = imageBase64
    registerForm.captchaId = newCaptchaId
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    if (!captchaId.value) {
      ElMessage.error('请先获取验证码')
      return
    }
    loading.value = true
    await api.post('/auth/register', {
      phoneNumber: registerForm.phoneNumber,
      nickname: registerForm.nickname,
      password: registerForm.password,
      verificationCode: registerForm.verificationCode,
      captchaId: captchaId.value
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    if (error.response?.status === 400) {
      ElMessage.error(error.response.data || '验证码错误或注册信息不正确')
      refreshCaptcha()
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
/* 样式同前 */
</style> 