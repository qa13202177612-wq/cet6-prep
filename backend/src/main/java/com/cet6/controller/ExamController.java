package com.cet6.controller;

import com.cet6.common.Result;
import com.cet6.service.GraderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 真题模考(迷你卷):抽 12 题限时 6 分钟,交卷自动评分并存档 */
@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final JdbcTemplate jdbc;
    private final GraderService grader;

    public ExamController(JdbcTemplate jdbc, GraderService grader) {
        this.jdbc = jdbc;
        this.grader = grader;
    }

    @GetMapping("/start")
    public Result<Map<String, Object>> start(HttpSession session) {
        List<Map<String, Object>> papers = jdbc.queryForList(
                "SELECT id, name, duration_min AS durationMin FROM t_exam ORDER BY id");
        Map<String, Object> exam = papers.isEmpty() ? null : papers.get(0);
        List<Map<String, Object>> questions = grader.pickQuestions("GRAMMAR,READING", 12);
        return Result.ok(Map.of(
                "exam", exam == null ? Map.of("id", 0, "name", "迷你模考卷", "durationMin", 6) : exam,
                "questions", questions));
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpSession session) {
        long uid = (Long) session.getAttribute("uid");
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.get("answers");
        long durationSec = Long.parseLong(String.valueOf(body.getOrDefault("durationSec", 0)));

        Map<String, Object> graded = grader.gradeAndRecord(uid, answers);

        // 换算成六级 710 分制:正确率 × 710(自用工具先粗算)
        int correct = (Integer) graded.get("correct");
        int total = (Integer) graded.get("total");
        int score = total == 0 ? 0 : (int) Math.round(correct * 710.0 / total);

        jdbc.update("INSERT INTO t_exam_record(user_id, exam_id, score_json, total_score, duration_sec) VALUES(?,?,?,?,?)",
                uid, 0, "{\"correct\":" + correct + ",\"total\":" + total + "}", score, durationSec);

        graded.put("score710", score);
        return Result.ok(graded);
    }
}
