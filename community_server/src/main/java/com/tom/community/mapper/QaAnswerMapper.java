package com.tom.community.mapper;

import com.tom.community.model.QaAnswer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * 问答中心 — 回答数据访问层
 */
@Repository
public class QaAnswerMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<QaAnswer> rowMapper = (rs, rowNum) -> {
        QaAnswer a = new QaAnswer();
        a.setId(rs.getLong("id"));
        a.setQuestionId(rs.getLong("question_id"));
        a.setUserId(rs.getLong("user_id"));
        a.setContent(rs.getString("content"));
        a.setIsAccepted(rs.getInt("is_accepted") != 0);
        a.setLikeCount(rs.getInt("like_count"));
        a.setStatus(rs.getInt("status"));
        try { a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { a.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { a.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { a.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return a;
    };

    private static final String JOIN_SELECT =
            "SELECT a.id, a.question_id, a.user_id, a.content, " +
            "a.is_accepted, a.like_count, a.status, a.created_at, a.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM qa_answer a LEFT JOIN user u ON a.user_id = u.id ";

    public QaAnswerMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ==================== 增 ==================== */

    public QaAnswer insert(QaAnswer a) {
        String sql = """
                INSERT INTO qa_answer (question_id, user_id, content, created_at, updated_at)
                VALUES (?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, a.getQuestionId());
            ps.setLong(2, a.getUserId());
            ps.setString(3, a.getContent());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            a.setId(key.longValue());
            findById(a.getId()).ifPresent(row -> {
                a.setCreatedAt(row.getCreatedAt());
                a.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return a;
    }

    /* ==================== 查 ==================== */

    public List<QaAnswer> findByQuestionId(Long questionId) {
        String sql = JOIN_SELECT +
                "WHERE a.question_id = ? AND a.status = 0 " +
                "ORDER BY a.is_accepted DESC, a.created_at ASC";
        return jdbc.query(sql, rowMapper, questionId);
    }

    public Optional<QaAnswer> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE a.id = ? AND a.status = 0";
        List<QaAnswer> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /* ==================== 改 ==================== */

    public void softDelete(Long id) {
        jdbc.update("UPDATE qa_answer SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void updateAccepted(Long id, boolean accepted) {
        jdbc.update("UPDATE qa_answer SET is_accepted=?, updated_at=NOW() WHERE id=? AND status=0",
                accepted ? 1 : 0, id);
    }

    /** 清除某个问题下所有回答的采纳标记 */
    public void clearAcceptedByQuestionId(Long questionId) {
        jdbc.update("UPDATE qa_answer SET is_accepted=0, updated_at=NOW() WHERE question_id=? AND status=0",
                questionId);
    }

    public void incrementLikeCount(Long id) {
        jdbc.update("UPDATE qa_answer SET like_count = like_count + 1 WHERE id=?", id);
    }

    public void decrementLikeCount(Long id) {
        jdbc.update("UPDATE qa_answer SET like_count = like_count - 1 WHERE id=?", id);
    }
}
