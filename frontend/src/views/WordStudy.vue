<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '../api/http'

const queue = ref([])
const idx = ref(0)
const revealed = ref(false)
const done = ref(0)
const finished = ref(false)
const loading = ref(true)

onMounted(async () => {
  queue.value = await http.get('/words/queue', { params: { size: 20 } })
  loading.value = false
  if (!queue.value.length) finished.value = true
})

const current = () => queue.value[idx.value]

async function answer(right) {
  const w = current()
  if (!w) return
  await http.post('/words/result', { wordId: w.id, right })
  done.value++
  idx.value++
  revealed.value = false
  if (idx.value >= queue.value.length) finished.value = true
}

async function reload() {
  loading.value = true
  idx.value = 0; done.value = 0; revealed.value = false
  queue.value = await http.get('/words/queue', { params: { size: 20 } })
  loading.value = false
  if (!queue.value.length) finished.value = true
}
</script>

<template>
  <div class="page">
    <div class="head">
      <el-button text @click="$router.push('/')">← 首页</el-button>
      <span class="progress">第 {{ Math.min(idx + 1, queue.length) }} / {{ queue.length }} 词 · 今日已背 {{ done }}</span>
    </div>

    <el-card v-if="!finished && !loading" class="card" v-loading="false">
      <div class="word">{{ current()?.word }}</div>
      <div class="pho">{{ current()?.phonetic }}</div>

      <div v-if="revealed" class="meaning">
        <p class="cn">{{ current()?.meaning }}</p>
        <p class="ex">{{ current()?.example }}</p>
        <p class="excn">{{ current()?.exampleCn }}</p>
        <el-tag size="small" type="info">真题频率 Top {{ current()?.freqRank }}</el-tag>
      </div>
      <el-button v-else class="reveal" text @click="revealed = true">点击显示释义</el-button>

      <div class="ops">
        <el-button size="large" type="danger" plain :disabled="!revealed" @click="answer(false)">
          😵 不认识
        </el-button>
        <el-button size="large" type="success" :disabled="!revealed" @click="answer(true)">
          😎 认识
        </el-button>
      </div>
      <p class="hint">先想释义 → 点开核对 → 如实回答,记忆曲线才算得准(答错 10 分钟后回锅)</p>
    </el-card>

    <el-card v-else-if="finished" class="card center">
      <div style="font-size:56px">🎉</div>
      <h3>{{ queue.length ? '今日队列完成!' : '暂时没有要背的词' }}</h3>
      <p v-if="queue.length">背了 {{ done }} 个,明天记得回来复习(到期词会自动排队)</p>
      <el-button type="primary" @click="reload">再来一组</el-button>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width: 620px; margin: 0 auto; padding: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.progress { color: #666; font-size: 13px; }
.card { border-radius: 16px; padding: 10px; text-align: center; }
.word { font-size: 44px; font-weight: 800; color: #1a1a1a; margin-top: 10px; }
.pho { color: #0b57d0; font-size: 16px; margin: 6px 0 18px; }
.meaning { background: #f6f9ff; border-radius: 10px; padding: 14px; margin-bottom: 10px; text-align: left; }
.meaning .cn { font-size: 17px; font-weight: 600; margin: 2px 0 10px; }
.meaning .ex { font-size: 14px; margin: 4px 0; }
.meaning .excn { font-size: 13px; color: #777; margin: 2px 0 8px; }
.reveal { margin: 26px 0; }
.ops { display: flex; gap: 16px; justify-content: center; margin-top: 14px; }
.hint { font-size: 12px; color: #999; margin-top: 12px; }
.center h3 { margin: 10px 0 6px; }
</style>
