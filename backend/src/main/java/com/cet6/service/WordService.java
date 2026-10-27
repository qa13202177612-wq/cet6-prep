package com.cet6.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cet6.entity.Word;
import com.cet6.entity.WordProgress;
import com.cet6.mapper.WordMapper;
import com.cet6.mapper.WordProgressMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 背单词核心:简化艾宾浩斯。
 * 学习队列 = 新词(没进度的) + 到期复习词,按 freq_rank 优先。
 * 答对:level+1,间隔 2^level 天;答错:level 归半,10 分钟后重问。
 */
@Service
public class WordService {

    private final WordMapper wordMapper;
    private final WordProgressMapper progressMapper;

    public WordService(WordMapper wordMapper, WordProgressMapper progressMapper) {
        this.wordMapper = wordMapper;
        this.progressMapper = progressMapper;
    }

    /** 取今日学习队列 */
    public List<Word> todayQueue(Long userId, int size) {
        List<WordProgress> due = progressMapper.selectList(
                new LambdaQueryWrapper<WordProgress>()
                        .eq(WordProgress::getUserId, userId)
                        .le(WordProgress::getNextReview, LocalDateTime.now()));
        Set<Long> dueIds = due.stream().map(WordProgress::getWordId).collect(Collectors.toSet());

        // 到期复习词优先
        if (!dueIds.isEmpty()) {
            List<Word> reviewWords = wordMapper.selectBatchIds(dueIds);
            if (reviewWords.size() >= size) return reviewWords.subList(0, size);
            // 不足用新词补
            List<Long> exclude = List.copyOf(dueIds);
            List<Word> fresh = freshWords(userId, size - reviewWords.size(), exclude);
            reviewWords.addAll(fresh);
            return reviewWords;
        }
        return freshWords(userId, size, List.of());
    }

    private List<Word> freshWords(Long userId, int size, List<Long> exclude) {
        List<WordProgress> learned = progressMapper.selectList(
                new LambdaQueryWrapper<WordProgress>().eq(WordProgress::getUserId, userId));
        List<Long> skip = new java.util.ArrayList<>(exclude);
        learned.forEach(p -> skip.add(p.getWordId()));
        LambdaQueryWrapper<Word> q = new LambdaQueryWrapper<Word>()
                .orderByAsc(Word::getFreqRank).last("limit " + size);
        if (!skip.isEmpty()) q.notIn(Word::getId, skip);
        return wordMapper.selectList(q);
    }

    /** 提交认识/不认识 */
    public void submitResult(Long userId, Long wordId, boolean right) {
        WordProgress p = progressMapper.selectOne(new LambdaQueryWrapper<WordProgress>()
                .eq(WordProgress::getUserId, userId).eq(WordProgress::getWordId, wordId));
        LocalDateTime now = LocalDateTime.now();
        if (p == null) {
            p = new WordProgress();
            p.setUserId(userId);
            p.setWordId(wordId);
            p.setLevel(0);
            p.setWrongCount(0);
        }
        if (right) {
            int level = Math.min(5, (p.getLevel() == null ? 0 : p.getLevel()) + 1);
            p.setLevel(level);
            p.setNextReview(now.plusDays(1L << (level - 1))); // 1,2,4,8,16 天
        } else {
            p.setLevel(p.getLevel() == null ? 0 : p.getLevel() / 2);
            p.setWrongCount((p.getWrongCount() == null ? 0 : p.getWrongCount()) + 1);
            p.setNextReview(now.plusMinutes(10));
        }
        p.setUpdateTime(now);
        if (p.getId() == null) progressMapper.insert(p);
        else progressMapper.updateById(p);
    }

    /** 进度统计(首页卡片用) */
    public Map<String, Object> stats(Long userId) {
        long total = wordMapper.selectCount(null);
        long learned = progressMapper.selectCount(
                new LambdaQueryWrapper<WordProgress>().eq(WordProgress::getUserId, userId));
        long due = progressMapper.selectCount(new LambdaQueryWrapper<WordProgress>()
                .eq(WordProgress::getUserId, userId)
                .le(WordProgress::getNextReview, LocalDateTime.now()));
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("learned", learned);
        m.put("due", due);
        return m;
    }
}
