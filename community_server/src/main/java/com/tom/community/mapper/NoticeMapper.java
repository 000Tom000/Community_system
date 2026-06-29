package com.tom.community.mapper;

import com.tom.community.model.Notice;
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

@Repository
public class NoticeMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<Notice> rowMapper = (rs, rowNum) -> {
        Notice n = new Notice();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setCategory(rs.getString("category"));
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        n.setLevel(rs.getString("level"));
        n.setIsPinned(rs.getInt("is_pinned") != 0);
        n.setViewCount(rs.getInt("view_count"));
        n.setStatus(rs.getInt("status"));
        try { n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { n.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { n.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { n.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return n;
    };

    private static final String JOIN_SELECT =
            "SELECT n.id, n.user_id, n.category, n.title, n.content, n.level, " +
            "n.is_pinned, n.view_count, n.status, n.created_at, n.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM notice n LEFT JOIN user u ON n.user_id = u.id ";

    public NoticeMapper(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Notice insert(Notice n) {
        String sql = """
                INSERT INTO notice (user_id, category, title, content, level, is_pinned, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, n.getUserId());
            ps.setString(2, n.getCategory());
            ps.setString(3, n.getTitle());
            ps.setString(4, n.getContent());
            ps.setString(5, n.getLevel());
            ps.setInt(6, Boolean.TRUE.equals(n.getIsPinned()) ? 1 : 0);
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            n.setId(key.longValue());
            findById(n.getId()).ifPresent(row -> {
                n.setCreatedAt(row.getCreatedAt());
                n.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return n;
    }

    public Optional<Notice> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE n.id = ? AND n.status != 2";
        List<Notice> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Notice> findList(String category, String keyword, String sort, int offset, int size) {
        StringBuilder sql = new StringBuilder(JOIN_SELECT + "WHERE n.status = 0 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append("AND n.category = ? ");
            params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND n.title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        sql.append("ORDER BY n.is_pinned DESC, ");
        if ("hot".equals(sort)) {
            sql.append("n.view_count DESC, n.created_at DESC ");
        } else {
            sql.append("n.created_at DESC ");
        }
        sql.append("LIMIT ?, ?");
        params.add(offset);
        params.add(size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Long countList(String category, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM notice WHERE status = 0 ");
        List<Object> params = new ArrayList<>();
        if (category != null && !category.isEmpty()) {
            sql.append("AND category = ? "); params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND title LIKE ? "); params.add("%" + keyword + "%");
        }
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c == null ? 0L : c;
    }

    public void update(Notice n) {
        jdbc.update("""
                UPDATE notice SET title=?, content=?, category=?, level=?, updated_at=NOW()
                WHERE id=? AND status != 2
                """, n.getTitle(), n.getContent(), n.getCategory(), n.getLevel(), n.getId());
    }

    public void softDelete(Long id) {
        jdbc.update("UPDATE notice SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void incrementViewCount(Long id) {
        jdbc.update("UPDATE notice SET view_count = view_count + 1 WHERE id=?", id);
    }

    public void updatePinned(Long id, boolean pinned) {
        jdbc.update("UPDATE notice SET is_pinned=?, updated_at=NOW() WHERE id=? AND status != 2",
                pinned ? 1 : 0, id);
    }
}
