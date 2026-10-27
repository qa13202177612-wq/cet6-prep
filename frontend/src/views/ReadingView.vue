<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const passage = ref(null)
const answers = ref({})
const result = ref(null)
const submitting = ref(false)

onMounted(async () => {
  passage.value = await http.get('/reading/current')
})

async function submit() {
  const filled = Object.keys(answers.value).length
  if (filled < passage.value.questions.length) {
    if (!confirm(`还有 ${passage.value.questions.length - filled} 题没作答,确定交卷?`)) return
  }
  submitting.value = true
  try {
    result.value = await http.post('/reading/submit', answers.value)
  } finally { submitting.value = false }
}
function retry() { result.value = null; answers.value = {}; window.scrollTo(0, 0) }
</script>

<template>
  <div class="page" v-if="passage">
    <div class="head">
      <el-button text @click="$router.push('/skills')">← 返回</el-button>
      <el-tag size="small" type="info">{{ passage.passageType }} · {{ passage.sourceYear }}</el-tag>
    </div>

    <template v-if="!result">
      <el-card class="card">
        <h3>{{ passage.title }}</h3>
        <p class="body">{{ passage.content }}</p>
      </el-card>
      <el-card class="card" v-for="(q, i) in passage.questions" :key="q.id">
        <p class="stem">{{ i + 1 }}. {{ q.stem }}</p>
        <el-radio-group v-model="answers[q.id]">
          <el-radio v-for="opt in q.options" :key="opt" :value="opt[0]" class="opt">{{ opt }}</el-radio>
        </el-radio-group>
      </el-card>
      <div class="submit">
        <el-button type="primary" size="large" :loading="submitting" @click="submit">交卷判分</el-button>
      </div>
    </template>

    <el-card v-else class="card center">
      <div style="font-size:52px">{{ result.correct === result.total ? '🏆' : result.correct >= result.total - 1 ? '🎯' : '💪' }}</div>
      <h2>答对 {{ result.correct }} / {{ result.total }}</h2>
      <div v-for="r in result.results" :key="r.id" class="review" :class="{ bad: !r.right }">
        <span>{{ r.right ? '✅' : '❌' }} 你的答案 {{ r.userAnswer || '(空)' }} → 正确 {{ r.answer }}</span>
        <p>{{ r.analysis }}</p>
      </div>
      <el-button type="primary" @click="retry" style="margin-top:14px">再做一遍</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:760px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.card { border-radius:14px; margin-bottom:14px; }
.body { line-height:2; color:#333; }
.stem { font-weight:600; }
.opt { display:block; height:auto; margin:6px 0; white-space:normal; line-height:1.6; }
.submit { text-align:center; margin:10px 0 30px; }
.center { text-align:center; }
.review { text-align:left; background:#f2fbf4; border-radius:8px; padding:10px 12px; margin-top:10px; font-size:14px; }
.review.bad { background:#fff2f0; }
.review p { margin:4px 0 0; color:#666; font-size:13px; }
</style>
