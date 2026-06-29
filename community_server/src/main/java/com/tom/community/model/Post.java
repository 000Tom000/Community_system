package com.tom.community.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体
 * <p>
 * 后续评论通过 post_id 关联，点赞/收藏通过 post_id + user_id 关联。
 */
@Data
public class Post {

    private Long id;
    private Long userId;         // 作者 ID
    private String category;     // study/life/kaoyan/teamup/tech/other
    private String title;        // 标题
    private String content;      // 正文

    private Integer viewCount;   // 阅读量
    private Integer likeCount;   // 点赞数（冗余，避免每次 COUNT）
    private Integer commentCount;// 评论数（冗余）

    private Boolean isPinned;    // 置顶
    private Boolean isSolved;    // 已解决（问答用）
    private Integer status;      // 0-正常 1-隐藏 2-删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段：JOIN user 查询填充（仅列表/详情查询时有值）---- */
    private String authorNickname;
    private String authorAvatar;
}
