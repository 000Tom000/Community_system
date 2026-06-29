package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.PostLikeMapper;
import com.tom.community.mapper.PostMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.PageResult;
import com.tom.community.model.Post;
import com.tom.community.model.User;
import com.tom.community.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;

    public PostServiceImpl(PostMapper postMapper, UserMapper userMapper, PostLikeMapper postLikeMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.postLikeMapper = postLikeMapper;
    }

    @Override
    public Post createPost(Long userId, String category, String title, String content) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法发帖");
        }

        // 校验参数
        if (!StringUtils.hasText(title) || title.length() > 200) {
            throw new BusinessException("标题需 1~200 字");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("内容不能为空");
        }
        if (!StringUtils.hasText(category)) {
            category = "other";
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setCategory(category);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        postMapper.insert(post);
        return post;
    }

    @Override
    public Post getPost(Long id) {
        Post post = postMapper.findById(id)
                .orElseThrow(() -> new BusinessException("帖子不存在或已删除"));
        // 阅读量 +1
        postMapper.incrementViewCount(id);
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    @Override
    public PageResult<Post> listPosts(String category, Integer page, Integer size, String keyword) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        int offset = (p - 1) * s;

        List<Post> list;
        Long total;
        if (StringUtils.hasText(keyword)) {
            // 搜索
            list = postMapper.search(keyword, offset, s);
            total = postMapper.countSearch(keyword);
        } else if (StringUtils.hasText(category)) {
            // 按分类
            list = postMapper.findByCategory(category, offset, s);
            total = postMapper.countByCategory(category);
        } else {
            // 全部分类
            list = postMapper.search("", offset, s);
            total = postMapper.countSearch("");
        }
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void updatePost(Long userId, Long postId, String title, String content, String category) {
        Post post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的帖子");
        }
        if (StringUtils.hasText(title)) {
            post.setTitle(title.trim());
        }
        if (StringUtils.hasText(content)) {
            post.setContent(content.trim());
        }
        if (StringUtils.hasText(category)) {
            post.setCategory(category);
        }
        postMapper.update(post);
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        // 作者或管理员可删
        if (!post.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此帖子");
        }

        postMapper.softDelete(postId);
    }

    @Override
    public void togglePin(Long userId, Long postId) {
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!"admin".equals(user.getRole())) {
            throw new BusinessException("仅管理员可置顶");
        }
        Post post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        postMapper.updatePinned(postId, !Boolean.TRUE.equals(post.getIsPinned()));
    }

    @Override
    public void toggleSolved(Long userId, Long postId) {
        Post post = postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("仅作者可标记解决");
        }
        postMapper.updateSolved(postId, !Boolean.TRUE.equals(post.getIsSolved()));
    }

    /** 点赞 or 取消点赞 → true=已点赞, false=已取消 */
    @Override
    public boolean toggleLike(Long userId, Long postId) {
        postMapper.findById(postId)
                .orElseThrow(() -> new BusinessException("帖子不存在"));
        if (postLikeMapper.exists(postId, userId)) {
            postLikeMapper.delete(postId, userId);
            postMapper.updateLikeCount(postId, -1);
            return false;
        } else {
            postLikeMapper.insert(postId, userId);
            postMapper.updateLikeCount(postId, 1);
            return true;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        return postLikeMapper.exists(postId, userId);
    }
}
