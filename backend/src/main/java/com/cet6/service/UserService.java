package com.cet6.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cet6.entity.User;
import com.cet6.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /** 注册:用户名查重 + BCrypt 加密存储 */
    public String register(String username, String password, String nickname) {
        if (username == null || username.length() < 3 || password == null || password.length() < 6) {
            return "用户名至少3位,密码至少6位";
        }
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (exists > 0) {
            return "用户名已被占用";
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(password));
        u.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        u.setGoalScore(425);
        userMapper.insert(u);
        return null; // null = 成功
    }

    /** 登录成功返回用户,失败返回 null */
    public User login(String username, String password) {
        User u = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u != null && encoder.matches(password, u.getPassword())) {
            return u;
        }
        return null;
    }
}
