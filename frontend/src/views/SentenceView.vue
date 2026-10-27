<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const list = ref([])
const idx = ref(0)
const showTip = ref(false)

async function load() {
  list.value = await http.get('/skills/sentences', { params: { size: 5 } })
  idx.value = 0; showTip.value = false
}
function next() {
  if (idx.value < list.value.length - 1) { idx.value++; showTip.value = false }
  else load()
}
onMounted(load)
</script>

<template>
  <div class="page" v-if="list.length">
    <div class="head">
      <el-button text @click="$router.push('/skills')">← 返回</el-button>
      <span class="cnt">{{ idx + 1 }} / {{ list.length }}</span>
    </div>
    <el-card class="card">
      <p class="sent">{{ list[idx].content }}</p>
      <el-tag size="small" color="#fff3e0" style="color:#e37400;border:none">考点:{{ list[idx].grammarPoint }}</el-tag>
      <div v-if="showTip" class="tip">
        <h5>🔧 主干拆解</h5>
        <p>{{ list[idx].structureTip }}</p>
        <h5>🈶 参考译文</h5>
        <p>{{ list[idx].translation }}</p>
      </div>
      <div class="ops">
        <el-button v-if="!showTip" type="primary" @click="showTip = true">看拆解</el-button>
        <el-button v-else type="success" @click="next">下一句 →</el-button>
      </div>
      <p class="hint">先自己划成分、试着翻译,再点「看拆解」对答案</p>
    </el-card>
  </div>
</template>

<style scoped>
.page { max-width: 720px; margin: 0 auto; padding: 16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.cnt { color:#888; }
.card { border-radius:14px; }
.sent { font-size:19px; line-height:1.9; font-weight:600; }
.tip { background:#f6f9ff; border-radius:10px; padding:12px 14px; margin-top:12px; text-align:left; }
.tip h5 { margin:6px 0 4px; }
.tip p { margin:0 0 8px; color:#444; font-size:14px; }
.ops { margin-top:14px; }
.hint { font-size:12px; color:#999; }
</style>
