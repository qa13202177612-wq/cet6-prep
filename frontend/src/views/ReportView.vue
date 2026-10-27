<script setup>
import { onMounted, ref } from 'vue'
import http from '../api/http'

const r = ref(null)
onMounted(async () => { r.value = await http.get('/report/summary') })
</script>

<template>
  <div class="page" v-if="r">
    <div class="head">
      <el-button text @click="$router.push('/')">← 首页</el-button>
      <el-tag :type="r.checkedToday ? 'success' : 'info'" size="small">{{ r.checkedToday ? '今日已打卡 ✅' : '今天还没练' }}</el-tag>
    </div>

    <el-card class="card">
      <h4>📈 各题型正确率</h4>
      <div v-if="!r.accuracy.length" style="color:#999">还没有答题记录,先去刷两组题吧</div>
      <div v-for="a in r.accuracy" :key="a.bizType" class="acc">
        <span class="lab">{{ a.label }}</span>
        <el-progress :percentage="a.rate" :stroke-width="14" style="flex:1"
                     :status="a.rate >= 80 ? 'success' : a.rate < 60 ? 'exception' : ''" />
        <span class="cnt">{{ a.rightCount }}/{{ a.total }}</span>
      </div>
    </el-card>

    <el-card class="card">
      <h4>🧭 个性化建议</h4>
      <p v-for="(line, i) in r.advice" :key="i" class="advice">• {{ line }}</p>
    </el-card>

    <el-row :gutter="14">
      <el-col :span="12">
        <el-card class="card">
          <h4>📕 错题本({{ r.wrongTotal }})</h4>
          <div v-for="w in r.wrongBook" :key="w.id" class="wrong">
            <el-tag size="small" type="danger">错{{ w.timesWrong }}次</el-tag>
            <span>{{ w.stem }}</span>
          </div>
          <div v-if="!r.wrongBook.length" style="color:#999">空空如也,保持 👏</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="card">
          <h4>🏅 我的勋章</h4>
          <div v-for="b in r.badges" :key="b.code" class="badge">
            <span style="font-size:26px">{{ b.icon }}</span>
            <div><b>{{ b.name }}</b><p>{{ b.description }}</p></div>
          </div>
          <div v-if="!r.badges.length" style="color:#999">还没有勋章,继续积累(连续打卡/背词/模考都会触发)</div>
          <p style="color:#666;font-size:13px;margin-top:10px">近30天打卡 {{ r.checkinDays }} 天 · 单词 {{ r.words.learned }}/{{ r.words.total }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page { max-width:860px; margin:0 auto; padding:16px; }
.head { display:flex; justify-content:space-between; margin-bottom:12px; }
.card { border-radius:14px; margin-bottom:14px; }
.acc { display:flex; align-items:center; gap:10px; margin:10px 0; }
.lab { width:82px; font-size:14px; }
.cnt { width:52px; color:#888; font-size:13px; text-align:right; }
.advice { margin:6px 0; color:#444; }
.wrong { display:flex; gap:8px; align-items:flex-start; margin:8px 0; font-size:13px; }
.badge { display:flex; gap:10px; align-items:center; margin:8px 0; }
.badge p { margin:0; font-size:12px; color:#888; }
</style>
