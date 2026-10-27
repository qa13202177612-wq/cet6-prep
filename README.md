# 🎓 六级备考站(CET-6 Prep)

为英语六级考生打造的自学备考平台:艾宾浩斯记忆曲线背单词、长难句拆解、听力精听(TTS)、阅读自动判分、AI 作文批改、限时模考(710 分制)、学情报告与勋章激励。

## 功能地图

| 模块 | 能力 |
|---|---|
| 📖 词汇基础 | 1200 核心词,简化艾宾浩斯调度(答对 1/2/4/8/16 天间隔,答错 10 分钟回锅) |
| 🧩 长难句 | 真题风格句子,先自析再对「主干拆解+译文」 |
| 🎧 听力精听 | 句级循环,浏览器 TTS 朗读,0.75x/1x/1.25x 变速,盲听→原文→译文三步 |
| 📰 阅读训练 | 选文+题目,交卷判分,解析回显 |
| ✒️ AI 批改 | 作文/翻译提交后大模型逐句批改、评分、给提分建议(Anthropic 兼容接口,支持任意 MaaS 网关) |
| 🎯 真题模考 | 随机 12 题限时卷,自动换算 710 分制 |
| 📊 学情报告 | 分题型正确率、错题本(连对 3 次销账)、每日打卡、勋章、个性化建议 |

## 技术栈

- 后端:Spring Boot 3.3 / JDK 17 / MyBatis-Plus + JdbcTemplate / MySQL 8
- 前端:Vue 3 + Vite + Element Plus(听力朗读用浏览器原生 speechSynthesis,零音频文件)
- 鉴权:HttpSession + BCrypt 密码存储

## 本地跑起来

```bash
# 0. 前置:JDK17、Node 18+、MySQL 8(root 密码自定)
# 1. 建库(18 张表 + 1200 词 + 题库种子)
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/words/batch1.sql
mysql -uroot -p < backend/src/main/resources/db/words/batch2.sql
mysql -uroot -p --default-character-set=utf8mb4 -e "USE cet6; SET @b2:=(SELECT MIN(id) FROM t_word WHERE word='abide'); SET @rk:=0; UPDATE t_word SET freq_rank=(@rk:=@rk+1) WHERE id<=8 OR id>=@b2 ORDER BY id; UPDATE t_word SET freq_rank=(@rk:=@rk+1) WHERE id>8 AND id<@b2 ORDER BY id; DELETE FROM t_word WHERE freq_rank>1200;"
mysql -uroot -p < backend/src/main/resources/db/m2_seed.sql

# 2. 启动后端(默认端口 8080)
cd backend
DB_PASSWORD=你的密码 mvn spring-boot:run

# 3. 启动前端(默认端口 5173,已配 /api 代理)
cd frontend
npm install && npm run dev
```

浏览器打开 `http://localhost:5173`,注册即用。

### 解锁 AI 批改(可选)

创建 `backend/config/maas.properties`(该文件已在 .gitignore,不会入库):

```properties
MAAS_API_KEY=你的key
MAAS_BASE_URL=https://api.anthropic.com
MAAS_MODEL=claude-sonnet-4-5-20250929
MAAS_ENABLED=true
```

任何提供 Anthropic 兼容 `/v1/messages` 的网关均可。未配置时该功能优雅降级,其余功能不受影响。

## 项目结构

```
backend/  src/main/java/com/cet6/{controller,service,mapper,entity,common,config}
          src/main/resources/db/*.sql   全量建表与种子
frontend/ src/views/*.vue               9 个页面
```

## 说明

- 题库/例句为真题风格**仿写**,不含任何真题原文
- 本项目为个人学习用途开发,数据层为单机 MySQL,未做并发与多租户设计
- License: MIT
