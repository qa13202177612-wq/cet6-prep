<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../api/http'

const questions = ref([])
const started = ref(false)
const answers = ref({})
const seconds = ref(360)
const result = ref(null)
const startAt = ref(0)
let timer = null

async function start() {
  const data = await http.get('/exam/start')
  questions.value = data.questions
  started.value = true
  startAt.value = Date.now()
  seconds.value = (data.exam.durationMin || 6) * 60
  timer = setInterval(() => {
    seconds.value--
    if (seconds.value <= 0) submit(true)
  }, 1000)
}
function fmt(s) { return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}` }

async function submit(auto = false) {
  if (result.value) return
  if (!auto && Object.keys(answers.value).length < questions.value.length) {
    if (!confirm('还有题目未作答,确定交卷?')) return
  }
  clearInterval(timer)
  const durationSec = Math.round((Date.now() - startAt.value) / 1000)
  result.value = await http.post('/exam/submit', { answers: answers.value, durationSec })
}
function reset() { result.value = null; answers.value = {}; started.value = false }
onMounted(() => {})
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="page">
    <div class="head">
      <el-button text @click="$router.push('/')">← 首页</el-button>
      <span v-if="started && !result" class="timer" :class="{ urgent: seconds < 60 }">⏱ {{ fmt(seconds) }}</span>
    </div>

    <el-card v-if="!started" class="center card">
      <div style="font-size:52px">🎯</div>
      <h3>迷你模考(12 题 / 限时 6 分钟)</h3>
      <p style="color:#777">从题库随机抽语法 + 阅读题,交卷自动换算 710 分制成绩并存档</p>
      <el-button type="primary" size="large" @click="start">开始考试</el-button>
    </el-card>

    <template v-else-if="!result">
      <el-card class="card" v-for="(q, i) in questions" :key="q.id">
        <p class="stem">{{ i + 1 }}. [{{ q.bizType === 'GRAMMAR' ? '语法' : '阅读' }}] {{ q.stem }}</p>
        <el-radio-group v-model="answers[q.id]">
          <el-radio v-for="opt in q.options" :key="opt" :value="opt[0]" class="opt">{{ opt }}</el-radio>
        </el-radio-group>
      </el-card>
      <div class="center"><el-button type="danger" size="large" @click="submit()">交 卷</el-button></div>
    </template>

    <el-card v-else class="center card">
      <h2>本次成绩(710 分制)</h2>
      <div class="big">{{ result.score710 }}</div>
      <p>答对 {{ result.correct }}/{{ result.total }} · 用时 {{ Math.round((Date.now() - startAt) / 1000) }} 秒</p>
      <el-space>
        <el-button @click="reset">再来一套</el-button>
        <el-button type="primary" @click="$router.push('/report')">看学情报告 →</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:760px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.timer { font-size:20px; font-weight:800; color:#0b57d0; }
.timer.urgent { color:#d93025; animation: blink 1s infinite; }
@keyframes blink { 50% { opacity:.4; } }
.card { border-radius:14px; margin-bottom:14px; }
.center { text-align:center; }
.stem { font-weight:600; }
.opt { display:block; height:auto; margin:6px 0; white-space:normal; line-height:1.6; }
.big { font-size:64px; font-weight:800; color:#0b57d0; margin:6px 0; }
</style>
