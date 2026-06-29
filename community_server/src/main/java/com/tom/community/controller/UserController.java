package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.User;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户接口 — RESTful，前缀 /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> body) {
        User user = userService.register(
                body.get("username"),
                body.get("password"),
                body.get("nickname"));
        return Result.success(user);
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body) {
        User user = userService.login(
                body.get("username"),
                body.get("password"));
        return Result.success(user);
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/me")
    public Result<User> me(@RequestHeader("X-Token") String token) {
        User user = userService.getCurrentUser(token);
        return Result.success(user);
    }

    /** 查看用户主页（公开信息） */
    @GetMapping("/{id}")
    public Result<User> profile(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /** 修改个人资料 */
    @PutMapping("/me")
    public Result<Void> updateProfile(
            @RequestHeader("X-Token") String token,
            @RequestBody User body) {
        User current = userService.getCurrentUser(token);
        userService.updateProfile(current.getId(), body);
        return Result.success();
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, String> body) {
        User current = userService.getCurrentUser(token);
        userService.changePassword(current.getId(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return Result.success();
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("X-Token") String token) {
        User current = userService.getCurrentUser(token);
        userService.logout(current.getId());
        return Result.success();
    }
}
