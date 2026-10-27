-- 六级备考平台 全量建表脚本(16 表,M1 建全表,功能按里程碑逐步开放)
CREATE DATABASE IF NOT EXISTS cet6 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cet6;

-- 1 用户
CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt密文',
  nickname VARCHAR(50),
  goal_score INT DEFAULT 425 COMMENT '目标分',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 2 核心词汇(Top1500 分批导入)
CREATE TABLE IF NOT EXISTS t_word (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  word VARCHAR(60) NOT NULL,
  phonetic VARCHAR(80),
  meaning VARCHAR(200) NOT NULL COMMENT '核心释义',
  example VARCHAR(300) COMMENT '真题风例句(仿写非原文)',
  example_cn VARCHAR(300),
  freq_rank INT COMMENT '近5年真题频率排名',
  stage_tag VARCHAR(20) DEFAULT '词汇基础',
  KEY idx_rank (freq_rank)
) COMMENT '六级高频词';

-- 3 单词记忆进度(艾宾浩斯)
CREATE TABLE IF NOT EXISTS t_word_progress (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  word_id BIGINT NOT NULL,
  level INT DEFAULT 0 COMMENT '0-5熟悉度',
  wrong_count INT DEFAULT 0,
  next_review DATETIME COMMENT '下次复习时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_word (user_id, word_id)
) COMMENT '单词记忆曲线';

-- 4 长难句
CREATE TABLE IF NOT EXISTS t_sentence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content VARCHAR(600) NOT NULL,
  grammar_point VARCHAR(100) COMMENT '考点:从句/非谓/倒装...',
  structure_tip VARCHAR(400) COMMENT '主干拆解提示',
  translation VARCHAR(600),
  source_year INT
) COMMENT '长难句库';

-- 5 听力套题元信息
CREATE TABLE IF NOT EXISTS t_listening (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100),
  section VARCHAR(20) COMMENT '讲座/长对话/短篇',
  audio_url VARCHAR(300),
  duration_sec INT
) COMMENT '听力材料';

-- 6 听力逐句(精听/跟读用)
CREATE TABLE IF NOT EXISTS t_listening_line (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  listening_id BIGINT NOT NULL,
  seq INT,
  en_text VARCHAR(400),
  cn_text VARCHAR(400),
  start_ms INT, end_ms INT,
  KEY idx_l (listening_id)
) COMMENT '听力句级字幕';

-- 7 阅读文章
CREATE TABLE IF NOT EXISTS t_reading_passage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  passage_type VARCHAR(20) COMMENT '选词填空/匹配/仔细理解',
  title VARCHAR(150),
  content TEXT,
  source_year INT
) COMMENT '阅读篇章';

-- 8 选择题题目(阅读/语法共用)
CREATE TABLE IF NOT EXISTS t_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_type VARCHAR(20) NOT NULL COMMENT 'READING/GRAMMAR/LISTENING',
  ref_id BIGINT COMMENT '所属篇章/听力id',
  seq INT,
  stem VARCHAR(500),
  options JSON COMMENT '选项数组',
  answer VARCHAR(10),
  analysis VARCHAR(500)
) COMMENT '题库';

-- 9 答题流水(学情报告数据源)
CREATE TABLE IF NOT EXISTS t_answer_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  biz_type VARCHAR(20),
  question_id BIGINT,
  user_answer VARCHAR(20),
  is_right TINYINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_time (user_id, create_time)
) COMMENT '答题记录';

-- 10 错题本
CREATE TABLE IF NOT EXISTS t_wrong_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  biz_type VARCHAR(20),
  question_id BIGINT,
  times_wrong INT DEFAULT 1,
  is_cleared TINYINT DEFAULT 0 COMMENT '连对3次自动销账',
  last_wrong_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk (user_id, biz_type, question_id)
) COMMENT '错题本';

-- 11 作文/翻译题
CREATE TABLE IF NOT EXISTS t_writing_topic (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  type VARCHAR(10) COMMENT 'ESSAY/TRANSLATION',
  title VARCHAR(200),
  prompt TEXT,
  ref_answer TEXT,
  source_year INT
) COMMENT '写作翻译题库';

