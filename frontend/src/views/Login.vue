<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>我聊 (WoLiao)</h2>
        <p>欢迎回来，请登录您的账户</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="phoneNumber">
          <el-input
            v-model="loginForm.phoneNumber"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
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
              v-model="loginForm.verificationCode"
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
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
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
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaId = ref('')

const loginForm = reactive({
  phoneNumber: '',
  password: '',
  verificationCode: '',
  captchaId: ''
})

const loginRules: FormRules = {
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
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
    loginForm.captchaId = newCaptchaId
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    
    if (!captchaId.value) {
      ElMessage.error('请先获取验证码')
      return
    }
    
    loading.value = true
    
    const response = await api.post('/auth/login', {
      phoneNumber: loginForm.phoneNumber,
      password: loginForm.password,
      verificationCode: loginForm.verificationCode,
      captchaId: captchaId.value
    })
    
    const { accessToken, refreshToken } = response.data
    
    // 存储令牌
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    
    ElMessage.success('登录成功')
    router.push('/')
    
  } catch (error: any) {
    if (error.response?.status === 400) {
      ElMessage.error('验证码错误或登录信息不正确')
      // 刷新验证码
      refreshCaptcha()
    } else {
      ElMessage.error('登录失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #333;
  margin-bottom: 8px;
  font-size: 28px;
  font-weight: 600;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-form {
  width: 100%;
}

.captcha-container {
  display: flex;
  align-items: center;
  width: 100%;
}

.captcha-image-container {
  width: 110px;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.3s;
}

.captcha-image-container:hover {
  border-color: #409eff;
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}
</style> 