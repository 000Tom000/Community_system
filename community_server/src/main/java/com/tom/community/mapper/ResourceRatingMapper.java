package com.tom.community.mapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRatingMapper {

    private final JdbcTemplate jdbc;

    public ResourceRatingMapper(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** 用户的评分（可能为空） */
    public Integer findByUser(Long resourceId, Long userId) {
        try {
            return jdbc.queryForObject(
                    "SELECT rating FROM resource_rating WHERE resource_id=? AND user_id=?",
                    Integer.class, resourceId, userId);
        } catch (Exception e) { return null; }
    }

    /** 评分或更新 */
    public void upsert(Long resourceId, Long userId, Integer rating) {
        jdbc.update("""
                INSERT INTO resource_rating (resource_id, user_id, rating, created_at)
                VALUES (?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE rating=?
                """, resourceId, userId, rating, rating);
    }

    /** 重新计算平均分并更新 resource 表 */
    public void recalcAvg(Long resourceId) {
        var result = jdbc.queryForMap(
                "SELECT COALESCE(AVG(rating), 0) AS avg, COUNT(*) AS cnt FROM resource_rating WHERE resource_id=?",
                resourceId);
        double avg = ((Number) result.get("avg")).doubleValue();
        int cnt = ((Number) result.get("cnt")).intValue();
        jdbc.update("UPDATE resource SET rating_avg=?, rating_count=? WHERE id=?",
                Math.round(avg * 100.0) / 100.0, cnt, resourceId);
    }
}
