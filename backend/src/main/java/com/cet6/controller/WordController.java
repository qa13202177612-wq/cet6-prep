package com.cet6.controller;

import com.cet6.common.Result;
import com.cet6.entity.Word;
import com.cet6.service.WordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    /** 今日学习队列(新词+到期复习) */
    @GetMapping("/queue")
    public Result<List<Word>> queue(@RequestParam(defaultValue = "20") int size,
                                    HttpSession session) {
        return Result.ok(wordService.todayQueue(uid(session), size));
    }

    /** 提交 认识/不认识 */
    @PostMapping("/result")
    public Result<Void> result(@RequestBody Map<String, Object> body, HttpSession session) {
        Long wordId = Long.valueOf(String.valueOf(body.get("wordId")));
        boolean right = Boolean.TRUE.equals(body.get("right"));
        wordService.submitResult(uid(session), wordId, right);
        return Result.ok();
    }

    /** 进度统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpSession session) {
        return Result.ok(wordService.stats(uid(session)));
    }

    private Long uid(HttpSession session) {
        return (Long) session.getAttribute("uid");
    }
}
