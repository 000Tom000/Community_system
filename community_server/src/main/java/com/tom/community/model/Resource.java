package com.tom.community.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资源实体
 */
@Data
public class Resource {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String fileName;      // 原始文件名
    private String filePath;      // 服务器存储路径
    private Long fileSize;        // 字节
    private String fileType;      // pdf/ppt/doc/zip/other
    private String category;      // courseware/notes/booklist/exam/other
    private Integer downloadCount;
    private Double ratingAvg;     // 平均评分
    private Integer ratingCount;  // 评分人数
    private Integer status;       // 0正常 1隐藏 2删除

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ---- 非表字段 ---- */
    private String authorNickname;
    private String authorAvatar;

    /** 给前端用的友好大小 */
    public String getFileSizeStr() {
        if (fileSize == null) return "未知";
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}
