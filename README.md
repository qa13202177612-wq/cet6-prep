<div align="center">

# 🎓 六级备考站 · CET-6 Prep

**为英语六级考生打造的一站式自学备考平台**
背单词 · 长难句 · 听力精听 · 阅读 · AI 作文批改 · 限时模考 · 学情报告

[![License](https://img.shields.io/badge/license-MIT-green.svg)](#-许可证)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen.svg)](https://spring.io/)
[![Vue](https://img.shields.io/badge/Vue-3-42b883.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-blue.svg)](https://www.mysql.com/)

</div>

---

## ✨ 为什么做这个

市面上的刷题 App 要么满屏广告、要么把真题锁在会员后。我用一个周末搭了个**干净、可自部署、数据全在自己手里**的六级备考站——顺便把 Java 后端 + Vue 前端的栈串通了一遍。

## 🧭 功能一览

| 模块 | 说明 |
|---|---|
| 📖 **词汇基础** | 1200 高频核心词,**简化艾宾浩斯记忆曲线**调度:答对按 1/2/4/8/16 天间隔复习,答错 10 分钟后回锅 |
| 🧩 **长难句拆解** | 真题风格例句,先自己划分成分,再对照「句子主干 + 参考译文」 |
| 🎧 **听力精听** | 句级循环,**浏览器原生 TTS 朗读**(零音频文件),0.75x / 1x / 1.25x 变速,盲听 → 原文 → 译文三步法 |
| 📰 **阅读训练** | 选文 + 题目,交卷即时判分,逐题解析回显 |
| ✒️ **AI 作文/翻译批改** | 接入大模型,逐句批改、评分、给出提分建议(可插拔,见 [配置](#-ai-批改配置可选)) |
| 🎯 **真题模考** | 随机组卷 + 倒计时,交卷自动换算 **710 分制**成绩并存档 |
| 📊 **学情报告** | 分题型正确率、错题本(连对 3 次自动销账)、每日打卡、勋章体系、个性化学习建议 |

## 🏗️ 技术栈与架构

- **后端**:Spring Boot 3.3 · JDK 17 · MyBatis-Plus + JdbcTemplate · MySQL 8 · HttpSession + BCrypt
- **前端**:Vue 3 · Vite · Element Plus · Vue Router · Axios

```
┌──────────────┐   /api (Vite proxy)   ┌──────────────────┐   JDBC   ┌─────────┐
│  Vue3 + EP   │ ────────────────────► │  Spring Boot 8080 │ ───────► │ MySQL 8 │
│  (dev 5173)  │ ◄──────────────────── │  REST + Session   │ ◄─────── └─────────
└──────────────┘      JSON Result      └────────┬─────────┘
   浏览器 TTS 朗读     ┌───────────────────────┐ │ 可选
   speechSynthesis    │  大模型 /v1/messages  │◄┘  Anthropic 兼容网关
                      └───────────────────────┘
```

## 🚀 快速开始

**前置**:JDK 17 · Node 18+ · MySQL 8(root 密码自定)

### 1) 初始化数据库(建 18 张表 + 1200 词 + 题库种子)

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/words/batch1.sql
mysql -uroot -p < backend/src/main/resources/db/words/batch2.sql
mysql -uroot -p --default-character-set=utf8mb4 -e "USE cet6; SET @b2:=(SELECT MIN(id) FROM t_word WHERE word='abide'); SET @rk:=0; UPDATE t_word SET freq_rank=(@rk:=@rk+1) WHERE id<=8 OR id>=@b2 ORDER BY id; UPDATE t_word SET freq_rank=(@rk:=@rk+1) WHERE id>8 AND id<@b2 ORDER BY id; DELETE FROM t_word WHERE freq_rank>1200;"
mysql -uroot -p < backend/src/main/resources/db/m2_seed.sql
```

### 2) 启动后端(默认 8080)

```bash
cd backend
DB_PASSWORD=你的密码 mvn spring-boot:run
```

### 3) 启动前端(默认 5173,已配 `/api` 代理到 8080)

```bash
cd frontend
npm install && npm run dev
```

打开 <http://localhost:5173>,注册即用 🎉

## 🤖 AI 批改配置(可选)

创建 `backend/config/maas.properties`(已在 `.gitignore`,**不会入库**):

```properties
MAAS_API_KEY=你的key
MAAS_BASE_URL=https://api.anthropic.com
MAAS_MODEL=claude-sonnet-4-5-20250929
MAAS_ENABLED=true
```

任何提供 Anthropic 兼容 `/v1/messages` 的网关均可。**未配置时该功能优雅降级,其余功能不受影响。**

## 🔌 API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` · `/login` · `/logout` | 注册 / 登录 / 登出 |
| GET | `/api/words/queue` · `/stats` | 取今日背诵队列 / 学习进度 |
| POST | `/api/words/result` | 提交「认识 / 不认识」 |
| GET | `/api/skills/sentences` · `/listening` · `/writing/topics` | 长难句 / 听力素材 / 写作题 |
| GET·POST | `/api/reading/current` · `/submit` | 阅读取题 / 判分 |
| GET·POST | `/api/exam/start` · `/submit` | 组卷 / 交卷(710 制) |
| GET | `/api/report/summary` | 学情报告 + 打卡 + 勋章 + 建议 |
| POST | `/api/ai/review` | 作文/翻译 AI 批改 |

> 统一返回 `{ code, msg, data }`,`code=0` 为成功。

## 📁 项目结构

```
backend/  src/main/java/com/cet6/{controller,service,mapper,entity,common,config}
          src/main/resources/db/*.sql      # 全量建表与词库/题库种子
frontend/ src/views/*.vue                  # 9 个页面
          src/{router,api}                 # 路由与 Axios 封装
```

## 🗺️ Roadmap

- [x] M1 注册登录 + 艾宾浩斯背单词 + 首页导航
- [x] M2 长难句 / 听力精听 / 阅读判分
- [x] M3 限时模考(710 制)+ 学情报告 + 错题本 + 勋章
- [x] M4 AI 作文/翻译批改
- [ ] M5 社区留言 / 研友动态
- [ ] M6 完整套卷模考(听力音频 + 全题型)+ 多用户云端部署

## ❓ 说明与免责

- 题库、例句均为**真题风格仿写**,不含任何真题原文或音频,仅供学习。
- 当前为个人学习项目,数据层为单机 MySQL,未做多租户与高并发设计。
- 密码使用 BCrypt 加盐存储,接口经 Session 鉴权;`config/` 目录及密钥文件已在 `.gitignore` 中排除。

## 📝 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

---

<div align="center">用 ❤️ 和 Spring Boot 构建 · 如果对你有用,欢迎点个 Star ⭐</div>
