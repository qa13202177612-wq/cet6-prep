<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const items = [
  { to: '/sentence', icon: '🧩', name: '长难句拆解', desc: '真题风格句子,划分句子主干' },
  { to: '/listening', icon: '🎧', name: '听力精听', desc: '逐句听写 · TTS 朗读 · 变速' },
  { to: '/reading', icon: '📰', name: '仔细阅读', desc: '段落+题目,提交自动判分进错题本' },
  { to: '/writing', icon: '✒️', name: '作文 / 翻译 AI 批改', desc: '写满 30 字交卷,千问逐句批改' }
]
</script>

<template>
  <div class="page">
    <div class="head">
      <el-button text @click="router.push('/')">← 首页</el-button>
      <h3 style="margin:0">专项突破</h3>
      <span></span>
    </div>
    <el-row :gutter="14">
      <el-col :span="12" v-for="i in items" :key="i.to" style="margin-bottom:14px">
        <el-card shadow="hover" class="tile" @click="router.push(i.to)">
          <div style="font-size:38px">{{ i.icon }}</div>
          <h4>{{ i.name }}</h4>
          <p>{{ i.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page { max-width: 860px; margin: 0 auto; padding: 16px; }
.head { display:flex; justify-content:space-between; align-items:center; margin-bottom:14px; }
.tile { text-align:center; cursor:pointer; border-radius:14px; height:100%; }
.tile h4 { margin:8px 0 4px; }
.tile p { font-size:12px; color:#777; margin:0; }
</style>
