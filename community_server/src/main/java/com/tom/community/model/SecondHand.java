package com.tom.community.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 二手市场实体
 */
@Data
public class SecondHand {

    private Long id;
    private Long userId;
    private String category;      // electronics/books/clothing/sports/daily/other
    private String title;
    private String description;
    private Double price;
    private Double originalPrice;
    private String condition;     // new/like-new/slight/normal
    private Boolean isNegotiable;
    private String location;
    private String contact;
    private String imageUrl;
    private Integer status;       // 0=在售, 1=已售, 2=删除
    private Integer viewCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;

    public String getConditionText() {
        if (condition == null) return "未知";
        return switch (condition) {
            case "new" -> "全新";
            case "like-new" -> "几乎全新";
            case "slight" -> "轻微使用";
            case "normal" -> "正常使用";
            default -> "未知";
        };
    }

    public String getStatusText() {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "在售";
            case 1 -> "已售";
            default -> "未知";
        };
    }
}
