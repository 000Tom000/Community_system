package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.PageResult;
import com.tom.community.model.SecondHand;
import com.tom.community.service.SecondHandService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secondhand")
public class SecondHandController {

    private final SecondHandService secondHandService;
    private final UserService userService;

    public SecondHandController(SecondHandService secondHandService, UserService userService) {
        this.secondHandService = secondHandService;
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<SecondHand>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(secondHandService.list(category, keyword, sort, page, size));
    }

    @PostMapping
    public Result<SecondHand> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, Object> body) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(secondHandService.create(
                uid,
                (String) body.getOrDefault("category", "other"),
                (String) body.get("title"),
                (String) body.get("description"),
                toDouble(body.get("price")),
                toDouble(body.get("originalPrice")),
                (String) body.getOrDefault("condition", "normal"),
                toBoolean(body.get("isNegotiable")),
                (String) body.get("location"),
                (String) body.get("contact"),
                (String) body.get("imageUrl")));
    }

    @GetMapping("/{id}")
    public Result<SecondHand> detail(@PathVariable Long id) {
        return Result.success(secondHandService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long uid = userService.getCurrentUser(token).getId();
        secondHandService.update(uid, id,
                (String) body.get("title"),
                (String) body.get("description"),
                (String) body.get("category"),
                toDouble(body.get("price")),
                toDouble(body.get("originalPrice")),
                (String) body.get("condition"),
                toBoolean(body.get("isNegotiable")),
                (String) body.get("location"),
                (String) body.get("contact"),
                (String) body.get("imageUrl"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        secondHandService.delete(uid, id);
        return Result.success();
    }

    @PutMapping("/{id}/sold")
    public Result<Void> markSold(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        secondHandService.markSold(uid, id);
        return Result.success();
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.valueOf(v.toString()); } catch (Exception e) { return null; }
    }

    private Boolean toBoolean(Object v) {
        if (v == null) return null;
        if (v instanceof Boolean b) return b;
        return "true".equalsIgnoreCase(v.toString());
    }
}
