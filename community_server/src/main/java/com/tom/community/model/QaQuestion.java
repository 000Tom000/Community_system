package com.tom.community.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问答中心 — 问题实体
 */
@Data
public class QaQuestion {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String category;      // course-selection/internship/kaoyan/competition/other
    private Integer viewCount;
    private Integer answerCount;
    private Boolean isSolved;     // 是否已解决（有采纳回答）
    private Integer status;       // 0正常 1隐藏 2删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<QaAnswer> answers;  // 详情接口才填充
}
