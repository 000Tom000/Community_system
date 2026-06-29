package com.tom.community.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校园公告实体
 */
@Data
public class Notice {

    private Long id;
    private Long userId;
    private String category;      // academic/club/admin/exam/general
    private String title;
    private String content;
    private String level;         // normal/important/urgent
    private Boolean isPinned;
    private Integer viewCount;
    private Integer status;       // 0正常 1隐藏 2删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;

    public String getLevelText() {
        if (level == null) return "普通";
        return switch (level) {
            case "urgent" -> "紧急";
            case "important" -> "重要";
            default -> "普通";
        };
    }

    public String getCategoryText() {
        if (category == null) return "综合";
        return switch (category) {
            case "academic" -> "学术讲座";
            case "club" -> "社团活动";
            case "admin" -> "学校通知";
            case "exam" -> "考试教务";
            default -> "综合";
        };
    }
}
