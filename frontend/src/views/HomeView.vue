<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

const router = useRouter()
const stats = ref({ total: 0, learned: 0, due: 0 })
const user = JSON.parse(localStorage.getItem('user') || '{}')

onMounted(async () => {
  stats.value = await http.get('/words/stats')
})

const stages = [
  { key: 'vocab', icon: '📖', title: '词汇基础', desc: '真题高频词库 · 艾宾浩斯记忆曲线', to: '/words', ready: true },
  { key: 'skill', icon: '🧩', title: '专项突破', desc: '长难句 · 听力精听 · 阅读 · AI批改', to: '/skills', ready: true },
  { key: 'exam', icon: '🎯', title: '真题模考', desc: '计时刷题 · 自动评分 · 学情报告', to: '/exam', ready: true }
]
</script>

<template>
  <div class="page">
    <h3 style="margin:18px 4px 14px">{{ user.nickname }}，今天也离 {{ user.goalScore }}+ 近一步 💪</h3>

    <el-row :gutter="14">
      <el-col :span="24" style="margin-bottom:14px">
        <el-card>
          <div class="stats">
            <div><b>{{ stats.learned }}</b><span>已学</span></div>
            <div><b style="color:#e37400">{{ stats.due }}</b><span>待复习</span></div>
            <div><b>{{ stats.total }}</b><span>词库总量</span></div>
          </div>
          <el-progress :percentage="stats.total ? Math.round(stats.learned / stats.total * 100) : 0" striped />
        </el-card>
      </el-col>

      <el-col v-for="s in stages" :key="s.key" :span="8">
        <el-card shadow="hover" class="stage" :class="{ dim: !s.ready }"
                 @click="s.ready && router.push(s.to)">
          <div style="font-size:34px">{{ s.icon }}</div>
          <h4>{{ s.title }}</h4>
          <p>{{ s.desc }}</p>
          <el-tag v-if="!s.ready" type="info" size="small">M2-M3 里程碑开发中</el-tag>
          <el-tag v-else color="#e6f4ea" size="small" style="color:#137333;border:none">点击进入 →</el-tag>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page { max-width: 960px; margin: 0 auto; padding: 0 16px; }
.stats { display: flex; gap: 40px; margin-bottom: 12px; }
.stats div { display: flex; flex-direction: column; }
.stats b { font-size: 26px; }
.stats span { font-size: 12px; color: #888; }
.stage { text-align: center; cursor: pointer; height: 100%; border-radius: 12px; }
.stage h4 { margin: 8px 0 4px; }
.stage p { font-size: 12px; color: #777; min-height: 34px; }
.dim { opacity: .65; cursor: not-allowed; }
</style>
