package com.tom.community.controller;

import com.tom.community.common.Result;
import com.tom.community.model.PageResult;
import com.tom.community.model.Post;
import com.tom.community.service.PostService;
import com.tom.community.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 帖子接口 — RESTful，前缀 /api/posts
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    /** 发帖 */
    @PostMapping
    public Result<Post> create(
            @RequestHeader("X-Token") String token,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        Post post = postService.createPost(uid,
                body.get("category"), body.get("title"), body.get("content"));
        return Result.success(post);
    }

    /** 帖子详情（阅读量自动+1） */
    @GetMapping("/{id}")
    public Result<Post> detail(@PathVariable Long id) {
        return Result.success(postService.getPost(id));
    }

    /** 帖子列表（?category=&page=&size=&keyword=） */
    @GetMapping
    public Result<PageResult<Post>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(postService.listPosts(category, page, size, keyword));
    }

    /** 编辑帖子 */
    @PutMapping("/{id}")
    public Result<Void> update(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long uid = userService.getCurrentUser(token).getId();
        postService.updatePost(uid, id,
                body.get("title"), body.get("content"), body.get("category"));
        return Result.success();
    }

    /** 删除帖子（软删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        postService.deletePost(uid, id);
        return Result.success();
    }

    /** 置顶/取消置顶（管理员） */
    @PutMapping("/{id}/pin")
    public Result<Void> togglePin(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        postService.togglePin(uid, id);
        return Result.success();
    }

    /** 点赞/取消点赞 → 返回 {"liked": true/false} */
    @PostMapping("/{id}/like")
    public Result<Map<String, Boolean>> toggleLike(
            @RequestHeader("X-Token") String token,
            @PathVariable Long id) {
        Long uid = userService.getCurrentUser(token).getId();
        boolean liked = postService.toggleLike(uid, id);
        return Result.success(Map.of("liked", liked));
    }

    /** 查询当前用户是否已点赞 */
    @GetMapping("/{id}/like")
    public Result<Map<String, Boolean>> isLiked(
            @RequestHeader(required = false, value = "X-Token") String token,
            @PathVariable Long id) {
        boolean liked = false;
        try {
            Long uid = userService.getCurrentUser(token).getId();
            liked = postService.isLiked(uid, id);
        } catch (Exception ignored) {
        }
        return Result.success(Map.of("liked", liked));
    }
}
