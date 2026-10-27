package com.cet6.controller;

import com.cet6.common.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 作文/翻译 AI 批改:调用千问 MaaS(Anthropic 兼容接口)。
 * Key 配置:backend/config/maas.properties(MAAS_API_KEY / MAAS_BASE_URL / MAAS_MODEL),
 * 与简记 App 同一套服务。未配置时优雅降级。
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    public AiController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostMapping("/review")
    public Result<Map<String, Object>> review(@RequestBody Map<String, String> body, HttpSession session) {
        long uid = (Long) session.getAttribute("uid");
        String content = body.getOrDefault("content", "").trim();
        String type = body.getOrDefault("type", "ESSAY");
        if (content.length() < 30) return Result.fail("先写够 30 个字符再交批改");

        Properties cfg = loadConfig();
        if (cfg == null) {
            return Result.fail("AI 未配置:把手机上的 maas.properties 内容写入 backend/config/maas.properties 后重启");
        }

        String system = "你是大学英语六级阅卷专家。用中文严格批改。只输出 JSON 对象,"
                + "字段:score(数字,ESSAY 为 15 分制,TRANSLATION 为 106.5 分制),"
                + "grade(等级 优/良/及格/不及格), summary(两句话总评),"
                + "corrections(数组,每项 {original, revised, reason}, 挑最多5个关键错误),"
                + "improvements(数组,2条提分建议)。不要输出多余文字。";
        String user = (type.equals("ESSAY") ? "【作文题】" : "【翻译题】")
                + body.getOrDefault("prompt", "") + "\n【考生答案】\n" + content;

        try {
            Map<String, Object> req = new HashMap<>();
            req.put("model", cfg.getProperty("MAAS_MODEL"));
            req.put("max_tokens", 2000);
            req.put("system", system);
            req.put("messages", List.of(Map.of("role", "user", "content", user)));

            HttpRequest httpReq = HttpRequest.newBuilder()
                    .uri(URI.create(cfg.getProperty("MAAS_BASE_URL",
                            "https://maas.qianwenaiapi.com/apps/anthropic") + "/v1/messages"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", cfg.getProperty("MAAS_API_KEY"))
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(ascii(json.writeValueAsString(req))))
                    .build();
            HttpResponse<String> resp = HttpClient.newHttpClient()
                    .send(httpReq, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return Result.fail("AI 服务返回 " + resp.statusCode());

            // content 数组可能先给 thinking 块再给 text 块,遍历取真正的 text
            StringBuilder textBuilder = new StringBuilder();
            for (JsonNode item : json.readTree(resp.body()).path("content")) {
                if ("text".equals(item.path("type").asText())) {
                    if (textBuilder.length() > 0) textBuilder.append('\n');
                    textBuilder.append(item.path("text").asText());
                }
            }
            String raw = textBuilder.toString();
            int a = raw.indexOf('{'), b = raw.lastIndexOf('}');
            if (a < 0 || b <= a) return Result.fail("AI 返回格式异常,请重试");
            JsonNode parsed = json.readTree(raw.substring(a, b + 1));

            Map<String, Object> out = new HashMap<>();
            out.put("score", parsed.path("score"));
            out.put("grade", parsed.path("grade").asText("—"));
            out.put("summary", parsed.path("summary").asText(""));
            out.put("corrections", parsed.path("corrections"));
            out.put("improvements", parsed.path("improvements"));

            jdbc.update("INSERT INTO t_ai_review(user_id, topic_id, content, score, feedback, detail) VALUES(?,?,?,?,?,?)",
                    uid, body.get("topicId"), content,
                    parsed.path("score").asDouble(), parsed.path("summary").asText(),
                    raw.substring(a, b + 1));

            return Result.ok(out);
        } catch (Exception e) {
            return Result.fail("AI 批改失败:" + e.getClass().getSimpleName());
        }
    }

    private Properties loadConfig() {
        File f = new File("config/maas.properties"); // 相对 backend 工作目录
        if (!f.exists()) return null;
        Properties p = new Properties();
        try (var in = Files.newInputStream(f.toPath())) {
            p.load(in);
            return p.getProperty("MAAS_API_KEY", "").isBlank() ? null : p;
        } catch (Exception e) {
            return null;
        }
    }

    /** 非 ASCII 转 backslash-u 四位十六进制,防网关按 Latin-1 解码丢字(简记同款方案)。
     *  注意:Java 词法预处理器连注释里的反斜杠+u 序列都会当 Unicode 转义解析,所以要拆开拼。 */
    private String ascii(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c > 127) {
                sb.append('\\').append('u').append(String.format("%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
