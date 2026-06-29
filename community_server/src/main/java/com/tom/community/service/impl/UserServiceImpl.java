package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.common.PasswordUtil;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.User;
import com.tom.community.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User register(String username, String password, String nickname) {
        // 1. 参数校验
        if (!StringUtils.hasText(username) || username.length() < 3 || username.length() > 50) {
            throw new BusinessException("用户名需 3~50 个字符");
        }
        if (!StringUtils.hasText(password) || password.length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        if (!StringUtils.hasText(nickname)) {
            throw new BusinessException("昵称不能为空");
        }
        if (userMapper.existsByUsername(username)) {
            throw new BusinessException("用户名已被注册");
        }

        // 2. 组装 User
        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setNickname(nickname);
        user.setAvatar("/uploads/avatars/default.svg");  // 默认头像
        user.setRole("user");
        user.setStatus(0);
        user.setToken(UUID.randomUUID().toString().replace("-", ""));

        // 3. 入库
        userMapper.insert(user);
        return user; // 已有 id + token
    }

    @Override
    public User login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == 2) {
            throw new BusinessException("账号已被封禁，请联系管理员");
        }

        // 生成新 token
        String token = UUID.randomUUID().toString().replace("-", "");
        userMapper.updateToken(user.getId(), token);
        user.setToken(token);
        // 清掉 password 再返回
        user.setPassword(null);
        return user;
    }

    @Override
    public User getCurrentUser(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "未登录");
        }
        return userMapper.findByToken(token)
                .orElseThrow(() -> new BusinessException(401, "登录已过期，请重新登录"));
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setToken(null);   // 公开查询不暴露 token
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateProfile(Long userId, User updated) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 只允许更新这些字段
        if (StringUtils.hasText(updated.getNickname())) {
            user.setNickname(updated.getNickname());
        }
        // 头像：空字符串视为不修改，null 视为不修改，有内容才更新
        if (StringUtils.hasText(updated.getAvatar())) {
            user.setAvatar(updated.getAvatar());
        }
        user.setEmail(updated.getEmail());
        user.setPhone(updated.getPhone());
        user.setGender(updated.getGender());
        user.setBio(updated.getBio());
        user.setSchool(updated.getSchool());
        user.setMajor(updated.getMajor());

        userMapper.update(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasText(newPassword) || newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于 6 位");
        }

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        userMapper.updatePassword(userId, PasswordUtil.encrypt(newPassword));
    }

    @Override
    public void logout(Long userId) {
        userMapper.clearToken(userId);
    }
}
