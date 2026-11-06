<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import http from '../api/http'

const paper = ref(null)
const started = ref(false)
const seconds = ref(90 * 60)
const answers = ref({})          // {qid: 'A'}
const checkedSent = ref([])      // 自测完成的句子 id
const essay = ref('')
const result = ref(null)
const startAt = ref(0)
let timer = null

async function start() {
  paper.value = await http.get('/exam/full')
  seconds.value = paper.value.durationSec
  started.value = true
  startAt.value = Date.now()
  timer = setInterval(() => {
    seconds.value--
    if (seconds.value <= 0) submit(true)
  }, 1000)
}
function fmt(s) {
  if (s < 0) s = 0
  return `${String(Math.floor(s / 3600)).padStart(2, '0')}:${String(Math.floor(s % 3600 / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`
}

async function submit(auto = false) {
  if (result.value) return
  if (!auto) {
    const unanswered = (paper.value.objective.length - Object.keys(answers.value).length)
    if (unanswered > 0 && !confirm(`还有 ${unanswered} 道客观题没选,确定交卷?`)) return
  }
  clearInterval(timer)
  const durationSec = Math.round((Date.now() - startAt.value) / 1000)
  result.value = await http.post('/exam/full/submit', {
    answers: answers.value,
    sentenceChecked: checkedSent.value,
    essay: essay.value,
    durationSec
  })
  window.scrollTo(0, 0)
}
function reset() { result.value = null; started.value = false; answers.value = {}; checkedSent.value = []; essay.value = '' }
function toggleSent(id) {
  const i = checkedSent.value.indexOf(id)
  i >= 0 ? checkedSent.value.splice(i, 1) : checkedSent.value.push(id)
}
onMounted(() => {})
onBeforeUnmount(() => clearInterval(timer))
</script>

<template>
  <div class="page">
    <div class="head">
      <el-button text @click="$router.push('/exam')">← 模考台</el-button>
      <span v-if="started && !result" class="timer" :class="{ urgent: seconds < 300 }">⏱ {{ fmt(seconds) }}</span>
    </div>

    <el-card v-if="!started" class="center card">
      <div style="font-size:52px">📄</div>
      <h3>整卷模考 · 适配版(90 分钟)</h3>
      <div class="brief">
        <p>Part 1 客观题:语法 ×8 + 阅读 ×4(判分 + 进错题本)</p>
        <p>Part 2 长难句:10 句拆主干自测(勾选打卡)</p>
        <p>Part 3 写作:150 词以上,交卷可再送 AI 精批</p>
        <p class="score-rule">折算 710 制:客观 500 + 长难句 100 + 写作 110</p>
      </div>
      <el-button type="primary" size="large" @click="start">开始考试</el-button>
    </el-card>

    <template v-else-if="!result && paper">
      <el-card class="card">
        <h4>Part 1 · 客观题({{ paper.objective.length }})</h4>
        <div v-for="(q, i) in paper.objective" :key="q.id" class="q">
          <p class="stem">{{ i + 1 }}. [{{ q.bizType === 'GRAMMAR' ? '语法' : '阅读' }}] {{ q.stem }}</p>
          <el-radio-group v-model="answers[q.id]">
            <el-radio v-for="opt in q.options" :key="opt" :value="opt[0]" class="opt">{{ opt }}</el-radio>
          </el-radio-group>
        </div>
      </el-card>

      <el-card class="card">
        <h4>Part 2 · 长难句自测(已勾 {{ checkedSent.length }}/10)</h4>
        <div v-for="s in paper.sentences" :key="s.id" class="sent" @click="toggleSent(s.id)">
          <el-checkbox :model-value="checkedSent.includes(s.id)" @click.stop />
          <div class="sent-body">
            <p>{{ s.content }}</p>
            <el-tag size="small" type="info">{{ s.grammarPoint }}</el-tag>
          </div>
        </div>
        <p class="tip">自己划完主干、心里翻译了再勾选,别骗记忆曲线 😉</p>
      </el-card>

      <el-card class="card">
        <h4>Part 3 · {{ paper.essay.title }}</h4>
        <p class="prompt">{{ paper.essay.prompt }}</p>
        <el-input v-model="essay" type="textarea" :rows="8" placeholder="写满 150 词再交,交完可以马上送 AI 批改" />
        <div class="ops">
          <span class="len">{{ essay.length }} 字符</span>
          <el-button type="danger" size="large" @click="submit()">交 卷</el-button>
        </div>
      </el-card>
    </template>

    <el-card v-else-if="result" class="center card">
      <h3>本次整卷成绩(710 制)</h3>
      <div class="big">{{ result.score710 }}</div>
      <el-row class="parts">
        <el-col :span="8"><p class="lab">客观题</p><b>{{ result.objScore }}/500</b><p class="sub">对 {{ result.correct }}/{{ result.total }}</p></el-col>
        <el-col :span="8"><p class="lab">长难句</p><b>{{ result.sentScore }}/100</b><p class="sub">{{ result.sentences }}</p></el-col>
        <el-col :span="8"><p class="lab">写作</p><b>{{ result.essayScore }}/110</b><p class="sub">{{ result.essayDone ? '已交' : '未交' }}</p></el-col>
      </el-row>
      <el-alert v-for="(a, i) in result.advice" :key="i" :title="a" type="warning" :closable="false" class="adv" />
      <el-space style="margin-top:14px">
        <el-button @click="reset">再来一套</el-button>
        <el-button type="primary" @click="$router.push('/report')">学情报告 →</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:760px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.timer { font-size:18px; font-weight:800; color:#0b57d0; }
.timer.urgent { color:#d93025; animation: blink 1s infinite; }
@keyframes blink { 50% { opacity:.4; } }
.card { border-radius:14px; margin-bottom:14px; }
.center { text-align:center; }
.brief { text-align:left; background:#f6f9ff; border-radius:10px; padding:10px 14px; font-size:14px; }
.score-rule { color:#0b57d0; font-weight:600; }
.q { border-bottom:1px dashed #eee; padding-bottom:8px; margin-bottom:8px; }
.stem { font-weight:600; margin:6px 0; }
.opt { display:block; height:auto; margin:4px 0; white-space:normal; }
.sent { display:flex; gap:8px; align-items:flex-start; padding:8px; border-radius:8px; cursor:pointer; }
.sent:hover { background:#f6f9ff; }
.sent-body p { margin:2px 0 6px; font-size:14px; }
.tip { font-size:12px; color:#999; }
.prompt { background:#f6f9ff; padding:10px; border-radius:8px; font-size:14px; }
.ops { display:flex; justify-content:space-between; align-items:center; margin-top:12px; }
.len { color:#999; font-size:12px; }
.big { font-size:64px; font-weight:800; color:#0b57d0; }
.parts { margin:10px 0; }
.lab { margin:0; color:#888; font-size:13px; }
.sub { margin:2px; color:#aaa; font-size:12px; }
.adv { margin-top:8px; text-align:left; }
</style>
