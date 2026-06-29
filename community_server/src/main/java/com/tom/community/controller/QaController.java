package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.PageResult;
import com.tom.community.model.QaAnswer;
import com.tom.community.model.QaQuestion;
import com.tom.community.service.QaService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 问答中心 REST API
 */
@RestController
@RequestMapping("/api/qa")
public class QaController {

    private final QaService qaService;
    private final UserService userService;

    public QaController(QaService qaService, UserService userService) {
        this.qaService = qaService;
        this.userService = userService;
    }

    /** 问题列表 */
    @GetMapping
    public Result<PageResult<QaQuestion>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "new") String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return Result.success(qaService.listQuestions(category, keyword, sort, page, size));
    }

    /** 提问 */
    @PostMapping
    public Result<QaQuestion> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(qaService.createQuestion(
                uid,
                body.get("title"),
                body.get("content"),
                body.getOrDefault("category", "other")));
    }

    /** 问题详情（token 可选，用于标记回答点赞状态） */
    @GetMapping("/{id}")
    public Result<QaQuestion> detail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Token", required = false) String token) {
        Long uid = null;
        try {
            uid = userService.getCurrentUser(token).getId();
        } catch (Exception ignored) {}
        return Result.success(qaService.getQuestionWithAnswers(id, uid));
    }

    /** 编辑问题 */
    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        qaService.updateQuestion(uid, id,
                body.get("title"),
                body.get("content"),
                body.get("category"));
        return Result.success();
    }

    /** 删除问题 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        qaService.deleteQuestion(uid, id);
        return Result.success();
    }

    /** 写回答 */
    @PostMapping("/{id}/answers")
    public Result<QaAnswer> createAnswer(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        return Result.success(qaService.createAnswer(uid, id, body.get("content")));
    }

    /** 删除回答 */
    @DeleteMapping("/answers/{id}")
    public Result<Void> deleteAnswer(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        qaService.deleteAnswer(uid, id);
        return Result.success();
    }

    /** 采纳/取消采纳回答 */
    @PutMapping("/answers/{id}/accept")
    public Result<Void> acceptAnswer(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long uid = userService.getCurrentUser(token).getId();
        Long questionId = body.get("questionId");
        qaService.acceptAnswer(uid, questionId, id);
        return Result.success();
    }

    /** 点赞/取消点赞 */
    @PostMapping("/answers/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        boolean liked = qaService.toggleAnswerLike(uid, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }
}
