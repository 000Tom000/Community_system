package com.tom.community.controller;

import com.tom.community.common.BusinessException;
import com.tom.community.common.Result;
import com.tom.community.mapper.ResourceMapper;
import com.tom.community.mapper.ResourceRatingMapper;
import com.tom.community.model.PageResult;
import com.tom.community.model.Resource;
import com.tom.community.service.ResourceService;
import com.tom.community.service.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;
    private final ResourceRatingMapper ratingMapper;
    private final UserService userService;

    public ResourceController(ResourceService resourceService, ResourceMapper resourceMapper,
                              ResourceRatingMapper ratingMapper, UserService userService) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
        this.ratingMapper = ratingMapper;
        this.userService = userService;
    }

    /** 上传资源 */
    @PostMapping
    public Result<Resource> upload(
            @RequestHeader("X-Token") String token,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "other") String category,
            @RequestParam MultipartFile file) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(resourceService.upload(uid, title, description, category, file));
    }

    /** 资源列表  ?category=&keyword=&sort=new|hot|rating&page=&size= */
    @GetMapping
    public Result<PageResult<Resource>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(resourceService.listResources(category, keyword, sort, page, size));
    }

    /** 资源详情 + 当前用户评分 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(
            @PathVariable Long id,
            @RequestHeader(required = false, value = "X-Token") String token) {
        Resource r = resourceService.getResource(id);
        Map<String, Object> result = new HashMap<>();
        result.put("resource", r);
        // 查当前用户评分
        Integer myRate = null;
        try {
            Long uid = userService.getCurrentUser(token).getId();
            myRate = ratingMapper.findByUser(id, uid);
        } catch (Exception ignored) {}
        result.put("myRating", myRate);
        return Result.success(result);
    }

    /** 下载 */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        Resource r = resourceService.getResource(id);
        resourceMapper.incrementDownload(id);

        java.io.File file = new java.io.File(r.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        try {
            String encoded = URLEncoder.encode(r.getFileName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(r.getFileSize())
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (IOException e) {
            throw new BusinessException("文件读取失败");
        }
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token, @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        resourceService.deleteResource(uid, id);
        return Result.success();
    }

    /** 评分 { "rating": 1~5 } */
    @PostMapping("/{id}/rate")
    public Result<Void> rate(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Long uid = userService.getCurrentUser(token).getId();
        resourceService.rate(uid, id, body.get("rating"));
        return Result.success();
    }
}
