package com.cet6.controller;

import com.cet6.common.Result;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 社区·留言墙(自用版):发帖 / 看帖 / 点赞 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final JdbcTemplate jdbc;

    public PostController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(jdbc.queryForList(
                "SELECT p.id, p.content, p.like_count AS likeCount, " +
                "DATE_FORMAT(p.create_time, '%m-%d %H:%i') AS time, " +
                "u.nickname, u.id AS uid " +
                "FROM t_post p JOIN t_user u ON u.id = p.user_id " +
                "ORDER BY p.id DESC LIMIT 50"));
    }

    @PostMapping
    public Result<Void> publish(@RequestBody Map<String, String> body, HttpSession session) {
        long uid = (Long) session.getAttribute("uid");
        String content = body.getOrDefault("content", "").trim();
        if (content.isEmpty()) return Result.fail("说点什么吧");
        if (content.length() > 300) return Result.fail("最多 300 字");
        jdbc.update("INSERT INTO t_post(user_id, content) VALUES(?,?)", uid, content);
        return Result.ok();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable long id) {
        jdbc.update("UPDATE t_post SET like_count = like_count + 1 WHERE id = ?", id);
        return Result.ok();
    }
}
