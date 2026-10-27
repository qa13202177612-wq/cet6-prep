package com.cet6.controller;

import com.cet6.common.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学情报告:各题型正确率、错题本、连续打卡、勋章、个性化推荐。
 * 顺带在这里做「打卡」——每天调一次即签到。
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final JdbcTemplate jdbc;

    public ReportController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(HttpSession session) {
        long uid = (Long) session.getAttribute("uid");

        // 1. 分题型正确率
        List<Map<String, Object>> accuracy = jdbc.queryForList(
                "SELECT biz_type AS bizType, COUNT(*) AS total, SUM(is_right) AS rightCount " +
                "FROM t_answer_record WHERE user_id=? GROUP BY biz_type", uid);
        Map<String, String> labels = Map.of(
                "GRAMMAR", "语法/长难句", "READING", "阅读理解", "LISTENING", "听力");
        String weakType = null;
        double weakRate = 1.1;
        for (Map<String, Object> a : accuracy) {
            long total = ((Number) a.get("total")).longValue();
            long right = ((Number) a.get("rightCount")).longValue();
            double rate = total == 0 ? 1 : 1.0 * right / total;
            a.put("rate", Math.round(rate * 100));
            a.put("label", labels.getOrDefault((String) a.get("bizType"), (String) a.get("bizType")));
            if (total >= 3 && rate < weakRate) { weakRate = rate; weakType = (String) a.get("bizType"); }
        }

        // 2. 错题本
        List<Map<String, Object>> wrong = jdbc.queryForList(
                "SELECT w.id, w.biz_type AS bizType, w.times_wrong AS timesWrong, q.stem " +
                "FROM t_wrong_book w JOIN t_question q ON q.id = w.question_id " +
                "WHERE w.user_id=? AND w.is_cleared=0 ORDER BY w.times_wrong DESC LIMIT 10", uid);
        long wrongTotal = jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_wrong_book WHERE user_id=? AND is_cleared=0", Long.class, uid);

        // 3. 词汇进度
        Map<String, Object> words = jdbc.queryForMap(
                "SELECT (SELECT COUNT(*) FROM t_word) AS total," +
                " (SELECT COUNT(*) FROM t_word_progress WHERE user_id=?) AS learned," +
                " (SELECT COUNT(*) FROM t_word_progress WHERE user_id=? AND next_review<=NOW()) AS due",
                uid, uid);

        // 4. 打卡(今日 upsert)
        jdbc.update("INSERT IGNORE INTO t_check_in(user_id, day) VALUES(?,?)", uid, LocalDate.now());
        Long streak = jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_check_in WHERE user_id=? AND day > DATE_SUB(CURDATE(), INTERVAL 30 DAY)",
                Long.class, uid);
        Long activeDays = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT day) FROM t_check_in WHERE user_id=?", Long.class, uid);

        // 5. 勋章判定
        List<Map<String, Object>> badges = awardBadges(uid, ((Number) words.get("learned")).longValue(), activeDays);

        // 6. 个性化推荐路径
        List<String> advice = new ArrayList<>();
        if (weakType != null) {
            advice.add("你的「" + labels.getOrDefault(weakType, weakType) + "」正确率仅 "
                    + Math.round(weakRate * 100) + "%,建议今天专项练 10 题");
        }
        long due = ((Number) words.get("due")).longValue();
        if (due > 0) advice.add("有 " + due + " 个单词到期该复习了,先去词汇模块清账");
        if (wrongTotal > 0) advice.add("错题本还有 " + wrongTotal + " 题未销账,连对 3 次自动清出");
        if (advice.isEmpty()) advice.add("各题型状态健康,保持每日节奏即可 🎯");

        Map<String, Object> out = new HashMap<>();
        out.put("accuracy", accuracy);
        out.put("wrongBook", wrong);
        out.put("wrongTotal", wrongTotal);
        out.put("words", words);
        out.put("streakDays", streak);
        out.put("checkinDays", activeDays);
        out.put("badges", badges);
        out.put("advice", advice);
        out.put("checkedToday", jdbc.queryForObject(
                "SELECT COUNT(*) FROM t_check_in WHERE user_id=? AND day=CURDATE()", Long.class, uid) > 0);
        return Result.ok(out);
    }

    /** 规则引擎极简版:达标就 INSERT IGNORE 发勋章,返回已得 */
    private List<Map<String, Object>> awardBadges(long uid, long learned, long activeDays) {
        tryAward(uid, "STREAK7", activeDays >= 7);
        tryAward(uid, "WORD100", learned >= 100);
        tryAward(uid, "WORD365", learned >= 365);
        long exams = jdbc.queryForObject("SELECT COUNT(*) FROM t_exam_record WHERE user_id=?", Long.class, uid);
        tryAward(uid, "EXAM_DONE", exams >= 1);
        return jdbc.queryForList(
                "SELECT b.code, b.name, b.icon, b.description, ub.got_time AS gotTime " +
                "FROM t_user_badge ub JOIN t_badge b ON b.id=ub.badge_id WHERE ub.user_id=?", uid);
    }

    private void tryAward(long uid, String code, boolean hit) {
        if (!hit) return;
        jdbc.update("INSERT IGNORE INTO t_user_badge(user_id, badge_id) " +
                "SELECT ?, id FROM t_badge WHERE code=?", uid, code);
    }
}
