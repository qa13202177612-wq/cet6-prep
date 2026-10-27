<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const router = useRouter()
const tab = ref('login')
const loading = ref(false)
const form = reactive({ username: '', password: '', nickname: '' })

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('用户名和密码都要填')
  loading.value = true
  try {
    if (tab.value === 'login') {
      const user = await http.post('/auth/login', { username: form.username, password: form.password })
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      await http.post('/auth/register', { ...form })
      const user = await http.post('/auth/login', { username: form.username, password: form.password })
      localStorage.setItem('user', JSON.stringify(user))
    }
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="wrap">
    <el-card class="box">
      <h2 style="text-align:center;margin:4px 0 16px">🎓 六级备考站</h2>
      <el-tabs v-model="tab" stretch>
        <el-tab-pane label="登录" name="login" />
        <el-tab-pane label="注册" name="register" />
      </el-tabs>
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="至少3位" />
        </el-form-item>
        <el-form-item v-if="tab === 'register'" label="昵称">
          <el-input v-model="form.nickname" placeholder="可留空" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password
                    placeholder="至少6位" @keyup.enter="submit" />
        </el-form-item>
      </el-form>
      <el-button type="primary" style="width:100%" :loading="loading" @click="submit">
        {{ tab === 'login' ? '登 录' : '注册并登录' }}
      </el-button>
    </el-card>
  </div>
</template>

<style scoped>
.wrap { height: 100vh; display: flex; align-items: center; justify-content: center;
        background: linear-gradient(135deg, #0b57d0 0%, #6fa8ff 100%); }
.box { width: 380px; border-radius: 14px; }
</style>
