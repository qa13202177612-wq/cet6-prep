package com.cet6.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 判分共享服务:抽题、判分、写答题流水、错题本维护 */
@Service
public class GraderService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    public GraderService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** 抽 N 道题(不含答案)用于前端展示。types 例:'GRAMMAR,READING' */
    public List<Map<String, Object>> pickQuestions(String types, int count) {
        List<String> list = new ArrayList<>();
        for (String t : types.split(",")) list.add("'" + t.trim() + "'");
        String sql = "SELECT id, biz_type AS bizType, ref_id AS refId, seq, stem, options " +
                     "FROM t_question WHERE biz_type IN (" + String.join(",", list) + ") " +
                     "ORDER BY RAND() LIMIT " + count;
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        for (Map<String, Object> row : rows) {
            row.put("options", parseOptions((String) row.remove("options")));
        }
        return rows;
    }

    public Map<String, Object> questionDetail(long qid) {
        Map<String, Object> q = jdbc.queryForMap(
                "SELECT id, biz_type AS bizType, stem, options, answer, analysis FROM t_question WHERE id=?", qid);
        q.put("options", parseOptions((String) q.remove("options")));
        return q;
    }

    /**
     * 判分并落库。answers: {questionId: "A"}
     * 返回 {correct, total, results:[{id, right, userAnswer, answer, analysis}]}
     */
    public Map<String, Object> gradeAndRecord(long uid, Map<String, String> answers) {
        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<String, String> e : answers.entrySet()) {
            long qid = Long.parseLong(e.getKey());
            Map<String, Object> q = jdbc.queryForMap(
                    "SELECT biz_type, answer, analysis FROM t_question WHERE id=?", qid);
            String std = (String) q.get("answer");
            String user = e.getValue() == null ? "" : e.getValue().trim().toUpperCase();
            boolean right = std.equalsIgnoreCase(user);
            if (right) correct++;

            jdbc.update("INSERT INTO t_answer_record(user_id, biz_type, question_id, user_answer, is_right) VALUES(?,?,?,?,?)",
                    uid, q.get("biz_type"), qid, user, right ? 1 : 0);

            if (right) {
                // 连对销账:模考/练习做对后,错题本记一次连对数,满 3 清出
                jdbc.update("UPDATE t_wrong_book SET times_wrong = GREATEST(times_wrong - 1, 0), " +
                        "is_cleared = IF(times_wrong <= 1, 1, 0) WHERE user_id=? AND question_id=?", uid, qid);
            } else {
                jdbc.update("INSERT INTO t_wrong_book(user_id, biz_type, question_id) VALUES(?,?,?) " +
                        "ON DUPLICATE KEY UPDATE times_wrong = times_wrong + 1, is_cleared = 0",
                        uid, q.get("biz_type"), qid);
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", qid);
            item.put("right", right);
            item.put("userAnswer", user);
            item.put("answer", std);
            item.put("analysis", q.get("analysis"));
            results.add(item);
        }
        Map<String, Object> out = new HashMap<>();
        out.put("correct", correct);
        out.put("total", answers.size());
        out.put("results", results);
        return out;
    }

    public List<String> parseOptions(String raw) {
        try {
            return json.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return List.of(raw == null ? "" : raw);
        }
    }
}
