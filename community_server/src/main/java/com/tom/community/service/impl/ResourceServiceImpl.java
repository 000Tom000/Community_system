package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.ResourceMapper;
import com.tom.community.mapper.ResourceRatingMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.PageResult;
import com.tom.community.model.Resource;
import com.tom.community.model.User;
import com.tom.community.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final ResourceMapper resourceMapper;
    private final ResourceRatingMapper ratingMapper;
    private final UserMapper userMapper;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public ResourceServiceImpl(ResourceMapper resourceMapper,
                               ResourceRatingMapper ratingMapper,
                               UserMapper userMapper) {
        this.resourceMapper = resourceMapper;
        this.ratingMapper = ratingMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Resource upload(Long userId, String title, String description,
                           String category, MultipartFile file) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        // 校验参数
        if (!StringUtils.hasText(title) || title.length() > 200) {
            throw new BusinessException("资源名称需 1~200 字");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择文件");
        }
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException("文件不能超过 50MB");
        }
        if (!StringUtils.hasText(category)) {
            category = "other";
        }

        // 保存文件到磁盘
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        // 解析为绝对路径，避免相对路径在不同启动方式下找不到的问题
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path dest = dir.resolve(savedName);
        try {
            Files.createDirectories(dir);
            log.info("正在保存文件到: {}", dest);
            file.transferTo(dest.toFile());
            log.info("文件保存成功: {} ({} bytes)", dest, file.getSize());
        } catch (IOException e) {
            log.error("文件保存失败: dir={}, fileName={}", dir, savedName, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // 判断文件类型
        String fileType = detectFileType(ext);

        Resource r = new Resource();
        r.setUserId(userId);
        r.setTitle(title.trim());
        r.setDescription(description);
        r.setCategory(category);
        r.setFileName(originalName);
        r.setFilePath(dest.toString());
        r.setFileSize(file.getSize());
        r.setFileType(fileType);
        resourceMapper.insert(r);

        r.setAuthorNickname(user.getNickname());
        r.setAuthorAvatar(user.getAvatar());
        return r;
    }

    @Override
    public Resource getResource(Long id) {
        return resourceMapper.findById(id)
                .orElseThrow(() -> new BusinessException("资源不存在"));
    }

    @Override
    public PageResult<Resource> listResources(String category, String keyword, String sort,
                                              Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        List<Resource> list = resourceMapper.findList(category, keyword, sort, (p - 1) * s, s);
        Long total = resourceMapper.countList(category, keyword);
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void deleteResource(Long userId, Long resourceId) {
        Resource r = resourceMapper.findById(resourceId)
                .orElseThrow(() -> new BusinessException("资源不存在"));
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!r.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此资源");
        }
        resourceMapper.softDelete(resourceId);
    }

    @Override
    public void rate(Long userId, Long resourceId, Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分需在 1~5 之间");
        }
        resourceMapper.findById(resourceId)
                .orElseThrow(() -> new BusinessException("资源不存在"));
        ratingMapper.upsert(resourceId, userId, rating);
        ratingMapper.recalcAvg(resourceId);
    }

    /** 根据扩展名判断文件类型 */
    private String detectFileType(String ext) {
        return switch (ext.toLowerCase()) {
            case ".pdf" -> "pdf";
            case ".ppt", ".pptx" -> "ppt";
            case ".doc", ".docx" -> "doc";
            case ".xls", ".xlsx" -> "xls";
            case ".zip", ".rar", ".7z" -> "zip";
            case ".jpg", ".jpeg", ".png", ".gif", ".bmp" -> "img";
            case ".txt", ".md" -> "txt";
            default -> "other";
        };
    }
}
