package com.tom.community.controller;

import com.tom.community.common.BusinessException;
import com.tom.community.common.Result;
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
 * 通用文件上传
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestHeader("X-Token") String token,
                                                   @RequestParam MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("请上传图片文件");
        }

        String originalName = file.getOriginalFilename();
        String ext = ".jpg";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                ext = ".jpg";
            }
        }
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadDir, "images").toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(savedName);
            file.transferTo(dest.toFile());
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }

        Map<String, String> result = new HashMap<>();
        result.put("url", "/uploads/images/" + savedName);
        return Result.success(result);
    }
}
