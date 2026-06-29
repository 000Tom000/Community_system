package com.tom.community.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 点赞数据访问层
 */
@Repository
public class PostLikeMapper {

    private final JdbcTemplate jdbc;

    public PostLikeMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 是否已点赞 */
    public boolean exists(Long postId, Long userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM post_like WHERE post_id=? AND user_id=?",
                Integer.class, postId, userId);
        return count != null && count > 0;
    }

    /** 点赞 */
    public void insert(Long postId, Long userId) {
        jdbc.update("INSERT IGNORE INTO post_like (post_id, user_id) VALUES (?, ?)",
                postId, userId);
    }

    /** 取消点赞 */
    public void delete(Long postId, Long userId) {
        jdbc.update("DELETE FROM post_like WHERE post_id=? AND user_id=?",
                postId, userId);
    }

    /** 帖子的点赞数 */
    public Long countByPostId(Long postId) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM post_like WHERE post_id=?", Long.class, postId);
        return c == null ? 0L : c;
    }
}
