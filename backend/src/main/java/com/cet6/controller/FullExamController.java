package com.cet6.controller;

import com.cet6.common.Result;
import com.cet6.service.GraderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * M6 整卷模考(适配版,90 分钟):
 * 客观题 = 语法8题 + 阅读4题(真实判分,进错题本)
 * 主观题 = 长难句自测10句 + 写作1篇(交卷即计分,AI批改可选)
 * 总分按 710 制折算:客观 500 + 长难句 100 + 写作 110
 */
@RestController
@RequestMapping("/api/exam/full")
public class FullExamController {

    private final JdbcTemplate jdbc;
    private final GraderService grader;

    public FullExamController(JdbcTemplate jdbc, GraderService grader) {
        this.jdbc = jdbc;
        this.grader = grader;
    }

    @GetMapping
    public Result<Map<String, Object>> paper(HttpSession session) {
        Map<String, Object> out = new HashMap<>();
        out.put("durationSec", 90 * 60);

        List<Map<String, Object>> objective = new ArrayList<>();
        objective.addAll(grader.pickQuestions("GRAMMAR", 8));
        objective.addAll(grader.pickQuestions("READING", 4));
        out.put("objective", objective);

        out.put("sentences", jdbc.queryForList(
                "SELECT id, content, grammar_point AS grammarPoint, structure_tip AS structureTip, translation " +
                "FROM t_sentence ORDER BY RAND() LIMIT 10"));

        out.put("essay", jdbc.queryForMap(
                "SELECT id, type, title, prompt FROM t_writing_topic WHERE type='ESSAY' ORDER BY RAND() LIMIT 1"));
        return Result.ok(out);
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, Object> body, HttpSession session) {
        long uid = (Long) session.getAttribute("uid");

        // 1. 客观题判分(复用 GraderService,自动写答题流水+错题本)
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.getOrDefault("answers", Map.of());
        Map<String, Object> graded = grader.gradeAndRecord(uid, answers);
        int objCorrect = (Integer) graded.get("correct");
        int objTotal = (Integer) graded.get("total");

        // 2. 长难句自测
        @SuppressWarnings("unchecked")
        List<Object> checked = (List<Object>) body.getOrDefault("sentenceChecked", List.of());
        int sentDone = checked.size();
        int sentTotal = 10;

        // 3. 写作
        String essay = String.valueOf(body.getOrDefault("essay", ""));
        boolean essayDone = essay.trim().length() >= 30;

        // 4. 710 折算
        double objScore = objTotal == 0 ? 0 : objCorrect * 500.0 / objTotal;
        double sentScore = sentDone * 10.0;                       // 100 分
        double essayScore = essayDone ? 110 : 30;                // 交了就给辛苦分,精判走 AI
        int total710 = (int) Math.round(objScore + sentScore + essayScore);

        long durationSec = Long.parseLong(String.valueOf(body.getOrDefault("durationSec", 0)));
        // MySQL JSON 列只收合法 JSON:每个数字独立成字段,别把 "a/b" 塞进数值位
        jdbc.update("INSERT INTO t_exam_record(user_id, exam_id, score_json, total_score, duration_sec) VALUES(?,?,?,?,?)",
                uid, 1,
                String.format("{\"objectiveCorrect\":%d,\"objectiveTotal\":%d,\"sentencesDone\":%d," +
                                "\"sentencesTotal\":%d,\"essayDone\":%b,\"objScore\":%.0f}",
                        objCorrect, objTotal, sentDone, sentTotal, essayDone, objScore),
                total710, durationSec);

        // 5. 分项建议
        List<String> advice = new ArrayList<>();
        double rate = objTotal == 0 ? 0 : 1.0 * objCorrect / objTotal;
        if (rate < 0.6) advice.add("客观题正确率 " + Math.round(rate * 100) + "%,错题已进错题本,建议今天清一半");
        if (sentDone < sentTotal) advice.add("长难句只过了 " + sentDone + "/" + sentTotal + " 句,去专项突破继续拆");
        if (!essayDone) advice.add("作文没交/太短,写一篇丢给 AI 批改(15分制见真章)");
        if (advice.isEmpty()) advice.add("整卷完成度很高,保持这个节奏到考前 💪");

        Map<String, Object> out = new HashMap<>(graded);
        out.put("score710", total710);
        out.put("objScore", Math.round(objScore));
        out.put("sentScore", Math.round(sentScore));
        out.put("essayScore", Math.round(essayScore));
        out.put("sentences", sentDone + "/" + sentTotal);
        out.put("essayDone", essayDone);
        out.put("advice", advice);
        return Result.ok(out);
    }
}
