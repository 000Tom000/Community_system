package com.tom.community.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 失物招领实体
 */
@Data
public class LostFound {

    private Long id;
    private Long userId;
    private String type;          // lost=寻物, found=招领
    private String category;      // electronics/documents/clothing/books/keys/other
    private String title;
    private String description;
    private String location;
    private String contact;
    private String imageUrl;
    private Integer status;       // 0=进行中, 1=已关闭, 2=删除
    private Integer viewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;

    /** 状态文本 */
    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "进行中";
            case 1 -> "已关闭";
            default -> "未知";
        };
    }

    /** 类型文本 */
    public String getTypeText() {
        if ("lost".equals(type)) return "寻物";
        if ("found".equals(type)) return "招领";
        return "未知";
    }
}
