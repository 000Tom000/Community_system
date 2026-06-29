package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.*;
import com.tom.community.model.Comment;
import com.tom.community.model.User;
import com.tom.community.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, CommentLikeMapper commentLikeMapper,
                              PostMapper postMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<Comment> getComments(Long postId) {
        // 顶级评论
        List<Comment> topLevel = commentMapper.findTopLevelByPost(postId);
        // 填充回复
        for (Comment c : topLevel) {
            List<Comment> replies = commentMapper.findReplies(c.getId());
            c.setReplies(replies);
        }
        return topLevel;
    }

    @Override
    @Transactional
    public Comment addComment(Long userId, Long postId, Long parentId, String content) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法评论");
        }
        // 校验帖子
        postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        // 校验内容
        if (!StringUtils.hasText(content) || content.trim().length() < 1) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 5000) {
            throw new BusinessException("评论内容不能超过5000字");
        }
        // 如果是回复，校验父评论存在且属于同一帖子
        if (parentId != null) {
            commentMapper.findById(parentId)
                    .orElseThrow(() -> new BusinessException("回复的评论不存在"));
        }

        Comment c = new Comment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setParentId(parentId);
        c.setContent(content.trim());
        commentMapper.insert(c);

        // 增加帖子评论数
        postMapper.updateCommentCount(postId, 1);

        // 回填作者信息
        c.setAuthorNickname(user.getNickname());
        c.setAuthorAvatar(user.getAvatar());
        return c;
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment c = commentMapper.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));

        // 作者或管理员可删
        if (!c.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此评论");
        }

        commentMapper.softDelete(commentId);
        // 评论数 -1（仅顶级评论减，回复暂不计入评论数）
        if (c.getParentId() == null) {
            postMapper.updateCommentCount(c.getPostId(), -1);
        }
    }

    @Override
    public boolean toggleLike(Long userId, Long commentId) {
        commentMapper.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (commentLikeMapper.exists(commentId, userId)) {
            commentLikeMapper.delete(commentId, userId);
            commentMapper.updateLikeCount(commentId, -1);
            return false;
        } else {
            commentLikeMapper.insert(commentId, userId);
            commentMapper.updateLikeCount(commentId, 1);
            return true;
        }
    }
}
