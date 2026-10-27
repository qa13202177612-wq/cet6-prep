package com.cet6.controller;

import com.cet6.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 专项突破:长难句 / 听力精听素材 / 写作题目 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final JdbcTemplate jdbc;

    public SkillController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/sentences")
    public Result<List<Map<String, Object>>> sentences(@RequestParam(defaultValue = "5") int size) {
        return Result.ok(jdbc.queryForList(
                "SELECT id, content, grammar_point AS grammarPoint, structure_tip AS structureTip, " +
                "translation, source_year AS sourceYear FROM t_sentence ORDER BY RAND() LIMIT ?", size));
    }

    @GetMapping("/listening")
    public Result<List<Map<String, Object>>> listening() {
        // 一次给全部套题:标题 + 逐句(前端用浏览器 TTS 朗读精听)
        List<Map<String, Object>> sets = jdbc.queryForList(
                "SELECT id, title, section FROM t_listening ORDER BY id");
        for (Map<String, Object> set : sets) {
            set.put("lines", jdbc.queryForList(
                    "SELECT seq, en_text AS enText, cn_text AS cnText FROM t_listening_line " +
                    "WHERE listening_id=? ORDER BY seq", set.get("id")));
        }
        return Result.ok(sets);
    }

    @GetMapping("/writing/topics")
    public Result<List<Map<String, Object>>> writingTopics() {
        return Result.ok(jdbc.queryForList(
                "SELECT id, type, title, prompt, source_year AS sourceYear FROM t_writing_topic ORDER BY id"));
    }
}
