package com.tom.community.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问答中心 — 回答实体
 */
@Data
public class QaAnswer {

    private Long id;
    private Long questionId;
    private Long userId;
    private String content;
    private Boolean isAccepted;   // 是否被采纳
    private Integer likeCount;
    private Integer status;       // 0正常 1隐藏 2删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;
    private Boolean liked;        // 当前用户是否已点赞
}
