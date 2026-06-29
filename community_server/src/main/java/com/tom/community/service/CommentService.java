package com.tom.community.service;

import com.tom.community.model.Comment;

import java.util.List;

public interface CommentService {

    /** 获取帖子的评论列表（顶级+嵌套回复） */
    List<Comment> getComments(Long postId);

    /** 添加评论或回复 */
    Comment addComment(Long userId, Long postId, Long parentId, String content);

    /** 删除评论（软删除，仅作者或管理员） */
    void deleteComment(Long userId, Long commentId);

    /** 点赞/取消 → true=已点赞 false=已取消 */
    boolean toggleLike(Long userId, Long commentId);
}
