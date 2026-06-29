package com.tom.community.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 应用启动时自动创建上传目录，确保 jar 部署后目录存在
 */
@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        String[] subDirs = {"avatars", "images", "tmp"};
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        for (String sub : subDirs) {
            try {
                Path dir = base.resolve(sub);
                Files.createDirectories(dir);
                log.info("创建目录: {}", dir);
            } catch (Exception e) {
                log.warn("无法创建目录: {}/{} ({})", base, sub, e.getMessage());
            }
        }
    }
}
