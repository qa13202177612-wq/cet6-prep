<script setup>
import { useRouter } from 'vue-router'
import http from './api/http'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')

async function logout() {
  await http.post('/auth/logout').catch(() => {})
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<template>
  <div v-if="$route.path !== '/login'" class="topbar">
    <span class="brand" @click="router.push('/')" style="cursor:pointer">🎓 六级备考站</span>
    <el-button size="small" text @click="router.push('/report')">📊 学情报告</el-button>
    <span class="who">{{ user.nickname || user.username }} · 目标 {{ user.goalScore }}</span>
    <el-button size="small" text @click="logout">退出</el-button>
  </div>
  <router-view />
</template>

<style>
body { margin: 0; background: #f4f6f8; font-family: -apple-system, "PingFang SC", "Microsoft YaHei", sans-serif; }
.topbar {
  display: flex; align-items: center; gap: 12px;
  background: #fff; padding: 10px 20px; box-shadow: 0 1px 4px rgba(0,0,0,.08);
}
.topbar .brand { font-weight: 700; font-size: 17px; color: #0b57d0; }
.topbar .who { margin-left: auto; color: #666; font-size: 13px; }
</style>
