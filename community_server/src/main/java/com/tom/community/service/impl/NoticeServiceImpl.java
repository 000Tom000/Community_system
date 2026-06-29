package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.NoticeMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.Notice;
import com.tom.community.model.PageResult;
import com.tom.community.model.User;
import com.tom.community.service.NoticeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper, UserMapper userMapper) {
        this.noticeMapper = noticeMapper;
        this.userMapper = userMapper;
    }

    /** 仅管理员可发布 */
    @Override
    public Notice create(Long userId, String category, String title, String content, String level) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException("仅管理员可发布公告");
        }
        if (!StringUtils.hasText(title) || title.length() > 200) {
            throw new BusinessException("标题需 1~200 字");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("内容不能为空");
        }
        if (!StringUtils.hasText(category)) category = "general";
        if (!StringUtils.hasText(level)) level = "normal";

        Notice n = new Notice();
        n.setUserId(userId);
        n.setCategory(category);
        n.setTitle(title.trim());
        n.setContent(content.trim());
        n.setLevel(level);
        noticeMapper.insert(n);

        n.setAuthorNickname(user.getNickname());
        n.setAuthorAvatar(user.getAvatar());
        return n;
    }

    @Override
    public Notice getById(Long id) {
        Notice n = noticeMapper.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在或已删除"));
        noticeMapper.incrementViewCount(id);
        n.setViewCount(n.getViewCount() + 1);
        return n;
    }

    @Override
    public PageResult<Notice> list(String category, String keyword, String sort,
                                    Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        int offset = (p - 1) * s;

        List<Notice> list = noticeMapper.findList(category, keyword, sort, offset, s);
        Long total = noticeMapper.countList(category, keyword);
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void update(Long userId, Long id, String title, String content, String category, String level) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException("仅管理员可编辑公告");
        }
        Notice n = noticeMapper.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (StringUtils.hasText(title)) n.setTitle(title.trim());
        if (StringUtils.hasText(content)) n.setContent(content.trim());
        if (StringUtils.hasText(category)) n.setCategory(category);
        if (StringUtils.hasText(level)) n.setLevel(level);

        noticeMapper.update(n);
    }

    @Override
    public void delete(Long userId, Long id) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException("仅管理员可删除公告");
        }
        noticeMapper.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        noticeMapper.softDelete(id);
    }

    @Override
    public void togglePinned(Long userId, Long id) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException("仅管理员可置顶");
        }
        Notice n = noticeMapper.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        noticeMapper.updatePinned(id, !Boolean.TRUE.equals(n.getIsPinned()));
    }
}
