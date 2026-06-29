package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.Notice;
import com.tom.community.model.PageResult;
import com.tom.community.service.NoticeService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;

    public NoticeController(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<Notice>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(noticeService.list(category, keyword, sort, page, size));
    }

    @PostMapping
    public Result<Notice> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(noticeService.create(
                uid,
                body.getOrDefault("category", "general"),
                body.get("title"),
                body.get("content"),
                body.getOrDefault("level", "normal")));
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.success(noticeService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        noticeService.update(uid, id,
                body.get("title"),
                body.get("content"),
                body.get("category"),
                body.get("level"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        noticeService.delete(uid, id);
        return Result.success();
    }

    @PutMapping("/{id}/pin")
    public Result<Void> togglePinned(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        noticeService.togglePinned(uid, id);
        return Result.success();
    }
}
