<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const sets = ref([])
const setIdx = ref(0)
const lineIdx = ref(0)
const showText = ref(false)
const showCn = ref(false)
const rate = ref(1)

onMounted(async () => {
  sets.value = await http.get('/skills/listening')
})

const current = () => sets.value[setIdx.value] ?? { lines: [] }

function speak(line) {
  if (!window.speechSynthesis) return alert('浏览器不支持语音合成,请用 Chrome/Edge')
  speechSynthesis.cancel()
  const u = new SpeechSynthesisUtterance(line)
  u.lang = 'en-US'
  u.rate = rate.value
  speechSynthesis.speak(u)
}
function replay() { speak(current().lines[lineIdx.value]?.enText) }
function next() {
  const lines = current().lines
  if (lineIdx.value < lines.length - 1) { lineIdx.value++; showText.value = false; showCn.value = false }
  else if (setIdx.value < sets.value.length - 1) { setIdx.value++; lineIdx.value = 0; showText.value = false; showCn.value = false }
}
function prev() {
  if (lineIdx.value > 0) lineIdx.value--
  else if (setIdx.value > 0) { setIdx.value--; lineIdx.value = sets.value[setIdx.value].lines.length - 1 }
  showText.value = false; showCn.value = false
}
</script>

<template>
  <div class="page" v-if="sets.length">
    <div class="head">
      <el-button text @click="$router.push('/skills')">← 返回</el-button>
      <span class="cnt">{{ current().title }} · 第 {{ lineIdx + 1 }}/{{ current().lines.length }} 句</span>
    </div>
    <el-card class="card">
      <div class="play" @click="replay">🔊</div>
      <p class="label">点击喇叭重听,先「心译」再看文本</p>
      <div v-if="showText" class="text">
        <p class="en">{{ current().lines[lineIdx]?.enText }}</p>
        <p v-if="showCn" class="cn">{{ current().lines[lineIdx]?.cnText }}</p>
      </div>
      <div class="ops">
        <el-select v-model="rate" size="small" style="width:96px" @change="replay">
          <el-option :value="0.75" label="0.75x" />
          <el-option :value="1" label="1.0x" />
          <el-option :value="1.25" label="1.25x" />
        </el-select>
        <el-button @click="prev">← 上一句</el-button>
        <el-button v-if="!showText" type="primary" @click="showText = true">显示原文</el-button>
        <el-button v-else-if="!showCn" @click="showCn = true">看译文</el-button>
        <el-button v-else type="success" @click="next">下一句 →</el-button>
      </div>
      <p class="hint">精听三步:盲听抓意 → 逐词对原文 → 跟读模仿(浏览器 TTS 朗读)</p>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width:720px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.cnt { color:#888; font-size:13px; }
.card { border-radius:14px; text-align:center; }
.play { font-size:60px; cursor:pointer; user-select:none; transition:transform .15s; }
.play:active { transform:scale(.9); }
.label { color:#999; font-size:12px; }
.text { background:#f6f9ff; border-radius:10px; padding:14px; margin:8px 0; text-align:left; }
.en { font-size:17px; font-weight:600; margin:0 0 6px; }
.cn { color:#666; font-size:14px; margin:0; }
.ops { display:flex; gap:10px; justify-content:center; margin-top:12px; }
.hint { font-size:12px; color:#999; }
</style>
