package com.tom.community.service;

import com.tom.community.model.PageResult;
import com.tom.community.model.Post;

/**
 * 帖子业务接口
 */
public interface PostService {

    /** 发帖 */
    Post createPost(Long userId, String category, String title, String content);

    /** 帖子详情（含作者信息，并增加阅读量） */
    Post getPost(Long id);

    /** 分页列表（按分类） */
    PageResult<Post> listPosts(String category, Integer page, Integer size, String keyword);

    /** 编辑帖子（仅作者可改） */
    void updatePost(Long userId, Long postId, String title, String content, String category);

    /** 删除帖子（软删除，仅作者或管理员） */
    void deletePost(Long userId, Long postId);

    /** 置顶/取消置顶（管理员） */
    void togglePin(Long userId, Long postId);

    /** 标记已解决（问答，仅作者） */
    void toggleSolved(Long userId, Long postId);

    /** 点赞/取消点赞 → 返回 true=已点赞 false=已取消 */
    boolean toggleLike(Long userId, Long postId);

    /** 当前用户是否已点赞该帖子 */
    boolean isLiked(Long userId, Long postId);
}
