package com.tom.community.mapper;

import com.tom.community.model.QaQuestion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 问答中心 — 问题数据访问层
 */
@Repository
public class QaQuestionMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<QaQuestion> rowMapper = (rs, rowNum) -> {
        QaQuestion q = new QaQuestion();
        q.setId(rs.getLong("id"));
        q.setUserId(rs.getLong("user_id"));
        q.setTitle(rs.getString("title"));
        q.setContent(rs.getString("content"));
        q.setCategory(rs.getString("category"));
        q.setViewCount(rs.getInt("view_count"));
        q.setAnswerCount(rs.getInt("answer_count"));
        q.setIsSolved(rs.getInt("is_solved") != 0);
        q.setStatus(rs.getInt("status"));
        try { q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { q.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { q.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { q.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return q;
    };

    private static final String JOIN_SELECT =
            "SELECT q.id, q.user_id, q.title, q.content, q.category, " +
            "q.view_count, q.answer_count, q.is_solved, q.status, q.created_at, q.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM qa_question q LEFT JOIN user u ON q.user_id = u.id ";

    public QaQuestionMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ==================== 增 ==================== */

    public QaQuestion insert(QaQuestion q) {
        String sql = """
                INSERT INTO qa_question (user_id, title, content, category, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, q.getUserId());
            ps.setString(2, q.getTitle());
            ps.setString(3, q.getContent());
            ps.setString(4, q.getCategory());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            q.setId(key.longValue());
            findById(q.getId()).ifPresent(row -> {
                q.setCreatedAt(row.getCreatedAt());
                q.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return q;
    }

    /* ==================== 查 ==================== */

    public Optional<QaQuestion> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE q.id = ? AND q.status != 2";
        List<QaQuestion> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** 分页列表（分类+关键词+排序） */
    public List<QaQuestion> findList(String category, String keyword, String sort,
                                      int offset, int size) {
        StringBuilder sql = new StringBuilder(JOIN_SELECT + "WHERE q.status = 0 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append("AND q.category = ? ");
            params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND q.title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        sql.append("ORDER BY ");
        if ("hot".equals(sort)) {
            sql.append("q.answer_count DESC, q.view_count DESC ");
        } else if ("unsolved".equals(sort)) {
            sql.append("CASE WHEN q.is_solved = 0 THEN 0 ELSE 1 END, q.created_at DESC ");
        } else {
            sql.append("q.created_at DESC ");
        }

        sql.append("LIMIT ?, ?");
        params.add(offset);
        params.add(size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Long countList(String category, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM qa_question WHERE status = 0 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c == null ? 0L : c;
    }

    /* ==================== 改 ==================== */

    public void update(QaQuestion q) {
        jdbc.update("""
                UPDATE qa_question SET title=?, content=?, category=?, updated_at=NOW()
                WHERE id=? AND status != 2
                """, q.getTitle(), q.getContent(), q.getCategory(), q.getId());
    }

    public void softDelete(Long id) {
        jdbc.update("UPDATE qa_question SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void incrementViewCount(Long id) {
        jdbc.update("UPDATE qa_question SET view_count = view_count + 1 WHERE id=?", id);
    }

    public void updateSolved(Long id, boolean solved) {
        jdbc.update("UPDATE qa_question SET is_solved=?, updated_at=NOW() WHERE id=? AND status != 2",
                solved ? 1 : 0, id);
    }

    public void updateAnswerCount(Long id, int delta) {
        jdbc.update("UPDATE qa_question SET answer_count = answer_count + ? WHERE id=?", delta, id);
    }
}
