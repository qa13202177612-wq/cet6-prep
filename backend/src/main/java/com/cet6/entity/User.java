package com.cet6.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    /** BCrypt 密文,绝不返回给前端 */
    private String password;
    private String nickname;
    private Integer goalScore;
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getGoalScore() { return goalScore; }
    public void setGoalScore(Integer goalScore) { this.goalScore = goalScore; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    /** 脱敏:返回给前端的副本 */
    public User safe() {
        User u = new User();
        u.id = id; u.username = username; u.nickname = nickname;
        u.goalScore = goalScore; u.createTime = createTime;
        return u;
    }
}
