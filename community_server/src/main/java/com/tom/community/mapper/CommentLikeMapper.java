package com.tom.community.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommentLikeMapper {

    private final JdbcTemplate jdbc;

    public CommentLikeMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean exists(Long commentId, Long userId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM comment_like WHERE comment_id=? AND user_id=?",
                Integer.class, commentId, userId);
        return c != null && c > 0;
    }

    public void insert(Long commentId, Long userId) {
        jdbc.update("INSERT IGNORE INTO comment_like (comment_id, user_id) VALUES (?, ?)",
                commentId, userId);
    }

    public void delete(Long commentId, Long userId) {
        jdbc.update("DELETE FROM comment_like WHERE comment_id=? AND user_id=?",
                commentId, userId);
    }
}
