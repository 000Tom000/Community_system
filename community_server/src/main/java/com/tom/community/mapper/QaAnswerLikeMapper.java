package com.tom.community.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 问答中心 — 回答点赞数据访问层
 */
@Repository
public class QaAnswerLikeMapper {

    private final JdbcTemplate jdbc;

    public QaAnswerLikeMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 是否已点赞 */
    public boolean exists(Long answerId, Long userId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM qa_answer_like WHERE answer_id=? AND user_id=?",
                Long.class, answerId, userId);
        return count != null && count > 0;
    }

    /** 点赞（重复点赞忽略） */
    public void insert(Long answerId, Long userId) {
        jdbc.update("INSERT IGNORE INTO qa_answer_like (answer_id, user_id) VALUES (?, ?)",
                answerId, userId);
    }

    /** 取消点赞 */
    public void delete(Long answerId, Long userId) {
        jdbc.update("DELETE FROM qa_answer_like WHERE answer_id=? AND user_id=?",
                answerId, userId);
    }
}
