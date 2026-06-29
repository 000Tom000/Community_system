package com.tom.community.service;

import com.tom.community.model.User;

/**
 * 用户业务接口
 */
public interface UserService {

    /** 注册 → 返回用户（含 id + token） */
    User register(String username, String password, String nickname);

    /** 登录 → 返回用户（含新 token），失败抛异常 */
    User login(String username, String password);

    /** 根据 token 获取当前登录用户 */
    User getCurrentUser(String token);

    /** 查看用户公开信息（帖子作者信息等场景） */
    User getUserById(Long id);

    /** 更新个人资料 */
    void updateProfile(Long userId, User updated);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 退出登录，清除 token */
    void logout(Long userId);
}
