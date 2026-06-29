package com.tom.community.mapper;

import com.tom.community.model.SecondHand;
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
 * 二手市场 — 数据访问层
 */
@Repository
public class SecondHandMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<SecondHand> rowMapper = (rs, rowNum) -> {
        SecondHand sh = new SecondHand();
        sh.setId(rs.getLong("id"));
        sh.setUserId(rs.getLong("user_id"));
        sh.setCategory(rs.getString("category"));
        sh.setTitle(rs.getString("title"));
        sh.setDescription(rs.getString("description"));
        sh.setPrice(rs.getDouble("price"));
        sh.setOriginalPrice(rs.getObject("original_price") != null ? rs.getDouble("original_price") : null);
        sh.setCondition(rs.getString("condition"));
        sh.setIsNegotiable(rs.getInt("is_negotiable") != 0);
        sh.setLocation(rs.getString("location"));
        sh.setContact(rs.getString("contact"));
        sh.setImageUrl(rs.getString("image_url"));
        sh.setStatus(rs.getInt("status"));
        sh.setViewCount(rs.getInt("view_count"));
        try { sh.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { sh.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { sh.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { sh.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return sh;
    };

    private static final String JOIN_SELECT =
            "SELECT sh.id, sh.user_id, sh.category, sh.title, sh.description, " +
            "sh.price, sh.original_price, sh.`condition`, sh.is_negotiable, " +
            "sh.location, sh.contact, sh.image_url, sh.status, sh.view_count, " +
            "sh.created_at, sh.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM secondhand sh LEFT JOIN user u ON sh.user_id = u.id ";

    public SecondHandMapper(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /* ==================== 增 ==================== */

    public SecondHand insert(SecondHand sh) {
        String sql = """
                INSERT INTO secondhand (user_id, category, title, description, price, original_price, `condition`, is_negotiable, location, contact, image_url, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, sh.getUserId());
            ps.setString(2, sh.getCategory());
            ps.setString(3, sh.getTitle());
            ps.setString(4, sh.getDescription());
            ps.setDouble(5, sh.getPrice() != null ? sh.getPrice() : 0);
            if (sh.getOriginalPrice() != null) ps.setDouble(6, sh.getOriginalPrice()); else ps.setNull(6, java.sql.Types.DOUBLE);
            ps.setString(7, sh.getCondition());
            ps.setInt(8, Boolean.TRUE.equals(sh.getIsNegotiable()) ? 1 : 0);
            ps.setString(9, sh.getLocation());
            ps.setString(10, sh.getContact());
            ps.setString(11, sh.getImageUrl());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            sh.setId(key.longValue());
            findById(sh.getId()).ifPresent(row -> {
                sh.setCreatedAt(row.getCreatedAt());
                sh.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return sh;
    }

    /* ==================== 查 ==================== */

    public Optional<SecondHand> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE sh.id = ? AND sh.status != 2";
        List<SecondHand> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<SecondHand> findList(String category, String keyword, String sort,
                                      int offset, int size) {
        StringBuilder sql = new StringBuilder(JOIN_SELECT + "WHERE sh.status = 0 ");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append("AND sh.category = ? ");
            params.add(category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND sh.title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        sql.append("ORDER BY ");
        switch (sort != null ? sort : "new") {
            case "hot" -> sql.append("sh.view_count DESC, sh.created_at DESC ");
            case "price_asc" -> sql.append("sh.price ASC, sh.created_at DESC ");
            case "price_desc" -> sql.append("sh.price DESC, sh.created_at DESC ");
            default -> sql.append("sh.created_at DESC ");
        }

        sql.append("LIMIT ?, ?");
        params.add(offset);
        params.add(size);

        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Long countList(String category, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM secondhand WHERE status = 0 ");
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

    public void update(SecondHand sh) {
        jdbc.update("""
                UPDATE secondhand SET title=?, description=?, category=?, price=?, original_price=?, `condition`=?, is_negotiable=?, location=?, contact=?, image_url=?, updated_at=NOW()
                WHERE id=? AND status != 2
                """, sh.getTitle(), sh.getDescription(), sh.getCategory(),
                sh.getPrice() != null ? sh.getPrice() : 0,
                sh.getOriginalPrice(),
                sh.getCondition(),
                Boolean.TRUE.equals(sh.getIsNegotiable()) ? 1 : 0,
                sh.getLocation(), sh.getContact(), sh.getImageUrl(), sh.getId());
    }

    public void softDelete(Long id) {
        jdbc.update("UPDATE secondhand SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void incrementViewCount(Long id) {
        jdbc.update("UPDATE secondhand SET view_count = view_count + 1 WHERE id=?", id);
    }

    public void markSold(Long id) {
        jdbc.update("UPDATE secondhand SET status=1, updated_at=NOW() WHERE id=? AND status=0", id);
    }
}
