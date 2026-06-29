package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.SecondHandMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.PageResult;
import com.tom.community.model.SecondHand;
import com.tom.community.model.User;
import com.tom.community.service.SecondHandService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SecondHandServiceImpl implements SecondHandService {

    private final SecondHandMapper secondHandMapper;
    private final UserMapper userMapper;

    public SecondHandServiceImpl(SecondHandMapper secondHandMapper, UserMapper userMapper) {
        this.secondHandMapper = secondHandMapper;
        this.userMapper = userMapper;
    }

    @Override
    public SecondHand create(Long userId, String category, String title, String description,
                             Double price, Double originalPrice, String condition,
                             Boolean isNegotiable, String location, String contact, String imageUrl) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法发布");
        }

        if (!StringUtils.hasText(title) || title.length() > 200) {
            throw new BusinessException("标题需 1~200 字");
        }
        if (price == null || price < 0) {
            throw new BusinessException("请输入有效价格");
        }
        if (!StringUtils.hasText(contact)) {
            throw new BusinessException("请填写联系方式");
        }
        if (!StringUtils.hasText(category)) category = "other";
        if (!StringUtils.hasText(condition)) condition = "normal";
        if (isNegotiable == null) isNegotiable = true;

        SecondHand sh = new SecondHand();
        sh.setUserId(userId);
        sh.setCategory(category);
        sh.setTitle(title.trim());
        sh.setDescription(description);
        sh.setPrice(price);
        sh.setOriginalPrice(originalPrice);
        sh.setCondition(condition);
        sh.setIsNegotiable(isNegotiable);
        sh.setLocation(location);
        sh.setContact(contact);
        sh.setImageUrl(imageUrl);
        secondHandMapper.insert(sh);

        sh.setAuthorNickname(user.getNickname());
        sh.setAuthorAvatar(user.getAvatar());
        return sh;
    }

    @Override
    public SecondHand getById(Long id) {
        SecondHand sh = secondHandMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在或已删除"));
        secondHandMapper.incrementViewCount(id);
        sh.setViewCount(sh.getViewCount() + 1);
        return sh;
    }

    @Override
    public PageResult<SecondHand> list(String category, String keyword, String sort,
                                        Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        int offset = (p - 1) * s;

        List<SecondHand> list = secondHandMapper.findList(category, keyword, sort, offset, s);
        Long total = secondHandMapper.countList(category, keyword);
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void update(Long userId, Long id, String title, String description, String category,
                       Double price, Double originalPrice, String condition,
                       Boolean isNegotiable, String location, String contact, String imageUrl) {
        SecondHand sh = secondHandMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));
        if (!sh.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的信息");
        }

        if (StringUtils.hasText(title)) sh.setTitle(title.trim());
        if (description != null) sh.setDescription(description);
        if (StringUtils.hasText(category)) sh.setCategory(category);
        if (price != null && price >= 0) sh.setPrice(price);
        if (originalPrice != null) sh.setOriginalPrice(originalPrice);
        if (StringUtils.hasText(condition)) sh.setCondition(condition);
        if (isNegotiable != null) sh.setIsNegotiable(isNegotiable);
        if (location != null) sh.setLocation(location);
        if (contact != null) sh.setContact(contact);
        if (imageUrl != null) sh.setImageUrl(imageUrl);

        secondHandMapper.update(sh);
    }

    @Override
    public void delete(Long userId, Long id) {
        SecondHand sh = secondHandMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!sh.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此信息");
        }

        secondHandMapper.softDelete(id);
    }

    @Override
    public void markSold(Long userId, Long id) {
        SecondHand sh = secondHandMapper.findById(id)
                .orElseThrow(() -> new BusinessException("信息不存在"));
        if (!sh.getUserId().equals(userId)) {
            throw new BusinessException("仅发布者可操作");
        }
        if (sh.getStatus() == 1) {
            throw new BusinessException("该物品已售出");
        }
        secondHandMapper.markSold(id);
    }
}