-- 12 AI批改结果
CREATE TABLE IF NOT EXISTS t_ai_review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  topic_id BIGINT,
  content TEXT COMMENT '用户原文',
  score INT COMMENT '换算15分制/翻译106.5制',
  feedback TEXT COMMENT '总评',
  detail JSON COMMENT '逐句批改',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'AI批改记录';

-- 13 模考试卷
CREATE TABLE IF NOT EXISTS t_exam (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100),
  year INT,
  parts JSON COMMENT '各部分题目id配比',
  duration_min INT DEFAULT 130
) COMMENT '真题模考卷';

-- 14 模考成绩
CREATE TABLE IF NOT EXISTS t_exam_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  exam_id BIGINT,
  score_json JSON COMMENT '分项得分',
  total_score INT,
  duration_sec INT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '模考记录';

-- 15 打卡与勋章
CREATE TABLE IF NOT EXISTS t_check_in (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  day DATE NOT NULL,
  study_seconds INT DEFAULT 0,
  UNIQUE KEY uk (user_id, day)
) COMMENT '每日打卡';

CREATE TABLE IF NOT EXISTS t_badge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) UNIQUE COMMENT 'FIRST_LOGIN/STREAK7/WORD365...',
  name VARCHAR(30), icon VARCHAR(10), description VARCHAR(100)
) COMMENT '勋章定义';

CREATE TABLE IF NOT EXISTS t_user_badge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT, badge_id BIGINT,
  got_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk (user_id, badge_id)
) COMMENT '已获得勋章';

-- 16 社区(自用版=留言墙)
CREATE TABLE IF NOT EXISTS t_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  content VARCHAR(500),
  like_count INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '研友留言';

-- ===== 种子数据:先放 8 个词验证流程,1500 词 M2 分批灌入 =====
INSERT INTO t_word (word, phonetic, meaning, example, example_cn, freq_rank, stage_tag) VALUES
('abandon', '/əˈbændən/', 'v.放弃;抛弃 n.放纵', 'He had to abandon the experiment due to lack of funding.', '由于缺乏资金,他不得不放弃这项实验。', 1, '词汇基础'),
('absorb', '/əbˈzɔːrb/', 'v.吸收;使全神贯注', 'Plants absorb carbon dioxide from the air.', '植物从空气中吸收二氧化碳。', 2, '词汇基础'),
('abstract', '/ˈæbstrækt/', 'adj.抽象的 n.摘要', 'Beauty is an abstract concept.', '美是一个抽象的概念。', 3, '词汇基础'),
('academic', '/ˌækəˈdemɪk/', 'adj.学术的 n.大学教师', 'The paper was rejected by an academic journal.', '这篇论文被一份学术期刊拒稿了。', 4, '词汇基础'),
('accelerate', '/əkˈseləreɪt/', 'v.加速;促进', 'Technology continues to accelerate social change.', '技术持续加速社会变革。', 5, '词汇基础'),
('accommodate', '/əˈkɑːmədeɪt/', 'v.容纳;适应;提供住宿', 'The hotel can accommodate 500 guests.', '这家酒店能容纳500位客人。', 6, '词汇基础'),
('accumulate', '/əˈkjumjəleɪt/', 'v.积累;堆积', 'Dust had accumulated on the shelf over years.', '多年来架子上积了灰。', 7, '词汇基础'),
('accurate', '/ˈækjərət/', 'adj.精确的,准确的', 'The weather forecast is not always accurate.', '天气预报并不总是准确。', 8, '词汇基础');

-- 勋章种子
INSERT INTO t_badge (code, name, icon, description) VALUES
('FIRST_LOGIN', '初来乍到', '🌱', '完成注册'),
('STREAK7', '七日之约', '🔥', '连续打卡7天'),
('WORD100', '单词新手', '📖', '累计复习100个单词'),
('WORD365', '词汇大师', '🏆', '累计复习365个单词'),
('AI_FIRST', '以文会友', '✒️', '首次AI批改作文'),
('EXAM_DONE', '全真演练', '🎯', '完成一次完整模考');
