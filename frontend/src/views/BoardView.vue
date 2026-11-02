<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const posts = ref([])
const draft = ref('')
const sending = ref(false)
const me = JSON.parse(localStorage.getItem('user') || '{}')

async function load() { posts.value = await http.get('/posts') }
onMounted(load)

async function publish() {
  if (!draft.value.trim()) return ElMessage.warning('先写点什么')
  sending.value = true
  try {
    await http.post('/posts', { content: draft.value.trim() })
    draft.value = ''
    await load()
    ElMessage.success('已上墙')
  } finally { sending.value = false }
}

async function like(p) {
  await http.post(`/posts/${p.id}/like`)
  p.likeCount++
}
</script>

<template>
  <div class="page">
    <div class="head">
      <el-button text @click="$router.push('/')">← 首页</el-button>
      <h3 style="margin:0">🧱 研友留言墙</h3>
    </div>

    <el-card class="card composer">
      <div class="row">
        <div class="avatar" :style="{ background: '#0b57d0' }">{{ (me.nickname||'我')[0] }}</div>
        <el-input v-model="draft" type="textarea" :rows="3" maxlength="300" show-word-limit
                  placeholder="打卡 / 吐槽 / 求助,比如:今天 60 个词全对,爽!" />
      </div>
      <div class="ops">
        <el-button type="primary" :loading="sending" @click="publish">上墙</el-button>
      </div>
    </el-card>

    <el-empty v-if="!posts.length" description="墙还是空的,发第一条打卡吧 ✊" />

    <el-card v-for="p in posts" :key="p.id" class="card post">
      <div class="row top">
        <div class="avatar">{{ p.nickname[0] }}</div>
        <div class="meta">
          <b>{{ p.nickname }}</b>
          <span>{{ p.time }}</span>
        </div>
        <el-button class="like" text @click="like(p)">👍 {{ p.likeCount }}</el-button>
      </div>
      <p class="content">{{ p.content }}</p>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:680px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.card { border-radius:14px; margin-bottom:12px; }
.row { display:flex; gap:10px; align-items:flex-start; }
.row.top { align-items:center; }
.avatar { width:36px; height:36px; border-radius:50%; background:#6fa8ff; color:#fff;
          display:flex; align-items:center; justify-content:center; font-weight:700; flex:none; }
.meta { flex:1; display:flex; flex-direction:column; }
.meta span { font-size:12px; color:#999; }
.like { color:#0b57d0; }
.ops { display:flex; justify-content:flex-end; margin-top:10px; }
.content { margin:10px 0 0; line-height:1.7; color:#333; white-space:pre-wrap; }
</style>
