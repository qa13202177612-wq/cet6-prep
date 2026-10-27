-- 词库清洗:去重 + 修错误释义
USE cet6;

-- 1. 重复单词保留最小 id,其余删除
DELETE w FROM t_word w
JOIN (SELECT word, MIN(id) AS keep_id FROM t_word GROUP BY word HAVING COUNT(*) > 1) d
  ON w.word = d.word AND w.id > d.keep_id;

-- 2. 剔除错拼/无效条目
DELETE FROM t_word WHERE word IN ('archieve','disclipine','contraverse','nex');

-- 3. 修正脏释义
UPDATE t_word SET meaning='n.妥协 v.妥协'          WHERE meaning LIKE '%v. compromise%';
UPDATE t_word SET meaning='n.容器;船'              WHERE meaning LIKE '%n. vessel%';
UPDATE t_word SET meaning='adj.缺乏的'             WHERE meaning LIKE '%deficient 缺乏的%';
UPDATE t_word SET meaning='adj.蓄意的 v.慎重考虑'  WHERE meaning LIKE '%deliberation 思考%';
UPDATE t_word SET meaning='n.主食;订书机'          WHERE word='staple' AND meaning LIKE '%主食%';

-- 4. 重排频率名次(按插入顺序:种子1-8 → 批次1 → 批次2进阶词)
SET @rk := 0;
UPDATE t_word SET freq_rank = (@rk := @rk + 1), stage_tag = '词汇基础' ORDER BY id;

SELECT COUNT(*) AS final_word_count, MIN(freq_rank) AS min_r, MAX(freq_rank) AS max_r FROM t_word;
