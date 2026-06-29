package com.tom.community.service;

import com.tom.community.model.PageResult;
import com.tom.community.model.QaAnswer;
import com.tom.community.model.QaQuestion;

public interface QaService {

    /** 提问 */
    QaQuestion createQuestion(Long userId, String title, String content, String category);

    /** 问题详情（阅读量+1） */
    QaQuestion getQuestion(Long id);

    /** 问题详情 + 回答列表 + 用户点赞状态 */
    QaQuestion getQuestionWithAnswers(Long id, Long userId);

    /** 问题列表（分页+筛选+排序） */
    PageResult<QaQuestion> listQuestions(String category, String keyword, String sort,
                                         Integer page, Integer size);

    /** 编辑问题 */
    void updateQuestion(Long userId, Long questionId, String title, String content, String category);

    /** 删除问题 */
    void deleteQuestion(Long userId, Long questionId);

    /** 写回答 */
    QaAnswer createAnswer(Long userId, Long questionId, String content);

    /** 删除回答 */
    void deleteAnswer(Long userId, Long answerId);

    /** 采纳回答 */
    void acceptAnswer(Long userId, Long questionId, Long answerId);

    /** 点赞/取消点赞 → true=已点赞, false=已取消 */
    boolean toggleAnswerLike(Long userId, Long answerId);
}
