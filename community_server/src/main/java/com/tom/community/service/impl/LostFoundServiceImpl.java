package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.LostFoundMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.LostFound;
import com.tom.community.model.PageResult;
import com.tom.community.model.User;
import com.tom.community.service.LostFoundService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class LostFoundServiceImpl implements LostFoundService {

    private final LostFoundMapper lostFoundMapper;
    private final UserMapper userMapper;

    public LostFoundServiceImpl(LostFoundMapper lostFoundMapper, UserMapper userMapper) {
        this.lostFoundMapper = lostFoundMapper;
        this.userMapper = userMapper;
    }

    @Override
    public LostFound create(Long userId, String type, String category, String title,
                            String description, String location, String contact, String imageUrl) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法发布");
        }

        // 校验参数
        if (!StringUtils.hasText(title) || title.length() > 200) {
            throw new BusinessException("标题需 1~200 字");
        }
        if (!"lost".equals(type) && !"found".equals(type)) {
            throw new BusinessException("请选择寻物或招领");
        }
        if (!StringUtils.hasText(category)) {
            category = "other";
        }

        LostFound lf = new LostFound();
        lf.setUserId(userId);
        lf.setType(type);
        lf.setCategory(category);
        lf.setTitle(title.trim());
        lf.setDescription(description);
        lf.setLocation(location);
        lf.setContact(contact);
        lf.setImageUrl(imageUrl);
        lostFoundMapper.insert(lf);

        lf.setAuthorNickname(user.getNickname());
        lf.setAuthorAvatar(user.getAvatar());
        return lf;
    }

    @Override
    public LostFound getById(Long id) {
        LostFound lf = lostFoundMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在或已删除"));
        lostFoundMapper.incrementViewCount(id);
        lf.setViewCount(lf.getViewCount() + 1);
        return lf;
    }

    @Override
    public PageResult<LostFound> list(String type, String category, String keyword,
                                       String sort, Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        int offset = (p - 1) * s;

        List<LostFound> list = lostFoundMapper.findList(type, category, keyword, sort, offset, s);
        Long total = lostFoundMapper.countList(type, category, keyword);
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void update(Long userId, Long id, String title, String description, String category,
                       String location, String contact, String imageUrl) {
        LostFound lf = lostFoundMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));
        if (!lf.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的信息");
        }

        if (StringUtils.hasText(title)) lf.setTitle(title.trim());
        if (description != null) lf.setDescription(description);
        if (StringUtils.hasText(category)) lf.setCategory(category);
        if (location != null) lf.setLocation(location);
        if (contact != null) lf.setContact(contact);
        if (imageUrl != null) lf.setImageUrl(imageUrl);

        lostFoundMapper.update(lf);
    }

    @Override
    public void delete(Long userId, Long id) {
        LostFound lf = lostFoundMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!lf.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此信息");
        }

        lostFoundMapper.softDelete(id);
    }

    @Override
    public void close(Long userId, Long id) {
        LostFound lf = lostFoundMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));
        if (!lf.getUserId().equals(userId)) {
            throw new BusinessException("仅发布者可关闭");
        }
        if (lf.getStatus() == 1) {
            throw new BusinessException("该信息已关闭");
        }
        lostFoundMapper.updateStatus(id, 1);
    }
}
