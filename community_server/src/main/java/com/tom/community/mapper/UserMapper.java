package com.tom.community.mapper;

import com.tom.community.model.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
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
 * 用户数据访问层 — 基于 JdbcTemplate
 * <p>
 * 后续新增帖子/评论/点赞等表时，依此模式创建对应的 Mapper 即可。
 */
@Repository
public class UserMapper {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);

    private static final String SELECT_COLS =
            "id, username, password, nickname, avatar, email, phone, " +
            "gender, bio, school, major, role, status, token, " +
            "created_at AS createdAt, updated_at AS updatedAt, last_login_at AS lastLoginAt";

    public UserMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ==================== 增 ==================== */

    public User insert(User user) {
        String sql = """
                INSERT INTO user (username, password, nickname, avatar, email, phone,
                        gender, bio, school, major, role, status, token, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setString(4, user.getAvatar());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            ps.setObject(7, user.getGender());
            ps.setString(8, user.getBio());
            ps.setString(9, user.getSchool());
            ps.setString(10, user.getMajor());
            ps.setString(11, user.getRole() != null ? user.getRole() : "user");
            ps.setObject(12, user.getStatus() != null ? user.getStatus() : 0);
            ps.setString(13, user.getToken());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
            // 回读时间戳
            findById(user.getId()).ifPresent(row -> {
                user.setCreatedAt(row.getCreatedAt());
                user.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return user;
    }

    /* ==================== 查 ==================== */

    public Optional<User> findById(Long id) {
        List<User> list = jdbc.query("SELECT " + SELECT_COLS + " FROM user WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findByUsername(String username) {
        List<User> list = jdbc.query("SELECT " + SELECT_COLS + " FROM user WHERE username = ?", rowMapper, username);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<User> findByToken(String token) {
        List<User> list = jdbc.query("SELECT " + SELECT_COLS + " FROM user WHERE token = ?", rowMapper, token);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    /* ==================== 改 ==================== */

    public void update(User user) {
        jdbc.update("""
                UPDATE user
                SET nickname=?, avatar=?, email=?, phone=?, gender=?,
                    bio=?, school=?, major=?, updated_at=NOW()
                WHERE id=?
                """,
                user.getNickname(), user.getAvatar(), user.getEmail(), user.getPhone(),
                user.getGender(), user.getBio(), user.getSchool(), user.getMajor(),
                user.getId());
    }

    public void updatePassword(Long id, String newPassword) {
        jdbc.update("UPDATE user SET password=?, updated_at=NOW() WHERE id=?",
                newPassword, id);
    }

    public void updateToken(Long id, String token) {
        jdbc.update("UPDATE user SET token=?, last_login_at=NOW() WHERE id=?",
                token, id);
    }

    public void clearToken(Long id) {
        jdbc.update("UPDATE user SET token=NULL WHERE id=?", id);
    }
}
