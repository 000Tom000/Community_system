package com.tom.community.mapper;

import com.tom.community.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<Comment> rowMapper = (rs, rowNum) -> {
        Comment c = new Comment();
        c.setId(rs.getLong("id"));
        c.setPostId(rs.getLong("post_id"));
        c.setUserId(rs.getLong("user_id"));
        c.setParentId(rs.getObject("parent_id") != null ? rs.getLong("parent_id") : null);
        c.setContent(rs.getString("content"));
        c.setLikeCount(rs.getInt("like_count"));
        c.setStatus(rs.getInt("status"));
        try { c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { c.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { c.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return c;
    };

    private static final String JOIN_SELECT =
            "SELECT c.id, c.post_id, c.user_id, c.parent_id, c.content, c.like_count, c.status, " +
            "c.created_at, c.updated_at, " +
            "u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM comment c LEFT JOIN user u ON c.user_id = u.id ";

    public CommentMapper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 添加评论 */
    public Comment insert(Comment comment) {
        String sql = "INSERT INTO comment (post_id, user_id, parent_id, content, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, NOW(), NOW())";
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getPostId());
            ps.setLong(2, comment.getUserId());
            if (comment.getParentId() != null) ps.setLong(3, comment.getParentId());
            else ps.setNull(3, java.sql.Types.BIGINT);
            ps.setString(4, comment.getContent());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) {
            comment.setId(key.longValue());
            findById(comment.getId()).ifPresent(c -> comment.setCreatedAt(c.getCreatedAt()));
        }
        return comment;
    }

    /** 查单条 */
    public Optional<Comment> findById(Long id) {
        List<Comment> list = jdbc.query(
                JOIN_SELECT + "WHERE c.id = ? AND c.status = 0", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** 帖子的顶级评论（按时间升序） */
    public List<Comment> findTopLevelByPost(Long postId) {
        return jdbc.query(
                JOIN_SELECT + "WHERE c.post_id = ? AND c.parent_id IS NULL AND c.status = 0 " +
                "ORDER BY c.created_at ASC", rowMapper, postId);
    }

    /** 某条评论的所有回复（按时间升序） */
    public List<Comment> findReplies(Long parentId) {
        return jdbc.query(
                JOIN_SELECT + "WHERE c.parent_id = ? AND c.status = 0 " +
                "ORDER BY c.created_at ASC", rowMapper, parentId);
    }

    /** 软删除 */
    public void softDelete(Long id) {
        jdbc.update("UPDATE comment SET status = 2, updated_at = NOW() WHERE id = ?", id);
    }

    /** 更新点赞数 */
    public void updateLikeCount(Long id, int delta) {
        jdbc.update("UPDATE comment SET like_count = like_count + ? WHERE id = ?", delta, id);
    }
}
