package com.tom.community.service;

import com.tom.community.model.LostFound;
import com.tom.community.model.PageResult;

public interface LostFoundService {

    /** 发布失物/招领 */
    LostFound create(Long userId, String type, String category, String title,
                     String description, String location, String contact, String imageUrl);

    /** 详情（浏览+1） */
    LostFound getById(Long id);

    /** 列表 */
    PageResult<LostFound> list(String type, String category, String keyword, String sort,
                                Integer page, Integer size);

    /** 编辑 */
    void update(Long userId, Long id, String title, String description, String category,
                String location, String contact, String imageUrl);

    /** 删除 */
    void delete(Long userId, Long id);

    /** 关闭（标记已找到/已归还） */
    void close(Long userId, Long id);
}
