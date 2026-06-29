package com.tom.community.service;

import com.tom.community.model.Notice;
import com.tom.community.model.PageResult;

public interface NoticeService {

    Notice create(Long userId, String category, String title, String content, String level);

    Notice getById(Long id);

    PageResult<Notice> list(String category, String keyword, String sort, Integer page, Integer size);

    void update(Long userId, Long id, String title, String content, String category, String level);

    void delete(Long userId, Long id);

    void togglePinned(Long userId, Long id);
}
