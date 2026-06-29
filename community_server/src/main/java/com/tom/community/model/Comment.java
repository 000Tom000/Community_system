package com.tom.community.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体 — 支持嵌套回复（两级结构）
 */
@Data
public class Comment {

    private Long id;
    private Long postId;        // 所属帖子
    private Long userId;        // 评论者 ID
    private Long parentId;      // 父评论 ID（null=顶级）
    private String content;
    private Integer likeCount;
    private Integer status;     // 0-正常 1-隐藏 2-删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段：JOIN user 填充 ---- */
    private String authorNickname;
    private String authorAvatar;

    /* ---- 非表字段：子回复列表（只有顶级评论有）---- */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Comment> replies;

    /* ---- 非表字段：当前用户是否已点赞 ---- */
    private Boolean liked;
}
