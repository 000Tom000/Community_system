package com.tom.community.mapper;

import com.tom.community.model.Resource;
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
public class ResourceMapper {

    private final JdbcTemplate jdbc;

    private final RowMapper<Resource> rowMapper = (rs, rowNum) -> {
        Resource r = new Resource();
        r.setId(rs.getLong("id"));
        r.setUserId(rs.getLong("user_id"));
        r.setTitle(rs.getString("title"));
        r.setDescription(rs.getString("description"));
        r.setFileName(rs.getString("file_name"));
        r.setFilePath(rs.getString("file_path"));
        r.setFileSize(rs.getLong("file_size"));
        r.setFileType(rs.getString("file_type"));
        r.setCategory(rs.getString("category"));
        r.setDownloadCount(rs.getInt("download_count"));
        r.setRatingAvg(rs.getDouble("rating_avg"));
        r.setRatingCount(rs.getInt("rating_count"));
        r.setStatus(rs.getInt("status"));
        try { r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime()); } catch (Exception e) {}
        try { r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime()); } catch (Exception e) {}
        try { r.setAuthorNickname(rs.getString("author_nickname")); } catch (Exception e) {}
        try { r.setAuthorAvatar(rs.getString("author_avatar")); } catch (Exception e) {}
        return r;
    };

    private static final String JOIN_SELECT =
            "SELECT r.*, u.nickname AS author_nickname, u.avatar AS author_avatar " +
            "FROM resource r LEFT JOIN user u ON r.user_id = u.id ";

    public ResourceMapper(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Resource insert(Resource r) {
        String sql = """
                INSERT INTO resource (user_id, title, description, file_name, file_path, file_size, file_type, category, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                """;
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, r.getUserId());
            ps.setString(2, r.getTitle());
            ps.setString(3, r.getDescription());
            ps.setString(4, r.getFileName());
            ps.setString(5, r.getFilePath());
            ps.setLong(6, r.getFileSize());
            ps.setString(7, r.getFileType());
            ps.setString(8, r.getCategory());
            return ps;
        }, kh);
        Number key = kh.getKey();
        if (key != null) { r.setId(key.longValue()); }
        return r;
    }

    public Optional<Resource> findById(Long id) {
        List<Resource> list = jdbc.query(JOIN_SELECT + "WHERE r.id = ? AND r.status=0", rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /** 列表（按分类+关键词+排序） */
    public List<Resource> findList(String category, String keyword, String sort, int offset, int size) {
        StringBuilder sql = new StringBuilder(JOIN_SELECT + "WHERE r.status=0 ");
        if (category != null && !category.isEmpty()) {
            sql.append("AND r.category='").append(category.replace("'", "''")).append("' ");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND r.title LIKE '%").append(keyword.replace("'", "''")).append("%' ");
        }
        sql.append("ORDER BY ");
        switch (sort != null ? sort : "new") {
            case "hot" -> sql.append("r.download_count DESC, r.rating_avg DESC ");
            case "rating" -> sql.append("r.rating_avg DESC, r.download_count DESC ");
            default -> sql.append("r.created_at DESC ");
        }
        sql.append("LIMIT ?, ?");
        return jdbc.query(sql.toString(), rowMapper, offset, size);
    }

    public Long countList(String category, String keyword) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM resource WHERE status=0 ");
        if (category != null && !category.isEmpty()) {
            sql.append("AND category='").append(category.replace("'", "''")).append("' ");
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND title LIKE '%").append(keyword.replace("'", "''")).append("%' ");
        }
        Long c = jdbc.queryForObject(sql.toString(), Long.class);
        return c == null ? 0L : c;
    }

    public void softDelete(Long id) {
        jdbc.update("UPDATE resource SET status=2, updated_at=NOW() WHERE id=?", id);
    }

    public void incrementDownload(Long id) {
        jdbc.update("UPDATE resource SET download_count = download_count + 1 WHERE id=?", id);
    }

    /** 更新评分（新增评分后重新计算） */
    public void updateRating(Long id, double avg, int count) {
        jdbc.update("UPDATE resource SET rating_avg=?, rating_count=? WHERE id=?", avg, count, id);
    }
}
