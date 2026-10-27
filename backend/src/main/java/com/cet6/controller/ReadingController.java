package com.cet6.controller;

import com.cet6.common.Result;
import com.cet6.service.GraderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 阅读训练:取当前文章+题目,提交判分 */
@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    private final JdbcTemplate jdbc;
    private final GraderService grader;

    public ReadingController(JdbcTemplate jdbc, GraderService grader) {
        this.jdbc = jdbc;
        this.grader = grader;
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> current(HttpSession session) {
        Map<String, Object> passage = jdbc.queryForMap(
                "SELECT id, passage_type AS passageType, title, content, source_year AS sourceYear " +
                "FROM t_reading_passage ORDER BY id LIMIT 1");
        List<Map<String, Object>> questions = jdbc.queryForList(
                "SELECT id, seq, stem, options FROM t_question WHERE biz_type='READING' AND ref_id=? ORDER BY seq",
                passage.get("id"));
        for (Map<String, Object> q : questions) {
            q.put("options", grader.parseOptions((String) q.remove("options")));
        }
        passage.put("questions", questions);
        return Result.ok(passage);
    }

    @PostMapping("/submit")
    public Result<Map<String, Object>> submit(@RequestBody Map<String, String> answers, HttpSession session) {
        long uid = (Long) session.getAttribute("uid");
        return Result.ok(grader.gradeAndRecord(uid, answers));
    }
}
