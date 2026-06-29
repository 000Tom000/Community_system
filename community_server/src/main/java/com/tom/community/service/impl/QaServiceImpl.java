package com.tom.community.service.impl;

import com.tom.community.common.BusinessException;
import com.tom.community.mapper.QaAnswerLikeMapper;
import com.tom.community.mapper.QaAnswerMapper;
import com.tom.community.mapper.QaQuestionMapper;
import com.tom.community.mapper.UserMapper;
import com.tom.community.model.PageResult;
import com.tom.community.model.QaAnswer;
import com.tom.community.model.QaQuestion;
import com.tom.community.model.User;
import com.tom.community.service.QaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class QaServiceImpl implements QaService {

    private final QaQuestionMapper questionMapper;
    private final QaAnswerMapper answerMapper;
    private final QaAnswerLikeMapper answerLikeMapper;
    private final UserMapper userMapper;

    public QaServiceImpl(QaQuestionMapper questionMapper,
                         QaAnswerMapper answerMapper,
                         QaAnswerLikeMapper answerLikeMapper,
                         UserMapper userMapper) {
        this.questionMapper = questionMapper;
        this.answerMapper = answerMapper;
        this.answerLikeMapper = answerLikeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public QaQuestion createQuestion(Long userId, String title, String content, String category) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法提问");
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

        QaQuestion q = new QaQuestion();
        q.setUserId(userId);
        q.setTitle(title.trim());
        q.setContent(content.trim());
        q.setCategory(category);
        questionMapper.insert(q);
        return q;
    }

    @Override
    public QaQuestion getQuestion(Long id) {
        QaQuestion q = questionMapper.findById(id)
                .orElseThrow(() -> new BusinessException("问题不存在或已删除"));
        questionMapper.incrementViewCount(id);
        q.setViewCount(q.getViewCount() + 1);
        return q;
    }

    @Override
    public QaQuestion getQuestionWithAnswers(Long id, Long userId) {
        QaQuestion q = getQuestion(id);  // 内含 view+1
        List<QaAnswer> answers = answerMapper.findByQuestionId(id);
        // 标出当前用户是否点赞
        if (userId != null) {
            for (QaAnswer a : answers) {
                a.setLiked(answerLikeMapper.exists(a.getId(), userId));
            }
        }
        q.setAnswers(answers);
        return q;
    }

    @Override
    public PageResult<QaQuestion> listQuestions(String category, String keyword, String sort,
                                                 Integer page, Integer size) {
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1 || size > 50) ? 20 : size;
        int offset = (p - 1) * s;

        List<QaQuestion> list = questionMapper.findList(category, keyword, sort, offset, s);
        Long total = questionMapper.countList(category, keyword);
        return new PageResult<>(list, total, p, s);
    }

    @Override
    public void updateQuestion(Long userId, Long questionId, String title, String content,
                               String category) {
        QaQuestion q = questionMapper.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        if (!q.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的问题");
        }
        if (StringUtils.hasText(title)) {
            q.setTitle(title.trim());
        }
        if (StringUtils.hasText(content)) {
            q.setContent(content.trim());
        }
        if (StringUtils.hasText(category)) {
            q.setCategory(category);
        }
        questionMapper.update(q);
    }

    @Override
    public void deleteQuestion(Long userId, Long questionId) {
        QaQuestion q = questionMapper.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!q.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此问题");
        }

        questionMapper.softDelete(questionId);
    }

    @Override
    public QaAnswer createAnswer(Long userId, Long questionId, String content) {
        // 校验用户
        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "用户不存在"));
        if (user.getStatus() == 1) {
            throw new BusinessException("你已被禁言，无法回答");
        }
        // 校验问题存在
        questionMapper.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在或已删除"));

        if (!StringUtils.hasText(content)) {
            throw new BusinessException("回答内容不能为空");
        }

        QaAnswer a = new QaAnswer();
        a.setQuestionId(questionId);
        a.setUserId(userId);
        a.setContent(content.trim());
        answerMapper.insert(a);

        // 回填作者信息
        a.setAuthorNickname(user.getNickname());
        a.setAuthorAvatar(user.getAvatar());

        // 问题回答数 +1
        questionMapper.updateAnswerCount(questionId, 1);

        return a;
    }

    @Override
    public void deleteAnswer(Long userId, Long answerId) {
        QaAnswer a = answerMapper.findById(answerId)
                .orElseThrow(() -> new BusinessException("回答不存在"));

        User user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "未登录"));
        if (!a.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException("无权删除此回答");
        }

        answerMapper.softDelete(answerId);

        // 问题回答数 -1
        questionMapper.updateAnswerCount(a.getQuestionId(), -1);

        // 若该回答是被采纳的，取消问题的已解决状态
        if (Boolean.TRUE.equals(a.getIsAccepted())) {
            questionMapper.updateSolved(a.getQuestionId(), false);
        }
    }

    @Override
    @Transactional
    public void acceptAnswer(Long userId, Long questionId, Long answerId) {
        QaQuestion q = questionMapper.findById(questionId)
                .orElseThrow(() -> new BusinessException("问题不存在"));
        if (!q.getUserId().equals(userId)) {
            throw new BusinessException("仅提问者可以采纳回答");
        }

        QaAnswer a = answerMapper.findById(answerId)
                .orElseThrow(() -> new BusinessException("回答不存在"));
        if (!a.getQuestionId().equals(questionId)) {
            throw new BusinessException("该回答不属于此问题");
        }
        if (Boolean.TRUE.equals(a.getIsAccepted())) {
            // 已采纳 → 取消采纳
            answerMapper.updateAccepted(answerId, false);
            questionMapper.updateSolved(questionId, false);
        } else {
            // 取消之前的采纳 → 采纳新回答
            answerMapper.clearAcceptedByQuestionId(questionId);
            answerMapper.updateAccepted(answerId, true);
            questionMapper.updateSolved(questionId, true);
        }
    }

    @Override
    public boolean toggleAnswerLike(Long userId, Long answerId) {
        answerMapper.findById(answerId)
                .orElseThrow(() -> new BusinessException("回答不存在"));
        if (answerLikeMapper.exists(answerId, userId)) {
            answerLikeMapper.delete(answerId, userId);
            answerMapper.decrementLikeCount(answerId);
            return false;
        } else {
            answerLikeMapper.insert(answerId, userId);
            answerMapper.incrementLikeCount(answerId);
            return true;
        }
    }
}
