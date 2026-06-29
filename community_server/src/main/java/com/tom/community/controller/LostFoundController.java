package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.LostFound;
import com.tom.community.model.PageResult;
import com.tom.community.service.LostFoundService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 失物招领 REST API
 */
@RestController
@RequestMapping("/api/lost-found")
public class LostFoundController {

    private final LostFoundService lostFoundService;
    private final UserService userService;

    public LostFoundController(LostFoundService lostFoundService, UserService userService) {
        this.lostFoundService = lostFoundService;
        this.userService = userService;
    }

    /** 列表 */
    @GetMapping
    public Result<PageResult<LostFound>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(lostFoundService.list(type, category, keyword, sort, page, size));
    }

    /** 发布 */
    @PostMapping
    public Result<LostFound> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(lostFoundService.create(
                uid,
                body.get("type"),
                body.getOrDefault("category", "other"),
                body.get("title"),
                body.get("description"),
                body.get("location"),
                body.get("contact"),
                body.get("imageUrl")));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<LostFound> detail(@PathVariable Long id) {
        return Result.success(lostFoundService.getById(id));
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        lostFoundService.update(uid, id,
                body.get("title"),
                body.get("description"),
                body.get("category"),
                body.get("location"),
                body.get("contact"),
                body.get("imageUrl"));
        return Result.success();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        lostFoundService.delete(uid, id);
        return Result.success();
    }

    /** 标记已找到/已归还（关闭） */
    @PutMapping("/{id}/close")
    public Result<Void> close(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        lostFoundService.close(uid, id);
        return Result.success();
    }
}
