package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.Comment;
import com.tom.community.service.CommentService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论接口 — RESTful，前缀 /api/comments
 */
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    public CommentController(CommentService commentService, UserService userService) {
        this.commentService = commentService;
        this.userService = userService;
    }

    /** 获取帖子的评论列表（含嵌套回复） */
    @GetMapping
    public Result<List<Comment>> list(@RequestParam Long postId) {
        return Result.success(commentService.getComments(postId));
    }

    /** 发表评论/回复 */
    @PostMapping
    public Result<Comment> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, Object> body) {
        Long uid = userService.getCurrentUser(token).getId();
        Long postId = toLong(body.get("postId"));
        Long parentId = toLong(body.get("parentId")); // 可为 null
        String content = (String) body.get("content");
        Comment c = commentService.addComment(uid, postId, parentId, content);
        return Result.success(c);
    }

    /** 删除评论 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        commentService.deleteComment(uid, id);
        return Result.success();
    }

    /** 点赞/取消点赞 */
    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        boolean liked = commentService.toggleLike(uid, id);
        return Result.success(Map.of("liked", liked));
    }

    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number n) return n.longValue();
        try { return Long.parseLong(obj.toString()); } catch (Exception e) { return null; }
    }
}
