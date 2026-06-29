package com.tom.community.mapper;

import com.tom.community.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * 帖子数据访问层
 * <p>
 * 列表查询 JOIN user 表，填充作者昵称和头像。
 */
@Repository
public class PostMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<Post> rowMapper = (rs, rowNum) -> {
        Post p = new Post();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setCategory(rs.getString("category"));
        p.setTitle(rs.getString("title"));
        p.setContent(rs.getString("content"));
        p.setViewCount(rs.getInt("view_count"));
        p.setLikeCount(rs.getInt("like_count"));
        p.setCommentCount(rs.getInt("comment_count"));
        p.setIsPinned(rs.getInt("is_pinned") != 0);
        p.setIsSolved(rs.getInt("is_solved") != 0);
        p.setStatus(rs.getInt("status"));
        // 时间戳可能为 null
        try { p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        // author
        try { p.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { p.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return p;
    };

    private static final String JOIN_SELECT =
            "SELECT p.id, p.user_id, p.category, p.title, p.content, " +
            "p.view_count, p.like_count, p.comment_count, " +
            "p.is_pinned, p.is_solved, p.status, p.created_at, p.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM post p LEFT JOIN user u ON p.user_id = u.id ";

    public PostMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ==================== 增 ==================== */

    public Post insert(Post post) {
        String sql = """
                INSERT INTO post (user_id, category, title, content, is_solved, created_at, updated_at)
                VALUES (?, ?, ?, ?, 0, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, post.getUserId());
            ps.setString(2, post.getCategory());
            ps.setString(3, post.getTitle());
            ps.setString(4, post.getContent());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            post.setId(key.longValue());
            findById(post.getId()).ifPresent(row -> {
                post.setCreatedAt(row.getCreatedAt());
                post.setUpdatedAt(row.getUpdatedAt());
            });
        }
        return post;
    }

    /* ==================== 查 ==================== */

    /** 单条详情（含作者） */
    public Optional<Post> findById(Long id) {
        String sql = JOIN_SELECT + "WHERE p.id = ? AND p.status != 2";
        List<Post> list = jdbc.query(sql, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** 分页列表（按类别筛选） */
    public List<Post> findByCategory(String category, int offset, int size) {
        String sql = JOIN_SELECT +
                "WHERE p.category = ? AND p.status = 0 " +
                "ORDER BY p.is_pinned DESC, p.created_at DESC LIMIT ?, ?";
        return jdbc.query(sql, rowMapper, category, offset, size);
    }

    public Long countByCategory(String category) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM post WHERE category=? AND status=0", Long.class, category);
    }

    /** 关键词搜索（标题模糊匹配） */
    public List<Post> search(String keyword, int offset, int size) {
        String sql = JOIN_SELECT +
                "WHERE p.title LIKE ? AND p.status = 0 " +
                "ORDER BY p.created_at DESC LIMIT ?, ?";
        return jdbc.query(sql, rowMapper, "%" + keyword + "%", offset, size);
    }

    public Long countSearch(String keyword) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM post WHERE title LIKE ? AND status=0",
                Long.class, "%" + keyword + "%");
    }

    /** 用户发布的帖子（用于个人主页） */
    public List<Post> findByUserId(Long userId, int offset, int size) {
        String sql = JOIN_SELECT +
                "WHERE p.user_id = ? AND p.status != 2 " +
                "ORDER BY p.created_at DESC LIMIT ?, ?";
        return jdbc.query(sql, rowMapper, userId, offset, size);
    }

    public Long countByUserId(Long userId) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM post WHERE user_id=? AND status!=2", Long.class, userId);
    }

    /* ==================== 改 ==================== */

    public void update(Post post) {
        jdbc.update("""
                UPDATE post SET title=?, content=?, category=?, updated_at=NOW()
                WHERE id=? AND status!=2
                """, post.getTitle(), post.getContent(), post.getCategory(), post.getId());
    }

    /** 软删除 */
    public void softDelete(Long id) {
        jdbc.update("UPDATE post SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    /** 阅读量 +1 */
    public void incrementViewCount(Long id) {
        jdbc.update("UPDATE post SET view_count = view_count + 1 WHERE id=?", id);
    }

    /** 置顶/取消置顶 */
    public void updatePinned(Long id, boolean pinned) {
        jdbc.update("UPDATE post SET is_pinned=?, updated_at=NOW() WHERE id=? AND status!=2",
                pinned ? 1 : 0, id);
    }

    /** 标记已解决（问答用） */
    public void updateSolved(Long id, boolean solved) {
        jdbc.update("UPDATE post SET is_solved=?, updated_at=NOW() WHERE id=? AND status!=2",
                solved ? 1 : 0, id);
    }

    /** 更新评论数（评论模块会用） */
    public void updateCommentCount(Long id, int delta) {
        jdbc.update("UPDATE post SET comment_count = comment_count + ? WHERE id=?", delta, id);
    }

    /** 更新点赞数（点赞模块会用） */
    public void updateLikeCount(Long id, int delta) {
        jdbc.update("UPDATE post SET like_count = like_count + ? WHERE id=?", delta, id);
    }
}
