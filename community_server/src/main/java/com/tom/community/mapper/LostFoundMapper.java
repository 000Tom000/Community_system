package com.tom.community.mapper;

import com.tom.community.model.LostFound;
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
 * 失物招领 — 数据访问层
 */
@Repository
public class LostFoundMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<LostFound> rowMapper = (rs, rowNum) -> {
        LostFound lf = new LostFound();
        lf.setId(rs.getLong("id"));
        lf.setUserId(rs.getLong("user_id"));
        lf.setType(rs.getString("type"));
        lf.setCategory(rs.getString("category"));
        lf.setTitle(rs.getString("title"));
        lf.setDescription(rs.getString("description"));
        lf.setLocation(rs.getString("location"));
        lf.setContact(rs.getString("contact"));
        lf.setImageUrl(rs.getString("image_url"));
        lf.setStatus(rs.getInt("status"));
        lf.setViewCount(rs.getInt("view_count"));
        try { lf.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { lf.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { lf.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { lf.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return lf;
    };

    private static final String JOIN_SELECT =
            "SELECT lf.id, lf.user_id, lf.type, lf.category, lf.title, lf.description, " +
            "lf.location, lf.contact, lf.image_url, lf.status, lf.view_count, " +
            "lf.created_at, lf.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM lost_found lf LEFT JOIN user u ON lf.user_id = u.id ";

    public LostFoundMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ==================== 增 ==================== */

    public LostFound insert(LostFound lf) {
        String sql = """
                INSERT INTO lost_found (user_id, type, category, title, description, location, contact, image_url, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, lf.getUserId());
            ps.setString(2, lf.getType());
            ps.setString(3, lf.getCategory());
            ps.setString(4, lf.getTitle());
            ps.setString(5, lf.getDescription());
            ps.setString(6, lf.getLocation());
            ps.setString(7, lf.getContact());
            ps.setString(8, lf.getImageUrl());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            lf.setId(key.longValue());
            findById(lf.getId()).ifPresent(row -> {
                lf.setCreatedAt(row.getCreatedAt());
                lf.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return lf;
    }

    /* ==================== 查 ==================== */

    public Optional<LostFound> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE lf.id = ? AND lf.status != 2";
        List<LostFound> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** 分页列表（类型+分类+关键词+排序） */
    public List<LostFound> findList(String type, String category, String keyword,
                                     String sort, int offset, int size) {
        StringBuilder sql = new StringBuilder(JOIN_SELECT + "WHERE lf.status = 0 ");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isEmpty()) {
            sql.append("AND lf.type = ? ");
            params.add(type);
        }
        if (category != null && !category.isEmpty()) {
            sql.append("AND lf.category = ? ");
            params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND lf.title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        sql.append("ORDER BY ");
        if ("hot".equals(sort)) {
            sql.append("lf.view_count DESC, lf.created_at DESC ");
        } else {
            sql.append("lf.created_at DESC ");
        }

        sql.append("LIMIT ?, ?");
        params.add(offset);
        params.add(size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Long countList(String type, String category, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM lost_found WHERE status = 0 ");
        List<Object> params = new ArrayList<>();

        if (type != null && !type.isEmpty()) {
            sql.append("AND type = ? ");
            params.add(type);
        }
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

    public void update(LostFound lf) {
        jdbc.update("""
                UPDATE lost_found SET title=?, description=?, category=?, location=?, contact=?, image_url=?, updated_at=NOW()
                WHERE id=? AND status != 2
                """, lf.getTitle(), lf.getDescription(), lf.getCategory(),
                lf.getLocation(), lf.getContact(), lf.getImageUrl(), lf.getId());
    }

    public void softDelete(Long id) {
        jdbc.update("UPDATE lost_found SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void incrementViewCount(Long id) {
        jdbc.update("UPDATE lost_found SET view_count = view_count + 1 WHERE id=?", id);
    }

    public void updateStatus(Long id, int status) {
        jdbc.update("UPDATE lost_found SET status=?, updated_at=NOW() WHERE id=? AND status != 2",
                status, id);
    }
}
