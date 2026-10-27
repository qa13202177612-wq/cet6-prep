package com.cet6.controller;

import com.cet6.common.Result;
import com.cet6.entity.User;
import com.cet6.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> body, HttpSession session) {
        String err = userService.register(
                body.get("username"), body.get("password"), body.get("nickname"));
        if (err != null) return Result.fail(err);
        // 注册成功直接登录
        User u = userService.login(body.get("username"), body.get("password"));
        session.setAttribute("uid", u.getId());
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body, HttpSession session) {
        User u = userService.login(body.get("username"), body.get("password"));
        if (u == null) return Result.fail("用户名或密码错误");
        session.setAttribute("uid", u.getId());
        return Result.ok(u.safe());
    }

    @GetMapping("/me")
    public Result<User> me(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return Result.fail("未登录");
        User u = userService.login(String.valueOf(uid), null); // 不走密码,直接查
        return Result.ok(u == null ? null : u.safe());
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.ok();
    }
}
