package com.tom.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 — 后续帖子/评论/点赞/收藏/关注/通知 均通过 userId 关联此表
 */
@Data
public class User {

    private Long id;
    private String username;

    @JsonIgnore  // 密码永远不输出到前端
    private String password;

    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;       // 0-未知  1-男  2-女
    private String bio;           // 个人简介
    private String school;        // 学校
    private String major;         // 专业
    private String role;          // user | admin
    private Integer status;       // 0-正常  1-禁言  2-封禁

    private String token;         // 登录令牌（注册/登录时返回，公开查询时清掉）

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
