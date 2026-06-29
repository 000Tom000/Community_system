package com.tom.community.controller;

import com.tom.community.common.BusinessException;
import com.tom.community.common.Result;
import com.tom.community.model.User;
import com.tom.community.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 用户接口 — RESTful，前缀 /api/users
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

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

    /** 上传头像 */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(
            @RequestHeader("X-Token") String token,
            @RequestParam MultipartFile file) {
        userService.getCurrentUser(token);  // 验证登录
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("头像不能超过 2MB");
        }
        // 校验图片类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("请上传图片文件");
        }

        // 保存到 uploads/avatars/
        String originalName = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                ext = ".jpg";
            }
        }
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(savedName).toFile());
        } catch (IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/avatars/" + savedName);
        return Result.success(result);
    }
}
