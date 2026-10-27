<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const topics = ref([])
const topic = ref(null)
const content = ref('')
const reviewing = ref(false)
const result = ref(null)

onMounted(async () => {
  topics.value = await http.get('/skills/writing/topics')
  topic.value = topics.value[0]
})
async function review() {
  reviewing.value = true
  result.value = null
  try {
    result.value = await http.post('/ai/review', {
      content: content.value, type: topic.value.type,
      prompt: topic.value.prompt, topicId: topic.value.id
    })
  } finally { reviewing.value = false }
}
</script>

<template>
  <div class="page" v-if="topics.length">
    <div class="head">
      <el-button text @click="$router.push('/skills')">← 返回</el-button>
      <el-select v-model="topic" value-key="id" style="width:230px" size="small" @change="content=''; result=null">
        <el-option v-for="t in topics" :key="t.id" :label="t.title" :value="t" />
      </el-select>
    </div>
    <el-card class="card">
      <h4>{{ topic.title }}</h4>
      <p class="prompt">{{ topic.prompt }}</p>
      <el-input v-model="content" type="textarea" :rows="9"
                :placeholder="topic.type === 'ESSAY' ? '在此写你的英文作文(150-200 词)…' : '在此写下你的英文翻译…'" />
      <div class="ops">
        <span class="len">{{ content.length }} 字符</span>
        <el-button type="primary" :loading="reviewing" @click="review">🤖 交 AI 批改</el-button>
      </div>
    </el-card>

    <el-card v-if="result" class="card">
      <div class="score">
        <b>{{ result.score }}</b><span>分 · {{ result.grade }}</span>
      </div>
      <p class="sum">{{ result.summary }}</p>
      <h5>逐句批改</h5>
      <div v-for="(c, i) in result.corrections" :key="i" class="corr">
        <p class="orig">❌ {{ c.original }}</p>
        <p class="rev">✔ {{ c.revised }}</p>
        <p class="why">{{ c.reason }}</p>
      </div>
      <h5>提分建议</h5>
      <p v-for="(s, i) in result.improvements" :key="i" class="imp">• {{ s }}</p>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:760px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.card { border-radius:14px; margin-bottom:14px; }
.prompt { background:#f6f9ff; padding:10px 12px; border-radius:8px; font-size:14px; color:#444; }
.ops { display:flex; justify-content:space-between; align-items:center; margin-top:12px; }
.len { color:#999; font-size:12px; }
.score { font-size:20px; } .score b { font-size:40px; color:#0b57d0; }
.sum { color:#444; }
.corr { background:#fff8f6; border-radius:8px; padding:8px 12px; margin:8px 0; font-size:13px; }
.orig { color:#c5221f; margin:2px 0; } .rev { color:#137333; margin:2px 0; } .why { color:#777; margin:2px 0; }
.imp { font-size:14px; color:#444; }
</style>
